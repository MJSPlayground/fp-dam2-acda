package es.mjsplayground.acda.spring.catalogo.config;

import java.nio.file.*;

public final class MySqlLocal {
  public static void main(String[] args) throws Exception {
    Path compose = Path.of("compose.yaml").toAbsolutePath();
    if (!Files.exists(compose))
      throw new IllegalStateException(
          "Working directory debe ser esta carpeta, junto a compose.yaml");
    int codigo =
        new ProcessBuilder(
                "docker", "compose", "-f", compose.toString(), "up", "-d", "--wait", "mysql")
            .inheritIO()
            .start()
            .waitFor();
    if (codigo != 0) throw new IllegalStateException("Revisa Docker Desktop y el puerto 3309.");
  }
}
