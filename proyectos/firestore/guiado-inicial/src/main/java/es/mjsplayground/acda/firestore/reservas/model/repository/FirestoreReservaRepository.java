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
    // TODO 2b: crear un documento, esperar y devolver su ID.
    throw new UnsupportedOperationException(
        "Etapa pendiente: 2: completar operaciones Firestore y conversión documental");
  }

  public Optional<Reserva> obtener(String id) {
    // TODO 2b: leer y distinguir ausencia de fallo técnico.
    throw new UnsupportedOperationException(
        "Etapa pendiente: 2: completar operaciones Firestore y conversión documental");
  }

  public List<Reserva> listar(String espacio, String fecha, Integer minimo) {
    // TODO 2d: encadenar filtros opcionales y ordenar.
    throw new UnsupportedOperationException(
        "Etapa pendiente: 2: completar operaciones Firestore y conversión documental");
  }

  public Reserva actualizar(String id, Reserva r) {
    // TODO 2c: actualizar un documento existente sin cambiar su ID.
    throw new UnsupportedOperationException(
        "Etapa pendiente: 2: completar operaciones Firestore y conversión documental");
  }

  public void eliminar(String id) {
    // TODO 2c: esperar la eliminación del documento.
    throw new UnsupportedOperationException(
        "Etapa pendiente: 2: completar operaciones Firestore y conversión documental");
  }

  public Map<String, Object> datos(Reserva r) {
    // TODO 2a: convertir Reserva a campos permitidos.
    throw new UnsupportedOperationException("Etapa pendiente: mapeo documental");
  }

  @SuppressWarnings("unchecked")
  public Reserva mapear(DocumentSnapshot d) {
    // TODO 2a: convertir un snapshot comprobando tipos, identidad y causas.
    throw new UnsupportedOperationException("Etapa pendiente: mapeo documental");
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
