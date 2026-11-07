package es.mjsplayground.acda.firestore.reservas.model.entity;

import java.util.*;

public record Reserva(
    String id,
    String espacio,
    String fecha,
    int inicio,
    int fin,
    String responsable,
    int asistentes,
    List<String> equipo,
    Map<String, String> detalles,
    String estado,
    String motivoCancelacion) {}
