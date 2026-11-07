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
import org.apache.commons.csv.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

class ConciliacionTest {
  @TempDir Path dir;

  Path lote(String datos) throws Exception {
    Path p = dir.resolve("lote.csv");
    Files.writeString(p, "codigo,nombre,unidades,precio\n" + datos, StandardCharsets.UTF_8);
    return p;
  }

  @Test
  void previewConciliacionBackupInformeYRecuperacion() throws Exception {
    var s = new InventarioService();
    var antes = s.listar();
    var plan =
        s.previsualizar(
            lote(
                "P01,Teclado,2,24.90\n"
                    + "P04,Cable,3,5.90\n"
                    + "P02,Otro nombre,1,1.00\n"
                    + "DUP,Uno,1,1.00\n"
                    + "DUP,Dos,1,1.00\n"
                    + "BAD,Negativo,-1,1.00\n"),
            Formato.CSV);
    assertEquals(antes, s.listar());
    assertEquals(4, plan.resultado().size());
    assertEquals(4, plan.incidencias().size());
    assertEquals(
        12,
        plan.resultado().stream()
            .filter(p -> p.codigo().equals("P01"))
            .findFirst()
            .orElseThrow()
            .unidades());
    Path backup = dir.resolve("recuperacion/previo.bin");
    s.aplicar(plan, backup);
    assertEquals(plan.resultado(), s.listar());
    assertEquals(antes, new BinProductoRepository().leer(backup));
    Path informe = dir.resolve("incidencias.csv");
    s.exportarIncidencias(plan, informe);
    try (var in = Files.newBufferedReader(informe, StandardCharsets.UTF_8);
        var csv =
            CSVFormat.RFC4180.builder().setHeader().setSkipHeaderRecord(true).get().parse(in)) {
      assertEquals(List.of("fila", "codigo", "motivo"), csv.getHeaderNames());
      assertEquals(4, csv.getRecords().size());
    }
    s.recuperar(backup);
    assertEquals(antes, s.listar());
  }

  @Test
  void duplicadoIncluyeFilaInvalidaYCodigoNoSeAceptaPorOrden() throws Exception {
    var s = new InventarioService();
    Path f = dir.resolve("lote.json");
    Files.writeString(
        f,
        "[{\"codigo\":\"P04\",\"nombre\":\"Cable\",\"unidades\":1,\"precio\":1},{\"codigo\":\"P04\",\"nombre\":\"Cable\",\"unidades\":999999999999999999999,\"precio\":1},{\"codigo\":\"P05\",\"nombre\":\"Otro\",\"unidades\":2,\"precio\":2}]",
        StandardCharsets.UTF_8);
    var plan = s.previsualizar(f, Formato.JSON);
    assertEquals(2, plan.incidencias().size());
    assertTrue(plan.resultado().stream().noneMatch(p -> p.codigo().equals("P04")));
    assertTrue(plan.resultado().stream().anyMatch(p -> p.codigo().equals("P05")));
  }

  @Test
  void planObsoletoYBackupExistenteNoMutanNiSobrescriben() throws Exception {
    var s = new InventarioService();
    var plan = s.previsualizar(lote("P04,Cable,3,5.90\n"), Formato.CSV);
    s.guardarProducto("P01", new Producto("P01", "Teclado", 11, new BigDecimal("25.90")));
    var actual = s.listar();
    Path backup = dir.resolve("no-creado.bin");
    assertThrows(InventarioException.class, () -> s.aplicar(plan, backup));
    assertFalse(Files.exists(backup));
    assertEquals(actual, s.listar());
    var vigente = s.previsualizar(dir.resolve("lote.csv"), Formato.CSV);
    Files.writeString(backup, "backup ajeno previo");
    assertThrows(InventarioException.class, () -> s.aplicar(vigente, backup));
    assertEquals("backup ajeno previo", Files.readString(backup));
    assertEquals(actual, s.listar());
  }

  @Test
  void falloBackupLimpiaSoloReservaPropiaYConservaCausa() throws Exception {
    IOException causa = new IOException("disco lleno");
    Map<Formato, InventarioRepository> repos = new EnumMap<>(Formato.class);
    repos.putAll(Repositorios.crear());
    repos.put(
        Formato.BIN,
        new InventarioRepository() {
          public List<Producto> leer(Path p) throws IOException {
            throw causa;
          }

          public void guardar(Path p, List<Producto> lista) throws IOException {
            throw causa;
          }
        });
    var s = new InventarioService(repos, new ArchivosRepository());
    var antes = s.listar();
    var plan = s.previsualizar(lote("P04,Cable,3,5.90\n"), Formato.CSV);
    Path backup = dir.resolve("fallo.bin");
    InventarioException e = assertThrows(InventarioException.class, () -> s.aplicar(plan, backup));
    assertSame(causa, e.getCause());
    assertEquals(antes, s.listar());
    assertFalse(Files.exists(backup));
  }

  @Test
  void exportarSeleccionYErroresEstructuralesNoCambianCatalogo() throws Exception {
    var s = new InventarioService();
    Path seleccion = dir.resolve("seleccion.json");
    s.exportarFiltrados(seleccion, Formato.JSON, "TECL", 10);
    assertEquals(List.of(s.listar().getFirst()), new JsonProductoRepository().leer(seleccion));
    var antes = s.listar();
    Path mal = dir.resolve("roto.json");
    Files.writeString(mal, "[{\"codigo\":");
    assertThrows(InventarioException.class, () -> s.previsualizar(mal, Formato.JSON));
    assertEquals(antes, s.listar());
    Path backup = dir.resolve("invalido.bin");
    Files.writeString(backup, "no es binario");
    assertThrows(InventarioException.class, () -> s.recuperar(backup));
    assertEquals(antes, s.listar());
  }
 @Test void lotePublicadoProduceElResultadoDocumentado(){var s=new InventarioService();s.copiarEjemplos(dir);var plan=s.previsualizar(dir.resolve("lote.csv"),Formato.CSV);assertEquals(4,plan.resultado().size());assertEquals(4,plan.incidencias().size());assertEquals(12,plan.resultado().stream().filter(p->p.codigo().equals("P01")).findFirst().orElseThrow().unidades());}
}
