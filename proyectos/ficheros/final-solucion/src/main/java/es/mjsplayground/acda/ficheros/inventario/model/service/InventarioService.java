package es.mjsplayground.acda.ficheros.inventario.model.service;

import es.mjsplayground.acda.ficheros.inventario.config.Repositorios;
import es.mjsplayground.acda.ficheros.inventario.model.entity.*;
import es.mjsplayground.acda.ficheros.inventario.model.repository.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;
import org.slf4j.*;

public class InventarioService {
  protected static final Logger log = LoggerFactory.getLogger(InventarioService.class);
  protected final Map<Formato, InventarioRepository> repositorios;
  protected final ArchivosRepository archivos;
  protected List<Producto> productos;
  protected long version;

  public InventarioService() {
    this(Repositorios.crear(), new ArchivosRepository());
  }

  public InventarioService(
      Map<Formato, InventarioRepository> repositorios, ArchivosRepository archivos) {
    this.repositorios = Map.copyOf(repositorios);
    this.archivos = archivos;
    productos =
        List.of(
            new Producto("P01", "Teclado", 10, new BigDecimal("25.90")),
            new Producto("P02", "Ratón", 20, new BigDecimal("12.95")),
            new Producto("P03", "Monitor", 5, new BigDecimal("159.00")));
  }

  public synchronized List<Producto> listar() {
    return List.copyOf(productos);
  }

  public synchronized List<Producto> filtrar(String texto, int minimo) {
    if (minimo < 0 || minimo > 1000000)
      throw new InventarioException("Stock mínimo entre 0 y 1000000");
    String filtro = texto == null ? "" : texto.strip().toLowerCase(Locale.ROOT);
    return productos.stream()
        .filter(
            p ->
                p.unidades() >= minimo
                    && (p.codigo().toLowerCase(Locale.ROOT).contains(filtro)
                        || p.nombre().toLowerCase(Locale.ROOT).contains(filtro)))
        .toList();
  }

  public void validarProducto(Producto p) {
    if (p == null
        || p.codigo() == null
        || !p.codigo().matches("[A-Z0-9_-]{1,20}")
        || p.nombre() == null
        || p.nombre().isBlank()
        || p.nombre().strip().length() > 100
        || p.unidades() < 0
        || p.unidades() > 1000000
        || p.precio() == null
        || p.precio().signum() <= 0
        || p.precio().compareTo(new BigDecimal("1000000")) > 0
        || p.precio().scale() > 2)
      throw new InventarioException(
          "Código 1–20 mayúsculas/dígitos/guion; nombre 1–100; stock 0–1000000; precio 0,01–1000000"
              + " y hasta 2 decimales");
  }

  protected List<Producto> validarCatalogo(List<Producto> nuevos) {
    if (nuevos == null || nuevos.size() > 5000)
      throw new InventarioException("Máximo 5000 productos");
    Set<String> codigos = new HashSet<>();
    List<Producto> lista = new ArrayList<>();
    for (Producto p : nuevos) {
      validarProducto(p);
      if (!codigos.add(p.codigo()))
        throw new InventarioException("Código duplicado: " + p.codigo());
      lista.add(normalizar(p));
    }
    return List.copyOf(lista);
  }

  protected Producto normalizar(Producto p) {
    return new Producto(p.codigo(), p.nombre().strip(), p.unidades(), p.precio().setScale(2));
  }

  public synchronized void guardarProducto(String anterior, Producto p) {
    validarProducto(p);
    if (anterior != null && !anterior.equals(p.codigo()))
      throw new InventarioException("No cambies el código de un producto existente");
    List<Producto> lista = new ArrayList<>(productos);
    int posicion = -1;
    for (int n = 0; n < lista.size(); n++)
      if (lista.get(n).codigo().equals(p.codigo())) posicion = n;
    if (anterior == null) {
      if (posicion >= 0) throw new InventarioException("Código ya existente");
      lista.add(p);
    } else {
      if (posicion < 0) throw new InventarioException("Producto ya no existente");
      lista.set(posicion, p);
    }
    productos = validarCatalogo(lista);
    version++;
  }

  public synchronized void eliminar(String codigo) {
    if (productos.stream().noneMatch(p -> p.codigo().equals(codigo)))
      throw new InventarioException("Producto no encontrado");
    productos = productos.stream().filter(p -> !p.codigo().equals(codigo)).toList();
    version++;
  }

  public synchronized void cargar(Path archivo, Formato formato) {
    try {
      List<Producto> leidos = repositorios.get(formato).leer(archivo);
      List<Producto> validos = validarCatalogo(leidos);
      productos = validos;
      version++;
      log.info("Catálogo cargado: {} productos desde {}", productos.size(), archivo);
    } catch (IOException e) {
      throw fallo("No se pudo cargar " + archivo, e);
    }
  }

