package es.mjsplayground.acda.ficheros.model.entity;

import java.math.BigDecimal;

public record ResumenOperacion(String clienteId, String productoCodigo, long unidades, BigDecimal importe) {}
