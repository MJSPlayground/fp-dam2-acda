package es.mjsplayground.acda.ficheros.model.service;

import es.mjsplayground.acda.ficheros.model.dao.ComercialJsonDao;
import es.mjsplayground.acda.ficheros.model.entity.Cliente;
import es.mjsplayground.acda.ficheros.model.entity.EstadoComercial;
import es.mjsplayground.acda.ficheros.model.entity.LineaOperacion;
import es.mjsplayground.acda.ficheros.model.entity.Operacion;
import es.mjsplayground.acda.ficheros.model.entity.Producto;
import es.mjsplayground.acda.ficheros.model.entity.ResumenOperacion;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ComercialService {
    private final ComercialJsonDao dao = new ComercialJsonDao();
    private final Path archivo;
    private EstadoComercial estado;
    public ComercialService(Path archivo) throws IOException { this.archivo = archivo; estado = dao.leer(archivo); }
    public EstadoComercial estado() { return estado; }
    private void cambiar(EstadoComercial nuevo) throws IOException { dao.guardar(archivo, nuevo); estado = nuevo; }
    public void guardarCliente(Cliente cliente) throws IOException {
        String error = ClienteService.validar(cliente);
        if (error != null) throw new IllegalArgumentException(error);
        List<Cliente> lista = new ArrayList<>(estado.clientes());
        lista.removeIf(c -> c.id().equals(cliente.id()));
        lista.add(cliente);
        cambiar(new EstadoComercial(lista, estado.productos(), estado.operaciones()));
    }
    public void eliminarCliente(String id) throws IOException {
        if (estado.operaciones().stream().anyMatch(o -> o.clienteId().equals(id)))
            throw new IllegalArgumentException("El cliente tiene operaciones y no puede eliminarse");
        List<Cliente> lista = new ArrayList<>(estado.clientes());
        lista.removeIf(c -> c.id().equals(id));
        cambiar(new EstadoComercial(lista, estado.productos(), estado.operaciones()));
    }
    public void guardarProducto(Producto producto) throws IOException {
        if (producto.codigo() == null || producto.codigo().isBlank() || producto.nombre() == null || producto.nombre().isBlank()
                || producto.unidades() < 0 || producto.precio() == null || producto.precio().signum() <= 0)
            throw new IllegalArgumentException("Código, nombre, unidades o precio incorrectos");
        List<Producto> lista = new ArrayList<>(estado.productos());
        lista.removeIf(p -> p.codigo().equals(producto.codigo())); lista.add(producto);
        cambiar(new EstadoComercial(estado.clientes(), lista, estado.operaciones()));
    }
    public void eliminarProducto(String codigo) throws IOException {
        if (estado.operaciones().stream().anyMatch(o -> o.lineas().stream().anyMatch(l -> l.productoCodigo().equals(codigo))))
            throw new IllegalArgumentException("El producto tiene operaciones y no puede eliminarse");
        List<Producto> lista = new ArrayList<>(estado.productos());
        lista.removeIf(p -> p.codigo().equals(codigo));
        cambiar(new EstadoComercial(estado.clientes(), lista, estado.operaciones()));
    }
    public Operacion registrar(String id, String clienteId, String productoCodigo, int cantidad) throws IOException {
        return registrar(id, clienteId, Map.of(productoCodigo, cantidad));
    }
    public Operacion registrar(String id, String clienteId, Map<String, Integer> cantidades) throws IOException {
        if (id == null || id.isBlank() || estado.operaciones().stream().anyMatch(o -> o.id().equals(id)))
            throw new IllegalArgumentException("Identificador de operación vacío o duplicado");
        if (estado.clientes().stream().noneMatch(c -> c.id().equals(clienteId)))
            throw new IllegalArgumentException("Cliente inexistente");
        if (cantidades.isEmpty()) throw new IllegalArgumentException("La operación no tiene productos");
        Map<String, Producto> productos = new LinkedHashMap<>();
        for (Producto p : estado.productos()) productos.put(p.codigo(), p);
        List<LineaOperacion> lineas = new ArrayList<>();
        BigDecimal importe = BigDecimal.ZERO;
        for (var entrada : cantidades.entrySet()) {
            Producto producto = productos.get(entrada.getKey());
            if (producto == null) throw new IllegalArgumentException("Producto inexistente: " + entrada.getKey());
            int cantidad = entrada.getValue();
            if (cantidad <= 0 || cantidad > producto.unidades())
                throw new IllegalArgumentException("Cantidad no positiva o sin existencias suficientes: " + producto.codigo());
            BigDecimal subtotal = producto.precio().multiply(BigDecimal.valueOf(cantidad));
            lineas.add(new LineaOperacion(producto.codigo(), cantidad, subtotal));
            importe = importe.add(subtotal);
            productos.put(producto.codigo(), new Producto(producto.codigo(), producto.nombre(), producto.unidades() - cantidad, producto.precio()));
        }
        Operacion operacion = new Operacion(id, clienteId, lineas, importe);
        List<Operacion> operaciones = new ArrayList<>(estado.operaciones()); operaciones.add(operacion);
        cambiar(new EstadoComercial(estado.clientes(), new ArrayList<>(productos.values()), operaciones));
        return operacion;
    }
    public List<ResumenOperacion> resumen(List<Operacion> operaciones) {
        record Clave(String cliente, String producto) {}
        Map<Clave, ResumenOperacion> datos = new LinkedHashMap<>();
        for (Operacion o : operaciones) for (LineaOperacion linea : o.lineas()) {
            Clave clave = new Clave(o.clienteId(), linea.productoCodigo());
            ResumenOperacion anterior = datos.get(clave);
            long unidades = linea.cantidad() + (anterior == null ? 0 : anterior.unidades());
            BigDecimal importe = linea.importe().add(anterior == null ? BigDecimal.ZERO : anterior.importe());
            datos.put(clave, new ResumenOperacion(clave.cliente(), clave.producto(), unidades, importe));
        }
        return datos.values().stream().sorted(java.util.Comparator.comparing(ResumenOperacion::clienteId)
                .thenComparing(ResumenOperacion::productoCodigo)).toList();
    }
}
