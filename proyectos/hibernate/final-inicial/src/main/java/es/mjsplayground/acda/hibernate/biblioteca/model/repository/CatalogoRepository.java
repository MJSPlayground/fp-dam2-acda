package es.mjsplayground.acda.hibernate.biblioteca.model.repository;

import es.mjsplayground.acda.hibernate.biblioteca.model.entity.*;
import java.util.*;
import org.hibernate.Session;

public class CatalogoRepository {
  public List<Editorial> editoriales(Session s) {
    return s.createSelectionQuery("from Editorial e order by e.nombre,e.id", Editorial.class)
        .getResultList();
  }

  public List<Autor> autores(Session s) {
    return s.createSelectionQuery("from Autor a order by a.nombre,a.id", Autor.class)
        .getResultList();
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
