package es.mjsplayground.acda.hibernate.biblioteca.model.service;

import es.mjsplayground.acda.hibernate.biblioteca.model.entity.*;
import java.math.BigDecimal;
import java.util.*;

/** Vista inmutable construida dentro de sesión: la UI no accede a proxies LAZY. */
public record LibroDatos(
    long id,
    String isbn,
    String titulo,
    BigDecimal precio,
    long editorialId,
    String editorial,
    List<Long> autorIds,
    String autores,
    Long detalleId,
    String idioma,
    Integer paginas,
    long version) {
  public static LibroDatos de(Libro l) {
    Detalle d = l.getDetalle();
    return new LibroDatos(
        l.getId(),
        l.getIsbn(),
        l.getTitulo(),
        l.getPrecio(),
        l.getEditorial().getId(),
        l.getEditorial().getNombre(),
        l.getAutores().stream().map(Autor::getId).sorted().toList(),
        l.getAutores().stream()
            .map(Autor::getNombre)
            .sorted()
            .collect(java.util.stream.Collectors.joining(", ")),
        d == null ? null : d.getId(),
        d == null ? null : d.getIdioma(),
        d == null ? null : d.getPaginas(),
        l.getVersion());
  }
}
