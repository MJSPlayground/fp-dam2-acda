package es.mjsplayground.acda.hibernate.biblioteca.model.service;

import es.mjsplayground.acda.hibernate.biblioteca.model.entity.*;
import es.mjsplayground.acda.hibernate.biblioteca.model.repository.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.*;
import org.hibernate.*;
import org.slf4j.*;

public class BibliotecaService {
  protected static final Logger log = LoggerFactory.getLogger(BibliotecaService.class);
  protected final Supplier<SessionFactory> factory;
  protected final LibroRepository libros;
  protected final CatalogoRepository catalogos;

  public BibliotecaService(Supplier<SessionFactory> factory) {
    this(factory, new LibroRepository(), new CatalogoRepository());
  }

  public BibliotecaService(
      Supplier<SessionFactory> factory, LibroRepository libros, CatalogoRepository catalogos) {
    this.factory = factory;
    this.libros = libros;
    this.catalogos = catalogos;
  }

  public record Referencia(long id, String nombre) {
    @Override
    public String toString() {
      return nombre;
    }
  }

  protected <T> T transaccion(Function<Session, T> tarea) {
    try (Session s = factory.get().openSession()) {
      Transaction tx = s.beginTransaction();
      try {
        T resultado = tarea.apply(s);
        tx.commit();
        return resultado;
      } catch (RuntimeException e) {
        try {
          if (tx.isActive()) tx.rollback();
        } catch (RuntimeException rollback) {
          e.addSuppressed(rollback);
        }
        throw e;
      }
    } catch (BibliotecaException | UnsupportedOperationException e) {
      throw e;
    } catch (RuntimeException e) {
      log.error("Fallo de persistencia en biblioteca", e);
      throw new BibliotecaException(
          "No se pudo guardar o consultar. Comprueba conexión, ISBN/nombres duplicados y"
              + " conflictos; consulta el registro técnico.",
          e);
    }
  }

  protected void nombre(String n, int max) {
    if (n == null || n.isBlank() || n.strip().length() > max)
      throw new BibliotecaException("Escribe un nombre de 1 a " + max + " caracteres.");
  }

  protected Libro requerido(Session s, long id) {
    Libro l = libros.buscar(s, id);
    if (l == null) throw new BibliotecaException("Libro no encontrado.");
    return l;
  }

  protected Editorial editorial(Session s, long id) {
    Editorial e = catalogos.editorial(s, id);
    if (e == null) throw new BibliotecaException("Editorial no encontrada.");
    return e;
  }

  protected List<Autor> autores(Session s, List<Long> ids) {
    if (ids == null
        || ids.isEmpty()
        || ids.size() > 5
        || ids.stream().anyMatch(Objects::isNull)
        || new HashSet<>(ids).size() != ids.size())
      throw new BibliotecaException("Selecciona de 1 a 5 autores distintos.");
    List<Autor> lista = new ArrayList<>();
    for (long id : ids) {
      Autor a = catalogos.autor(s, id);
      if (a == null) throw new BibliotecaException("Autor no encontrado.");
      lista.add(a);
    }
    return lista;
  }

  public List<Referencia> editoriales() {
    return transaccion(
        s ->
            catalogos.editoriales(s).stream()
                .map(e -> new Referencia(e.getId(), e.getNombre()))
                .toList());
  }

  public List<Referencia> autores() {
    return transaccion(
        s ->
            catalogos.autores(s).stream()
                .map(a -> new Referencia(a.getId(), a.getNombre()))
                .toList());
  }

  public List<LibroDatos> listar(String texto, Long editorialId) {
    String t = texto == null ? "" : texto.strip();
    if (t.length() > 120) throw new BibliotecaException("Filtro de título demasiado largo.");
    return transaccion(s -> libros.listar(s, t, editorialId).stream().map(LibroDatos::de).toList());
  }

  public LibroDatos obtener(long id) {
    return transaccion(s -> LibroDatos.de(requerido(s, id)));
  }

  public long guardarEditorial(Long id, String n) {
    nombre(n, 80);
    return transaccion(
        s -> {
          Editorial e = id == null ? new Editorial(n.strip()) : editorial(s, id);
          e.setNombre(n.strip());
          if (id == null) catalogos.insertar(s, e);
          libros.flush(s);
          return e.getId();
        });
  }

  public long guardarAutor(Long id, String n) {
    nombre(n, 80);
    return transaccion(
        s -> {
          Autor a;
          if (id == null) {
            a = new Autor(n.strip());
            catalogos.insertar(s, a);
          } else {
            a = catalogos.autor(s, id);
            if (a == null) throw new BibliotecaException("Autor no encontrado.");
            a.setNombre(n.strip());
          }
          libros.flush(s);
          return a.getId();
        });
  }

