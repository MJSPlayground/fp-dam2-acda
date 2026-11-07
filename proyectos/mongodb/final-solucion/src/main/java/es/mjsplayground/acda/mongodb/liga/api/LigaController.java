package es.mjsplayground.acda.mongodb.liga.api;

import es.mjsplayground.acda.mongodb.liga.model.entity.*;
import es.mjsplayground.acda.mongodb.liga.model.service.LigaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class LigaController {
  protected final LigaService service;

  public LigaController(LigaService service) {
    this.service = service;
  }

  @GetMapping("/sedes")
  public List<Sede> sedes() {
    return service.sedes();
  }

  @GetMapping("/equipos")
  public LigaService.Pagina listar(
      @RequestParam(required = false) String categoria,
      @RequestParam(required = false) String posicion,
      @RequestParam(defaultValue = "0") int pagina,
      @RequestParam(defaultValue = "10") int tamanio) {
    return service.listar(categoria, posicion, pagina, tamanio);
  }

  public record DisponibleRequest(@jakarta.validation.constraints.NotNull Boolean disponible) {}

  @GetMapping("/equipos/{id}/jugadores")
  public List<Jugador> filtrar(
      @PathVariable String id,
      @RequestParam(defaultValue = "0") int minGoles,
      @RequestParam(required = false) Boolean disponibles) {
    return service.filtrarJugadores(id, minGoles, disponibles);
  }

  @PatchMapping("/equipos/{id}/jugadores/{jugadorId}/disponibilidad")
  public Equipo disponibilidad(
      @PathVariable String id,
      @PathVariable String jugadorId,
      @Valid @RequestBody DisponibleRequest body) {
    return service.disponibilidad(id, jugadorId, body.disponible());
  }

  @PostMapping("/equipos/{id}/jornadas")
  public Equipo jornada(@PathVariable String id, @Valid @RequestBody JornadaRequest body) {
    return service.registrarJornada(id, body);
  }

  @GetMapping("/clasificacion")
  public List<LigaService.Clasificacion> clasificacion() {
    return service.clasificacion();
  }

  @GetMapping("/equipos/{id}/racha")
  public LigaService.Racha racha(@PathVariable String id) {
    return service.racha(id);
  }

  @GetMapping("/equipos/{id}")
  public Equipo obtener(@PathVariable String id) {
    return service.obtener(id);
  }

  @PostMapping("/equipos")
  public ResponseEntity<Equipo> crear(@Valid @RequestBody EquipoRequest body) {
    Equipo e = service.crear(body);
    return ResponseEntity.created(URI.create("/api/equipos/" + e.id())).body(e);
  }

  @PutMapping("/equipos/{id}")
  public Equipo actualizar(@PathVariable String id, @Valid @RequestBody EquipoRequest body) {
    return service.actualizar(id, body);
  }

  @DeleteMapping("/equipos/{id}")
  public ResponseEntity<Void> eliminar(@PathVariable String id) {
    service.eliminar(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/equipos/{id}/jugadores")
  public Equipo agregar(@PathVariable String id, @Valid @RequestBody JugadorRequest body) {
    return service.agregarJugador(id, body);
  }

  @PutMapping("/equipos/{id}/jugadores/{jugadorId}")
  public Equipo editar(
      @PathVariable String id,
      @PathVariable String jugadorId,
      @Valid @RequestBody JugadorRequest body) {
    return service.editarJugador(id, jugadorId, body);
  }

  @DeleteMapping("/equipos/{id}/jugadores/{jugadorId}")
  public Equipo borrarJugador(@PathVariable String id, @PathVariable String jugadorId) {
    return service.eliminarJugador(id, jugadorId);
  }

  @PatchMapping("/equipos/{id}/jugadores/{jugadorId}/estadisticas")
  public Equipo estadisticas(
      @PathVariable String id,
      @PathVariable String jugadorId,
      @Valid @RequestBody EstadisticasRequest body) {
    return service.sumarEstadisticas(id, jugadorId, body);
  }
}
