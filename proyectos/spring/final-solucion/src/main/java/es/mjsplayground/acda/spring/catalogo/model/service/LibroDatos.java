package es.mjsplayground.acda.spring.catalogo.model.service;

import es.mjsplayground.acda.spring.catalogo.model.entity.Libro;
import java.math.BigDecimal;

public record LibroDatos(
    Long id,
    Long version,
    String isbn,
    String titulo,
    BigDecimal precio,
    int stock,
    Long editorialId,
    String editorial) {
  public static LibroDatos de(Libro l) {
    return new LibroDatos(
        l.getId(),
        l.getVersion(),
        l.getIsbn(),
        l.getTitulo(),
        l.getPrecio(),
        l.getStock(),
        l.getEditorial().getId(),
        l.getEditorial().getNombre());
  }
}
