package es.mjsplayground.acda.jdbc.almacen.model.entity;

public record Producto(long id, String nombre, long precioCentimos, int stock) {}
