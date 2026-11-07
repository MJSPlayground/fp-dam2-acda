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

class AlmacenServiceTest {
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
  void datosReproducibles() throws Exception {
    db.preparar();
    assertEquals(3, servicio.listar().size());
  }

  @Test
  void noResucitaProductosEliminadosAlReabrir() throws Exception {
    servicio.eliminar(1);
    db.preparar();
    assertEquals(2, servicio.listar().size());
  }

  @Test
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
  void ventaExactaYProteccionHistorial() {
    assertEquals(25900, servicio.vender(1, 10));
    assertEquals(0, producto(1).stock());
    assertEquals(1, servicio.ventas().size());
    assertThrows(ReglaNegocioException.class, () -> servicio.eliminar(1));
  }

  @Test
  void stockInsuficienteNoEscribe() {
    assertThrows(ReglaNegocioException.class, () -> servicio.vender(1, 11));
    assertEquals(10, producto(1).stock());
    assertTrue(servicio.ventas().isEmpty());
    assertThrows(ReglaNegocioException.class, () -> servicio.vender(999, 1));
  }

  @Test
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
  void duplicadoYRegistroInexistente() {
    assertThrows(
        PersistenciaException.class, () -> servicio.guardar(new Producto(0, "Teclado", 20, 1)));
    assertThrows(
        PersistenciaException.class, () -> servicio.guardar(new Producto(999, "No existe", 20, 1)));
  }

  Producto producto(long id) {
    return servicio.listar().stream().filter(p -> p.id() == id).findFirst().orElseThrow();
  }
}
