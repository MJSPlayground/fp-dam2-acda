package es.mjsplayground.acda.firestore.reservas;

import static org.junit.jupiter.api.Assertions.*;

import es.mjsplayground.acda.firestore.reservas.model.service.ReservaService;
import org.junit.jupiter.api.Test;

class StarterTest {
  @Test
  void interfazDisponeDeEspaciosPreparados() {
    var service = new ReservaService(null);
    assertEquals(2, service.espacios().size());
    assertEquals(12, service.espacios().getFirst().capacidad());
  }
}
