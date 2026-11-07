package es.mjsplayground.acda.ficheros.inventario.config;

import es.mjsplayground.acda.ficheros.inventario.model.entity.*;
import java.io.*;
import java.math.*;
import java.nio.file.*;
import java.util.Set;

/** Solo para archivos propios. Una lista blanca no convierte datos externos en confiables. */
public final class FiltroSerializacion {
  private static final Set<Class<?>> CLASES =
      Set.of(
          Instantanea.class,
          Producto.class,
          Producto[].class,
          BigDecimal.class,
          BigInteger.class,
          Number.class,
          String.class,
          byte[].class,
          int[].class);

  public static ObjectInputStream abrirSeguro(Path archivo) throws IOException {
    if (Files.size(archivo) > 5_000_000) throw new IOException("Archivo binario demasiado grande");
    InputStream fuente = Files.newInputStream(archivo);
    try {
      ObjectInputStream in = new ObjectInputStream(fuente);
      in.setObjectInputFilter(
          info -> {
            if (info.depth() > 10
                || info.references() > 60000
                || info.streamBytes() > 5_000_000
                || info.arrayLength() > 5000) return ObjectInputFilter.Status.REJECTED;
            Class<?> c = info.serialClass();
            return c == null
                ? ObjectInputFilter.Status.UNDECIDED
                : CLASES.contains(c)
                    ? ObjectInputFilter.Status.ALLOWED
                    : ObjectInputFilter.Status.REJECTED;
          });
      return in;
    } catch (IOException | RuntimeException e) {
      try {
        fuente.close();
      } catch (IOException cierre) {
        e.addSuppressed(cierre);
      }
      throw e;
    }
  }
}
