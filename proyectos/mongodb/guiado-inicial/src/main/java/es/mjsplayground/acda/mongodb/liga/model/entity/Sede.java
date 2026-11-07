package es.mjsplayground.acda.mongodb.liga.model.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("sedes")
public record Sede(@Id String id, String nombre, String ciudad) {}
