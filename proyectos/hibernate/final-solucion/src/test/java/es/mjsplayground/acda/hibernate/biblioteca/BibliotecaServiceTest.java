package es.mjsplayground.acda.hibernate.biblioteca;

import static org.junit.jupiter.api.Assertions.*;

import es.mjsplayground.acda.hibernate.biblioteca.model.service.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;

class BibliotecaServiceTest {
  BibliotecaService service =
      new BibliotecaService(
          () -> {
            throw new AssertionError("No debe abrirse la BD para una entrada mal formada");
          });

  @Test
  void isbnYPrecioSeValidanAntesDePersistir() {
    for (LibroSolicitud r :
        List.of(
            new LibroSolicitud("x", "X", new BigDecimal("1"), 1, List.of(1L), null, null),
            new LibroSolicitud(
                "9780000000001", "X", new BigDecimal("1.001"), 1, List.of(1L), null, null),
            new LibroSolicitud(
                "9780000000001", "X", new BigDecimal("0"), 1, List.of(1L), null, null)))
      assertThrows(BibliotecaException.class, () -> service.guardarLibro(null, null, r));
  }

  @Test
  void detalleSeCompletaOSeDejaVacio() {
    assertThrows(
        BibliotecaException.class,
        () ->
            service.guardarLibro(
                null,
                null,
                new LibroSolicitud(
                    "9780000000001", "X", new BigDecimal("1"), 1, List.of(1L), "Español", null)));
    assertThrows(
        BibliotecaException.class,
        () ->
            service.guardarLibro(
                null,
                null,
                new LibroSolicitud(
                    "9780000000001", "X", new BigDecimal("1"), 1, List.of(1L), "", 20)));
  }

  @Test
  void nombresYFiltrosLimitados() {
    assertThrows(BibliotecaException.class, () -> service.guardarAutor(null, " "));
    assertThrows(BibliotecaException.class, () -> service.guardarEditorial(null, "x".repeat(81)));
    assertThrows(BibliotecaException.class, () -> service.listar("x".repeat(121), null));
  }
}
