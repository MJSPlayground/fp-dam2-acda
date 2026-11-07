package es.mjsplayground.acda.firestore.reservas.api;

import jakarta.validation.constraints.*;
import java.util.*;

public record ReservaRequest(
    @NotBlank @Size(max = 20) String espacio,
    @NotBlank String fecha,
    @Min(8) @Max(19) int inicio,
    @Min(9) @Max(20) int fin,
    @NotBlank @Size(max = 60) String responsable,
    @Min(1) @Max(30) int asistentes,
    @NotNull @Size(max = 10) List<@NotBlank @Size(max = 40) String> equipo,
    @NotNull @Size(max = 10)
        Map<@NotBlank @Size(max = 30) String, @NotBlank @Size(max = 100) String> detalles) {}
