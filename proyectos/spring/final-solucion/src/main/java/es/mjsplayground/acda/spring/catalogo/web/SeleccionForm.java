package es.mjsplayground.acda.spring.catalogo.web;

import es.mjsplayground.acda.spring.catalogo.model.service.LineaSolicitud;
import java.util.*;

public class SeleccionForm {
  private String clave;
  private List<Linea> lineas = new ArrayList<>();

  public String getClave() {
    return clave;
  }

  public void setClave(String clave) {
    this.clave = clave;
  }

  public List<Linea> getLineas() {
    return lineas;
  }

  public void setLineas(List<Linea> lineas) {
    this.lineas = lineas;
  }

  public static class Linea {
    private Long libroId, version;
    private String titulo;
    private int cantidad;

    public Long getLibroId() {
      return libroId;
    }

    public void setLibroId(Long id) {
      libroId = id;
    }

    public Long getVersion() {
      return version;
    }

    public void setVersion(Long version) {
      this.version = version;
    }

    public String getTitulo() {
      return titulo;
    }

    public void setTitulo(String titulo) {
      this.titulo = titulo;
    }

    public int getCantidad() {
      return cantidad;
    }

    public void setCantidad(int cantidad) {
      this.cantidad = cantidad;
    }
  }

  public List<LineaSolicitud> solicitud() {
    return lineas == null
        ? null
        : lineas.stream()
            .filter(Objects::nonNull)
            .filter(l -> l.cantidad != 0)
            .map(l -> new LineaSolicitud(l.libroId, l.cantidad, l.version))
            .toList();
  }
}
