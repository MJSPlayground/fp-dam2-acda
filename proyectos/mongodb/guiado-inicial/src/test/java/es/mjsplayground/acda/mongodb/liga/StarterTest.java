package es.mjsplayground.acda.mongodb.liga;

import static org.junit.jupiter.api.Assertions.*;

import es.mjsplayground.acda.mongodb.liga.model.service.*;
import org.junit.jupiter.api.Test;

class StarterTest {
  @Test
  void identificadoresMalFormadosSeRechazanAntesDeConsultar() {
    var s = new LigaService(null, null, null);
    assertEquals(
        400, assertThrows(LigaException.class, () -> s.obtener("no-es-objectid")).estado());
  }
}
