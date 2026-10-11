# Listar, recorrer y buscar archivos

Un inventario no conoce de antemano los nombres de todos los archivos. Necesita descubrir las entradas de una carpeta y, quizá, de sus subdirectorios. **Listar** significa mirar un solo nivel; **recorrer** significa bajar por el árbol. Para distinguirlos usaremos la misma estructura:

```text
entrada/
├── clientes.csv
├── nota.txt
└── historico/
    └── 2025.CSV
```

## El primer nivel con `Files.list`

[`Files.list(directorio)`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html#list(java.nio.file.Path)) recibe un `Path` que debe apuntar a un directorio y devuelve `Stream<Path>`. Un *stream* permite procesar sus elementos sin pedir primero una lista completa. Aquí sus elementos son rutas de las **entradas directas**: `clientes.csv`, `nota.txt` y `historico`, pero no `historico/2025.CSV`. El orden que ofrece el sistema no está garantizado.

```java
Path entrada = Path.of("entrada");
try (Stream<Path> rutas = Files.list(entrada)) {
    rutas.sorted().forEach(p -> System.out.println(entrada.relativize(p)));
}
```

El resultado esperado, una línea por entrada y ordenado, es `clientes.csv`, `historico`, `nota.txt` (con el orden alfabético propio de los nombres). `sorted()` devuelve un nuevo stream ordenado; `forEach` ejecuta la impresión. El `try-with-resources` llama a `close()` incluso si ocurre un error. Es necesario porque el stream puede mantener un directorio abierto. Si `entrada` no existe o no es directorio, `list` lanza `IOException`.

Para listar solo archivos, añade `filter(Files::isRegularFile)` antes de `sorted()`. `filter` recibe una condición y conserva las rutas para las que sea verdadera. En este caso no aparecerá `historico`, porque es un directorio. `Files.isRegularFile` puede devolver `false` si no puede conocer el tipo: no lo tomes como prueba universal de que un archivo no existe.

## Bajar por el árbol con `Files.walk`

[`Files.walk(inicio, profundidadMáxima)`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html#walk(java.nio.file.Path,int,java.nio.file.FileVisitOption...)) devuelve también `Stream<Path>`, pero incluye la propia raíz y desciende a subdirectorios. La raíz tiene profundidad **0**; sus hijos, 1; `historico/2025.CSV`, 2. Una profundidad máxima de 1 no alcanza el CSV histórico.

```java
try (Stream<Path> rutas = Files.walk(Path.of("entrada"), 2)) {
    rutas.filter(Files::isRegularFile)
         .map(p -> Path.of("entrada").relativize(p))
         .sorted()
         .forEach(System.out::println);
}
```

Ahora aparecen `clientes.csv`, `historico/2025.CSV` y `nota.txt`. `map` transforma cada ruta completa en una ruta relativa fácil de leer. El stream vuelve a cerrarse al salir del `try`. El orden del recorrido por sí solo no sirve para un informe reproducible: se ordena explícitamente después de seleccionar.

## Seleccionar por extensión y ordenar

La extensión es una convención del **nombre** del archivo, no una propiedad que Java valide. Para aceptar `.csv` y `.CSV`, compara en minúsculas con una configuración regional estable:

```java
String sufijo = ".csv";
try (Stream<Path> rutas = Files.walk(Path.of("entrada"), 2)) {
    rutas.filter(Files::isRegularFile)
         .filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(sufijo))
         .map(p -> Path.of("entrada").relativize(p))
         .sorted()
         .forEach(System.out::println);
}
```

La salida contiene `clientes.csv` y `historico/2025.CSV`, nunca `nota.txt`. `getFileName()` evita mirar el nombre de una carpeta padre que quizá contenga `.csv`. En una aplicación, valida también que la extensión pedida no esté vacía y decide qué hacer con nombres como `sinextension` o `.csv`. Si quieres ordenar sin depender del separador del sistema, compara los componentes o normaliza la representación que mostrarás en el informe.

## Cerrar el recorrido y reconocer sus límites

`Files.walk` es cómodo para exploraciones pequeñas. El stream es **perezoso**: algunas rutas y errores se descubren durante la iteración, no al crear el stream. Un fallo tardío puede aparecer como `UncheckedIOException`, y filtrar primero con `isRegularFile` puede ocultar una incidencia. Tampoco hay un punto claro donde decidir «registra este fallo y continúa con las demás ramas».

Para ese requisito existe [`Files.walkFileTree`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html#walkFileTree(java.nio.file.Path,java.util.Set,int,java.nio.file.FileVisitor)). Recibe la raíz, opciones, profundidad máxima y un visitante. Java llama a `visitFile` para cada entrada y le entrega sus [`BasicFileAttributes`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/attribute/BasicFileAttributes.html), incluidos tipo, tamaño y fecha de modificación. Si no pudo visitar una entrada, llama a `visitFileFailed`. Devolver `FileVisitResult.CONTINUE` permite registrar la incidencia y seguir con las otras entradas. `postVisitDirectory` puede recibir un error al terminar de leer una carpeta.

```java
Files.walkFileTree(raiz, EnumSet.noneOf(FileVisitOption.class), 4,
    new SimpleFileVisitor<>() {
        @Override public FileVisitResult visitFile(Path archivo, BasicFileAttributes datos) {
            if (datos.isRegularFile()) System.out.println(raiz.relativize(archivo));
            return FileVisitResult.CONTINUE;
        }
        @Override public FileVisitResult visitFileFailed(Path archivo, IOException error) {
            System.err.println("No se pudo leer " + archivo + ": " + error.getMessage());
            return FileVisitResult.CONTINUE;
        }
    });
```

El visitante evita releer atributos que Java ya obtuvo para `visitFile`. No seguimos enlaces simbólicos en este ejemplo; seguirlos puede entrar en ciclos o llevar fuera de la carpeta esperada. Un fallo al abrir la **raíz** sigue siendo fatal: no existe un inventario útil que construir. El orden de visita no está garantizado, así que la solución guarda las filas y las ordena antes de exportar. Para colecciones muy grandes habría que estudiar otras estrategias de memoria; se trata en [ampliación](ampliacion-streaming.md).

## Cómo se organiza el código de referencia

Antes de abrir el proyecto, imagina un programa que inventaría entregas. `cli.Inventariar` contiene `main`: recoge argumentos, muestra el resultado y coordina la exportación. `model.service.InventarioArchivosService` aplica la regla «solo esta extensión y hasta cuatro niveles» y calcula totales. `model.entity.ArchivoInventariado` representa los datos de una fila: ruta, bytes y fecha. Las clases `model.dao` de **otras** prácticas del laboratorio leen o escriben formatos; para este primer inventario se usan directamente operaciones de `Files` y un exportador CSV desde `main`. `gestor` contiene la interfaz gráfica de una práctica final posterior.

```text
Main (cli) → Servicio (reglas) → Acceso a ficheros (Files) → Archivo
                    ↓
          ArchivoInventariado (datos)
```

`main` llama a `crear(raiz, extension)`; el servicio devuelve un `Inventario` con filas, incidencias y estadísticas; `main` las muestra y escribe el CSV. Separar responsabilidades ayuda a localizar un error, probar el cálculo sin abrir la interfaz y cambiar después la forma de presentación. No necesitas conocer ninguna tecnología de bases de datos para seguir estas llamadas.
