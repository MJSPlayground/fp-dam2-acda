package es.mjsplayground.acda.ficheros.inventario.model.entity;

import java.util.List;

public record PlanConciliacion(
    long versionBase, List<Producto> resultado, List<Incidencia> incidencias) {
  public PlanConciliacion {
    resultado = List.copyOf(resultado);
    incidencias = List.copyOf(incidencias);
  }
}
