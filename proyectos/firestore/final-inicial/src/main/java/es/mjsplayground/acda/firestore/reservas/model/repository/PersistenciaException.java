package es.mjsplayground.acda.firestore.reservas.model.repository;

public class PersistenciaException extends RuntimeException {
  public PersistenciaException(String mensaje, Throwable causa) {
    super(mensaje, causa);
  }
}
