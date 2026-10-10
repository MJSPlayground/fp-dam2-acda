package es.mjsplayground.acda.ficheros.model.service;

import es.mjsplayground.acda.ficheros.model.dao.ClienteDao;
import es.mjsplayground.acda.ficheros.model.dao.CsvClienteDao;
import es.mjsplayground.acda.ficheros.model.dao.JsonClienteDao;
import es.mjsplayground.acda.ficheros.model.dao.XmlClienteDao;
import es.mjsplayground.acda.ficheros.model.entity.Cliente;
import es.mjsplayground.acda.ficheros.model.entity.Importacion;
import es.mjsplayground.acda.ficheros.model.entity.Incidencia;
import es.mjsplayground.acda.ficheros.model.entity.Resultado;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public final class ClienteService {
    public ClienteDao dao(String formato) {
        return switch (formato.toLowerCase(Locale.ROOT)) {
            case "csv" -> new CsvClienteDao();
            case "xml" -> new XmlClienteDao();
            case "json" -> new JsonClienteDao();
            default -> throw new IllegalArgumentException("Formato no admitido: " + formato);
        };
    }
    public Resultado importar(String formato, Path origen) throws IOException {
        Importacion importacion = dao(formato).leer(origen);
        LinkedHashMap<String, Cliente> aceptados = new LinkedHashMap<>();
        List<Incidencia> incidencias = new ArrayList<>(importacion.incidencias());
        int registro = 0;
        for (Cliente c : importacion.clientes()) {
            registro++;
            Cliente normalizado = new Cliente(c.id(), c.nombre(), c.ciudad(), c.correo() == null ? "" : c.correo());
            String error = validar(normalizado);
            if (error != null) incidencias.add(new Incidencia(origen.toString(), registro, error));
            else if (aceptados.putIfAbsent(normalizado.id(), normalizado) != null)
                incidencias.add(new Incidencia(origen.toString(), registro, "Identificador duplicado: " + c.id()));
        }
        List<Cliente> clientes = new ArrayList<>(aceptados.values());
        clientes.sort(Comparator.comparing(Cliente::id));
        return new Resultado(clientes, incidencias);
    }
    public static String validar(Cliente c) {
        if (c.id() == null || c.id().isBlank()) return "Id vacío";
        if (c.nombre() == null || c.nombre().isBlank()) return "Nombre vacío";
        if (c.ciudad() == null) return "Falta ciudad";
        if (c.correo() != null && !c.correo().isBlank()) {
            int arroba = c.correo().indexOf('@');
            if (arroba <= 0 || arroba == c.correo().length() - 1) return "Correo incorrecto";
        }
        return null;
    }
    public void exportar(String formato, Path destino, List<Cliente> clientes) throws IOException {
        for (Cliente c : clientes) {
            String error = validar(c);
            if (error != null) throw new IllegalArgumentException(c.id() + ": " + error);
        }
        Path absoluto = destino.toAbsolutePath();
        Path carpeta = absoluto.getParent();
        Files.createDirectories(carpeta);
        Path temporal = Files.createTempFile(carpeta, ".clientes-", ".tmp");
        try {
            dao(formato).escribir(temporal, clientes);
            try { Files.move(temporal, absoluto, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException ex) { Files.move(temporal, absoluto, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporal); }
    }
    public Resultado convertir(String entradaFormato, Path entrada, String salidaFormato, Path salida) throws IOException {
        Resultado resultado = importar(entradaFormato, entrada);
        exportar(salidaFormato, salida, resultado.clientes());
        return resultado;
    }
}
