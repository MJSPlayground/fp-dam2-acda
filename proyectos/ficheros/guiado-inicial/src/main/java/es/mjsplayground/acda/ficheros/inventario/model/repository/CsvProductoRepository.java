package es.mjsplayground.acda.ficheros.inventario.model.repository;

import es.mjsplayground.acda.ficheros.inventario.model.entity.Producto;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.apache.commons.csv.*;

/** Adaptación focal de ProductoFormatos histórico: RFC4180 y UTF-8, sin split de texto. */
public class CsvProductoRepository implements InventarioRepository {
  public static final CSVFormat LECTURA =
      CSVFormat.RFC4180
          .builder()
          .setHeader()
          .setSkipHeaderRecord(true)
          .setDuplicateHeaderMode(DuplicateHeaderMode.DISALLOW)
          .get();
  public static final CSVFormat ESCRITURA =
      CSVFormat.RFC4180.builder().setHeader("codigo", "nombre", "unidades", "precio").get();

  public List<Producto> leer(Path archivo) throws IOException {
    // TODO 2: lectura y escritura del formato con recursos y errores; usa EscrituraProtegida
    // preparada
    throw new UnsupportedOperationException(
        "Etapa pendiente: 2: lectura y escritura del formato con recursos y errores; usa"
            + " EscrituraProtegida preparada");
  }

  public void guardar(Path archivo, List<Producto> productos) throws IOException {
    // TODO 2: lectura y escritura del formato con recursos y errores; usa EscrituraProtegida
    // preparada
    throw new UnsupportedOperationException(
        "Etapa pendiente: 2: lectura y escritura del formato con recursos y errores; usa"
            + " EscrituraProtegida preparada");
  }
}