  public synchronized void guardar(Path archivo, Formato formato) {
    try {
      repositorios.get(formato).guardar(archivo, validarCatalogo(productos));
      log.info("Catálogo guardado: {} productos en {}", productos.size(), archivo);
    } catch (IOException e) {
      throw fallo("No se pudo guardar " + archivo, e);
    }
  }

  public List<Path> archivos(Path carpeta, String extension) {
    try {
      return archivos.listar(carpeta, extension);
    } catch (IOException e) {
      throw fallo("No se pudo listar " + carpeta, e);
    }
  }

  public void copiarEjemplos(Path destino) {
    try {
      archivos.copiarEjemplos(destino);
    } catch (IOException e) {
      throw fallo(
          "No se pudieron copiar ejemplos; el destino puede contener archivos con esos nombres", e);
    }
  }

  protected InventarioException fallo(String mensaje, Exception causa) {
    log.error(mensaje, causa);
    return new InventarioException(mensaje, causa);
  }

  protected final LoteProductoRepository lotes = new LoteProductoRepository();

  public synchronized void exportarFiltrados(
      Path archivo, Formato formato, String texto, int minimo) {
    List<Producto> seleccion = filtrar(texto, minimo);
    try {
      repositorios.get(formato).guardar(archivo, seleccion);
    } catch (IOException e) {
      throw fallo("No se pudo exportar la selección", e);
    }
  }

  public synchronized PlanConciliacion previsualizar(Path origen, Formato formato) {
    List<FilaImportada> filas;
    try {
      filas = lotes.leer(origen, formato);
    } catch (IOException e) {
      throw fallo("No se pudo analizar el lote completo; catálogo sin cambios", e);
    }
    Map<String, Integer> apariciones = new HashMap<>();
    for (FilaImportada f : filas)
      if (f.codigo() != null && !f.codigo().isBlank())
        apariciones.merge(f.codigo(), 1, Integer::sum);
    Map<String, Producto> resultado = new LinkedHashMap<>();
    for (Producto p : productos) resultado.put(p.codigo(), p);
    List<Incidencia> errores = new ArrayList<>();
    for (FilaImportada f : filas) {
      if (apariciones.getOrDefault(f.codigo(), 0) > 1) {
        errores.add(
            new Incidencia(
                f.fila(),
                f.codigo(),
                "Código repetido dentro del lote: se descartan todas sus apariciones"));
        continue;
      }
      if (f.error() != null) {
        errores.add(new Incidencia(f.fila(), f.codigo(), f.error()));
        continue;
      }
      try {
        validarProducto(f.producto());
        Producto entrante = normalizar(f.producto());
        Producto anterior = resultado.get(entrante.codigo());
        if (anterior == null) {
          if (resultado.size() >= 5000)
            throw new InventarioException("El catálogo ya contiene 5000 productos");
          resultado.put(entrante.codigo(), entrante);
        } else {
          if (!anterior.nombre().equals(entrante.nombre()))
            throw new InventarioException("Nombre distinto para el mismo código");
          long suma = (long) anterior.unidades() + entrante.unidades();
          if (suma > 1000000)
            throw new InventarioException("La suma de existencias supera 1000000");
          resultado.put(
              entrante.codigo(),
              new Producto(anterior.codigo(), anterior.nombre(), (int) suma, entrante.precio()));
        }
      } catch (InventarioException e) {
        errores.add(new Incidencia(f.fila(), f.codigo(), e.getMessage()));
      }
    }
    return new PlanConciliacion(version, new ArrayList<>(resultado.values()), errores);
  }

  public synchronized void aplicar(PlanConciliacion plan, Path respaldo) {
    if (plan == null || plan.versionBase() != version)
      throw new InventarioException("El catálogo cambió: previsualiza de nuevo antes de aplicar");
    List<Producto> validados = validarCatalogo(plan.resultado());
    Path destino = respaldo.toAbsolutePath();
    boolean creado = false;
    try {
      Files.createDirectories(destino.getParent());
      Files.createFile(destino);
      creado = true;
      repositorios.get(Formato.BIN).guardar(destino, productos);
    } catch (IOException e) {
      if (creado)
        try {
          Files.deleteIfExists(destino);
        } catch (IOException limpieza) {
          e.addSuppressed(limpieza);
        }
      throw fallo("No se creó la recuperación; catálogo sin cambios", e);
    }
    productos = validados;
    version++;
    log.info(
        "Conciliación aplicada; {} incidencias; recuperación {}",
        plan.incidencias().size(),
        respaldo);
  }

  public synchronized void recuperar(Path respaldo) {
    cargar(respaldo, Formato.BIN);
  }

  public void exportarIncidencias(PlanConciliacion plan, Path archivo) {
    if (plan == null)
      throw new InventarioException("Previsualiza un lote antes de exportar incidencias");
    try {
      lotes.informe(archivo, plan.incidencias());
    } catch (IOException e) {
      throw fallo("No se pudo exportar el informe de incidencias", e);
    }
  }
}
