package es.mjsplayground.acda.spring.catalogo.model.service;

import java.math.*;
import org.springframework.stereotype.Component;

@Component
public class PoliticaDescuento {
  public BigDecimal descuento(BigDecimal subtotal) {
    return subtotal.compareTo(new BigDecimal("100.00")) >= 0
        ? subtotal.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP)
        : new BigDecimal("0.00");
  }
}
