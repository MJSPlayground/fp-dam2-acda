package es.mjsplayground.acda.spring.catalogo.model.service;

import es.mjsplayground.acda.spring.catalogo.model.entity.Libro;
import es.mjsplayground.acda.spring.catalogo.model.repository.LibroRepository;
import java.time.Clock;
import java.util.Objects;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExistenciasService {
  private final LibroRepository libros;
  private final ReglasCatalogo reglas;
  private final ApplicationEventPublisher eventos;
  private final Clock reloj;

  public ExistenciasService(
      LibroRepository libros,
      ReglasCatalogo reglas,
      ApplicationEventPublisher eventos,
      Clock reloj) {
    this.libros = libros;
    this.reglas = reglas;
    this.eventos = eventos;
    this.reloj = reloj;
  }

  private Libro obtener(Long id, Long version) {
    if (id == null || id < 1 || version == null)
      throw new ReglaCatalogoException("Libro y versión requeridos.");
    Libro l =
        libros.bloquear(id).orElseThrow(() -> new NoEncontradoException("Libro no encontrado."));
    if (!Objects.equals(version, l.getVersion()))
      throw new ReglaCatalogoException("El libro cambió; actualiza su versión antes de operar.");
    return l;
  }

  @Transactional
  public void reponer(Long id, Long version, int unidades) {
    // TODO reglas finales: resolver las nuevas necesidades de existencias y conflictos
    throw new UnsupportedOperationException(
        "Etapa pendiente: reglas finales: resolver las nuevas necesidades de existencias y"
            + " conflictos");
  }

  @Transactional
  public void descontar(Long id, Long version, int cantidad) {
    // TODO reglas finales: resolver las nuevas necesidades de existencias y conflictos
    throw new UnsupportedOperationException(
        "Etapa pendiente: reglas finales: resolver las nuevas necesidades de existencias y"
            + " conflictos");
  }
}
