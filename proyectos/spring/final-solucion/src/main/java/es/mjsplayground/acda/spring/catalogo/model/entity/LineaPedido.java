package es.mjsplayground.acda.spring.catalogo.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Embeddable
public class LineaPedido {
  @Column(nullable = false, length = 13)
  private String isbn;

  @Column(nullable = false, length = 120)
  private String titulo;

  @Column(nullable = false)
  private int cantidad;

  @Column(nullable = false, precision = 7, scale = 2)
  private BigDecimal precioUnitario;

  protected LineaPedido() {}

  public LineaPedido(String isbn, String titulo, int cantidad, BigDecimal precioUnitario) {
    this.isbn = isbn;
    this.titulo = titulo;
    this.cantidad = cantidad;
    this.precioUnitario = precioUnitario;
  }

  public String getIsbn() {
    return isbn;
  }

  public String getTitulo() {
    return titulo;
  }

  public int getCantidad() {
    return cantidad;
  }

  public BigDecimal getPrecioUnitario() {
    return precioUnitario;
  }
}
