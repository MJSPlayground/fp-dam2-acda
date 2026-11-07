package es.mjsplayground.acda.spring.catalogo.model.service;

import es.mjsplayground.acda.spring.catalogo.config.CatalogoProperties;
import org.springframework.stereotype.Component;

@Component
public class ReglasCatalogo {
  private final CatalogoProperties propiedades;

  public ReglasCatalogo(CatalogoProperties propiedades) {
    this.propiedades = propiedades;
  }

  public void nombre(String n, int max) {
    if (n == null || n.isBlank() || n.strip().length() > max)
      throw new ReglaCatalogoException("Nombre/título de 1 a " + max + " caracteres requerido.");
  }

  public void stock(int stock) {
    if (stock < 0 || stock > propiedades.maxStock())
      throw new ReglaCatalogoException("Existencias entre 0 y " + propiedades.maxStock() + ".");
  }

  public void validar(LibroSolicitud r) {
    // TODO 3: revalidar invariantes del libro en backend
    throw new UnsupportedOperationException(
        "Etapa pendiente: 3: revalidar invariantes del libro en backend");
  }
}
