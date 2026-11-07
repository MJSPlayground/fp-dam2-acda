package es.mjsplayground.acda.spring.catalogo.model.service;

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
    // TODO reglas finales: presupuesto sin modificar persistencia
    throw new UnsupportedOperationException(
        "Etapa pendiente: reglas finales: presupuesto sin modificar persistencia");
  }
}
