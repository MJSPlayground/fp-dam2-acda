package es.mjsplayground.acda.ficheros.inventario.model.repository;

import com.fasterxml.jackson.databind.*;
import es.mjsplayground.acda.ficheros.inventario.config.EscrituraProtegida;
import es.mjsplayground.acda.ficheros.inventario.model.entity.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.apache.commons.csv.*;

public class LoteProductoRepository {
  public List<FilaImportada> leer(Path archivo, Formato formato) throws IOException {
    List<FilaImportada> filas = new ArrayList<>();
    if (formato == Formato.CSV) {
      try (BufferedReader in = Files.newBufferedReader(archivo, StandardCharsets.UTF_8);
          CSVParser csv = CsvProductoRepository.LECTURA.parse(in)) {
        if (!csv.getHeaderNames().equals(List.of("codigo", "nombre", "unidades", "precio")))
          throw new IOException("Encabezados CSV incorrectos");
        for (CSVRecord r : csv) {
          String codigo = r.size() > 0 ? r.get(0) : "";
          Producto p = null;
          String error = null;
          try {
            if (!r.isConsistent()) throw new IllegalArgumentException("Columnas incorrectas");
            p =
                new Producto(
                    codigo,
                    r.get("nombre"),
                    Integer.parseInt(r.get("unidades")),
                    new BigDecimal(r.get("precio")));
          } catch (IllegalArgumentException ex) {
            error = "Formato de fila incorrecto: " + ex.getMessage();
          }
          filas.add(new FilaImportada(Math.toIntExact(r.getRecordNumber()), codigo, p, error));
          if (filas.size() > 5000) throw new IOException("Máximo 5000 filas por lote");
        }
      } catch (UncheckedIOException | IllegalArgumentException e) {
        throw new IOException("No se pudo analizar el CSV completo", e);
      }
    } else if (formato == Formato.JSON) {
      ObjectMapper mapper = JsonProductoRepository.mapper();
      JsonNode raiz;
      try (BufferedReader in = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
        raiz = mapper.readTree(in);
      }
      if (raiz == null || !raiz.isArray() || raiz.size() > 5000)
        throw new IOException("Se esperaba array de hasta 5000 filas");
      for (int n = 0; n < raiz.size(); n++) {
        JsonNode nodo = raiz.get(n);
        String codigo = nodo.path("codigo").isTextual() ? nodo.get("codigo").textValue() : "";
        Producto p = null;
        String error = null;
        try {
          p = mapper.treeToValue(nodo, Producto.class);
          if (p == null) throw new IllegalArgumentException("Fila nula");
        } catch (IOException | IllegalArgumentException ex) {
          error = "Formato de fila incorrecto: " + ex.getMessage();
        }
        filas.add(new FilaImportada(n + 1, codigo, p, error));
      }
    } else throw new IOException("El lote debe ser CSV o JSON");
    return List.copyOf(filas);
  }

  public void informe(Path destino, List<Incidencia> incidencias) throws IOException {
    EscrituraProtegida.escribir(
        destino,
        temp -> {
          try (BufferedWriter out = Files.newBufferedWriter(temp, StandardCharsets.UTF_8);
              CSVPrinter csv =
                  new CSVPrinter(
                      out,
                      CSVFormat.RFC4180.builder().setHeader("fila", "codigo", "motivo").get())) {
            for (Incidencia i : incidencias) csv.printRecord(i.fila(), i.codigo(), i.motivo());
          }
        });
  }
}
