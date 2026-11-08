package es.mjsplayground.acda.ficheros.inventario;

import static org.junit.jupiter.api.Assertions.*;

import es.mjsplayground.acda.ficheros.inventario.model.entity.*;
import es.mjsplayground.acda.ficheros.inventario.model.repository.*;
import es.mjsplayground.acda.ficheros.inventario.model.service.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

/** Oráculos optativos por fase; no implementan los métodos pendientes. */
class FasesGuiadoTest {
  @TempDir Path dir;

  Producto especial() {
    return new Producto("P09", "Caja, \"azul\"\nsegunda línea", 0, new BigDecimal("1.25"));
  }

  @Test
  @EnabledIfSystemProperty(named = "fase", matches = "rutas|completo")
  void rutas() throws Exception {
    Files.writeString(dir.resolve("uno.csv"), "");
    Files.writeString(dir.resolve("dos.json"), "");
    Files.createDirectory(dir.resolve("sub"));
    Files.writeString(dir.resolve("sub/oculto.csv"), "");
    assertEquals(List.of(dir.resolve("uno.csv")), new ArchivosRepository().listar(dir, "csv"));
  }

  @Test
  @EnabledIfSystemProperty(named = "fase", matches = "csv-lectura|completo")
  void csvLectura() throws Exception {
    Path p = dir.resolve("entrada.csv");
    Files.writeString(
        p, "codigo,nombre,unidades,precio\r\nP09,\"Caja, \"\"azul\"\"\nsegunda línea\",0,1.25\r\n");
    assertEquals(List.of(especial()), new CsvProductoRepository().leer(p));
  }

  @Test
  @EnabledIfSystemProperty(named = "fase", matches = "csv-escritura|completo")
  void csvEscritura() throws Exception {
    Path p = dir.resolve("salida.csv");
    new CsvProductoRepository().guardar(p, List.of(especial()));
    try (var lector = Files.newBufferedReader(p);
        var csv =
            org.apache.commons.csv.CSVFormat.RFC4180
                .builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .get()
                .parse(lector)) {
      var filas = csv.getRecords();
      assertEquals(1, filas.size());
      assertEquals(especial().nombre(), filas.getFirst().get("nombre"));
    }
  }

  @Test
  @EnabledIfSystemProperty(named = "fase", matches = "crud|completo")
  void crud() {
    var s = new InventarioService();
    s.guardarProducto(null, especial());
    assertEquals(4, s.listar().size());
    var antes = s.listar();
    assertThrows(InventarioException.class, () -> s.guardarProducto(null, especial()));
    assertEquals(antes, s.listar());
    s.guardarProducto("P09", new Producto("P09", especial().nombre(), 2, new BigDecimal("2.50")));
    assertEquals(
        2,
        s.listar().stream()
            .filter(p -> p.codigo().equals("P09"))
            .findFirst()
            .orElseThrow()
            .unidades());
  }

  @Test
  @EnabledIfSystemProperty(named = "fase", matches = "carga|completo")
  void cargaCompletaOConservada() throws Exception {
    var s = new InventarioService();
    var antes = s.listar();
    Path p = dir.resolve("tarde.csv");
    Files.writeString(
        p, "codigo,nombre,unidades,precio\nN01,Nuevo,2,1.00\nN02,Incorrecto,-1,1.00\n");
    assertThrows(InventarioException.class, () -> s.cargar(p, Formato.CSV));
    assertEquals(antes, s.listar());
  }

  @Test
  @EnabledIfSystemProperty(named = "fase", matches = "json|completo")
  void json() throws Exception {
    Path p = dir.resolve("datos.json");
    var r = new JsonProductoRepository();
    r.guardar(p, List.of(especial()));
    assertEquals(List.of(especial()), r.leer(p));
    Files.writeString(
        p, "[{\"codigo\":\"P09\",\"nombre\":\"X\",\"unidades\":2147483648,\"precio\":1}]");
    assertThrows(java.io.IOException.class, () -> r.leer(p));
  }

  @Test
  @EnabledIfSystemProperty(named = "fase", matches = "bin|completo")
  void binarioPropioYCorrupto() throws Exception {
    var r = new BinProductoRepository();
    Path p = dir.resolve("propio.bin");
    r.guardar(p, List.of(especial()));
    assertEquals(List.of(especial()), r.leer(p));
    Files.write(p, new byte[] {1, 2, 3});
    assertThrows(java.io.IOException.class, () -> r.leer(p));
  }
}
