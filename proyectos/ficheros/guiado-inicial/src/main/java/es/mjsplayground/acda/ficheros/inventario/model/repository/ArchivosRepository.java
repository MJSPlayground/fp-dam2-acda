package es.mjsplayground.acda.ficheros.inventario.model.repository;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ArchivosRepository {
  public List<Path> listar(Path directorio, String extension) throws IOException {
    // TODO 1: listar archivos del directorio y filtrar extensión sin recursión
    throw new UnsupportedOperationException(
        "Etapa pendiente: 1: listar archivos del directorio y filtrar extensión sin recursión");
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
