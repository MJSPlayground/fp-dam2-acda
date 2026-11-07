package es.mjsplayground.acda.hibernate.biblioteca.model.service;

import java.math.BigDecimal;
import java.util.List;

public record LibroSolicitud(
    String isbn,
    String titulo,
    BigDecimal precio,
    long editorialId,
    List<Long> autorIds,
    String idioma,
    Integer paginas) {}
