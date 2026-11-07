package es.mjsplayground.acda.spring.catalogo;

import static org.junit.jupiter.api.Assertions.*;

import es.mjsplayground.acda.spring.catalogo.config.CatalogoProperties;
import es.mjsplayground.acda.spring.catalogo.model.service.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ReglasCatalogoTest {
  ReglasCatalogo reglas = new ReglasCatalogo(new CatalogoProperties(50));

  @Test
  void dominioRevalidaSinControlador() {
    for (LibroSolicitud r :
        java.util.List.of(
            new LibroSolicitud(null, null, "x", "Título", new BigDecimal("1"), 1, 1L),
            new LibroSolicitud(null, null, "9780000000001", " ", new BigDecimal("1"), 1, 1L),
            new LibroSolicitud(null, null, "9780000000001", "X", new BigDecimal("1.001"), 1, 1L),
            new LibroSolicitud(null, null, "9780000000001", "X", new BigDecimal("1"), 51, 1L)))
      assertThrows(ReglaCatalogoException.class, () -> reglas.validar(r));
  }

  @Test
  void limitesValidosYPropiedadIncorrecta() {
    assertDoesNotThrow(
        () ->
            reglas.validar(
                new LibroSolicitud(
                    null, null, "9780000000001", "X", new BigDecimal("0.01"), 0, 1L)));
    assertThrows(IllegalArgumentException.class, () -> new CatalogoProperties(0));
  }
}
