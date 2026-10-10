package es.mjsplayground.acda.ficheros.model.entity;

import java.util.List;

public record Resultado(List<Cliente> clientes, List<Incidencia> incidencias) {
    public Resultado { clientes = List.copyOf(clientes); incidencias = List.copyOf(incidencias); }
}
