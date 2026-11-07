package es.mjsplayground.acda.firestore.reservas.model.service;

import es.mjsplayground.acda.firestore.reservas.api.ReservaRequest;
import es.mjsplayground.acda.firestore.reservas.model.entity.*;
import es.mjsplayground.acda.firestore.reservas.model.repository.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class ReservaService {
  protected final FirestoreReservaRepository repository;

  public ReservaService(FirestoreReservaRepository repository) {
    this.repository = repository;
  }

  public List<Espacio> espacios() {
    return List.of(
        new Espacio("SALA-A", "Sala de reuniones", 12),
        new Espacio("SALA-B", "Aula de trabajo", 30));
  }

  public void idValido(String id) {
    if (id == null || !id.matches("[A-Za-z0-9_-]{1,100}"))
      throw new ReservaException(400, "Identificador no válido");
  }

  public Reserva obtener(String id) {
    idValido(id);
    return repository
        .obtener(id)
        .orElseThrow(() -> new ReservaException(404, "Reserva no encontrada"));
  }

  public List<Reserva> listar(String espacio, String fecha, Integer minimo) {
    if (espacio != null && !espacio.isBlank()) espacio(espacio);
    if (fecha != null && !fecha.isBlank()) fecha(fecha);
    if (minimo != null && (minimo < 1 || minimo > 30))
      throw new ReservaException(400, "minAsistentes debe estar entre 1 y 30");
    return repository.listar(espacio, fecha, minimo);
  }

  public synchronized Reserva crear(ReservaRequest solicitud) {
    // TODO 3: validar reglas antes de escribir mediante el repositorio
    throw new UnsupportedOperationException(
        "Etapa pendiente: 3: validar reglas antes de escribir mediante el repositorio");
  }

  public synchronized Reserva actualizar(String id, ReservaRequest solicitud) {
    // TODO 3: validar reglas antes de escribir mediante el repositorio
    throw new UnsupportedOperationException(
        "Etapa pendiente: 3: validar reglas antes de escribir mediante el repositorio");
  }

  public synchronized void eliminar(String id) {
    obtener(id);
    repository.eliminar(id);
  }

  protected Espacio espacio(String id) {
    return espacios().stream()
        .filter(e -> e.id().equals(id))
        .findFirst()
        .orElseThrow(() -> new ReservaException(400, "Espacio desconocido"));
  }

  protected void fecha(String valor) {
    try {
      if (!LocalDate.parse(valor).toString().equals(valor)) throw new DateTimeException("Formato");
    } catch (Exception e) {
      throw new ReservaException(400, "La fecha debe ser válida con formato AAAA-MM-DD");
    }
  }

  protected Reserva validar(ReservaRequest r, String estado, String motivo) {
    if (r == null || r.espacio() == null)
      throw new ReservaException(400, "Faltan datos de la reserva");
    Espacio e = espacio(r.espacio());
    fecha(r.fecha());
    if (r.inicio() < 8 || r.fin() > 20 || r.inicio() >= r.fin())
      throw new ReservaException(400, "Horario entre 08:00 y 20:00, con inicio anterior al fin");
    if (r.responsable() == null
        || r.responsable().isBlank()
        || r.responsable().strip().length() > 60)
      throw new ReservaException(400, "Responsable de 1 a 60 caracteres");
    if (r.asistentes() < 1 || r.asistentes() > e.capacidad())
      throw new ReservaException(400, "Número de asistentes superior a la capacidad o no positivo");
    if (r.equipo() == null
        || r.detalles() == null
        || r.equipo().size() > 10
        || r.detalles().size() > 10
        || r.equipo().stream().anyMatch(v -> v == null || v.isBlank() || v.length() > 40)
        || r.detalles().entrySet().stream()
            .anyMatch(
                v ->
                    v.getKey() == null
                        || v.getValue() == null
                        || v.getKey().isBlank()
                        || v.getKey().length() > 30
                        || v.getValue().isBlank()
                        || v.getValue().length() > 100))
      throw new ReservaException(400, "Equipo o detalles no válidos");
    return new Reserva(
        null,
        r.espacio(),
        r.fecha(),
        r.inicio(),
        r.fin(),
        r.responsable().strip(),
        r.asistentes(),
        List.copyOf(r.equipo()),
        Map.copyOf(r.detalles()),
        estado,
        motivo);
  }
}
