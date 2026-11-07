package es.mjsplayground.acda.firestore.reservas.config;

import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.slf4j.*;

/** Arranque local de la herramienta oficial, independiente de Spring. */
public final class Emulador {
  private static final Logger log = LoggerFactory.getLogger(Emulador.class);

  public static void main(String[] args) throws Exception {
    Path jar =
        Path.of(
            System.getProperty("user.home"),
            ".cache",
            "firebase",
            "emulators",
            "cloud-firestore-emulator-v1.22.0.jar");
    if (!Files.exists(jar)) {
      Files.createDirectories(jar.getParent());
      Path temporal = Files.createTempFile(jar.getParent(), "firestore-", ".download");
      URI url =
          URI.create(
              "https://storage.googleapis.com/firebase-preview-drop/emulator/cloud-firestore-emulator-v1.22.0.jar");
      log.info("Descargando emulador oficial: {}", url);
      var respuesta =
          HttpClient.newBuilder()
              .connectTimeout(java.time.Duration.ofSeconds(30))
              .followRedirects(HttpClient.Redirect.NORMAL)
              .build()
              .send(
                  HttpRequest.newBuilder(url)
                      .timeout(java.time.Duration.ofMinutes(5))
                      .GET()
                      .build(),
                  HttpResponse.BodyHandlers.ofFile(temporal));
      if (respuesta.statusCode() != 200)
        throw new IllegalStateException(
            "No se pudo descargar el emulador: HTTP " + respuesta.statusCode());
      verificar(temporal);
      Files.move(temporal, jar, StandardCopyOption.REPLACE_EXISTING);
    }
    verificar(jar);
    String java =
        Path.of(
                System.getProperty("java.home"),
                "bin",
                System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java")
            .toString();
    Process proceso =
        new ProcessBuilder(
                java,
                "-jar",
                jar.toString(),
                "--host",
                "127.0.0.1",
                "--port",
                "8085",
                "--project_id",
                "demo-acda-reservas")
            .inheritIO()
            .start();
    Runtime.getRuntime().addShutdownHook(new Thread(proceso::destroy));
    int resultado = proceso.waitFor();
    if (resultado != 0)
      throw new IllegalStateException("El emulador terminó con código " + resultado);
  }

  // SHA-256: firebase-tools 15.35.0 lib/emulator/downloadableEmulatorInfo.json (fuente oficial).
  private static void verificar(Path archivo) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    try (var entrada = Files.newInputStream(archivo)) {
      byte[] buffer = new byte[8192];
      int leidos;
      while ((leidos = entrada.read(buffer)) != -1) digest.update(buffer, 0, leidos);
    }
    if (!HexFormat.of()
        .formatHex(digest.digest())
        .equals("9b6498b7f62714d67f48f59b3818883cd682dbcd46b9f59511de81c97bb5166c"))
      throw new IllegalStateException("SHA-256 inesperado: no se ejecuta el archivo descargado");
  }
}
