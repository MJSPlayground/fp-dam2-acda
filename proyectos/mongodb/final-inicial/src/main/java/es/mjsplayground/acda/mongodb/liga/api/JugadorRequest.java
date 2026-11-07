package es.mjsplayground.acda.mongodb.liga.api;

import jakarta.validation.constraints.*;

public record JugadorRequest(
    @NotBlank @Size(max = 60) String nombre,
    @Min(1) @Max(99) int dorsal,
    @NotBlank String posicion) {}
