package es.mjsplayground.acda.spring.catalogo.model.service;

import es.mjsplayground.acda.spring.catalogo.model.entity.*;
import es.mjsplayground.acda.spring.catalogo.model.repository.*;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CatalogoService {
  private final LibroRepository libros;
  private final EditorialRepository editoriales;
  private final EditorialService editorialService;
  private final ReglasCatalogo reglas;
  private final ApplicationEventPublisher eventos;
  private final Clock reloj;

  public CatalogoService(
      LibroRepository libros,
      EditorialRepository editoriales,
      EditorialService editorialService,
      ReglasCatalogo reglas,
      ApplicationEventPublisher eventos,
      Clock reloj) {
    this.libros = libros;
    this.editoriales = editoriales;
    this.editorialService = editorialService;
    this.reglas = reglas;
    this.eventos = eventos;
    this.reloj = reloj;
  }

  public List<LibroDatos> listar(String texto, Long editorial) {
    String filtro = texto == null ? "" : texto.strip();
    if (filtro.length() > 120) throw new ReglaCatalogoException("Filtro demasiado largo.");
    return (editorial == null
            ? libros.findByTituloContainingIgnoreCaseOrderByTituloAscIdAsc(filtro)
            : libros.findByEditorialIdAndTituloContainingIgnoreCaseOrderByTituloAscIdAsc(
                editorial, filtro))
        .stream().map(LibroDatos::de).toList();
  }

  public LibroDatos obtener(Long id) {
    return LibroDatos.de(
        libros.findById(id).orElseThrow(() -> new NoEncontradoException("Libro no encontrado.")));
  }

  @Transactional
  public Long guardar(LibroSolicitud r) {
    reglas.validar(r);
    Editorial editorial = editorialService.obtener(r.editorialId());
    Libro l;
    if (r.id() == null)
      l = new Libro(r.isbn(), r.titulo().strip(), r.precio().setScale(2), r.stock(), editorial);
    else {
      l =
          libros
              .findById(r.id())
              .orElseThrow(() -> new NoEncontradoException("Libro no encontrado."));
      if (!Objects.equals(r.version(), l.getVersion()))
        throw new ReglaCatalogoException(
            "El libro cambió; actualiza el formulario antes de guardar.");
      l.setIsbn(r.isbn());
      l.setTitulo(r.titulo().strip());
      l.setPrecio(r.precio().setScale(2));
      l.setStock(r.stock());
      l.setEditorial(editorial);
    }
    libros.saveAndFlush(l);
    eventos.publishEvent(new CambioCatalogo(l.getId(), "GUARDAR_LIBRO", reloj.instant()));
    return l.getId();
  }

  @Transactional
  public void eliminar(Long id) {
    Libro l =
        libros.findById(id).orElseThrow(() -> new NoEncontradoException("Libro no encontrado."));
    libros.delete(l);
    libros.flush();
    eventos.publishEvent(new CambioCatalogo(id, "ELIMINAR_LIBRO", reloj.instant()));
  }

  @Transactional
  public void demo() {
    if (libros.count() > 0 || editoriales.count() > 0)
      throw new ReglaCatalogoException("Demostración solo en base vacía; tus datos se conservan.");
    Editorial e = editoriales.saveAndFlush(new Editorial("Horizonte"));
    Editorial n = editoriales.saveAndFlush(new Editorial("Norte"));
    libros.save(new Libro("9780000000001", "Java y datos", new BigDecimal("29.90"), 10, e));
    libros.save(new Libro("9780000000002", "Historias locales", new BigDecimal("18.50"), 5, n));
    libros.flush();
    eventos.publishEvent(new CambioCatalogo(null, "DATOS_DEMO", reloj.instant()));
  }
}
