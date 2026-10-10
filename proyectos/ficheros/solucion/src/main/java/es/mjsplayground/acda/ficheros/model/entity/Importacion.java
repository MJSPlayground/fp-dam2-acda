package es.mjsplayground.acda.ficheros.model.entity;

import java.util.List;

public record Importacion(List<Cliente> clientes, List<Incidencia> incidencias) {
    public Importacion { clientes = List.copyOf(clientes); incidencias = List.copyOf(incidencias); }
}
