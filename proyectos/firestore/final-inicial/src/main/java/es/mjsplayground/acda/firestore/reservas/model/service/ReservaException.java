package es.mjsplayground.acda.firestore.reservas.model.service;

public class ReservaException extends RuntimeException {
  private final int estado;

  public ReservaException(int estado, String mensaje) {
    super(mensaje);
    this.estado = estado;
  }

  public int estado() {
    return estado;
  }
}
