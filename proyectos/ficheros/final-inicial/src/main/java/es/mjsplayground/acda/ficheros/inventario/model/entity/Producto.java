package es.mjsplayground.acda.ficheros.inventario.model.entity;

import java.io.Serializable;
import java.math.BigDecimal;

public record Producto(String codigo, String nombre, int unidades, BigDecimal precio)
    implements Serializable {
  private static final long serialVersionUID = 1L;
}
