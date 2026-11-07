package es.mjsplayground.acda.ficheros.inventario;

import static org.junit.jupiter.api.Assertions.*;

import es.mjsplayground.acda.ficheros.inventario.demo.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AmpliacionTest {
  @TempDir Path carpeta;

  @Test
  void registroFijoTiene16BytesYAccesoAleatorioValidaLimites() throws Exception {
    Path archivo = carpeta.resolve("registro.bin");
    BinariosDemo.escribir(archivo);
    assertEquals(16, Files.size(archivo));
    assertEquals(new BinariosDemo.Registro(42, 1250), BinariosDemo.leer(archivo, 0));
    assertThrows(IOException.class, () -> BinariosDemo.leer(archivo, -1));
    assertThrows(IOException.class, () -> BinariosDemo.leer(archivo, 1));
    Files.write(archivo, new byte[] {0, 0, 0, 1, 0});
    assertThrows(IOException.class, () -> BinariosDemo.leer(archivo, 0));
  }

  @Test
  void domConservaCaracteresYTransformaCsvSinAlterarElOriginal() throws Exception {
    Path xml = carpeta.resolve("nombres.xml"), csv = carpeta.resolve("nombres.csv");
    List<String> nombres = List.of("Taza, blanca", "Caja & soporte", "Dijo \"hola\"");
    AmpliacionDemo.escribirNombresXml(xml, nombres);
    assertEquals(nombres, AmpliacionDemo.leerNombresXml(xml));
    byte[] original = Files.readAllBytes(xml);
    AmpliacionDemo.nombresXmlACsv(xml, csv);
    assertArrayEquals(original, Files.readAllBytes(xml));
    String texto = Files.readString(csv, StandardCharsets.UTF_8);
    assertTrue(texto.contains("\"Taza, blanca\""));
    assertTrue(texto.contains("Dijo \"\"hola\"\""));
  }

  @Test
  void doctypeConEntidadExternaEsRechazadoYConservaCausa() throws Exception {
    Path secreto = carpeta.resolve("no-leer.txt"), xml = carpeta.resolve("externo.xml");
    Files.writeString(secreto, "CONTENIDO-PRIVADO", StandardCharsets.UTF_8);
    Files.writeString(
        xml,
        "<!DOCTYPE productos [<!ENTITY xxe SYSTEM \""
            + secreto.toUri()
            + "\">]>"
            + "<productos><producto><nombre>&xxe;</nombre></producto></productos>");
    IOException fallo = assertThrows(IOException.class, () -> AmpliacionDemo.leerNombresXml(xml));
    assertNotNull(fallo.getCause());
    assertFalse(fallo.toString().contains("CONTENIDO-PRIVADO"));
  }

  @Test
  void streamingRespetaRegistrosLogicosConComaYSalto() throws Exception {
    Path entrada = carpeta.resolve("entrada.csv"), salida = carpeta.resolve("salida.csv");
    Files.writeString(
        entrada,
        "codigo,nombre,unidades,precio\r\nP1,\"Taza,\nblanca\",10,4.50\r\nP2,Caja,2,1.00\r\n",
        StandardCharsets.UTF_8);
    assertEquals(1, AmpliacionDemo.filtrarCsv(entrada, salida, 5));
    String texto = Files.readString(salida, StandardCharsets.UTF_8);
    assertTrue(texto.contains("Taza,\nblanca"));
    assertFalse(texto.contains("P2"));
    assertThrows(IOException.class, () -> AmpliacionDemo.filtrarCsv(entrada, salida, 5));
  }

  @Test
  void watchObservaCreacionYLaEscrituraTemporalNoDejaResiduos() throws Exception {
    try (WatchService vigilancia = AmpliacionDemo.registrar(carpeta)) {
      Path nuevo = carpeta.resolve("nuevo.txt");
      Files.writeString(nuevo, "primero");
      List<AmpliacionDemo.Cambio> eventos = AmpliacionDemo.esperarEventos(vigilancia, 3000);
      assertTrue(
          eventos.stream()
              .anyMatch(
                  e ->
                      e.tipo().equals("ENTRY_CREATE")
                          && Path.of("nuevo.txt").equals(e.relativo())));
      AmpliacionDemo.escribirFiable(nuevo, "revisión");
      assertEquals("revisión", Files.readString(nuevo));
      try (var archivos = Files.list(carpeta)) {
        assertEquals(1, archivos.count());
      }
    }
  }
}
