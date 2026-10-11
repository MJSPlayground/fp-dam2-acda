package es.mjsplayground.acda.ficheros.cli;

import es.mjsplayground.acda.ficheros.model.service.InventarioArchivosService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public final class Inventariar {
    public static void main(String[] args) throws Exception {
        if (args.length != 3) throw new IllegalArgumentException("Uso: carpeta extensión archivoSalida.csv");
        Path raiz = Path.of(args[0]), salida = Path.of(args[2]);
        if (!Files.isDirectory(raiz)) throw new java.io.IOException("No es un directorio: " + raiz);
        Path raizReal = raiz.toRealPath();
        Path salidaReal = salida.toAbsolutePath().normalize();
        Path padre = salidaReal.getParent();
        if (Files.exists(padre)) salidaReal = padre.toRealPath().resolve(salidaReal.getFileName());
        if (salidaReal.startsWith(raizReal))
            throw new IllegalArgumentException("La salida debe estar fuera de la carpeta inventariada");
        var r = new InventarioArchivosService().crear(raiz, args[1]);
        Files.createDirectories(salida.toAbsolutePath().getParent());
        try (var writer = Files.newBufferedWriter(salida, StandardCharsets.UTF_8);
             CSVPrinter csv = new CSVPrinter(writer, CSVFormat.RFC4180.builder()
                     .setHeader("ruta", "bytes", "modificado").get())) {
            for (var a : r.archivos()) csv.printRecord(a.relativa(), a.bytes(), a.modificado());
        }
        System.out.printf("Archivos: %d, bytes: %d, mayor: %s%n", r.archivos().size(), r.bytes(),
                r.mayor() == null ? "ninguno" : r.mayor());
        r.incidencias().forEach(System.err::println);
    }
}
