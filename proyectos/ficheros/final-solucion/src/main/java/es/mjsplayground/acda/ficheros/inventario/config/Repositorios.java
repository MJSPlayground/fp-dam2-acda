package es.mjsplayground.acda.ficheros.inventario.config;

import es.mjsplayground.acda.ficheros.inventario.model.entity.Formato;
import es.mjsplayground.acda.ficheros.inventario.model.repository.*;
import java.util.*;

public final class Repositorios {
  public static Map<Formato, InventarioRepository> crear() {
    return Map.of(
        Formato.CSV,
        new CsvProductoRepository(),
        Formato.JSON,
        new JsonProductoRepository(),
        Formato.BIN,
        new BinProductoRepository());
  }
}
