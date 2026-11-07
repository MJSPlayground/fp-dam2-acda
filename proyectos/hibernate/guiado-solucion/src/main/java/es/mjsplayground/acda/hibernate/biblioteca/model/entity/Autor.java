package es.mjsplayground.acda.hibernate.biblioteca.model.entity;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(
    name = "autor",
    uniqueConstraints = @UniqueConstraint(name = "uk_autor_nombre", columnNames = "nombre"))
public class Autor {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 80)
  private String nombre;

  @ManyToMany(mappedBy = "autores", fetch = FetchType.LAZY)
  private Set<Libro> libros = new HashSet<>();

  protected Autor() {}

  public Autor(String nombre) {
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
