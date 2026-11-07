package es.mjsplayground.acda.mongodb.liga.model.entity;

public record Jugador(
    String id,
    String nombre,
    int dorsal,
    String posicion,
    boolean disponible,
    Estadisticas estadisticas) {}
