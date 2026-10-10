# Guiada: convertir JSON en CSV

El archivo de entrada contiene un array `clientes`; cada cliente tiene `id`, un objeto `contacto` con `nombre` y `ciudad`, y un campo opcional `telefono`. El CSV de salida tendrá `id,nombre,ciudad,telefono`.

1. Lee el árbol con `ObjectMapper.readTree` y confirma que `clientes` es un array. Una raíz distinta produce un error de estructura, no una lista vacía.
2. Recorre el array. Usa `isTextual()` antes de extraer `id`, `contacto.nombre` y `contacto.ciudad`. Trata `telefono` ausente como campo vacío; un número en `nombre` se rechaza y se registra con el índice del elemento.
3. Escribe con `CSVPrinter` y verifica que nombres con comillas o comas se pueden volver a leer. Compara la cantidad de elementos JSON con válidos y rechazados.

```java
JsonNode clientes = mapper.readTree(origen.toFile()).path("clientes");
if (!clientes.isArray()) {
    throw new IllegalArgumentException("Se esperaba un array clientes");
}
for (JsonNode cliente : clientes) {
    JsonNode nombre = cliente.path("contacto").path("nombre");
    if (!nombre.isTextual()) {
        // Registra la incidencia y continúa.
        continue;
    }
    // Valida el resto antes de exportar.
}
```

Este fragmento es el núcleo de lectura, no un programa completo. El resultado CSV es plano: el objeto `contacto` se proyecta en dos columnas. Si el JSON tuviera varias direcciones por cliente, habría que decidir si se genera una fila por dirección o un formato distinto; no se pueden colocar arrays arbitrarios en una celda sin acordar una representación.

??? tip "Pregunta de comprobación"
    Si `telefono` es `null`, decide si el contrato lo representa como celda vacía y deja esa decisión escrita junto al exportador. No conviertas `null` en la palabra `"null"` por accidente.

Consulta el [código de referencia](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/ficheros/solucion) tras comprobar el archivo exportado. La [práctica de bloque](practica-formatos-anidados.md) amplía este caso.
