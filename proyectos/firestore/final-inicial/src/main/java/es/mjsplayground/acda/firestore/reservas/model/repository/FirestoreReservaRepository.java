package es.mjsplayground.acda.firestore.reservas.model.repository;

import com.google.cloud.firestore.*;
import es.mjsplayground.acda.firestore.reservas.model.entity.Reserva;
import java.util.*;
import java.util.concurrent.*;
import org.springframework.stereotype.Repository;

@Repository
public class FirestoreReservaRepository {
  private final CollectionReference reservas;
  private final Firestore db;

  public FirestoreReservaRepository(Firestore db) {
    this.db = db;
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

  /**
   * Infraestructura preparada: el marcador serializa escritores de la misma agenda. La regla
   * recibida no produce efectos externos: Firestore puede reintentarla.
   */
  public Reserva coordinar(
      Reserva candidata, String id, java.util.function.Consumer<List<Reserva>> regla) {
    DocumentReference destino = id == null ? reservas.document() : reservas.document(id);
    return esperar(
        db.runTransaction(
            tx -> {
              DocumentReference marcador = db.collection("coordinacion").document("agenda");
              DocumentSnapshot anterior = leerTransaccion(tx.get(marcador));
              List<Reserva> existentes =
                  leerTransaccion(tx.get(reservas.whereEqualTo("fecha", candidata.fecha())))
                      .getDocuments()
                      .stream()
                      .map(this::mapear)
                      .toList();
              if (id != null) {
                DocumentSnapshot original = leerTransaccion(tx.get(destino));
                if (!original.exists())
                  throw new es.mjsplayground.acda.firestore.reservas.model.service.ReservaException(
                      404, "Reserva no encontrada");
                if ("CANCELADA".equals(original.getString("estado")))
                  throw new es.mjsplayground.acda.firestore.reservas.model.service.ReservaException(
                      409, "Una reserva cancelada no se modifica");
              }
              regla.accept(existentes);
              long version = anterior.exists() ? anterior.getLong("version") : 0;
              tx.set(marcador, Map.of("version", version + 1));
              if (id == null) tx.create(destino, datos(candidata));
              else tx.update(destino, datos(candidata));
              return conId(candidata, destino.getId());
            }));
  }

  public List<Reserva> pagina(int limite, String cursor) {
    // TODO ampliación: recuperar una página documental mediante cursor
    throw new UnsupportedOperationException(
        "Etapa pendiente: ampliación: recuperar una página documental mediante cursor");
  }

  public Reserva cancelar(String id, String motivo) {
    // TODO ampliación: persistir la cancelación conforme a requisitos del cliente
    throw new UnsupportedOperationException("Etapa pendiente: cancelación");
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

  private <T> T leerTransaccion(com.google.api.core.ApiFuture<T> future) throws Exception {
    try {
      return future.get();
    } catch (ExecutionException e) {
      if (e.getCause() instanceof RuntimeException causa) throw causa;
      throw e;
    }
  }

  protected <T> T esperar(com.google.api.core.ApiFuture<T> future) {
    try {
      return future.get(45, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new PersistenciaException("Consulta interrumpida", e);
    } catch (ExecutionException e) {
      Throwable causa = e;
      while (causa.getCause() != null) {
        causa = causa.getCause();
        if (causa
            instanceof
            es.mjsplayground.acda.firestore.reservas.model.service.ReservaException negocio)
          throw negocio;
      }
      throw new PersistenciaException("No se pudo acceder a Firestore", e);
    } catch (TimeoutException e) {
      throw new PersistenciaException("Firestore no respondió a tiempo", e);
    }
  }
}
