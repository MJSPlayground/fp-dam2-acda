package es.mjsplayground.acda.hibernate.biblioteca.model.repository;

import es.mjsplayground.acda.hibernate.biblioteca.model.entity.Libro;
import java.util.*;
import org.hibernate.Session;

public class LibroRepository {
  public List<Libro> listar(Session s, String titulo, Long editorial) {
    // TODO 2: completar repositorio Hibernate con la sesión recibida; no abrir ni confirmar
    // transacción aquí
    throw new UnsupportedOperationException(
        "Etapa pendiente: 2: completar repositorio Hibernate con la sesión recibida; no abrir ni"
            + " confirmar transacción aquí");
  }

  public Libro buscar(Session s, long id) {
    // TODO 2: completar repositorio Hibernate con la sesión recibida; no abrir ni confirmar
    // transacción aquí
    throw new UnsupportedOperationException(
        "Etapa pendiente: 2: completar repositorio Hibernate con la sesión recibida; no abrir ni"
            + " confirmar transacción aquí");
  }

  public void insertar(Session s, Libro libro) {
    // TODO 2: completar repositorio Hibernate con la sesión recibida; no abrir ni confirmar
    // transacción aquí
    throw new UnsupportedOperationException(
        "Etapa pendiente: 2: completar repositorio Hibernate con la sesión recibida; no abrir ni"
            + " confirmar transacción aquí");
  }

  public void eliminar(Session s, Libro libro) {
    // TODO 2: completar repositorio Hibernate con la sesión recibida; no abrir ni confirmar
    // transacción aquí
    throw new UnsupportedOperationException(
        "Etapa pendiente: 2: completar repositorio Hibernate con la sesión recibida; no abrir ni"
            + " confirmar transacción aquí");
  }

  public void flush(Session s) {
    s.flush();
  }
}
