# Ampliación: escrituras fiables

Escribir directamente sobre un archivo existente puede dejarlo incompleto si el proceso se interrumpe. Un patrón habitual consiste en crear un temporal **en el mismo directorio** que el destino, escribirlo y cerrarlo, y moverlo sobre el destino. Estar en el mismo sistema de ficheros aumenta la posibilidad de un reemplazo atómico.

```java
Path temporal = Files.createTempFile(destino.getParent(), ".nuevo-", ".tmp");
boolean sustituido = false;
try {
    Files.writeString(temporal, contenido, StandardCharsets.UTF_8);
    try {
        Files.move(temporal, destino, ATOMIC_MOVE, REPLACE_EXISTING);
    } catch (AtomicMoveNotSupportedException ex) {
        Files.move(temporal, destino, REPLACE_EXISTING);
    }
    sustituido = true;
} finally {
    if (!sustituido) Files.deleteIfExists(temporal);
}
```

El fragmento supone que `destino.getParent()` existe y es externo al JAR. [`ATOMIC_MOVE`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/StandardCopyOption.html#ATOMIC_MOVE) pide que otros observadores vean el archivo antiguo o el nuevo, sin estado intermedio, **si** el proveedor lo soporta. Si no, el reemplazo ordinario puede tener una ventana de fallo; el programa debe informar de esa garantía menor. La combinación exacta de opciones también depende del proveedor. No prometas atomicidad cuando se ha ejecutado la alternativa.

Para durabilidad ante pérdida de energía, pueden requerirse sincronización del archivo y del directorio, cuya disponibilidad depende del sistema. Un `move` exitoso no demuestra que todos los bytes estén persistidos físicamente. Cuando el resultado es crítico, conserva copia anterior o un registro de operaciones y define cómo recuperar un temporal pendiente en el siguiente arranque. Decide si un temporal incompleto se borra o se conserva para diagnóstico, sin confundirlo con un archivo final válido.

**Exploración:** provoca un fallo antes de `move` y comprueba que el destino anterior mantiene su contenido. Después prueba en un sistema que no soporte el movimiento atómico y observa el mensaje de degradación. El [código de referencia](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/ficheros/solucion) ofrece una función reutilizable.
