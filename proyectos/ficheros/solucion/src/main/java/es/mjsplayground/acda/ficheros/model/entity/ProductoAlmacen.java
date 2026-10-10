package es.mjsplayground.acda.ficheros.model.entity;

import java.math.BigDecimal;

public record ProductoAlmacen(String codigo, String nombre, String almacen, int unidades, BigDecimal precio) {}
