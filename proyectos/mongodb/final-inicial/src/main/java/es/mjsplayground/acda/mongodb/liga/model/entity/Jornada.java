package es.mjsplayground.acda.mongodb.liga.model.entity;

import java.util.*;

public record Jornada(
    String id,
    String fecha,
    String rival,
    int golesFavor,
    int golesContra,
    List<Aportacion> aportaciones) {}
