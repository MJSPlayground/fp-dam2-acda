package es.mjsplayground.acda.ficheros.model.service;

import es.mjsplayground.acda.ficheros.model.entity.ArchivoInventariado;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.util.EnumSet;
import java.nio.file.FileVisitOption;

public final class InventarioArchivosService {
    public record Inventario(List<ArchivoInventariado> archivos, List<String> incidencias, long bytes, Path mayor) {
        public Inventario { archivos = List.copyOf(archivos); incidencias = List.copyOf(incidencias); }
    }
    public Inventario crear(Path raiz, String extension) throws IOException {
        if (raiz == null || !Files.isDirectory(raiz)) throw new IOException("No es un directorio: " + raiz);
        if (extension == null || !extension.matches("\\.?[A-Za-z0-9]+"))
            throw new IllegalArgumentException("Indica una extensión simple, por ejemplo csv");
        String sufijo = (extension.startsWith(".") ? extension : "." + extension).toLowerCase(Locale.ROOT);
        List<ArchivoInventariado> archivos = new ArrayList<>(); List<String> incidencias = new ArrayList<>();
        Files.walkFileTree(raiz, EnumSet.noneOf(FileVisitOption.class), 4, new SimpleFileVisitor<>() {
            @Override public FileVisitResult visitFile(Path p, BasicFileAttributes a) {
                if (a.isRegularFile() && p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(sufijo))
                    archivos.add(new ArchivoInventariado(raiz.relativize(p), a.size(), a.lastModifiedTime().toInstant()));
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
        archivos.sort(Comparator.comparing(a -> a.relativa().toString()));
        incidencias.sort(String::compareTo);
        long bytes = archivos.stream().mapToLong(ArchivoInventariado::bytes).sum();
        Path mayor = archivos.stream().max(Comparator.comparingLong(ArchivoInventariado::bytes)
                .thenComparing(a -> a.relativa().toString(), Comparator.reverseOrder()))
                .map(ArchivoInventariado::relativa).orElse(null);
        return new Inventario(archivos, incidencias, bytes, mayor);
    }
}
