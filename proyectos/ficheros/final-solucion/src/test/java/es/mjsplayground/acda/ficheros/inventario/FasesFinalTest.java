package es.mjsplayground.acda.ficheros.inventario;

import static org.junit.jupiter.api.Assertions.*;

import es.mjsplayground.acda.ficheros.inventario.config.*;
import es.mjsplayground.acda.ficheros.inventario.model.entity.*;
import es.mjsplayground.acda.ficheros.inventario.model.repository.*;
import es.mjsplayground.acda.ficheros.inventario.model.service.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

class FasesFinalTest {
  @TempDir Path dir;

  Path lote() throws IOException {
    Path p = dir.resolve("lote.csv");
    Files.writeString(
        p,
        "codigo,nombre,unidades,precio\n"
            + "P01,Teclado,2,24.90\n"
            + "P04,Cable,3,5.90\n"
            + "DUP,Uno,1,1.00\n"
            + "DUP,Dos,1,1.00\n");
    return p;
  }

  @Test
  @EnabledIfSystemProperty(named = "fase", matches = "base|completo")
  void base() throws Exception {
    var s = new InventarioService();
    Path p = dir.resolve("seleccion.json");
    s.exportarFiltrados(p, Formato.JSON, "", 10);
    assertEquals(2, new JsonProductoRepository().leer(p).size());
    assertEquals(3, s.listar().size());
  }

  @Test
  @EnabledIfSystemProperty(named = "fase", matches = "aplicacion|completo")
  void aplicacion() throws Exception {
    var s = new InventarioService();
    var antes = s.listar();
    var plan = s.previsualizar(lote(), Formato.CSV);
    assertEquals(antes, s.listar());
    assertEquals(4, plan.resultado().size());
    assertEquals(2, plan.incidencias().size());
    assertTrue(plan.resultado().stream().noneMatch(p -> p.codigo().equals("DUP")));
  }

  @Test
  @EnabledIfSystemProperty(named = "fase", matches = "reto|completo")
  void retoVersionRecuperacionYFalloPreparado() throws Exception {
    var s = new InventarioService();
    var antes = s.listar();
    var plan = s.previsualizar(lote(), Formato.CSV);
    Path copia = dir.resolve("copia.bin");
    s.aplicar(plan, copia);
    assertEquals(antes, new BinProductoRepository().leer(copia));
    s.recuperar(copia);
    assertEquals(antes, s.listar());
    var viejo = s.previsualizar(lote(), Formato.CSV);
    s.guardarProducto("P01", new Producto("P01", "Teclado", 11, new BigDecimal("25.90")));
    var actual = s.listar();
    assertThrows(InventarioException.class, () -> s.aplicar(viejo, dir.resolve("vieja.bin")));
    assertEquals(actual, s.listar());
    assertFalse(Files.exists(dir.resolve("vieja.bin")));
    IOException disco = new IOException("Fallo de almacenamiento inducido para el ensayo");
    Map<Formato, InventarioRepository> repos = new EnumMap<>(Formato.class);
    repos.putAll(Repositorios.crear());
    repos.put(
        Formato.BIN,
        new BinProductoRepository() {
          @Override
          public void guardar(Path destino, List<Producto> productos) throws IOException {
            throw disco;
          }
        });
    var averiado = new InventarioService(repos, new ArchivosRepository());
    var previo = averiado.listar();
    var propuesta = averiado.previsualizar(lote(), Formato.CSV);
    InventarioException e =
        assertThrows(
            InventarioException.class,
            () -> averiado.aplicar(propuesta, dir.resolve("fallida.bin")));
    assertSame(disco, e.getCause());
    assertEquals(previo, averiado.listar());
    assertFalse(Files.exists(dir.resolve("fallida.bin")));
  }
}
