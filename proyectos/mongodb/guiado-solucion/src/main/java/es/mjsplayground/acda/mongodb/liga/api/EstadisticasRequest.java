package es.mjsplayground.acda.mongodb.liga.api;

import jakarta.validation.constraints.*;

public record EstadisticasRequest(@Min(0) @Max(20) int goles, @Min(0) @Max(20) int asistencias) {}
