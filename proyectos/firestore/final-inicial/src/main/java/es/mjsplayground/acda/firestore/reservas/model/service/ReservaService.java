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
    return repository.crear(validar(solicitud, "ACTIVA", ""));
  }

  public synchronized Reserva actualizar(String id, ReservaRequest solicitud) {
    Reserva anterior = obtener(id);
    return repository.actualizar(
        id, validar(solicitud, anterior.estado(), anterior.motivoCancelacion()));
  }

  public synchronized void eliminar(String id) {
    obtener(id);
    repository.eliminar(id);
  }

  public Reserva cancelar(String id, String motivo) {
    // TODO ampliación: resolver requisitos nuevos del cliente; conservar el guiado funcionando
    throw new UnsupportedOperationException(
        "Etapa pendiente: ampliación: resolver requisitos nuevos del cliente; conservar el guiado"
            + " funcionando");
  }

  public boolean disponible(String espacio, String fecha, int inicio, int fin) {
    // TODO ampliación: resolver requisitos nuevos del cliente; conservar el guiado funcionando
    throw new UnsupportedOperationException(
        "Etapa pendiente: ampliación: resolver requisitos nuevos del cliente; conservar el guiado"
            + " funcionando");
  }

  public List<Reserva> filtrar(
      String espacio, String fecha, Integer minimo, String estado, String responsable) {
    if (estado == null && responsable == null) return listar(espacio, fecha, minimo);
    // TODO ampliación: resolver requisitos nuevos del cliente; conservar el guiado funcionando
    throw new UnsupportedOperationException(
        "Etapa pendiente: ampliación: resolver requisitos nuevos del cliente; conservar el guiado"
            + " funcionando");
  }

  public record Pagina(List<Reserva> elementos, String siguienteCursor) {}

  public Pagina pagina(int limite, String cursor) {
    // TODO ampliación: resolver requisitos nuevos del cliente; conservar el guiado funcionando
    throw new UnsupportedOperationException(
        "Etapa pendiente: ampliación: resolver requisitos nuevos del cliente; conservar el guiado"
            + " funcionando");
  }

  protected void comprobarAgenda(Reserva candidata, String editando, List<Reserva> actuales) {
    // TODO ampliación: resolver requisitos nuevos del cliente; conservar el guiado funcionando
    throw new UnsupportedOperationException(
        "Etapa pendiente: ampliación: resolver requisitos nuevos del cliente; conservar el guiado"
            + " funcionando");
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
