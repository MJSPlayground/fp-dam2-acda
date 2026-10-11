package es.mjsplayground.acda.ficheros.codelab;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/** Guarda las filas ya seleccionadas; no recorre carpetas ni calcula estadísticas. */
public final class ExportadorCsv {
    private ExportadorCsv() {}

    public static void guardar(Path informe, List<InventarioCodelab.ArchivoInventariado> filas) throws IOException {
        try (var writer = Files.newBufferedWriter(informe, StandardCharsets.UTF_8);
             var csv = new CSVPrinter(writer, CSVFormat.RFC4180.builder()
                     .setHeader("ruta", "bytes", "modificado").get())) {
            for (var fila : filas) csv.printRecord(fila.relativa(), fila.bytes(), fila.modificado());
        }
    }
}
