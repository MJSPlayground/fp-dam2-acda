# Gestionar archivos y directorios

Ya sabemos construir un `Path`; ahora necesitamos realizar operaciones en disco. La clase [`Files`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html) reúne métodos estáticos que reciben rutas. La mayoría de operaciones modificadoras devuelven el `Path` creado o afectado y pueden lanzar `IOException`. En los ejemplos siguientes, los fragmentos se colocan dentro de un método que declara `throws IOException`; en una aplicación se captura el error cerca de la entrada para explicar al usuario qué ocurrió.

## Preparar carpetas y archivos

[`createDirectory`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html#createDirectory(java.nio.file.Path,java.nio.file.attribute.FileAttribute...)) crea **una** carpeta: el padre debe existir y el destino no debe existir. [`createDirectories`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html#createDirectories(java.nio.file.Path,java.nio.file.attribute.FileAttribute...)) crea también los padres ausentes y acepta que la carpeta ya exista. Ambas reciben el `Path` deseado y devuelven esa ruta.

```java
Path trabajo = Path.of("trabajo");
Path copias = Files.createDirectories(trabajo.resolve("copias"));
System.out.println(copias); // trabajo/copias
```

Después habrá una carpeta `trabajo/copias` relativa al directorio de trabajo. Si existe un *archivo* llamado `trabajo`, la operación fallará: una ruta válida en Java no implica que la estructura física permita usarla. [`createFile`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html#createFile(java.nio.file.Path,java.nio.file.attribute.FileAttribute...)) crea un archivo vacío y falla si el nombre está ocupado; todavía no escribe texto en él.

## Consultar el tipo y las propiedades

[`isDirectory`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html#isDirectory(java.nio.file.Path,java.nio.file.LinkOption...)) y [`isRegularFile`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html#isRegularFile(java.nio.file.Path,java.nio.file.LinkOption...)) devuelven un booleano. Son útiles para validar qué espera la aplicación, aunque `false` también puede significar que Java no pudo determinar el tipo. [`size`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html#size(java.nio.file.Path)) devuelve bytes, no caracteres. [`getLastModifiedTime`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html#getLastModifiedTime(java.nio.file.Path,java.nio.file.LinkOption...)) devuelve una fecha `FileTime`.

```java
Path documento = Path.of("entregas", "clientes.csv");
if (!Files.isRegularFile(documento)) {
    throw new IOException("Se esperaba un archivo: " + documento.toAbsolutePath());
}
System.out.println(Files.size(documento) + " bytes");
System.out.println(Files.getLastModifiedTime(documento));
```

Si el archivo contiene `a,b` en UTF-8 sin salto de línea, el tamaño será 3 bytes. La fecha real dependerá del equipo y del momento de creación. Para consultar juntas varias propiedades durante un recorrido, [`BasicFileAttributes`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/attribute/BasicFileAttributes.html) ofrece `size()`, `lastModifiedTime()` e `isRegularFile()`; lo retomaremos al inventariar.

## Copiar, mover y cambiar el nombre

[`Files.copy(origen, destino)`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html#copy(java.nio.file.Path,java.nio.file.Path,java.nio.file.CopyOption...)) conserva el origen y devuelve el `Path` del destino. El padre del destino debe existir. Sin opciones, un nombre ocupado produce `FileAlreadyExistsException`, protección útil frente a una repetición accidental.

```java
Path origen = Path.of("entregas", "clientes.csv");
Path destino = Path.of("trabajo", "copias", "clientes.csv");
Files.createDirectories(destino.getParent());
Files.copy(origen, destino);
```

Tras ejecutar, existen las dos copias. Repetir la última línea falla. Solo si el requisito permite sustituir una copia, añade [`StandardCopyOption.REPLACE_EXISTING`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/StandardCopyOption.html). Esto puede perder los bytes antiguos del destino: ofrece esa decisión de forma explícita.

[`Files.move`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html#move(java.nio.file.Path,java.nio.file.Path,java.nio.file.CopyOption...)) traslada la entrada; con otro nombre en el mismo directorio sirve para **renombrar**:

```java
Files.move(Path.of("trabajo", "borrador.csv"), Path.of("trabajo", "revisado.csv"));
```

Si el origen no existe falla; si el destino está ocupado, las opciones y el proveedor determinan el resultado. Un movimiento entre sistemas de ficheros distintos no tiene necesariamente las mismas garantías que un simple cambio de nombre.

## Eliminar con intención

[`delete`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html#delete(java.nio.file.Path)) no devuelve nada y falla si la entrada falta. [`deleteIfExists`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html#deleteIfExists(java.nio.file.Path)) devuelve `true` si eliminó algo y `false` si no había entrada. Ninguna de estas llamadas borra directamente un directorio no vacío.

```java
boolean eliminada = Files.deleteIfExists(Path.of("trabajo", "copia-antigua.csv"));
System.out.println(eliminada ? "Copia eliminada" : "No había copia");
```

## Leer el error antes de corregirlo

`NoSuchFileException` indica que falta una entrada usada en la operación; `FileAlreadyExistsException`, que el destino está ocupado; `AccessDeniedException`, que el proceso no obtuvo acceso. Todas son formas concretas de `IOException`. Una ruta puede ser correcta y aun así fallar por permisos, bloqueo, carpeta padre inexistente o cambios simultáneos. Muestra la ruta implicada y conserva la causa:

```java
try {
    Files.copy(origen, destino);
} catch (FileAlreadyExistsException ex) {
    System.err.println("La copia ya existe: " + ex.getFile());
} catch (IOException ex) {
    System.err.println("No se pudo copiar " + origen + " a " + destino + ": " + ex.getMessage());
}
```

Capturar `IOException` antes de `FileAlreadyExistsException` impediría distinguir este caso concreto. Tampoco conviene hacer `exists` y asumir que la copia siguiente será segura: otro proceso podría crear o borrar una entrada entre ambas llamadas.
