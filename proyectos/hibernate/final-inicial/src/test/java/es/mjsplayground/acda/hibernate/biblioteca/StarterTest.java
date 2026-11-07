package es.mjsplayground.acda.hibernate.biblioteca;

import static org.junit.jupiter.api.Assertions.*;

import es.mjsplayground.acda.hibernate.biblioteca.model.service.*;
import org.junit.jupiter.api.Test;

class StarterTest {
  @Test
  void filtroExcesivoSeRechazaAntesDeConectar() {
    var s =
        new BibliotecaService(
            () -> {
              throw new AssertionError("No debe conectar");
            });
    assertThrows(BibliotecaException.class, () -> s.listar("x".repeat(121), null));
  }
}
