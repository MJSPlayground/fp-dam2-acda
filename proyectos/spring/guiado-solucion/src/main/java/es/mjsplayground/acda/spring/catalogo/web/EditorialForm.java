package es.mjsplayground.acda.spring.catalogo.web;

import jakarta.validation.constraints.*;

public class EditorialForm {
  private Long id;

  @NotBlank
  @Size(max = 80)
  private String nombre;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }
}
