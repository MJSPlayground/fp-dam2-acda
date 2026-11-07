package es.mjsplayground.acda.ficheros.inventario.model.repository;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ArchivosRepository {
  public List<Path> listar(Path directorio, String extension) throws IOException {
    try (var archivos = Files.list(directorio)) {
      return archivos
          .filter(Files::isRegularFile)
          .filter(
              p ->
                  extension == null
                      || extension.isBlank()
                      || p.getFileName()
                          .toString()
                          .toLowerCase(Locale.ROOT)
                          .endsWith("." + extension.toLowerCase(Locale.ROOT)))
          .sorted(Comparator.comparing(p -> p.getFileName().toString()))
          .toList();
    }
  }

  public void copiarEjemplos(Path destino) throws IOException {
    Files.createDirectories(destino);
    for (String nombre : List.of("productos.csv", "productos.json", "lote.csv", "lote.json"))
      try (InputStream in = getClass().getResourceAsStream("/data/" + nombre)) {
        if (in == null) throw new IOException("Falta recurso classpath /data/" + nombre);
        Files.copy(in, destino.resolve(nombre));
      }
  }
}
