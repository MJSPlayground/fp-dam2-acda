package es.mjsplayground.acda.jdbc.almacen.model.entity;

public record Venta(long id, String producto, int cantidad, long totalCentimos, String fecha) {}
