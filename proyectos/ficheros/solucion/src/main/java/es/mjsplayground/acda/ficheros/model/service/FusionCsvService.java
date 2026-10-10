package es.mjsplayground.acda.ficheros.model.service;

import es.mjsplayground.acda.ficheros.model.entity.Incidencia;
import es.mjsplayground.acda.ficheros.model.entity.Movimiento;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public final class FusionCsvService {
    public record Resultado(List<Movimiento> filtrados, List<Incidencia> incidencias, BigDecimal total) {
        public Resultado { filtrados = List.copyOf(filtrados); incidencias = List.copyOf(incidencias); }
    }
    public Resultado fusionar(List<Path> entradas, String ciudad) throws IOException {
        LinkedHashMap<String, Movimiento> unicos = new LinkedHashMap<>();
        List<Incidencia> incidencias = new ArrayList<>();
        for (Path archivo : entradas) {
            CSVFormat formato = CSVFormat.RFC4180.builder().setHeader().setSkipHeaderRecord(true).get();
            try (Reader reader = Files.newBufferedReader(archivo, StandardCharsets.UTF_8);
                 CSVParser parser = formato.parse(reader)) {
                if (!parser.getHeaderMap().keySet().containsAll(Set.of("id", "nombre", "ciudad", "importe")))
                    throw new IOException("Faltan encabezados en " + archivo);
                for (CSVRecord r : parser) {
                    try {
                        if (!r.isConsistent()) throw new IllegalArgumentException("Columnas incorrectas");
                        String id = r.get("id").trim(), nombre = r.get("nombre").trim();
                        if (id.isEmpty() || nombre.isEmpty()) throw new IllegalArgumentException("Id o nombre vacío");
                        BigDecimal importe = new BigDecimal(r.get("importe"));
                        if (importe.signum() <= 0) throw new IllegalArgumentException("Importe no positivo");
                        Movimiento m = new Movimiento(id, nombre, r.get("ciudad"), importe);
                        if (unicos.putIfAbsent(id, m) != null) throw new IllegalArgumentException("Id duplicado: " + id);
                    } catch (IllegalArgumentException ex) {
                        incidencias.add(new Incidencia(archivo.toString(), r.getRecordNumber(), ex.getMessage()));
                    }
                }
            }
        }
        List<Movimiento> filtrados = unicos.values().stream()
                .filter(m -> m.ciudad().toLowerCase(Locale.ROOT).equals(ciudad.toLowerCase(Locale.ROOT)))
                .sorted(Comparator.comparing(Movimiento::id)).toList();
        BigDecimal total = filtrados.stream().map(Movimiento::importe).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Resultado(filtrados, incidencias, total);
    }
    public void exportar(Path destino, Resultado r) throws IOException {
        try (Writer writer = Files.newBufferedWriter(destino, StandardCharsets.UTF_8);
             CSVPrinter csv = new CSVPrinter(writer, CSVFormat.RFC4180.builder()
                     .setHeader("id", "nombre", "ciudad", "importe").get())) {
            for (Movimiento m : r.filtrados()) csv.printRecord(m.id(), m.nombre(), m.ciudad(), m.importe());
        }
    }
}
