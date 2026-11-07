package es.mjsplayground.acda.mongodb.liga.model.service;

public class LigaException extends RuntimeException {
  private final int estado;

  public LigaException(int estado, String mensaje) {
    super(mensaje);
    this.estado = estado;
  }

  public int estado() {
    return estado;
  }
}
