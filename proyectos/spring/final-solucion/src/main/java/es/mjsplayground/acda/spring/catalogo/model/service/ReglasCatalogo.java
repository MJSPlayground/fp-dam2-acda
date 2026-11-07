package es.mjsplayground.acda.spring.catalogo.model.service;

import es.mjsplayground.acda.spring.catalogo.config.CatalogoProperties;
import java.math.BigDecimal;
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
    if (r == null || r.isbn() == null || !r.isbn().matches("[0-9]{13}"))
      throw new ReglaCatalogoException(
          "ISBN de 13 dígitos requerido (sin comprobación del dígito de control en este"
              + " catálogo).");
    nombre(r.titulo(), 120);
    stock(r.stock());
    if (r.precio() == null
        || r.precio().signum() <= 0
        || r.precio().compareTo(new BigDecimal("10000")) > 0
        || r.precio().scale() > 2)
      throw new ReglaCatalogoException("Precio de 0,01 a 10000 y hasta dos decimales.");
    if (r.editorialId() == null || r.editorialId() < 1)
      throw new ReglaCatalogoException("Selecciona una editorial.");
  }
}
