package es.mjsplayground.acda.spring.catalogo.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Entity
@Table(
    name = "pedido",
    uniqueConstraints = @UniqueConstraint(name = "uk_pedido_clave", columnNames = "clave"))
public class Pedido {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(
      nullable = false,
      length = 60,
      columnDefinition = "varchar(60) CHARACTER SET ascii COLLATE ascii_bin")
  private String clave;

  @Column(nullable = false)
  private Instant fecha;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal subtotal;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal descuento;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal total;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "pedido_linea", joinColumns = @JoinColumn(name = "pedido_id"))
  @OrderColumn(name = "posicion")
  private List<LineaPedido> lineas = new ArrayList<>();

  protected Pedido() {}

  public Pedido(
      String clave,
      Instant fecha,
      BigDecimal subtotal,
      BigDecimal descuento,
      BigDecimal total,
      List<LineaPedido> lineas) {
    this.clave = clave;
    this.fecha = fecha;
    this.subtotal = subtotal;
    this.descuento = descuento;
    this.total = total;
    this.lineas.addAll(lineas);
  }

  public Long getId() {
    return id;
  }

  public String getClave() {
    return clave;
  }

  public Instant getFecha() {
    return fecha;
  }

  public BigDecimal getSubtotal() {
    return subtotal;
  }

  public BigDecimal getDescuento() {
    return descuento;
  }

  public BigDecimal getTotal() {
    return total;
  }

  public List<LineaPedido> getLineas() {
    return lineas;
  }
}
