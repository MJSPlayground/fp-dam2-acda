package es.mjsplayground.acda.spring.catalogo.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
    name = "libro",
    uniqueConstraints = @UniqueConstraint(name = "uk_libro_isbn", columnNames = "isbn"))
public class Libro {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 13)
  private String isbn;

  @Column(nullable = false, length = 120)
  private String titulo;

  @Column(nullable = false, precision = 7, scale = 2)
  private BigDecimal precio;

  @Column(nullable = false)
  private int stock;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "editorial_id", nullable = false)
  private Editorial editorial;

  @Version private Long version;

  protected Libro() {}

  public Libro(String isbn, String titulo, BigDecimal precio, int stock, Editorial editorial) {
    this.isbn = isbn;
    this.titulo = titulo;
    this.precio = precio;
    this.stock = stock;
    this.editorial = editorial;
  }

  public Long getId() {
    return id;
  }

  public String getIsbn() {
    return isbn;
  }

  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  public String getTitulo() {
    return titulo;
  }

  public void setTitulo(String titulo) {
    this.titulo = titulo;
  }

  public BigDecimal getPrecio() {
    return precio;
  }

  public void setPrecio(BigDecimal precio) {
    this.precio = precio;
  }

  public int getStock() {
    return stock;
  }

  public void setStock(int stock) {
    this.stock = stock;
  }

  public Editorial getEditorial() {
    return editorial;
  }

  public void setEditorial(Editorial editorial) {
    this.editorial = editorial;
  }

  public Long getVersion() {
    return version;
  }
}
