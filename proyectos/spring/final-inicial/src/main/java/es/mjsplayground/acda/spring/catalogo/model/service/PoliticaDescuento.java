package es.mjsplayground.acda.spring.catalogo.model.service;

import java.math.*;
import org.springframework.stereotype.Component;

@Component
public class PoliticaDescuento {
  public BigDecimal descuento(BigDecimal subtotal) {
    // TODO reglas finales: política de cálculo conforme al cliente
    throw new UnsupportedOperationException(
        "Etapa pendiente: reglas finales: política de cálculo conforme al cliente");
  }
}
