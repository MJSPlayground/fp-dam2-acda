package es.mjsplayground.acda.ficheros.model.dao;

import es.mjsplayground.acda.ficheros.model.entity.Cliente;
import es.mjsplayground.acda.ficheros.model.entity.Importacion;
import es.mjsplayground.acda.ficheros.model.entity.Incidencia;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class CsvClienteDao implements ClienteDao {
    private static final CSVFormat ENTRADA = CSVFormat.RFC4180.builder().setHeader().setSkipHeaderRecord(true).get();
    private static final CSVFormat SALIDA = CSVFormat.RFC4180.builder().setHeader("id", "nombre", "ciudad", "correo").get();
    @Override public Importacion leer(Path origen) throws IOException {
        List<Cliente> clientes = new ArrayList<>();
        List<Incidencia> incidencias = new ArrayList<>();
        try (Reader lector = Files.newBufferedReader(origen, StandardCharsets.UTF_8);
             CSVParser parser = ENTRADA.parse(lector)) {
            if (!parser.getHeaderMap().keySet().containsAll(Set.of("id", "nombre", "ciudad", "correo")))
                throw new IOException("Faltan encabezados obligatorios en " + origen);
            for (CSVRecord r : parser) {
                if (!r.isConsistent()) { incidencias.add(new Incidencia(origen.toString(), r.getRecordNumber(), "Columnas incompletas")); continue; }
                clientes.add(new Cliente(r.get("id"), r.get("nombre"), r.get("ciudad"), r.get("correo")));
            }
        }
        return new Importacion(clientes, incidencias);
    }
    @Override public void escribir(Path destino, List<Cliente> clientes) throws IOException {
        try (Writer escritor = Files.newBufferedWriter(destino, StandardCharsets.UTF_8);
             CSVPrinter csv = new CSVPrinter(escritor, SALIDA)) {
            for (Cliente c : clientes) csv.printRecord(c.id(), c.nombre(), c.ciudad(), c.correo());
        }
    }
}
