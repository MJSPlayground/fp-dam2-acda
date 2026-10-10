package es.mjsplayground.acda.ficheros.model.entity;

import java.math.BigDecimal;

public record Producto(String codigo, String nombre, int unidades, BigDecimal precio) {}
