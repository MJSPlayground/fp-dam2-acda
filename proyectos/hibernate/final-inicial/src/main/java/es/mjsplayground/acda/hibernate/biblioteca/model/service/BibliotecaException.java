package es.mjsplayground.acda.hibernate.biblioteca.model.service;

public class BibliotecaException extends RuntimeException {
  public BibliotecaException(String mensaje) {
    super(mensaje);
  }

  public BibliotecaException(String mensaje, Throwable causa) {
    super(mensaje, causa);
  }
}
