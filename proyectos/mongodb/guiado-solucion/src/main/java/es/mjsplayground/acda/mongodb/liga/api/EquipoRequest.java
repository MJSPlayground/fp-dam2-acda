package es.mjsplayground.acda.mongodb.liga.api;

import jakarta.validation.constraints.*;

public record EquipoRequest(
    @NotBlank @Size(max = 60) String nombre, @NotBlank String categoria, @NotBlank String sedeId) {}
