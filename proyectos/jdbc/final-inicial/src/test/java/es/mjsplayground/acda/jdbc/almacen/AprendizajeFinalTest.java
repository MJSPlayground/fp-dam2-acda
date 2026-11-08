package es.mjsplayground.acda.jdbc.almacen;

import static org.junit.jupiter.api.Assertions.*;

import es.mjsplayground.acda.jdbc.almacen.config.Database;
import es.mjsplayground.acda.jdbc.almacen.model.entity.*;
import es.mjsplayground.acda.jdbc.almacen.model.repository.*;
import es.mjsplayground.acda.jdbc.almacen.model.service.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

@org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "base|aplicacion|reto|todos")
class AprendizajeFinalTest {
  @TempDir Path temporal;
  Database db;
  OperacionesService servicio;
  AlmacenService almacen;

  @BeforeEach
  void preparar() throws Exception {
    db = new Database("jdbc:sqlite:" + temporal.resolve("test.db"));
    db.preparar();
    servicio = new OperacionesService(db);
    servicio.preparar();
    almacen = new AlmacenService(db);
  }

  @Test
  @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "base|todos")
  void filtraNombreLiteralInclusoTildesYStock() {
    assertEquals("Teclado", servicio.buscar("TE", 10).getFirst().nombre());
    assertTrue(servicio.buscar("monitor", 4).isEmpty());
    assertEquals("Ratón", servicio.buscar("RATÓN", 20).getFirst().nombre());
    assertTrue(servicio.buscar("%", 20).isEmpty());
    assertTrue(servicio.buscar("_", 20).isEmpty());
    assertThrows(ReglaNegocioException.class, () -> servicio.buscar("", -1));
  }

  @Test
  @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "base|todos")
  void precioRedondeadoSinCambiarResto() {
    assertEquals(2849, servicio.revisarPrecio(1, new BigDecimal("10")));
    assertEquals(10, producto(1).stock());
    assertEquals("Teclado", producto(1).nombre());
    assertEquals(648, servicio.revisarPrecio(2, new BigDecimal("-50")));
  }

  @Test
  @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "base|todos")
  void porcentajesInvalidosNoModifican() {
    for (String p : new String[] {"-50.01", "100.01", "0.001"})
      assertThrows(ReglaNegocioException.class, () -> servicio.revisarPrecio(1, new BigDecimal(p)));
    assertEquals(2590, producto(1).precioCentimos());
    assertThrows(ReglaNegocioException.class, () -> servicio.revisarPrecio(999, BigDecimal.TEN));
  }

  @Test
  @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "aplicacion|todos")
  void recepcionIndividualPersisteExistenciasEHistorial() {
    assertEquals(3750, servicio.recibirUna(new LineaRecepcion(1, 3, 1250)));
    assertEquals(13, producto(1).stock());
    assertEquals(2590, producto(1).precioCentimos());
    assertEquals(3750, servicio.historial().getFirst().totalCentimos());
    OperacionesService reabierto = new OperacionesService(db);
    assertEquals(1, reabierto.historial().size());
    assertThrows(
        ReglaNegocioException.class, () -> servicio.recibirUna(new LineaRecepcion(1, 0, 1250)));
    assertEquals(13, producto(1).stock());
  }

  @Test
  @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "reto|todos")
  void presupuestoExactoYRechazoSinEscrituras() {
    var lineas = List.of(new LineaRecepcion(1, 2, 1000), new LineaRecepcion(3, 1, 10000));
    assertThrows(ReglaNegocioException.class, () -> servicio.recibir(lineas, 11999));
    assertEquals(10, producto(1).stock());
    assertTrue(servicio.historial().isEmpty());
    assertEquals(12000, servicio.recibir(lineas, 12000));
    assertEquals(12, producto(1).stock());
    assertEquals(6, producto(3).stock());
    assertEquals(2, servicio.historial().size());
  }

  @Test
  @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "reto|todos")
  void ultimaLineaInexistenteRevierteLaPrimera() {
    assertThrows(
        ReglaNegocioException.class,
        () ->
            servicio.recibir(
                List.of(new LineaRecepcion(1, 2, 1000), new LineaRecepcion(999, 1, 10)), 100000));
    assertEquals(10, producto(1).stock());
    assertTrue(servicio.historial().isEmpty());
  }

  @Test
  @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "reto|todos")
  void falloTecnicoIntermedioRevierteTodoYConservaCausa() throws Exception {
    instalarFallo("NEW.producto_id=3");
    OperacionesService s = new OperacionesService(db);
    var e =
        assertThrows(
            PersistenciaException.class,
            () ->
                s.recibir(
                    List.of(new LineaRecepcion(1, 2, 1000), new LineaRecepcion(3, 1, 10000)),
                    12000));
    assertInstanceOf(SQLException.class, e.getCause());
    assertEquals(10, producto(1).stock());
    assertEquals(5, producto(3).stock());
    assertTrue(servicio.historial().isEmpty());
  }

  @Test
  @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "reto|todos")
  void duplicadosListaVaciaYDesbordamientoRechazados() {
    var l = new LineaRecepcion(1, 1, 10);
    assertThrows(ReglaNegocioException.class, () -> servicio.recibir(List.of(l, l), 100));
    assertThrows(ReglaNegocioException.class, () -> servicio.recibir(List.of(), 100));
    assertThrows(ReglaNegocioException.class, () -> servicio.recibir(List.of(l), 0));
    almacen.guardar(new Producto(1, "Teclado", 2590, Integer.MAX_VALUE));
    assertThrows(ReglaNegocioException.class, () -> servicio.recibirUna(l));
    assertEquals(Integer.MAX_VALUE, producto(1).stock());
    assertTrue(servicio.historial().isEmpty());
  }

  @Test
  @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "aplicacion|todos")
  void falloTrasJustificanteRevierteRecepcionIndividual() throws Exception {
    instalarFallo("1=1");
    var caso = new OperacionesService(db);
    var error = assertThrows(PersistenciaException.class,
        () -> caso.recibirUna(new LineaRecepcion(1, 3, 1250)));
    assertInstanceOf(SQLException.class, error.getCause());
    assertEquals(10, producto(1).stock());
    assertTrue(servicio.historial().isEmpty());
  }

  private void instalarFallo(String condicionConstante) throws SQLException {
    // SQL de fixture controlado: nunca recibe texto del usuario ni altera la base de la UI.
    try (Connection c = db.abrir(); var st = c.createStatement()) {
      st.execute("CREATE TRIGGER fallo_preparado AFTER INSERT ON recepcion WHEN "
          + condicionConstante + " BEGIN SELECT RAISE(ABORT, 'Fallo preparado tras escritura'); END");
    }
  }

  @Test
  @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "base|todos")
  void revisionFueraDePrecioMaximoConservaCatalogo() {
    almacen.guardar(new Producto(1, "Teclado", 100_000_000, 10));
    assertThrows(ReglaNegocioException.class, () -> servicio.revisarPrecio(1, new BigDecimal("0.01")));
    assertEquals(100_000_000, producto(1).precioCentimos());
    assertEquals(10, producto(1).stock());
  }

  @Test
  @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "reto|todos")
  void totalQueNoCabeSeRechazaAntesDeEscribir() {
    for (long coste : new long[]{0, -1, 100_000_001}) {
      assertThrows(ReglaNegocioException.class, () -> servicio.recibirUna(new LineaRecepcion(1, 1, coste)));
    }
    var lineas = new ArrayList<LineaRecepcion>();
    for (int id=1; id<=43; id++) lineas.add(new LineaRecepcion(id, Integer.MAX_VALUE, 100_000_000));
    assertThrows(ReglaNegocioException.class, () -> servicio.recibir(lineas, Long.MAX_VALUE));
    assertEquals(10, producto(1).stock());
    assertTrue(servicio.historial().isEmpty());
  }

  Producto producto(long id) {
    return almacen.listar().stream().filter(p -> p.id() == id).findFirst().orElseThrow();
  }
}
