package es.mjsplayground.acda.ficheros.inventario.model.repository;

import com.fasterxml.jackson.databind.*;
import es.mjsplayground.acda.ficheros.inventario.config.EscrituraProtegida;
import es.mjsplayground.acda.ficheros.inventario.model.entity.Producto;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class JsonProductoRepository implements InventarioRepository {
  public static ObjectMapper mapper() {
    ObjectMapper mapper =
        new ObjectMapper()
            .enable(com.fasterxml.jackson.core.JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
            .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
            .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
            .enable(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES,
                DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES,
                DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES,
                DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    mapper
        .coercionConfigFor(com.fasterxml.jackson.databind.type.LogicalType.Textual)
        .setCoercion(
            com.fasterxml.jackson.databind.cfg.CoercionInputShape.Integer,
            com.fasterxml.jackson.databind.cfg.CoercionAction.Fail)
        .setCoercion(
            com.fasterxml.jackson.databind.cfg.CoercionInputShape.Float,
            com.fasterxml.jackson.databind.cfg.CoercionAction.Fail)
        .setCoercion(
            com.fasterxml.jackson.databind.cfg.CoercionInputShape.Boolean,
            com.fasterxml.jackson.databind.cfg.CoercionAction.Fail);
    return mapper;
  }

  private final ObjectMapper mapper = mapper();

  public List<Producto> leer(Path archivo) throws IOException {
    try (BufferedReader in = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
      Producto[] lista = mapper.readValue(in, Producto[].class);
      if (lista == null || lista.length > 5000)
        throw new IOException("Se esperaba un array de hasta 5000 productos");
      return Arrays.asList(lista);
    }
  }

  public void guardar(Path archivo, List<Producto> productos) throws IOException {
    EscrituraProtegida.escribir(
        archivo,
        temp -> {
          try (BufferedWriter out = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(out, productos);
          }
        });
  }
}
