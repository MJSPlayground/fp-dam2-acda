package es.mjsplayground.acda.firestore.reservas.api;

import es.mjsplayground.acda.firestore.reservas.model.entity.*;
import es.mjsplayground.acda.firestore.reservas.model.service.ReservaService;
import jakarta.validation.Valid;
import java.net.URI;
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
      @RequestParam(required = false) Integer minAsistentes,
      @RequestParam(required = false) String estado,
      @RequestParam(required = false) String responsable) {
    return servicio.filtrar(espacio, fecha, minAsistentes, estado, responsable);
  }

  public record CancelacionRequest(
      @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max = 120)
          String motivo) {}

  @PatchMapping("/reservas/{id}/cancelacion")
  public Reserva cancelar(@PathVariable String id, @Valid @RequestBody CancelacionRequest body) {
    return servicio.cancelar(id, body.motivo());
  }

  @GetMapping("/disponibilidad")
  public Map<String, Boolean> disponibilidad(
      @RequestParam String espacio,
      @RequestParam String fecha,
      @RequestParam int inicio,
      @RequestParam int fin) {
    return Map.of("disponible", servicio.disponible(espacio, fecha, inicio, fin));
  }

  @GetMapping("/reservas/pagina")
  public ReservaService.Pagina pagina(
      @RequestParam(defaultValue = "10") int limite,
      @RequestParam(required = false) String cursor) {
    return servicio.pagina(limite, cursor);
  }

  @GetMapping("/reservas/{id}")
  public Reserva obtener(@PathVariable String id) {
    return servicio.obtener(id);
  }

  @PostMapping("/reservas")
  public ResponseEntity<Reserva> crear(@Valid @RequestBody ReservaRequest body) {
    Reserva r = servicio.crear(body);
    return ResponseEntity.created(URI.create("/api/reservas/" + r.id())).body(r);
  }

  @PutMapping("/reservas/{id}")
  public Reserva actualizar(@PathVariable String id, @Valid @RequestBody ReservaRequest body) {
    return servicio.actualizar(id, body);
  }

  @DeleteMapping("/reservas/{id}")
  public ResponseEntity<Void> eliminar(@PathVariable String id) {
    servicio.eliminar(id);
    return ResponseEntity.noContent().build();
  }
}
