package es.mjsplayground.acda.ficheros.inventario.model.repository;

import es.mjsplayground.acda.ficheros.inventario.config.*;
import es.mjsplayground.acda.ficheros.inventario.model.entity.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class BinProductoRepository implements InventarioRepository {
  public List<Producto> leer(Path archivo) throws IOException {
    try (ObjectInputStream in = FiltroSerializacion.abrirSeguro(archivo)) {
      Object objeto = in.readObject();
      if (!(objeto instanceof Instantanea estado) || estado.productos() == null)
        throw new IOException("El binario no contiene un inventario propio");
      try {
        in.readObject();
        throw new IOException("Hay objetos adicionales en el binario");
      } catch (EOFException fin) {
        /* un único objeto */
      }
      return new ArrayList<>(Arrays.asList(estado.productos()));
    } catch (ClassNotFoundException | RuntimeException e) {
      throw new IOException("No se pudo recuperar el inventario binario", e);
    }
  }

  public void guardar(Path archivo, List<Producto> productos) throws IOException {
    EscrituraProtegida.escribir(
        archivo,
        temp -> {
          try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(temp))) {
            out.writeObject(new Instantanea(productos.toArray(Producto[]::new)));
          }
        });
  }
}
