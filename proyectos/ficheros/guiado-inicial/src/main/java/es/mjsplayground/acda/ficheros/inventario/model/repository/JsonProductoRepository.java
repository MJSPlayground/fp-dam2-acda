package es.mjsplayground.acda.ficheros.inventario.model.repository;

import com.fasterxml.jackson.databind.*;
import es.mjsplayground.acda.ficheros.inventario.model.entity.Producto;
import java.io.*;
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
