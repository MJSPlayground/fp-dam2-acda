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

  public List<Libro> filtrar(
      Session s, Long autor, java.math.BigDecimal minimo, java.math.BigDecimal maximo) {
    String hql =
        "select distinct l from Libro l"
            + (autor == null ? "" : " join l.autores a")
            + " where 1=1"
            + (autor == null ? "" : " and a.id=:autor")
            + (minimo == null ? "" : " and l.precio>=:minimo")
            + (maximo == null ? "" : " and l.precio<=:maximo")
            + " order by l.titulo,l.id";
    var q = s.createSelectionQuery(hql, Libro.class);
    if (autor != null) q.setParameter("autor", autor);
    if (minimo != null) q.setParameter("minimo", minimo);
    if (maximo != null) q.setParameter("maximo", maximo);
    return q.getResultList();
  }

  public List<Libro> porEditorial(Session s, long id) {
    return s.createSelectionQuery(
            "from Libro l where l.editorial.id=:id order by l.id", Libro.class)
        .setParameter("id", id)
        .getResultList();
  }
}
