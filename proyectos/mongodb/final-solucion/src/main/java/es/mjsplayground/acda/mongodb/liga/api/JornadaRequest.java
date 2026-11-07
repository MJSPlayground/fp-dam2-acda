package es.mjsplayground.acda.mongodb.liga.api;

import es.mjsplayground.acda.mongodb.liga.model.entity.Aportacion;
import jakarta.validation.constraints.*;
import java.util.*;

public record JornadaRequest(
    @NotBlank @Size(max = 60) String id,
    @NotBlank String fecha,
    @NotBlank @Size(max = 60) String rival,
    @Min(0) @Max(100) int golesFavor,
    @Min(0) @Max(100) int golesContra,
    @NotNull @Size(max = 20) List<Aportacion> aportaciones) {}
