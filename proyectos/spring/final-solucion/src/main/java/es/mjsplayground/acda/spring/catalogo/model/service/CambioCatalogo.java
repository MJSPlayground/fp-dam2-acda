package es.mjsplayground.acda.spring.catalogo.model.service;

import java.time.Instant;

public record CambioCatalogo(Long id, String operacion, Instant instante) {}
