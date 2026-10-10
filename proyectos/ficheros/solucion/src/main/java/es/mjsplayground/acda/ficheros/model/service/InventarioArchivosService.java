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
import java.util.stream.Stream;

public final class InventarioArchivosService {
    public record Inventario(List<ArchivoInventariado> archivos, List<String> incidencias, long bytes, Path mayor) {
        public Inventario { archivos = List.copyOf(archivos); incidencias = List.copyOf(incidencias); }
    }
    public Inventario crear(Path raiz, String extension) throws IOException {
        if (!Files.isDirectory(raiz)) throw new IOException("No es un directorio: " + raiz);
        if (extension.isBlank()) throw new IllegalArgumentException("Extensión vacía");
        String sufijo = (extension.startsWith(".") ? extension : "." + extension).toLowerCase(Locale.ROOT);
        List<ArchivoInventariado> archivos = new ArrayList<>(); List<String> incidencias = new ArrayList<>();
        try (Stream<Path> rutas = Files.walk(raiz, 4)) {
            for (Path p : rutas.filter(Files::isRegularFile).toList()) {
                if (!p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(sufijo)) continue;
                try {
                    BasicFileAttributes a = Files.readAttributes(p, BasicFileAttributes.class);
                    archivos.add(new ArchivoInventariado(raiz.relativize(p), a.size(), a.lastModifiedTime().toInstant()));
                } catch (IOException ex) { incidencias.add(p + ": " + ex.getMessage()); }
            }
        }
        archivos.sort(Comparator.comparing(a -> a.relativa().toString()));
        long bytes = archivos.stream().mapToLong(ArchivoInventariado::bytes).sum();
        Path mayor = archivos.stream().max(Comparator.comparingLong(ArchivoInventariado::bytes)
                .thenComparing(a -> a.relativa().toString(), Comparator.reverseOrder()))
                .map(ArchivoInventariado::relativa).orElse(null);
        return new Inventario(archivos, incidencias, bytes, mayor);
    }
}
