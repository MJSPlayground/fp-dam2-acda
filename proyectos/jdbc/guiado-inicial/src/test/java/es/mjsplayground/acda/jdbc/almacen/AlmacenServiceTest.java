package es.mjsplayground.acda.jdbc.almacen;

import static org.junit.jupiter.api.Assertions.*;

import es.mjsplayground.acda.jdbc.almacen.config.Database;
import es.mjsplayground.acda.jdbc.almacen.model.service.AlmacenService;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AlmacenServiceTest {
  @TempDir Path temporal;

  @Test
  void infraestructuraEjecutableYTodoExplicito() throws Exception {
    Database db = new Database("jdbc:sqlite:" + temporal.resolve("prueba.db"));
    db.preparar();
    try (var c = db.abrir();
        var s = c.createStatement();
        var r = s.executeQuery("SELECT COUNT(*) FROM producto")) {
      assertTrue(r.next());
      assertEquals(3, r.getInt(1));
    }
    assertThrows(UnsupportedOperationException.class, () -> new AlmacenService(db).listar());
  }
}
