package es.mjsplayground.acda.hibernate.biblioteca.demo;

/** Entidad de demostración mapeada solo con demo/nota-orm.xml. */
public class NotaXml {
  private Long id;
  private String texto;

  protected NotaXml() {}

  public NotaXml(String texto) {
    this.texto = texto;
  }

  public Long getId() {
    return id;
  }

  public String getTexto() {
    return texto;
  }
}
