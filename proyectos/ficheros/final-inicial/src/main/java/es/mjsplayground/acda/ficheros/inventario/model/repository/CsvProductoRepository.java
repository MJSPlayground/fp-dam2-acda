package es.mjsplayground.acda.ficheros.inventario.model.repository;

import es.mjsplayground.acda.ficheros.inventario.config.EscrituraProtegida;
import es.mjsplayground.acda.ficheros.inventario.model.entity.Producto;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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
    List<Producto> resultado = new ArrayList<>();
    try (BufferedReader in = Files.newBufferedReader(archivo, StandardCharsets.UTF_8);
        CSVParser csv = LECTURA.parse(in)) {
      if (!csv.getHeaderNames().equals(List.of("codigo", "nombre", "unidades", "precio")))
        throw new IOException("Encabezados CSV esperados: codigo,nombre,unidades,precio");
      for (CSVRecord fila : csv) {
        if (!fila.isConsistent())
          throw new IOException("Columnas incorrectas en registro " + fila.getRecordNumber());
        resultado.add(
            new Producto(
                fila.get("codigo"),
                fila.get("nombre"),
                Integer.parseInt(fila.get("unidades")),
                new BigDecimal(fila.get("precio"))));
        if (resultado.size() > 5000) throw new IOException("Máximo 5000 productos");
      }
    } catch (IllegalArgumentException | UncheckedIOException e) {
      throw new IOException("CSV no válido", e);
    }
    return resultado;
  }

  public void guardar(Path archivo, List<Producto> productos) throws IOException {
    EscrituraProtegida.escribir(
        archivo,
        temp -> {
          try (BufferedWriter out = Files.newBufferedWriter(temp, StandardCharsets.UTF_8);
              CSVPrinter csv = new CSVPrinter(out, ESCRITURA)) {
            for (Producto p : productos)
              csv.printRecord(p.codigo(), p.nombre(), p.unidades(), p.precio().toPlainString());
          }
        });
  }
}
