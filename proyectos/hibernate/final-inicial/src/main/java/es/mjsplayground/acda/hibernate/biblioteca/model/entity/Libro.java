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

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "editorial_id", nullable = false)
  private Editorial editorial;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "libro_autor",
      joinColumns = @JoinColumn(name = "libro_id"),
      inverseJoinColumns = @JoinColumn(name = "autor_id"))
  private Set<Autor> autores = new HashSet<>();

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "detalle_id", unique = true)
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
