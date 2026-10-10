package es.mjsplayground.acda.ficheros.model.entity;

import java.math.BigDecimal;

public record LineaOperacion(String productoCodigo, int cantidad, BigDecimal importe) {}