  public void eliminarEditorial(long id) {
    transaccion(
        s -> {
          Editorial e = editorial(s, id);
          if (catalogos.editorialUsada(s, id))
            throw new BibliotecaException(
                "No elimines una editorial con libros; reasigna primero su catálogo.");
          catalogos.eliminar(s, e);
          return null;
        });
  }

  public void eliminarAutor(long id) {
    transaccion(
        s -> {
          Autor a = catalogos.autor(s, id);
          if (a == null) throw new BibliotecaException("Autor no encontrado.");
          if (catalogos.autorUsado(s, id))
            throw new BibliotecaException("No elimines un autor vinculado a libros.");
          catalogos.eliminar(s, a);
          return null;
        });
  }

  protected void validar(LibroSolicitud r) {
    if (r == null || r.isbn() == null || !r.isbn().matches("[0-9]{13}"))
      throw new BibliotecaException(
          "ISBN de 13 dígitos requerido (identificador del catálogo, sin cálculo de control en este"
              + " ejercicio).");
    nombre(r.titulo(), 120);
    if (r.precio() == null
        || r.precio().signum() <= 0
        || r.precio().compareTo(new BigDecimal("10000")) > 0
        || r.precio().scale() > 2)
      throw new BibliotecaException("Precio de 0,01 a 10000 con hasta dos decimales.");
    if (r.idioma() != null && !r.idioma().isBlank()) {
      nombre(r.idioma(), 30);
      if (r.idioma().strip().length() < 2
          || r.paginas() == null
          || r.paginas() < 1
          || r.paginas() > 10000)
        throw new BibliotecaException(
            "Detalle: idioma de 2 a 30 caracteres y páginas de 1 a 10000.");
    } else if (r.paginas() != null)
      throw new BibliotecaException("Completa idioma y páginas juntos o deja ambos vacíos.");
  }

  public long guardarLibro(Long id, Long version, LibroSolicitud r) {
    validar(r);
    return transaccion(
        s -> {
          Editorial e = editorial(s, r.editorialId());
          List<Autor> autores = autores(s, r.autorIds());
          Libro l;
          if (id == null) {
            Detalle d =
                r.idioma() == null || r.idioma().isBlank()
                    ? null
                    : new Detalle(r.idioma().strip(), r.paginas());
            l = new Libro(r.isbn(), r.titulo().strip(), r.precio().setScale(2), e, autores, d);
            libros.insertar(s, l);
          } else {
            l = requerido(s, id);
            if (version == null || version != l.getVersion())
              throw new BibliotecaException("El libro cambió: actualiza antes de editar.");
            l.setIsbn(r.isbn());
            l.setTitulo(r.titulo().strip());
            l.setPrecio(r.precio().setScale(2));
            l.setEditorial(e);
            l.setAutores(autores);
            if (r.idioma() == null || r.idioma().isBlank()) l.setDetalle(null);
            else if (l.getDetalle() == null)
              l.setDetalle(new Detalle(r.idioma().strip(), r.paginas()));
            else {
              l.getDetalle().setIdioma(r.idioma().strip());
              l.getDetalle().setPaginas(r.paginas());
            }
          }
          libros.flush(s);
          return l.getId();
        });
  }

  public void eliminarLibro(long id) {
    transaccion(
        s -> {
          libros.eliminar(s, requerido(s, id));
          return null;
        });
  }

  public void datosDemo() {
    transaccion(
        s -> {
          if (s.createSelectionQuery("select count(l) from Libro l", Long.class).getSingleResult()
                  != 0
              || !catalogos.editoriales(s).isEmpty()
              || !catalogos.autores(s).isEmpty())
            throw new BibliotecaException(
                "La demostración solo se prepara en una base vacía; no modifica tus datos"
                    + " existentes.");
          Editorial e = new Editorial("Horizonte"), otra = new Editorial("Norte");
          Autor a = new Autor("Alex Ríos"), b = new Autor("Luz Vega");
          catalogos.insertar(s, e);
          catalogos.insertar(s, otra);
          catalogos.insertar(s, a);
          catalogos.insertar(s, b);
          libros.insertar(
              s,
              new Libro(
                  "9780000000001",
                  "Java y datos",
                  new BigDecimal("29.90"),
                  e,
                  List.of(a, b),
                  new Detalle("Español", 320)));
          libros.insertar(
              s,
              new Libro(
                  "9780000000002",
                  "Historias locales",
                  new BigDecimal("18.50"),
                  otra,
                  List.of(a),
                  null));
          return null;
        });
  }
}
