# Lectura y escritura de texto

Un fichero de texto contiene **bytes** que una codificación convierte en caracteres. Si se escribe con UTF-8 y se lee con otra codificación, tildes y símbolos pueden aparecer corruptos. Define la codificación en ambos sentidos y no supongas que todos los archivos recibidos están bien formados. [`StandardCharsets.UTF_8`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/charset/StandardCharsets.html#UTF_8) proporciona una constante sin errores tipográficos.

## Archivos pequeños

[`Files.readString`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html#readString(java.nio.file.Path,java.nio.charset.Charset)) resulta cómodo si el contenido cabe en memoria: devuelve todo el texto o lanza `IOException`. `writeString` recibe la ruta, el texto, la codificación y, opcionalmente, opciones de apertura. Sin opciones, crea o trunca el destino; no lo uses para agregar líneas por accidente.

El siguiente fragmento es ilustrativo. Crea una carpeta externa, escribe un informe y lo recupera con la misma codificación.

```java
Path salida = Path.of("salida");
Files.createDirectories(salida);
Path informe = salida.resolve("resumen.txt");
Files.writeString(informe, "Artículos válidos: 3\n", StandardCharsets.UTF_8);
String contenido = Files.readString(informe, StandardCharsets.UTF_8);
System.out.println(contenido);
```

Se imprime `Artículos válidos: 3`. Para añadir contenido explícitamente se puede usar `StandardOpenOption.CREATE` y `APPEND`. La operación puede fallar por permisos, por espacio insuficiente o por cierre inesperado del dispositivo; capturar `IOException` permite informar al usuario sin fingir éxito.

## Lectura por líneas

Un informe de varios gigabytes no debe entrar entero en una `String`. [`Files.newBufferedReader`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html#newBufferedReader(java.nio.file.Path,java.nio.charset.Charset)) construye un [`BufferedReader`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/io/BufferedReader.html), que amortigua accesos al dispositivo. `readLine()` devuelve la siguiente línea **sin** su salto o `null` al terminar. El `try-with-resources` cierra el lector incluso si hay una excepción.

```java
try (BufferedReader lector = Files.newBufferedReader(entrada, StandardCharsets.UTF_8)) {
    String linea;
    while ((linea = lector.readLine()) != null) {
        System.out.println(linea);
    }
}
```

Este fragmento procesa una línea cada vez. La línea aún puede ser enorme; para datos verdaderamente masivos se pueden imponer límites o usar un parser incremental. `readLine` sirve para texto dividido en líneas, pero **no** interpreta CSV: un campo CSV entrecomillado puede contener un salto de línea.

[`Files.newBufferedWriter`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html#newBufferedWriter(java.nio.file.Path,java.nio.charset.Charset,java.nio.file.OpenOption...)) ofrece la operación inversa. `write` no agrega un salto automáticamente; `newLine` usa el salto del sistema. Si el formato exige una secuencia concreta, escríbela explícitamente. Cierra el escritor antes de leer el resultado para asegurar que el búfer haya entregado sus datos al sistema.

## Diagnosticar errores de codificación

Una entrada UTF-8 inválida puede provocar `MalformedInputException`; una ruta equivocada puede producir `NoSuchFileException`. Muestra el nombre del archivo real y la causa en un mensaje útil. Cambiar a otra codificación a ciegas puede ocultar el problema: acuerda la codificación con quien produce el archivo o detecta y registra el formato recibido. El [apartado de CSV](csv.md) añade estructura y validación al texto.
