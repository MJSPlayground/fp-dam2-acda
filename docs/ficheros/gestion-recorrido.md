# Gestión, recorrido y búsqueda

[`Files`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html) concentra operaciones de creación, copia, movimiento, borrado e inspección. Sus métodos reciben un `Path` y consultan el sistema de ficheros. Suelen lanzar `IOException`: el mensaje y la excepción concreta aportan más información que una comprobación previa con `exists`.

## Crear y modificar sin perder datos por accidente

`createDirectories(carpeta)` crea también los padres que falten y acepta una carpeta ya existente. `createDirectory` exige que exista el padre. Una copia sin opciones falla si el destino ya existe; `REPLACE_EXISTING` permite sobrescribirlo solo cuando sea una decisión explícita. `move` puede cambiar de nombre o mover a otro directorio, pero sus garantías dependen del proveedor y del sistema de ficheros. `delete` falla si no existe la entrada; `deleteIfExists` devuelve `false` en ese caso. Un directorio no vacío no se elimina con una simple llamada a `delete`.

Este fragmento ilustrativo prepara una carpeta de salida y copia un archivo. La copia sigue necesitando captura o propagación de `IOException` en el método que la contiene.

```java
Path salida = Path.of("trabajo", "copias");
Files.createDirectories(salida);
Path copia = Files.copy(Path.of("entrada", "ventas.csv"), salida.resolve("ventas.csv"));
System.out.println("Copia: " + copia);
```

Tras ejecutarlo, `trabajo/copias/ventas.csv` contiene los mismos bytes que la entrada. Si ya existía, `Files.copy` lanza `FileAlreadyExistsException`; así se evita un reemplazo silencioso. Para consultar tamaño y fecha usa `Files.size` y `Files.getLastModifiedTime`; para metadatos agrupados, [`BasicFileAttributes`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/attribute/BasicFileAttributes.html) evita varias consultas individuales.

## Listar un directorio

[`Files.list`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html#list(java.nio.file.Path)) devuelve un `Stream<Path>` con las entradas **directas**, sin entrar en subdirectorios. El stream mantiene recursos abiertos y debe cerrarse. El orden no está garantizado; ordénalo si la salida ha de ser reproducible.

```java
try (Stream<Path> entradas = Files.list(Path.of("entrada"))) {
    entradas.filter(Files::isRegularFile)
            .sorted()
            .forEach(System.out::println);
}
```

Este fragmento imprime solo archivos normales del primer nivel. `isRegularFile` puede devolver `false` cuando no puede determinar el tipo, de modo que un inventario riguroso también debe registrar incidencias de lectura.

## Recorrer un árbol

[`Files.walk`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html#walk(java.nio.file.Path,int,java.nio.file.FileVisitOption...)) recorre varios niveles y también devuelve un stream que se cierra. Es cómodo para árboles pequeños; los errores posteriores a abrirlo pueden surgir durante la iteración como `UncheckedIOException`. Si necesitas decidir qué hacer al entrar en cada directorio o continuar después de un fallo concreto, [`walkFileTree`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html#walkFileTree(java.nio.file.Path,java.nio.file.FileVisitor)) con `FileVisitor` ofrece control explícito de `visitFileFailed` y de la profundidad.

```java
try (Stream<Path> rutas = Files.walk(Path.of("entrada"), 3)) {
    rutas.filter(p -> Files.isRegularFile(p))
         .filter(p -> p.getFileName().toString().endsWith(".csv"))
         .forEach(System.out::println);
}
```

El límite `3` incluye la raíz como profundidad cero. La comparación de extensión es sensible a mayúsculas; si buscas `.CSV`, decide si tu regla debe normalizar el nombre. No sigas enlaces simbólicos salvo que el caso lo exija: pueden formar ciclos o salir de la carpeta esperada. Para conjuntos muy grandes consulta [streaming](ampliacion-streaming.md).

Aplica estas operaciones en la [práctica guiada](guiada-directorios.md) y después en el [inventario autónomo](practica-inventario.md).
