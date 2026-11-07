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

class OperacionesServiceTest {
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
  void filtraNombreLiteralInclusoTildesYStock() {
    assertEquals("Teclado", servicio.buscar("TE", 10).getFirst().nombre());
    assertTrue(servicio.buscar("monitor", 4).isEmpty());
    assertEquals("Ratón", servicio.buscar("RATÓN", 20).getFirst().nombre());
    assertTrue(servicio.buscar("%", 20).isEmpty());
    assertTrue(servicio.buscar("_", 20).isEmpty());
    assertThrows(ReglaNegocioException.class, () -> servicio.buscar("", -1));
  }

  @Test
  void precioRedondeadoSinCambiarResto() {
    assertEquals(2849, servicio.revisarPrecio(1, new BigDecimal("10")));
    assertEquals(10, producto(1).stock());
    assertEquals("Teclado", producto(1).nombre());
    assertEquals(648, servicio.revisarPrecio(2, new BigDecimal("-50")));
  }

  @Test
  void porcentajesInvalidosNoModifican() {
    for (String p : new String[] {"-50.01", "100.01", "0.001"})
      assertThrows(ReglaNegocioException.class, () -> servicio.revisarPrecio(1, new BigDecimal(p)));
    assertEquals(2590, producto(1).precioCentimos());
    assertThrows(ReglaNegocioException.class, () -> servicio.revisarPrecio(999, BigDecimal.TEN));
  }

  @Test
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
  void falloTecnicoIntermedioRevierteTodoYConservaCausa() {
    JdbcRecepcionRepository averiado =
        new JdbcRecepcionRepository() {
          int llamadas;

          @Override
          public void registrar(Connection c, LineaRecepcion l, long total) throws SQLException {
            super.registrar(c, l, total);
            if (++llamadas == 2) throw new SQLException("Disco lleno");
          }
        };
    OperacionesService s = new OperacionesService(db, averiado);
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

  Producto producto(long id) {
    return almacen.listar().stream().filter(p -> p.id() == id).findFirst().orElseThrow();
  }
}
