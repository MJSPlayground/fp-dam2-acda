package es.mjsplayground.acda.ficheros.inventario.model.repository;

import es.mjsplayground.acda.ficheros.inventario.model.entity.Producto;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface InventarioRepository {
  List<Producto> leer(Path archivo) throws IOException;

  void guardar(Path archivo, List<Producto> productos) throws IOException;
}
