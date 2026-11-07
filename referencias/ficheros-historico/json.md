# Estructura JSON y Jackson

JSON tiene objetos con claves, arrays ordenados, cadenas, números, booleanos y `null`. Un objeto puede contener otros objetos o arrays. Por ejemplo, una venta puede incluir `cliente` y una lista `lineas`, cada una con cantidad y precio. A diferencia de XML, JSON no distingue atributos de elementos ni incluye una gramática de etiquetas; a cambio, sus valores tienen tipos básicos. La [sintaxis estándar](https://www.rfc-editor.org/rfc/rfc8259) exige comillas dobles en nombres y cadenas.

Jackson proporciona [`ObjectMapper`](https://javadoc.io/doc/com.fasterxml.jackson.core/jackson-databind/latest/com/fasterxml/jackson/databind/ObjectMapper.html) para leer y escribir JSON. Hay dos estilos útiles: **árbol** (`JsonNode`) cuando la estructura varía y **mapeo a objetos Java** cuando se conoce un contrato estable. No hagas `split` sobre comas o llaves: las cadenas pueden contener esos caracteres y los objetos pueden estar anidados.

## Leer un árbol

El siguiente fragmento ilustrativo abre un archivo externo y accede a `cliente.nombre`. `path` devuelve un nodo ausente cuando falta la clave; `get` devuelve `null`. Comprueba la forma antes de convertir tipos: `asInt()` puede devolver un valor por defecto que ocultaría datos erróneos.

```java
ObjectMapper mapper = new ObjectMapper();
JsonNode raiz = mapper.readTree(entrada.toFile());
JsonNode nombre = raiz.path("cliente").path("nombre");
if (!nombre.isTextual() || nombre.textValue().isBlank()) {
    throw new IllegalArgumentException("Falta cliente.nombre");
}
System.out.println(nombre.textValue());
```

`readTree` puede lanzar `JsonProcessingException` por sintaxis inválida e `IOException` por acceso al archivo. El programa distingue ambos casos para ayudar a corregir el origen del problema. [`JsonNode`](https://javadoc.io/doc/com.fasterxml.jackson.core/jackson-databind/latest/com/fasterxml/jackson/databind/JsonNode.html) ofrece `isArray`, `isObject`, `isNumber` y otros predicados antes de extraer datos. Para importes decimales, convierte a `BigDecimal` en el servicio y evita cálculos monetarios con `double`.

## Mapear registros Java

Si el contrato es fijo, un `record Cliente(String id, String nombre)` expresa el modelo. `mapper.readValue(archivo, Cliente.class)` construye una instancia; `mapper.writeValue(archivo, cliente)` la guarda. Un record no sustituye la validación comercial: Jackson puede construir un objeto con cadena vacía o un número fuera de rango. Define qué claves son obligatorias y decide si se permiten propiedades desconocidas. Mantén separados el modelo de intercambio y las reglas del servicio cuando los formatos evolucionen.

Para exportar un árbol se pueden crear nodos con `createObjectNode` y `createArrayNode`, o escribir registros Java directamente. `writerWithDefaultPrettyPrinter()` facilita la inspección, aunque el espaciado no cambia el significado del JSON. La [guiada](guiada-json-csv.md) transforma un array de registros; la [práctica de bloque](practica-formatos-anidados.md) incorpora estructuras anidadas y filtros.
