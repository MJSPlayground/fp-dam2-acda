package es.mjsplayground.acda.spring.catalogo.model.service;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PresupuestoService {
  private final CatalogoService catalogo;
  private final PoliticaDescuento politica;

  public PresupuestoService(CatalogoService catalogo, PoliticaDescuento politica) {
    this.catalogo = catalogo;
    this.politica = politica;
  }

  public Presupuesto calcular(List<LineaSolicitud> solicitud) {
    if (solicitud == null || solicitud.isEmpty() || solicitud.size() > 20)
      throw new ReglaCatalogoException("Selecciona entre 1 y 20 libros.");
    Set<Long> ids = new HashSet<>();
    List<Presupuesto.Linea> lineas = new ArrayList<>();
    BigDecimal subtotal = new BigDecimal("0.00");
    for (LineaSolicitud linea : solicitud) {
      if (linea == null
          || linea.libroId() == null
          || linea.libroId() < 1
          || !ids.add(linea.libroId())
          || linea.cantidad() < 1
          || linea.cantidad() > 100
          || linea.version() == null)
        throw new ReglaCatalogoException(
            "Líneas únicas, cantidad de 1 a 100 y versión requeridas.");
      LibroDatos l = catalogo.obtener(linea.libroId());
      if (!Objects.equals(l.version(), linea.version()))
        throw new ReglaCatalogoException("Un libro cambió; actualiza la selección.");
      if (l.stock() < linea.cantidad())
        throw new ReglaCatalogoException("Existencias insuficientes de " + l.titulo());
      BigDecimal importe = l.precio().multiply(BigDecimal.valueOf(linea.cantidad())).setScale(2);
      subtotal = subtotal.add(importe);
      lineas.add(
          new Presupuesto.Linea(
              l.id(), l.version(), l.isbn(), l.titulo(), linea.cantidad(), l.precio(), importe));
    }
    BigDecimal descuento = politica.descuento(subtotal);
    return new Presupuesto(lineas, subtotal, descuento, subtotal.subtract(descuento));
  }
}
