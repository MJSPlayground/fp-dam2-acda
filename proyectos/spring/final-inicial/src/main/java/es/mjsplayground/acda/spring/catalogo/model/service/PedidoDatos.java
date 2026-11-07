package es.mjsplayground.acda.spring.catalogo.model.service;

import es.mjsplayground.acda.spring.catalogo.model.entity.Pedido;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PedidoDatos(
    Long id,
    String clave,
    Instant fecha,
    BigDecimal subtotal,
    BigDecimal descuento,
    BigDecimal total,
    List<Linea> lineas) {
  public record Linea(String isbn, String titulo, int cantidad, BigDecimal precioUnitario) {}

  public static PedidoDatos de(Pedido p) {
    return new PedidoDatos(
        p.getId(),
        p.getClave(),
        p.getFecha(),
        p.getSubtotal(),
        p.getDescuento(),
        p.getTotal(),
        p.getLineas().stream()
            .map(l -> new Linea(l.getIsbn(), l.getTitulo(), l.getCantidad(), l.getPrecioUnitario()))
            .toList());
  }
}
