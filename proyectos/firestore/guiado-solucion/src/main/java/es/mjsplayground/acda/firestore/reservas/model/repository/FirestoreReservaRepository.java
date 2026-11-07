package es.mjsplayground.acda.firestore.reservas.model.repository;

import com.google.cloud.firestore.*;
import es.mjsplayground.acda.firestore.reservas.model.entity.Reserva;
import java.util.*;
import java.util.concurrent.*;
import org.springframework.stereotype.Repository;

@Repository
public class FirestoreReservaRepository {
  private final CollectionReference reservas;

  public FirestoreReservaRepository(Firestore db) {
    reservas = db.collection("reservas");
  }

  public Reserva crear(Reserva r) {
    DocumentReference doc = reservas.document();
    esperar(doc.create(datos(r)));
    return conId(r, doc.getId());
  }

  public Optional<Reserva> obtener(String id) {
    DocumentSnapshot doc = esperar(reservas.document(id).get());
    return doc.exists() ? Optional.of(mapear(doc)) : Optional.empty();
  }

  public List<Reserva> listar(String espacio, String fecha, Integer minimo) {
    Query q = reservas;
    if (espacio != null && !espacio.isBlank()) q = q.whereEqualTo("espacio", espacio);
    if (fecha != null && !fecha.isBlank()) q = q.whereEqualTo("fecha", fecha);
    if (minimo != null) q = q.whereGreaterThanOrEqualTo("asistentes", minimo).orderBy("asistentes");
    q = q.orderBy(FieldPath.documentId());
    return esperar(q.get()).getDocuments().stream().map(this::mapear).toList();
  }

  public Reserva actualizar(String id, Reserva r) {
    esperar(reservas.document(id).update(datos(r)));
    return conId(r, id);
  }

  public void eliminar(String id) {
    esperar(reservas.document(id).delete());
  }

  public Map<String, Object> datos(Reserva r) {
    Map<String, Object> d = new HashMap<>();
    d.put("espacio", r.espacio());
    d.put("fecha", r.fecha());
    d.put("inicio", r.inicio());
    d.put("fin", r.fin());
    d.put("responsable", r.responsable());
    d.put("asistentes", r.asistentes());
    d.put("equipo", r.equipo());
    d.put("detalles", r.detalles());
    d.put("estado", r.estado());
    d.put("motivoCancelacion", r.motivoCancelacion());
    return d;
  }

  @SuppressWarnings("unchecked")
  public Reserva mapear(DocumentSnapshot d) {
    try {
      Object equipo = d.get("equipo"), detalles = d.get("detalles");
      if (!(equipo instanceof List<?> lista)
          || lista.stream().anyMatch(x -> !(x instanceof String))
          || !(detalles instanceof Map<?, ?> mapa)
          || mapa.entrySet().stream()
              .anyMatch(x -> !(x.getKey() instanceof String) || !(x.getValue() instanceof String)))
        throw new IllegalArgumentException("Equipo o detalles con tipos incompatibles");
      for (String campo : List.of("espacio", "fecha", "responsable", "estado", "motivoCancelacion"))
        if (d.getString(campo) == null) throw new IllegalArgumentException("Falta campo " + campo);
      return new Reserva(
          d.getId(),
          d.getString("espacio"),
          d.getString("fecha"),
          Math.toIntExact(d.getLong("inicio")),
          Math.toIntExact(d.getLong("fin")),
          d.getString("responsable"),
          Math.toIntExact(d.getLong("asistentes")),
          List.copyOf((List<String>) equipo),
          Map.copyOf((Map<String, String>) detalles),
          d.getString("estado"),
          d.getString("motivoCancelacion"));
    } catch (RuntimeException e) {
      throw new PersistenciaException("Documento de reserva incompatible: " + d.getId(), e);
    }
  }

  public Reserva conId(Reserva r, String id) {
    return new Reserva(
        id,
        r.espacio(),
        r.fecha(),
        r.inicio(),
        r.fin(),
        r.responsable(),
        r.asistentes(),
        r.equipo(),
        r.detalles(),
        r.estado(),
        r.motivoCancelacion());
  }

  protected <T> T esperar(com.google.api.core.ApiFuture<T> future) {
    try {
      return future.get(8, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new PersistenciaException("Consulta interrumpida", e);
    } catch (ExecutionException | TimeoutException e) {
      throw new PersistenciaException("No se pudo acceder a Firestore", e);
    }
  }
}
