package es.mjsplayground.acda.hibernate.biblioteca.model.repository;

import es.mjsplayground.acda.hibernate.biblioteca.model.entity.*;
import java.util.*;
import org.hibernate.Session;

public class CatalogoRepository {
  public List<Editorial> editoriales(Session s) {
    // TODO 2: consultar catálogos con HQL tipado
    throw new UnsupportedOperationException(
        "Etapa pendiente: 2: consultar catálogos con HQL tipado");
  }

  public List<Autor> autores(Session s) {
    // TODO 2: consultar catálogos con HQL tipado
    throw new UnsupportedOperationException(
        "Etapa pendiente: 2: consultar catálogos con HQL tipado");
  }

  public Editorial editorial(Session s, long id) {
    return s.find(Editorial.class, id);
  }

  public Autor autor(Session s, long id) {
    return s.find(Autor.class, id);
  }

  public void insertar(Session s, Object entidad) {
    s.persist(entidad);
  }

  public void eliminar(Session s, Object entidad) {
    s.remove(entidad);
  }

  public boolean editorialUsada(Session s, long id) {
    return s.createSelectionQuery(
                "select count(l) from Libro l where l.editorial.id=:id", Long.class)
            .setParameter("id", id)
            .getSingleResult()
        > 0;
  }

  public boolean autorUsado(Session s, long id) {
    return s.createSelectionQuery(
                "select count(l) from Libro l join l.autores a where a.id=:id", Long.class)
            .setParameter("id", id)
            .getSingleResult()
        > 0;
  }
}
