package es.mjsplayground.acda.spring.catalogo.model.service;

import java.math.BigDecimal;
import java.util.List;

public record Presupuesto(
    List<Linea> lineas, BigDecimal subtotal, BigDecimal descuento, BigDecimal total) {
  public Presupuesto {
    lineas = List.copyOf(lineas);
  }

  public record Linea(
      Long libroId,
      Long version,
      String isbn,
      String titulo,
      int cantidad,
      BigDecimal precioUnitario,
      BigDecimal importe) {}
}
