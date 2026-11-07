package es.mjsplayground.acda.spring.catalogo.model.service;

import es.mjsplayground.acda.spring.catalogo.model.entity.*;
import es.mjsplayground.acda.spring.catalogo.model.repository.*;
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
    // TODO 3: coordinar editorial, reglas, repositorio y evento dentro de la transacción
    throw new UnsupportedOperationException(
        "Etapa pendiente: 3: coordinar editorial, reglas, repositorio y evento dentro de la"
            + " transacción");
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
    // TODO 4: preparar datos solo en base vacía mediante operación coordinada
    throw new UnsupportedOperationException(
        "Etapa pendiente: 4: preparar datos solo en base vacía mediante operación coordinada");
  }
}
