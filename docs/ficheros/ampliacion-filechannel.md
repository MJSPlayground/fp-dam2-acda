# Ampliación: canales de ficheros

[`FileChannel`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/channels/FileChannel.html) permite leer o escribir a una posición, transferir bytes entre canales, consultar tamaño y solicitar que los cambios pendientes se sincronicen. Conviene cuando se necesitan operaciones especializadas o archivos grandes. Para texto corriente, `Files.newBufferedReader` suele ser más sencillo y expresa mejor la intención.

Un canal usa [`ByteBuffer`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/ByteBuffer.html). Al leer, `read` devuelve el número de bytes o `-1`; después `flip()` cambia el búfer de modo escritura a lectura. `clear()` lo prepara para reutilizarlo. Al escribir, una llamada puede consumir menos bytes de los disponibles: repite mientras `hasRemaining()` sea verdadero.

```java
try (FileChannel origenCanal = FileChannel.open(origen, StandardOpenOption.READ);
     FileChannel destinoCanal = FileChannel.open(destino,
             StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
    ByteBuffer buffer = ByteBuffer.allocateDirect(64 * 1024);
    while (origenCanal.read(buffer) != -1) {
        buffer.flip();
        while (buffer.hasRemaining()) destinoCanal.write(buffer);
        buffer.clear();
    }
}
```

El programa copia bytes, no interpreta caracteres. `CREATE_NEW` evita sobrescribir; si el destino existe, aparece `FileAlreadyExistsException`. Para copiar, `Files.copy` puede ser preferible cuando no se necesita control del búfer. `transferTo` y `transferFrom` pueden aprovechar mecanismos del sistema, pero su resultado puede ser menor que los bytes solicitados: también requieren un bucle para una copia completa.

Un canal puede abrirse en posición concreta con `position(long)`, útil para formatos de registros. `force(true)` solicita sincronizar datos y metadatos, pero la garantía final depende de dispositivo y sistema de ficheros; no equivale por sí sola a una transacción completa. **Exploración:** copia un archivo mayor que el búfer y compara tamaños y hash de entrada y salida. La [solución de referencia](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/ficheros/solucion) incluye una demostración.
