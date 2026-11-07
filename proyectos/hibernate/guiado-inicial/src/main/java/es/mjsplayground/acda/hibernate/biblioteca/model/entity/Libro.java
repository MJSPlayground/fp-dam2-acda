package es.mjsplayground.acda.hibernate.biblioteca.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.*;

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

  // TODO1: N:1 propietario obligatorio, editorial_id y LAZY
  @Transient
  private Editorial editorial;
  // TODO1: M:N propietaria, tabla libro_autor; nunca CascadeType.REMOVE a autores compartidos
  @Transient
  private Set<Autor> autores = new HashSet<>();
  // TODO1: 1:1 propietaria opcional, detalle_id único, cascade ALL/orphanRemoval solo del detalle
  @Transient
  private Detalle detalle;
  @Version private long version;

  protected Libro() {}

  public Libro(
      String isbn,
      String titulo,
      BigDecimal precio,
      Editorial editorial,
      Collection<Autor> autores,
      Detalle detalle) {
    this.isbn = isbn;
    this.titulo = titulo;
    this.precio = precio;
    this.editorial = editorial;
    this.autores.addAll(autores);
    this.detalle = detalle;
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

  public Editorial getEditorial() {
    return editorial;
  }

  public void setEditorial(Editorial editorial) {
    this.editorial = editorial;
  }

  public Set<Autor> getAutores() {
    return autores;
  }

  public void setAutores(Collection<Autor> autores) {
    this.autores.clear();
    this.autores.addAll(autores);
  }

  public Detalle getDetalle() {
    return detalle;
  }

  public void setDetalle(Detalle detalle) {
    this.detalle = detalle;
  }

  public long getVersion() {
    return version;
  }
}
