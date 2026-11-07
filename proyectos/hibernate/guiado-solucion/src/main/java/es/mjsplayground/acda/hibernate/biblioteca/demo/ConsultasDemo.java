package es.mjsplayground.acda.hibernate.biblioteca.demo;

import es.mjsplayground.acda.hibernate.biblioteca.config.HibernateFactory;
import es.mjsplayground.acda.hibernate.biblioteca.model.entity.Libro;
import java.math.BigDecimal;
import java.util.*;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Lecturas de Ampliación: no siembra ni modifica el catálogo. */
public final class ConsultasDemo {
  private static final Logger log = LoggerFactory.getLogger(ConsultasDemo.class);

  public record Resumen(String titulo, BigDecimal precio) {}

  public record TotalEditorial(String nombre, long libros, BigDecimal importe) {}

  public record Medida(int libros, long sentencias) {}

  public static List<String> librosYAutores(SessionFactory factory) {
    return factory.fromTransaction(
        s -> {
          List<Object[]> filas =
              s.createSelectionQuery(
                      "select l.titulo,a.nombre from Libro l join l.autores a order by"
                          + " l.titulo,a.nombre",
                      Object[].class)
                  .getResultList();
          List<String> resultado = new ArrayList<>();
          for (Object[] fila : filas) resultado.add(fila[0] + " — " + fila[1]);
          return List.copyOf(resultado);
        });
  }

  public static List<Resumen> proyeccion(SessionFactory factory) {
    return factory.fromTransaction(
        s -> {
          List<Object[]> filas =
              s.createSelectionQuery(
                      "select l.titulo,l.precio from Libro l order by l.titulo,l.id",
                      Object[].class)
                  .getResultList();
          List<Resumen> resultado = new ArrayList<>();
          for (Object[] fila : filas)
            resultado.add(new Resumen((String) fila[0], (BigDecimal) fila[1]));
          return List.copyOf(resultado);
        });
  }

  public static List<TotalEditorial> totales(SessionFactory factory) {
    return factory.fromTransaction(
        s -> {
          List<Object[]> filas =
              s.createSelectionQuery(
                      "select e.nombre,count(l),sum(l.precio) from Libro l join l.editorial e "
                          + "group by e.id,e.nombre order by e.nombre",
                      Object[].class)
                  .getResultList();
          List<TotalEditorial> resultado = new ArrayList<>();
          for (Object[] fila : filas)
            resultado.add(
                new TotalEditorial((String) fila[0], (Long) fila[1], (BigDecimal) fila[2]));
          return List.copyOf(resultado);
        });
  }

  public static List<String> pagina(SessionFactory factory, int pagina, int tamanio) {
    if (pagina < 0 || pagina > 10000 || tamanio < 1 || tamanio > 100) {
      throw new IllegalArgumentException("Página desde cero y tamaño entre 1 y 100");
    }
    return factory.fromTransaction(
        s ->
            s.createSelectionQuery(
                    "select l.titulo from Libro l order by l.titulo,l.id", String.class)
                .setFirstResult(pagina * tamanio)
                .setMaxResults(tamanio)
                .getResultList());
  }

  public static long recuentoNativo(SessionFactory factory) {
    return factory.fromTransaction(
        s -> s.createNativeQuery("select count(*) from libro", Long.class).getSingleResult());
  }

  /** Medición secuencial por factory propia; no cronometra ni promete una velocidad. */
  public static Medida medirCarga(SessionFactory factory, boolean conFetch) {
    factory.getStatistics().setStatisticsEnabled(true);
    factory.getStatistics().clear();
    int cantidad =
        factory.fromTransaction(
            s -> {
              String hql =
                  conFetch
                      ? "select distinct l from Libro l join fetch l.editorial left join fetch"
                            + " l.autores left join fetch l.detalle order by l.titulo,l.id"
                      : "from Libro l order by l.titulo,l.id";
              List<Libro> libros = s.createSelectionQuery(hql, Libro.class).getResultList();
              for (Libro libro : libros) {
                libro.getEditorial().getNombre();
                libro.getAutores().forEach(a -> a.getNombre());
                if (libro.getDetalle() != null) libro.getDetalle().getIdioma();
              }
              return libros.size();
            });
    return new Medida(cantidad, factory.getStatistics().getPrepareStatementCount());
  }

  public static void main(String[] args) {
    try (HibernateFactory proveedor = new HibernateFactory()) {
      SessionFactory factory = proveedor.get();
      log.info("Libros y autores: {}", librosYAutores(factory));
      log.info(
          "Proyección: {}; totales: {}; primera página: {}; SQL nativo: {}",
          proyeccion(factory),
          totales(factory),
          pagina(factory, 0, 5),
          recuentoNativo(factory));
      log.info(
          "Carga ordinaria: {}; carga con JOIN FETCH: {}",
          medirCarga(factory, false),
          medirCarga(factory, true));
    }
  }
}
