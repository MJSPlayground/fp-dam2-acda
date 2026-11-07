package es.mjsplayground.acda.ficheros.inventario.model.entity;

import java.io.Serializable;

public record Instantanea(Producto[] productos) implements Serializable {
  private static final long serialVersionUID = 1L;
}
