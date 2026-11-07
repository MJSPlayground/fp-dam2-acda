package es.mjsplayground.acda.spring.catalogo.model.service;

import java.math.BigDecimal;

public record LibroSolicitud(
    Long id,
    Long version,
    String isbn,
    String titulo,
    BigDecimal precio,
    int stock,
    Long editorialId) {}
