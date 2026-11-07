package es.mjsplayground.acda.spring.catalogo.model.service;

import es.mjsplayground.acda.spring.catalogo.model.entity.Editorial;
import es.mjsplayground.acda.spring.catalogo.model.repository.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EditorialService {
  private final EditorialRepository editoriales;
  private final LibroRepository libros;
  private final ReglasCatalogo reglas;

  public EditorialService(
      EditorialRepository editoriales, LibroRepository libros, ReglasCatalogo reglas) {
    this.editoriales = editoriales;
    this.libros = libros;
    this.reglas = reglas;
  }

  public record Datos(Long id, String nombre) {}

  public List<Datos> listar() {
    return editoriales.findAllByOrderByNombreAscIdAsc().stream()
        .map(e -> new Datos(e.getId(), e.getNombre()))
        .toList();
  }

  public Editorial obtener(Long id) {
    return editoriales
        .findById(id)
        .orElseThrow(() -> new NoEncontradoException("Editorial no encontrada."));
  }

  @Transactional
  public Long guardar(Long id, String nombre) {
    reglas.nombre(nombre, 80);
    Editorial e = id == null ? new Editorial(nombre.strip()) : obtener(id);
    e.setNombre(nombre.strip());
    return editoriales.saveAndFlush(e).getId();
  }

  @Transactional
  public void eliminar(Long id) {
    Editorial e = obtener(id);
    if (libros.existsByEditorialId(id))
      throw new ReglaCatalogoException(
          "Reasigna o elimina los libros antes de borrar su editorial.");
    editoriales.delete(e);
    editoriales.flush();
  }
}
