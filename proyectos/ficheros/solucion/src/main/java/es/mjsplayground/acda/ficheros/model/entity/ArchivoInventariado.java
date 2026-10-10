package es.mjsplayground.acda.ficheros.model.entity;

import java.nio.file.Path;
import java.time.Instant;

public record ArchivoInventariado(Path relativa, long bytes, Instant modificado) {}
