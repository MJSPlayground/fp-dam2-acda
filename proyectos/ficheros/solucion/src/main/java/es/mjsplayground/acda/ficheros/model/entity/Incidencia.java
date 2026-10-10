package es.mjsplayground.acda.ficheros.model.entity;

public record Incidencia(String origen, long registro, String motivo) {
    @Override public String toString() { return origen + " [" + registro + "]: " + motivo; }
}
