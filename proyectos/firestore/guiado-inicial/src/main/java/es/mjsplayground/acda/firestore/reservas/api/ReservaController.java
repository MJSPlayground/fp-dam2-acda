package es.mjsplayground.acda.firestore.reservas.api;

import es.mjsplayground.acda.firestore.reservas.model.entity.*;
import es.mjsplayground.acda.firestore.reservas.model.service.ReservaService;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReservaController {
  private final ReservaService servicio;

  public ReservaController(ReservaService servicio) {
    this.servicio = servicio;
  }

  @GetMapping("/espacios")
  public List<Espacio> espacios() {
    return servicio.espacios();
  }

  @GetMapping("/reservas")
  public List<Reserva> listar(
      @RequestParam(required = false) String espacio,
      @RequestParam(required = false) String fecha,
      @RequestParam(required = false) Integer minAsistentes) {
    return servicio.listar(espacio, fecha, minAsistentes);
  }

  @GetMapping("/reservas/{id}")
  public Reserva obtener(@PathVariable String id) {
    return servicio.obtener(id);
  }

  @PostMapping("/reservas")
  public ResponseEntity<Reserva> crear(@Valid @RequestBody ReservaRequest body) {
    // TODO 4: implementar endpoint REST y respuesta HTTP coherente
    throw new UnsupportedOperationException(
        "Etapa pendiente: 4: implementar endpoint REST y respuesta HTTP coherente");
  }

  @PutMapping("/reservas/{id}")
  public Reserva actualizar(@PathVariable String id, @Valid @RequestBody ReservaRequest body) {
    // TODO 4: implementar endpoint REST y respuesta HTTP coherente
    throw new UnsupportedOperationException(
        "Etapa pendiente: 4: implementar endpoint REST y respuesta HTTP coherente");
  }

  @DeleteMapping("/reservas/{id}")
  public ResponseEntity<Void> eliminar(@PathVariable String id) {
    // TODO 4: implementar endpoint REST y respuesta HTTP coherente
    throw new UnsupportedOperationException(
        "Etapa pendiente: 4: implementar endpoint REST y respuesta HTTP coherente");
  }
}
