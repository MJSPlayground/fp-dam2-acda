package es.mjsplayground.acda.ficheros.cli;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

public final class InformeCsvGuiado {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Uso: entrada.csv informe.txt");
        List<String> nombres = new ArrayList<>(), incidencias = new ArrayList<>();
        CSVFormat formato = CSVFormat.RFC4180.builder().setHeader().setSkipHeaderRecord(true).get();
        try (Reader in = Files.newBufferedReader(Path.of(args[0]), StandardCharsets.UTF_8);
             CSVParser csv = formato.parse(in)) {
            if (!csv.getHeaderMap().keySet().containsAll(Set.of("id", "nombre", "ciudad")))
                throw new IllegalArgumentException("Faltan encabezados obligatorios");
            for (var fila : csv) {
                if (!fila.isConsistent() || fila.get("id").isBlank() || fila.get("nombre").isBlank())
                    incidencias.add("Registro " + fila.getRecordNumber() + ": id o nombre vacío, o columnas incorrectas");
                else nombres.add(fila.get("nombre"));
            }
        }
        Path informe = Path.of(args[1]); Files.createDirectories(informe.toAbsolutePath().getParent());
        try (var out = Files.newBufferedWriter(informe, StandardCharsets.UTF_8)) {
            out.write("Válidos: " + nombres.size()); out.newLine();
            for (var nombre : nombres) { out.write(nombre); out.newLine(); }
            out.write("Rechazados: " + incidencias.size()); out.newLine();
            for (var i : incidencias) { out.write(i); out.newLine(); }
        }
    }
}
