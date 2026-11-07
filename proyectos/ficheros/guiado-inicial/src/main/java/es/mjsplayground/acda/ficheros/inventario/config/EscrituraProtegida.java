package es.mjsplayground.acda.ficheros.inventario.config;

import java.io.IOException;
import java.nio.file.*;

/**
 * Infraestructura preparada, derivada de ComercialJsonDao histórico. API detallada en Ampliación.
 */
public final class EscrituraProtegida {
  @FunctionalInterface
  public interface Accion {
    void escribir(Path temporal) throws IOException;
  }

  public static void escribir(Path archivo, Accion accion) throws IOException {
    Path destino = archivo.toAbsolutePath();
    Files.createDirectories(destino.getParent());
    Path temporal = Files.createTempFile(destino.getParent(), ".inventario-", ".tmp");
    try {
      accion.escribir(temporal);
      try {
        Files.move(
            temporal, destino, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } catch (AtomicMoveNotSupportedException e) {
        Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException | RuntimeException e) {
      try {
        Files.deleteIfExists(temporal);
      } catch (IOException cierre) {
        e.addSuppressed(cierre);
      }
      throw e;
    }
  }
}
