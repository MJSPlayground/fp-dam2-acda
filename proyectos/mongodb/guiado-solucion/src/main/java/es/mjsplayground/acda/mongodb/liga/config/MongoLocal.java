package es.mjsplayground.acda.mongodb.liga.config;

import java.nio.file.*;

public final class MongoLocal {
  public static void main(String[] args) throws Exception {
    Path compose = Path.of("compose.yaml").toAbsolutePath();
    if (!Files.exists(compose))
      throw new IllegalStateException(
          "Selecciona esta carpeta de proyecto como Working directory de IntelliJ");
    Process proceso =
        new ProcessBuilder("docker", "compose", "-f", compose.toString(), "up", "-d", "--wait")
            .inheritIO()
            .start();
    if (proceso.waitFor() != 0)
      throw new IllegalStateException(
          "No se pudo arrancar MongoDB. Comprueba Docker Desktop y puerto27018.");
  }
}
