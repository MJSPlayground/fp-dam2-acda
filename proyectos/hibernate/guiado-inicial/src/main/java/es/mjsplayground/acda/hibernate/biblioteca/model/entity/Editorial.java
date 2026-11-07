package es.mjsplayground.acda.hibernate.biblioteca.model.entity;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(
    name = "editorial",
    uniqueConstraints = @UniqueConstraint(name = "uk_editorial_nombre", columnNames = "nombre"))
public class Editorial {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 80)
  private String nombre;

  // TODO1: sustituye @Transient por el mapeo inverse 1:N, mappedBy editorial y LAZY; sin borrado en
  // cascada
  @Transient
  private Set<Libro> libros = new HashSet<>();

  protected Editorial() {}

  public Editorial(String nombre) {
    this.nombre = nombre;
  }

  public Long getId() {
    return id;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public Set<Libro> getLibros() {
    return libros;
  }
}
