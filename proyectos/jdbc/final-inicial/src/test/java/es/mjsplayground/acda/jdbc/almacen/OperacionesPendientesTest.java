package es.mjsplayground.acda.jdbc.almacen;

import static org.junit.jupiter.api.Assertions.*;

import es.mjsplayground.acda.jdbc.almacen.config.Database;
import es.mjsplayground.acda.jdbc.almacen.model.entity.LineaRecepcion;
import es.mjsplayground.acda.jdbc.almacen.model.service.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

class OperacionesPendientesTest {
  @TempDir Path temporal;

  @Test
  void starterConservaGuiadoSinResolverFinal() throws Exception {
    Database db = new Database("jdbc:sqlite:" + temporal.resolve("test.db"));
    db.preparar();
    var s = new OperacionesService(db);
    s.preparar();
    assertThrows(ReglaNegocioException.class, () -> s.buscar("", 10));
    assertThrows(ReglaNegocioException.class, () -> s.revisarPrecio(1, BigDecimal.TEN));
    assertThrows(ReglaNegocioException.class, () -> s.recibirUna(new LineaRecepcion(1, 2, 100)));
    assertEquals(
        10,
        new AlmacenService(db)
            .listar().stream().filter(p -> p.id() == 1).findFirst().orElseThrow().stock());
    assertTrue(s.historial().isEmpty());
  }
}
