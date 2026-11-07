package es.mjsplayground.acda.jdbc.almacen.model.repository;

public class PersistenciaException extends RuntimeException {
  public PersistenciaException(String mensaje, Throwable causa) {
    super(mensaje, causa);
  }
}
