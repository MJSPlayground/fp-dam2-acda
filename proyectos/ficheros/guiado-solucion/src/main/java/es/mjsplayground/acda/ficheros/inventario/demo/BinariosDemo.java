package es.mjsplayground.acda.ficheros.inventario.demo;

import java.io.*;
import java.nio.file.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Demostración pequeña: cabecera int y un registro int + long. */
public final class BinariosDemo {
  private static final Logger log = LoggerFactory.getLogger(BinariosDemo.class);

  public record Registro(int id, long centimos) {}

  public static void escribir(Path archivo) throws IOException {
    try (DataOutputStream salida =
        new DataOutputStream(Files.newOutputStream(archivo, StandardOpenOption.CREATE_NEW))) {
      salida.writeInt(1);
      salida.writeInt(42);
      salida.writeLong(1250);
    }
  }

  public static Registro leer(Path archivo, int indice) throws IOException {
    try (RandomAccessFile acceso = new RandomAccessFile(archivo.toFile(), "r")) {
      if (acceso.length() < 4 || acceso.readInt() != 1) {
        throw new IOException("Cabecera ausente o versión no compatible");
      }
      long posicion = 4L + 12L * indice;
      if (indice < 0 || posicion + 12L > acceso.length()) {
        throw new IOException("No existe ese registro completo");
      }
      acceso.seek(posicion);
      return new Registro(acceso.readInt(), acceso.readLong());
    }
  }

  public static void main(String[] args) throws IOException {
    Path carpeta = Files.createTempDirectory("inventario-binarios-demo-");
    Path archivo = carpeta.resolve("registro.bin");
    escribir(archivo);
    log.info("Demostración en {}: {} bytes, {}", archivo, Files.size(archivo), leer(archivo, 0));
  }
}
