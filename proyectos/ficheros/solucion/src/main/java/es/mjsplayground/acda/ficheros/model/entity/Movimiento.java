package es.mjsplayground.acda.ficheros.model.entity;

import java.math.BigDecimal;

public record Movimiento(String id, String nombre, String ciudad, BigDecimal importe) {}
