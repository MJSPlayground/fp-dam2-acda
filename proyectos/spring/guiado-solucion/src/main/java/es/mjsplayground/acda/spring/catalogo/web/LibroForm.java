package es.mjsplayground.acda.spring.catalogo.web;

import es.mjsplayground.acda.spring.catalogo.model.service.*;

public class LibroForm {
  @jakarta.validation.constraints.Positive private Long id;
  private Long version;

  @jakarta.validation.constraints.NotBlank
  @jakarta.validation.constraints.Pattern(regexp = "[0-9]{13}")
  private String isbn;

  @jakarta.validation.constraints.NotBlank
  @jakarta.validation.constraints.Size(max = 120)
  private String titulo;

  @jakarta.validation.constraints.NotNull
  @jakarta.validation.constraints.DecimalMin("0.01")
  @jakarta.validation.constraints.DecimalMax("10000")
  @jakarta.validation.constraints.Digits(integer = 5, fraction = 2)
  private java.math.BigDecimal precio;

  @jakarta.validation.constraints.Min(0)
  @jakarta.validation.constraints.Max(1000000)
  private int stock;

  @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Positive
  private Long editorialId;

  public Long getId() {
    return id;
  }

  public void setId(Long valor) {
    id = valor;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long valor) {
    version = valor;
  }

  public String getIsbn() {
    return isbn;
  }

  public void setIsbn(String valor) {
    isbn = valor;
  }

  public String getTitulo() {
    return titulo;
  }

  public void setTitulo(String valor) {
    titulo = valor;
  }

  public java.math.BigDecimal getPrecio() {
    return precio;
  }

  public void setPrecio(java.math.BigDecimal valor) {
    precio = valor;
  }

  public int getStock() {
    return stock;
  }

  public void setStock(int valor) {
    stock = valor;
  }

  public Long getEditorialId() {
    return editorialId;
  }

  public void setEditorialId(Long valor) {
    editorialId = valor;
  }

  public LibroSolicitud solicitud() {
    return new LibroSolicitud(id, version, isbn, titulo, precio, stock, editorialId);
  }

  public static LibroForm de(LibroDatos l) {
    LibroForm f = new LibroForm();
    f.id = l.id();
    f.version = l.version();
    f.isbn = l.isbn();
    f.titulo = l.titulo();
    f.precio = l.precio();
    f.stock = l.stock();
    f.editorialId = l.editorialId();
    return f;
  }
}
