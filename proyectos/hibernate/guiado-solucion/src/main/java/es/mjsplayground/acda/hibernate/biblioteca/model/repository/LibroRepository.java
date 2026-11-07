package es.mjsplayground.acda.hibernate.biblioteca.model.repository;

import es.mjsplayground.acda.hibernate.biblioteca.model.entity.Libro;
import java.util.*;
import org.hibernate.Session;

public class LibroRepository {
  public List<Libro> listar(Session s, String titulo, Long editorial) {
    String hql =
        "from Libro l where lower(l.titulo) like :titulo"
            + (editorial == null ? "" : " and l.editorial.id=:editorial")
            + " order by l.titulo,l.id";
    var q =
        s.createSelectionQuery(hql, Libro.class)
            .setParameter("titulo", "%" + titulo.toLowerCase(Locale.ROOT) + "%");
    if (editorial != null) q.setParameter("editorial", editorial);
    return q.getResultList();
  }

  public Libro buscar(Session s, long id) {
    return s.find(Libro.class, id);
  }

  public void insertar(Session s, Libro libro) {
    s.persist(libro);
  }

  public void eliminar(Session s, Libro libro) {
    s.remove(libro);
  }

  public void flush(Session s) {
    s.flush();
  }
}
