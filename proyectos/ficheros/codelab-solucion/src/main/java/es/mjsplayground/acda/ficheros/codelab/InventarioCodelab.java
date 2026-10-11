package es.mjsplayground.acda.ficheros.codelab;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/** Versión completa del mismo programa que construye el alumnado. */
public final class InventarioCodelab {
    public record ArchivoInventariado(Path relativa, long bytes, Instant modificado) {}
    public record Resultado(List<ArchivoInventariado> archivos, long totalBytes,
                            ArchivoInventariado mayor, List<String> incidencias, Path informe) {}

    public static void main(String[] args) throws Exception {
        if (args.length != 0 && args.length != 3 && args.length != 4)
            throw new IllegalArgumentException("Uso: [carpetaEntrada carpetaTrabajo extension [--reemplazar]]");
        if (args.length == 4 && !"--reemplazar".equals(args[3]))
            throw new IllegalArgumentException("La única opción adicional admitida es --reemplazar");
        Path origen = Path.of(args.length == 0 ? "trabajo-codelab/entrada" : args[0]);
        Path trabajo = Path.of(args.length == 0 ? "trabajo-codelab/salida" : args[1]);
        String extension = args.length == 0 ? "csv" : args[2];
        System.out.println("Directorio de trabajo: " + Path.of("").toAbsolutePath().normalize());
        if (args.length == 0) PrepararDatos.main(new String[0]);
        Resultado resultado = ejecutar(origen, trabajo, extension, args.length == 4);
        for (ArchivoInventariado archivo : resultado.archivos())
            System.out.printf("%s: %d bytes%n", archivo.relativa(), archivo.bytes());
        System.out.printf("Archivos: %d, bytes: %d%n", resultado.archivos().size(), resultado.totalBytes());
        System.out.println("Mayor: " + (resultado.mayor() == null ? "ninguno"
                : resultado.mayor().relativa() + " (" + resultado.mayor().bytes() + " bytes)"));
        System.out.println("Informe: " + resultado.informe().toAbsolutePath().normalize());
        if (resultado.incidencias().isEmpty()) System.out.println("Sin incidencias.");
        else resultado.incidencias().forEach(i -> System.out.println("Incidencia: " + i));
    }

    public static Resultado ejecutar(Path origen, Path trabajo, String extension, boolean reemplazar) throws IOException {
        if (!Files.isDirectory(origen)) throw new IOException("La entrada no es un directorio: " + origen.toAbsolutePath());
        if (extension == null || !extension.matches("[A-Za-z0-9]+"))
            throw new IllegalArgumentException("La extensión debe contener solo letras o números");
        Path entradaReal = origen.toRealPath();
        Path salida = trabajo.toAbsolutePath().normalize();
        Path existente = salida;
        while (existente != null && !Files.exists(existente)) existente = existente.getParent();
        if (existente != null) salida = existente.toRealPath().resolve(existente.relativize(salida));
        if (salida.startsWith(entradaReal)) throw new IllegalArgumentException("La salida debe quedar fuera de la entrada");

        Path copias = Files.createDirectories(trabajo.resolve("copias"));
        Path copia = copias.resolve("clientes.csv");
        List<String> incidencias = new ArrayList<>();
        try {
            if (reemplazar) Files.copy(origen.resolve("clientes.csv"), copia, StandardCopyOption.REPLACE_EXISTING);
            else Files.copy(origen.resolve("clientes.csv"), copia);
        } catch (FileAlreadyExistsException ex) {
            incidencias.add("Conservo la copia existente: " + copia);
        } catch (IOException ex) {
            incidencias.add("No se pudo copiar clientes.csv: " + ex.getMessage());
        }

        try (Stream<Path> directos = Files.list(origen)) {
            directos.sorted().forEach(p -> System.out.println("Primer nivel: " + origen.relativize(p)));
        }
        List<ArchivoInventariado> filas = new ArrayList<>();
        String sufijo = "." + extension.toLowerCase(Locale.ROOT);
        Files.walkFileTree(origen, EnumSet.noneOf(FileVisitOption.class), 4, new SimpleFileVisitor<>() {
            @Override public FileVisitResult visitFile(Path p, BasicFileAttributes a) {
                if (a.isRegularFile() && p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(sufijo))
                    filas.add(new ArchivoInventariado(origen.relativize(p), a.size(), a.lastModifiedTime().toInstant()));
                return FileVisitResult.CONTINUE;
            }
            @Override public FileVisitResult visitFileFailed(Path p, IOException ex) {
                incidencias.add(p + ": " + ex.getMessage());
                return FileVisitResult.CONTINUE;
            }
            @Override public FileVisitResult postVisitDirectory(Path p, IOException ex) {
                if (ex != null) incidencias.add(p + ": " + ex.getMessage());
                return FileVisitResult.CONTINUE;
            }
        });
        filas.sort(Comparator.comparing(f -> f.relativa().toString()));
        long total = filas.stream().mapToLong(ArchivoInventariado::bytes).sum();
        ArchivoInventariado mayor = null;
        for (ArchivoInventariado fila : filas)
            if (mayor == null || fila.bytes() > mayor.bytes()) mayor = fila;
        Path informe = trabajo.resolve("inventario.csv");
        try (var writer = Files.newBufferedWriter(informe, StandardCharsets.UTF_8);
             var csv = new CSVPrinter(writer, CSVFormat.RFC4180.builder()
                     .setHeader("ruta", "bytes", "modificado").get())) {
            for (ArchivoInventariado fila : filas)
                csv.printRecord(fila.relativa(), fila.bytes(), fila.modificado());
        }
        return new Resultado(List.copyOf(filas), total, mayor, List.copyOf(incidencias), informe);
    }
}
