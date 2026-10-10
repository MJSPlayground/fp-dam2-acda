package es.mjsplayground.acda.ficheros.model.entity;

import java.util.List;

public record EstadoComercial(List<Cliente> clientes, List<Producto> productos, List<Operacion> operaciones) {
    public EstadoComercial { clientes = List.copyOf(clientes); productos = List.copyOf(productos); operaciones = List.copyOf(operaciones); }
    public static EstadoComercial vacio() { return new EstadoComercial(List.of(), List.of(), List.of()); }
}
