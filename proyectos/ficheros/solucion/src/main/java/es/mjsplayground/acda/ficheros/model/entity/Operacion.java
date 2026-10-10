package es.mjsplayground.acda.ficheros.model.entity;

import java.math.BigDecimal;
import java.util.List;

public record Operacion(String id, String clienteId, List<LineaOperacion> lineas, BigDecimal importe) {
    public Operacion { lineas = List.copyOf(lineas); }
}
