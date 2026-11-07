package es.mjsplayground.acda.spring.catalogo.model.service;

public class NoEncontradoException extends RuntimeException {
  public NoEncontradoException(String mensaje) {
    super(mensaje);
  }
}
