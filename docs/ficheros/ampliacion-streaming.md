# Ampliación: procesamiento streaming

`Files.readString` y `ObjectMapper.readTree` cargan una representación completa en memoria. Para varios gigabytes, el consumo crece con el archivo y puede agotar la JVM. El procesamiento **incremental** lee un registro, lo valida y lo escribe antes de leer el siguiente. Solo conserva acumuladores pequeños, como contadores y un total. Es adecuado para archivos ordenados o transformaciones fila a fila; una deduplicación global puede requerir memoria adicional, clasificación externa o una base de datos.

## Texto y CSV por registros

`BufferedReader.readLine` sirve para líneas de texto sencillas. Un CSV con salto dentro de comillas necesita un parser que recorra **registros CSV**, no líneas físicas. [`CSVParser`](https://commons.apache.org/proper/commons-csv/apidocs/org/apache/commons/csv/CSVParser.html) implementa `Iterable<CSVRecord>`: itera sobre sus registros dentro de `try-with-resources` y envía los aceptados a `CSVPrinter`. No llames a `getRecords()` para archivos enormes, porque materializa toda la colección.

```java
try (Reader in = Files.newBufferedReader(origen, StandardCharsets.UTF_8);
     CSVParser parser = formato.parse(in);
     Writer out = Files.newBufferedWriter(destino, StandardCharsets.UTF_8);
     CSVPrinter printer = new CSVPrinter(out, formatoSalida)) {
    for (CSVRecord fila : parser) {
        if (fila.isConsistent()) printer.printRecord(fila.get("id"), fila.get("nombre"));
    }
}
```

El fragmento es ilustrativo; la validación de dominio y la política de errores deben añadirse. El consumo de memoria depende del tamaño del registro actual, no del número total de filas. Un campo desmesurado todavía puede causar problemas, así que impón límites si recibes entradas no controladas.

## JSON incremental

Jackson ofrece [`JsonParser`](https://javadoc.io/doc/com.fasterxml.jackson.core/jackson-core/latest/com/fasterxml/jackson/core/JsonParser.html) para recorrer tokens y [`JsonGenerator`](https://javadoc.io/doc/com.fasterxml.jackson.core/jackson-core/latest/com/fasterxml/jackson/core/JsonGenerator.html) para escribirlos. En un array grande, avanza hasta `START_ARRAY`, lee cada objeto con `ObjectMapper.readValue(parser, Cliente.class)` y procesa antes de pasar al siguiente. Verifica los tokens de cierre y no ignores estructuras inesperadas. Este enfoque requiere más control que `readTree`, pero evita construir el árbol completo.

**Exploración:** genera un CSV de muchos registros, transforma solo los que tienen identificador par y compara memoria y tiempo con una versión que llama a `getRecords()`. Guarda la salida en una carpeta distinta del origen y verifica recuentos; no uses el tamaño de archivo como única prueba de corrección. La [solución de referencia](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/ficheros/solucion) muestra un procesamiento incremental.
