package es.mjsplayground.acda.jdbc.almacen;

import static org.junit.jupiter.api.Assertions.*;

import es.mjsplayground.acda.jdbc.almacen.config.Database;
import es.mjsplayground.acda.jdbc.almacen.model.entity.Producto;
import es.mjsplayground.acda.jdbc.almacen.model.repository.*;
import es.mjsplayground.acda.jdbc.almacen.model.service.*;
import java.nio.file.Path;
import java.sql.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

@org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "aprendizaje.fase", matches = "[2345]|todas")
class AprendizajeGuiadoTest {
  @TempDir Path temporal;
  Database db;
  AlmacenService servicio;

  @BeforeEach
  void preparar() throws Exception {
    db = new Database("jdbc:sqlite:" + temporal.resolve("test.db"));
    db.preparar();
    servicio = new AlmacenService(db);
  }

  @Test
  @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "aprendizaje.fase", matches = "2|todas")
  void datosReproducibles() throws Exception {
    db.preparar();
    assertEquals(3, servicio.listar().size());
  }

  @Test
  @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "aprendizaje.fase", matches = "3|todas")
  void noResucitaProductosEliminadosAlReabrir() throws Exception {
    servicio.eliminar(1);
    db.preparar();
    assertEquals(2, servicio.listar().size());
  }

  @Test
  @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "aprendizaje.fase", matches = "3|todas")
  void crudYParametros() {
    long id = servicio.guardar(new Producto(0, "O'Brian ?;--", 1234, 0));
    assertTrue(id > 0);
    servicio.guardar(new Producto(id, "Nuevo", 2000, 4));
    assertEquals(
        4, servicio.listar().stream().filter(p -> p.id() == id).findFirst().orElseThrow().stock());
    servicio.eliminar(id);
    assertEquals(3, servicio.listar().size());
  }

  @Test
  @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "aprendizaje.fase", matches = "5|todas")
  void validaDominio() {
    for (Producto p :
        new Producto[] {
          new Producto(0, " ", 10, 1),
          new Producto(0, "X", 0, 1),
          new Producto(0, "X", 1, -1),
          new Producto(0, "X", 100000001, 1)
        }) assertThrows(ReglaNegocioException.class, () -> servicio.guardar(p));
    assertThrows(ReglaNegocioException.class, () -> servicio.vender(1, 0));
    assertThrows(ReglaNegocioException.class, () -> servicio.vender(1, -1));
  }

  @Test
  @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "aprendizaje.fase", matches = "5|todas")
  void ventaExactaYProteccionHistorial() {
    assertEquals(25900, servicio.vender(1, 10));
    assertEquals(0, producto(1).stock());
    assertEquals(1, servicio.ventas().size());
    assertThrows(ReglaNegocioException.class, () -> servicio.eliminar(1));
  }

  @Test
  @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "aprendizaje.fase", matches = "5|todas")
  void stockInsuficienteNoEscribe() {
    assertThrows(ReglaNegocioException.class, () -> servicio.vender(1, 11));
    assertEquals(10, producto(1).stock());
    assertTrue(servicio.ventas().isEmpty());
    assertThrows(ReglaNegocioException.class, () -> servicio.vender(999, 1));
  }

  @Test
  @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "aprendizaje.fase", matches = "5|todas")
  void falloSegundaEscrituraRevierteDescuentoYConservaCausa() {
    var averiado =
        new JdbcVentaRepository() {
          @Override
          public void registrar(Connection c, long id, int cantidad, long total)
              throws SQLException {
            throw new SQLException("Fallo simulado de disco");
          }
        };
    var s = new AlmacenService(db, new JdbcProductoRepository(), averiado);
    PersistenciaException e = assertThrows(PersistenciaException.class, () -> s.vender(1, 2));
    assertInstanceOf(SQLException.class, e.getCause());
    assertEquals(10, producto(1).stock());
    assertTrue(servicio.ventas().isEmpty());
  }

  @Test
  @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "aprendizaje.fase", matches = "3|todas")
  void duplicadoYRegistroInexistente() {
    assertThrows(
        PersistenciaException.class, () -> servicio.guardar(new Producto(0, "Teclado", 20, 1)));
    assertThrows(
        PersistenciaException.class, () -> servicio.guardar(new Producto(999, "No existe", 20, 1)));
  }

  @Test
  @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "aprendizaje.fase", matches = "3|todas")
  void validacionesAntesDeEscribir() {
    assertThrows(ReglaNegocioException.class, () -> servicio.guardar(new Producto(0, " ", 100, 1)));
    assertThrows(ReglaNegocioException.class, () -> servicio.guardar(new Producto(0, "X", 0, 1)));
    assertThrows(ReglaNegocioException.class, () -> servicio.guardar(new Producto(0, "X", 100, -1)));
    assertEquals(3, servicio.listar().size());
  }

  @Test
  @org.junit.jupiter.api.condition.EnabledIfSystemProperty(named = "aprendizaje.fase", matches = "4|todas")
  void repositoriosRespetanConexionYRollbackDelPropietario() throws Exception {
    try (Connection c = db.abrir()) {
      c.setAutoCommit(false);
      assertTrue(new JdbcProductoRepository().descontar(c, 1, 2));
      new JdbcVentaRepository().registrar(c, 1, 2, 5180);
      assertFalse(c.isClosed(), "La conexion pertenece al servicio");
      c.rollback();
    }
    try (Connection c = db.abrir(); var st = c.createStatement();
         var r = st.executeQuery("SELECT stock, (SELECT COUNT(*) FROM venta) AS ventas FROM producto WHERE id=1")) {
      assertTrue(r.next()); assertEquals(10, r.getInt("stock")); assertEquals(0, r.getInt("ventas"));
    }
  }

  Producto producto(long id) {
    return servicio.listar().stream().filter(p -> p.id() == id).findFirst().orElseThrow();
  }
}
