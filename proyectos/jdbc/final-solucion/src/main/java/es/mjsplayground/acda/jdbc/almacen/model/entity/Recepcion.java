package es.mjsplayground.acda.jdbc.almacen.model.entity;

public record Recepcion(String producto, int cantidad, long totalCentimos, String fecha) {}
