package es.mjsplayground.acda.hibernate.biblioteca.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "detalle")
public class Detalle {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 30)
  private String idioma;

  @Column(nullable = false)
  private int paginas;

  protected Detalle() {}

  public Detalle(String idioma, int paginas) {
    this.idioma = idioma;
    this.paginas = paginas;
  }

  public Long getId() {
    return id;
  }

  public String getIdioma() {
    return idioma;
  }

  public int getPaginas() {
    return paginas;
  }

  public void setIdioma(String idioma) {
    this.idioma = idioma;
  }

  public void setPaginas(int paginas) {
    this.paginas = paginas;
  }
}
