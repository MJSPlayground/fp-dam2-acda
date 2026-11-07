package es.mjsplayground.acda.ficheros.inventario;

import static org.junit.jupiter.api.Assertions.*;

import es.mjsplayground.acda.ficheros.inventario.config.*;
import es.mjsplayground.acda.ficheros.inventario.model.entity.*;
import es.mjsplayground.acda.ficheros.inventario.model.repository.*;
import es.mjsplayground.acda.ficheros.inventario.model.service.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

class InventarioTest {
  @TempDir Path dir;

  @Test
  void roundTripTresFormatosIncluyeComillasYUnicode() throws Exception {
    var service = new InventarioService();
    service.guardarProducto(
        null,
        new Producto("ESPECIAL", "Ratón, \"óptico\"\nsegunda línea", 0, new BigDecimal("12.34")));
    for (Formato formato : Formato.values()) {
      Path f = dir.resolve("datos." + formato.name().toLowerCase());
      service.guardar(f, formato);
      var recuperado = new InventarioService();
      recuperado.cargar(f, formato);
      assertEquals(service.listar(), recuperado.listar());
    }
  }

  @Test
  void csvNoHaceSplitDeTextoYValidaEncabezados() {
    Path f = dir.resolve("mal.csv");
    var s = new InventarioService();
    var anterior = s.listar();
    assertDoesNotThrow(
        () ->
            Files.writeString(
                f,
                "codigo,nombre,unidades,precio\r\nX,\"sin cerrar,1,2\r\n",
                StandardCharsets.UTF_8));
    InventarioException e = assertThrows(InventarioException.class, () -> s.cargar(f, Formato.CSV));
    assertNotNull(e.getCause());
    assertEquals(anterior, s.listar());
  }

  @Test
  void jsonRechazaRangosFraccionesNulosAusenciasYCola() throws Exception {
    var s = new InventarioService();
    var antes = s.listar();
    for (String json :
        List.of(
            "[{\"codigo\":\"X\",\"nombre\":\"X\",\"unidades\":2147483648,\"precio\":1}]",
            "[{\"codigo\":\"X\",\"nombre\":\"X\",\"unidades\":1.5,\"precio\":1}]",
            "[{\"codigo\":\"X\",\"nombre\":\"X\",\"unidades\":null,\"precio\":1}]",
            "[{\"codigo\":\"X\",\"nombre\":\"X\",\"precio\":1}]",
            "[] {}",
            "null",
            "{}")) {
      Path f = dir.resolve("incorrecto.json");
      Files.writeString(f, json);
      assertThrows(InventarioException.class, () -> s.cargar(f, Formato.JSON));
      assertEquals(antes, s.listar());
    }
  }

  @Test
  void validacionDeDominioYDuplicadosNoPierdeCatalogo() throws Exception {
    var s = new InventarioService();
    var antes = s.listar();
    for (Producto p :
        List.of(
            new Producto("x", "X", 0, new BigDecimal("1")),
            new Producto("X", " ", 0, new BigDecimal("1")),
            new Producto("X", "X", -1, new BigDecimal("1")),
            new Producto("X", "X", 0, new BigDecimal("0")),
            new Producto("X", "X", 0, new BigDecimal("1.001")),
            new Producto("X", "X", 1000001, new BigDecimal("1"))))
      assertThrows(InventarioException.class, () -> s.guardarProducto(null, p));
    Path f = dir.resolve("dup.csv");
    Files.writeString(f, "codigo,nombre,unidades,precio\nX,Uno,1,1.00\nX,Otro,2,2.00\n");
    assertThrows(InventarioException.class, () -> s.cargar(f, Formato.CSV));
    assertEquals(antes, s.listar());
  }

  @Test
  void binarioRechazaClaseAjenaYTamanioSinCambiarCatalogo() throws Exception {
    var s = new InventarioService();
    var antes = s.listar();
    Path f = dir.resolve("ajeno.bin");
    try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(f))) {
      out.writeObject(new java.util.Date());
    }
    InventarioException e = assertThrows(InventarioException.class, () -> s.cargar(f, Formato.BIN));
    assertInstanceOf(InvalidClassException.class, e.getCause());
    assertEquals(antes, s.listar());
    try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(f))) {
      out.writeObject(new Instantanea(new Producto[5001]));
    }
    assertThrows(InventarioException.class, () -> s.cargar(f, Formato.BIN));
    assertEquals(antes, s.listar());
  }

  @Test
  void escrituraFallidaConservaDestinoYCausa() throws Exception {
    Path f = dir.resolve("previo.json");
    Files.writeString(f, "contenido anterior");
    IOException original = new IOException("fallo inducido");
    IOException e =
        assertThrows(
            IOException.class,
            () ->
                EscrituraProtegida.escribir(
                    f,
                    temp -> {
                      Files.writeString(temp, "incompleto");
                      throw original;
                    }));
    assertSame(original, e);
    assertEquals("contenido anterior", Files.readString(f));
    try (var archivos = Files.list(dir)) {
      assertTrue(archivos.noneMatch(p -> p.getFileName().toString().startsWith(".inventario-")));
    }
  }

  @Test
  void listarNoRecurreYFiltraExtension() throws Exception {
    Files.writeString(dir.resolve("uno.csv"), "");
    Files.writeString(dir.resolve("dos.JSON"), "");
    Files.createDirectory(dir.resolve("sub"));
    Files.writeString(dir.resolve("sub/oculto.csv"), "");
    var r = new ArchivosRepository();
    assertEquals(List.of(dir.resolve("uno.csv")), r.listar(dir, "csv"));
    assertEquals(List.of(dir.resolve("dos.JSON")), r.listar(dir, "json"));
    assertEquals(2, r.listar(dir, "").size());
  }
 @Test void recursosPublicosCsvJsonCarganTalComoSeDistribuyen(){
  var s=new InventarioService();var esperado=s.listar();s.copiarEjemplos(dir);s.cargar(dir.resolve("productos.csv"),Formato.CSV);assertEquals(esperado,s.listar());s.cargar(dir.resolve("productos.json"),Formato.JSON);assertEquals(esperado,s.listar());
 }
 @Test void jsonNoConvierteNumeroEnTextoNiAceptaClavesDuplicadas()throws Exception{
  var s=new InventarioService();var anterior=s.listar();Path archivo=dir.resolve("tipos.json");
  for(String json:List.of("[{\"codigo\":123,\"nombre\":\"X\",\"unidades\":1,\"precio\":1}]","[{\"codigo\":\"X\",\"nombre\":\"X\",\"unidades\":1,\"unidades\":2,\"precio\":1}]")){Files.writeString(archivo,json);assertThrows(InventarioException.class,()->s.cargar(archivo,Formato.JSON));assertEquals(anterior,s.listar());}
 }
}
