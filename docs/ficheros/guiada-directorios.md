# Práctica guiada: organizar e inventariar directorios

Una productora recibe entregas en distintas carpetas. Necesita conservar una copia de `clientes.csv` y entregar un inventario de los CSV recibidos: ruta relativa, tamaño, modificación y un resumen. El programa debe aceptar `.csv` en cualquier combinación de mayúsculas, recorrer hasta cuatro niveles, ordenar las filas, registrar incidencias y guardar el informe **fuera** de la carpeta examinada.

Al terminar, `trabajo-codelab/salida/copias/clientes.csv` será la copia y `trabajo-codelab/salida/inventario.csv` contendrá cuatro filas de muestra. La suma de sus tamaños será **22 bytes** y el mayor será `historico/2025/marzo.CsV` (8 bytes; Windows puede mostrar `\` entre componentes). Tu programa estará en `InventarioCodelab.java`; el proyecto de referencia usa varias clases para separar responsabilidades, como se explicó en [recorrido y organización](listar-recorrer.md#como-se-organiza-el-codigo-de-referencia).

<div class="mjs-progress-panel" role="status" aria-live="polite"><span class="mjs-progress-label">Tu avance en esta práctica</span><strong class="mjs-progress-count">0 de 11 pasos</strong><div class="mjs-progress-track"><div class="mjs-progress-fill"></div></div></div>

<div class="mjs-resources" markdown="1">
[Proyecto inicial](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/ficheros/codelab-inicial){ .md-button .md-button--primary }
[Generador de datos](https://github.com/MJSPlayground/fp-dam2-acda/blob/main/proyectos/ficheros/codelab-inicial/src/main/java/es/mjsplayground/acda/ficheros/codelab/PrepararDatos.java){ .md-button }
</div>

## 1. Preparar el proyecto y los datos { .mjs-step-heading }

Abre **`proyectos/ficheros/codelab-inicial`**, no la carpeta raíz del repositorio, como proyecto Maven. Selecciona JDK 25. En una terminal situada en esa carpeta, ejecuta `./mvnw.cmd test` en Windows (o `./mvnw test` en Linux/macOS). El starter compila, pero aún no inventaría nada. Después crea el conjunto de datos externo:

```powershell
.\mvnw.cmd -q exec:java '-Dexec.mainClass=es.mjsplayground.acda.ficheros.codelab.PrepararDatos' '-Dexec.args=trabajo-codelab/entrada'
```

`PrepararDatos` crea `clientes.csv`, `ventas.CSV`, `notas.txt`, dos CSV en subdirectorios y una carpeta realmente vacía. Los tamaños de los CSV son 4, 4, 6 y 8 bytes. Si repites la preparación sobre el mismo destino, falla deliberadamente para no borrar cambios: usa otra carpeta nueva. Los datos se crean fuera de `src/main/resources` porque vamos a copiarlos y a provocar incidencias con ellos.

<div class="mjs-task"><input type="checkbox" id="b1-p1"><label for="b1-p1">He importado el starter, lo he compilado y veo la estructura de entrada y la carpeta vacía.</label></div>

## 2. Construir rutas sin adivinar la carpeta de trabajo { .mjs-step-heading }

Ejecuta `InventarioCodelab` con `trabajo-codelab/entrada trabajo-codelab/salida csv`. El `main` inicial ya imprime el directorio de trabajo y las dos rutas absolutas. Si no apunta a la carpeta del proyecto, ajusta el *working directory* de la configuración de ejecución o ejecuta desde la terminal indicada. `Path.of(args[0])` recibe la primera cadena y crea una ruta; **todavía no abre** esa carpeta. Añade la validación física antes de modificar nada:

```java
if (!Files.isDirectory(origen)) {
    throw new IOException("La entrada no es un directorio: " + origen.toAbsolutePath());
}
```

Importa `java.nio.file.Files` y `java.io.IOException`. Comprueba el mensaje pasando `trabajo-codelab/ausente` como primer argumento. Una ruta inválida debe detener esta ejecución; no debe crear un informe vacío que parezca correcto.

<div class="mjs-task"><input type="checkbox" id="b1-p2"><label for="b1-p2">Distingo la ruta relativa, el directorio de trabajo y el error de entrada inexistente.</label></div>

## 3. Crear la salida y copiar sin sobrescribir { .mjs-step-heading }

Construye `trabajo.resolve("copias")` y usa `Files.createDirectories`: crea tanto `salida` como `copias` si faltan. Después copia el archivo concreto al destino. `Files.copy` recibe origen y destino; no sustituye un archivo ya existente sin una opción explícita.

```java
Path copias = Files.createDirectories(trabajo.resolve("copias"));
Path muestra = origen.resolve("clientes.csv");
Path copia = copias.resolve("clientes.csv");
Files.copy(muestra, copia);
```

Al acabar, compara el contenido de las dos copias. Ejecuta otra vez: debe aparecer `FileAlreadyExistsException`. Captura **ese caso concreto** para mostrar «La copia ya existe» y decide si quieres conservarla. Como pequeña exploración, añade un argumento opcional `--reemplazar` y usa `StandardCopyOption.REPLACE_EXISTING` solo cuando esté presente. En la solución de referencia, esa opción se pasa a `OrganizarDirectorios`.

Después de observar el fallo, rodea la copia con `try { ... } catch (FileAlreadyExistsException ex) { System.out.println("Conservo la copia existente: " + copia); }`. Así podrás volver a ejecutar los pasos siguientes sin perder la copia anterior. Importa `java.nio.file.FileAlreadyExistsException`.

??? tip "Pista: seleccionar la opción"
    Puedes usar `if (args.length == 4 && "--reemplazar".equals(args[3]))` para elegir entre dos llamadas a `Files.copy`. Importa `java.nio.file.StandardCopyOption`.

<div class="mjs-task"><input type="checkbox" id="b1-p3"><label for="b1-p3">La primera copia funciona y he comprobado qué ocurre al repetirla.</label></div>

## 4. Reconocer primero los archivos de un nivel { .mjs-step-heading }

Antes de descender por el árbol, usa `Files.list(origen)` dentro de `try-with-resources`. Devuelve un `Stream<Path>` de entradas directas; ciérralo para liberar el directorio. Muestra nombres ordenados:

```java
try (Stream<Path> entradas = Files.list(origen)) {
    entradas.sorted().forEach(p -> System.out.println(origen.relativize(p)));
}
```

Importa `java.util.stream.Stream`. Debes ver también `historico` y `vacio`, pero **no** `historico/enero.csv`: ese archivo está en otro nivel. Este pequeño resultado ayuda a distinguir listar de recorrer.

<div class="mjs-task"><input type="checkbox" id="b1-p4"><label for="b1-p4">He comprobado que `Files.list` solo muestra el primer nivel.</label></div>

## 5. Recorrer y seleccionar CSV { .mjs-step-heading }

`Files.walk(origen, 4)` sería suficiente para explorar un árbol pequeño. Prueba temporalmente ese stream y cuenta las rutas; recuerda que la raíz tiene profundidad cero. Para el inventario definitivo necesitamos continuar cuando falle una entrada. Cambia a `Files.walkFileTree`: `visitFile` recibe el archivo y sus atributos ya leídos; `visitFileFailed` recibe la ruta y el error. Prepara dos listas, una de filas y otra de incidencias, antes de iniciar el recorrido.

```java
List<ArchivoInventariado> filas = new ArrayList<>();
List<String> incidencias = new ArrayList<>();
String sufijo = "." + extension.toLowerCase(Locale.ROOT);
Files.walkFileTree(origen, EnumSet.noneOf(FileVisitOption.class), 4,
    new SimpleFileVisitor<>() {
        @Override public FileVisitResult visitFile(Path p, BasicFileAttributes a) {
            // En el paso siguiente registrarás aquí los archivos seleccionados.
            return FileVisitResult.CONTINUE;
        }
        @Override public FileVisitResult visitFileFailed(Path p, IOException e) {
            incidencias.add(p + ": " + e.getMessage());
            return FileVisitResult.CONTINUE;
        }
    });
```

`ArchivoInventariado` será un `record` propio con `Path relativa`, `long bytes` e `Instant modificado`. Los imports se completan con la ayuda del IDE: `java.nio.file.*`, `java.nio.file.attribute.BasicFileAttributes`, `java.util.*` y `java.time.Instant`. Añade `postVisitDirectory` si quieres registrar un error al terminar de leer una carpeta. No seguimos enlaces simbólicos.

Créalo en otro archivo del mismo paquete. Su declaración, sin lógica de recorrido, es `public record ArchivoInventariado(Path relativa, long bytes, Instant modificado) {}`. El compilador genera automáticamente el constructor y los accesores `relativa()`, `bytes()` y `modificado()`.

??? tip "Pista: un fallo al cerrar una carpeta"
    Sobrescribe `postVisitDirectory(Path p, IOException e)`; si `e != null`, agrégalo a `incidencias`, y devuelve `CONTINUE`. La raíz inexistente se valida antes y sigue siendo un error que detiene la ejecución.

<div class="mjs-task"><input type="checkbox" id="b1-p5"><label for="b1-p5">He distinguido `Files.list`, `Files.walk` y el visitante que puede registrar fallos.</label></div>

## 6. Guardar tamaño, fecha y ruta relativa { .mjs-step-heading }

Dentro de `visitFile`, comprueba `a.isRegularFile()` y compara **solo el nombre** con el sufijo en minúsculas. `a.size()` devuelve bytes; `a.lastModifiedTime().toInstant()` da la fecha. `origen.relativize(p)` evita que el informe dependa de una ruta absoluta del equipo.

```java
if (a.isRegularFile() && p.getFileName().toString()
        .toLowerCase(Locale.ROOT).endsWith(sufijo)) {
    filas.add(new ArchivoInventariado(origen.relativize(p),
            a.size(), a.lastModifiedTime().toInstant()));
}
```

Comprueba que se registran cuatro rutas, entre ellas `ventas.CSV` y `historico/2025/marzo.CsV`; ni `notas.txt` ni el directorio `vacio` son filas. Si falta el CSV más profundo, revisa cómo cuentas la profundidad desde la raíz. Valida que la extensión no esté vacía ni contenga separadores de ruta antes de formar `sufijo`.

<div class="mjs-task"><input type="checkbox" id="b1-p6"><label for="b1-p6">Obtengo cuatro CSV, con tamaños 4, 4, 6 y 8 bytes.</label></div>

## 7. Ordenar y calcular el resumen { .mjs-step-heading }

El sistema de ficheros no promete un orden de visita. Tras terminar el recorrido, ordena por el texto de la ruta relativa para que dos ejecuciones produzcan las mismas filas. Calcula la cantidad con `filas.size()` y suma los bytes con `mapToLong`. Para el mayor, define antes el desempate: entre tamaños iguales gana la ruta alfabéticamente menor.

```java
filas.sort(Comparator.comparing(f -> f.relativa().toString()));
long totalBytes = filas.stream().mapToLong(ArchivoInventariado::bytes).sum();
```

Imprime el total y el mayor. Para la muestra, son **4 archivos y 22 bytes**. Modifica temporalmente dos archivos para que empaten en tamaño y comprueba que la selección no cambie entre ejecuciones. Si no hay archivos coincidentes, el total es cero y el mayor debe mostrarse como «ninguno», nunca provocar un error por intentar acceder a una lista vacía.

??? tip "Pista: desempate sin depender de la visita"
    Usa `max` con un comparador de `bytes` y después uno de ruta **invertido**, o recorre la lista ya ordenada y sustituye el mayor solo cuando el tamaño sea estrictamente superior. Así el primer nombre conserva el empate.

<div class="mjs-task"><input type="checkbox" id="b1-p7"><label for="b1-p7">El orden y las estadísticas son estables, también con empates y sin coincidencias.</label></div>

## 8. Exportar el informe fuera de la entrada { .mjs-step-heading }

El destino será `trabajo.resolve("inventario.csv")`; como `trabajo` es una carpeta distinta de `origen`, el informe no puede incluirse a sí mismo. Valida esa separación con rutas absolutas normalizadas **antes** de recorrer. Para escribir CSV con comillas correctas si un nombre contiene coma, el starter ya incluye Apache Commons CSV como dependencia; no necesitas estudiar todavía todo el formato.

```java
Path informe = trabajo.resolve("inventario.csv");
if (informe.toAbsolutePath().normalize().startsWith(origen.toAbsolutePath().normalize())) {
    throw new IllegalArgumentException("El informe debe quedar fuera de la entrada");
}
try (var writer = Files.newBufferedWriter(informe, StandardCharsets.UTF_8);
     var csv = new CSVPrinter(writer, CSVFormat.RFC4180.builder()
             .setHeader("ruta", "bytes", "modificado").get())) {
    for (var fila : filas) csv.printRecord(fila.relativa(), fila.bytes(), fila.modificado());
}
```

El `try` cierra escritor e impresora. `CSVPrinter` escribe las tres columnas y se ocupa de las comillas; el bloque de CSV profundizará en sus reglas. Importa `java.nio.charset.StandardCharsets`, `org.apache.commons.csv.CSVFormat` y `CSVPrinter`. Comprueba que el archivo tiene cabecera y cuatro filas, y que volver a inventariar `entrada` sigue dando cuatro. La solución de referencia comprueba también rutas reales para evitar que un enlace simbólico esconda una salida dentro de la entrada.

<div class="mjs-task"><input type="checkbox" id="b1-p8"><label for="b1-p8">El CSV contiene cuatro filas, está fuera de entrada y no se inventaría a sí mismo.</label></div>

## 9. Informar de incidencias con claridad { .mjs-step-heading }

Muestra cada elemento de `incidencias` al terminar, sin tirar las filas válidas. Si no hay incidencias, muestra un mensaje breve. Prueba primero errores reproducibles: una carpeta de entrada inexistente, un destino que sea un archivo en vez de carpeta y un destino de copia ya ocupado. Para fallos de lectura durante el recorrido, los permisos dependen del sistema operativo; revisa con un depurador qué método del visitante recibiría el error. No llames «archivo inexistente» a cualquier `IOException`.

En una prueba más avanzada puedes borrar un archivo entre su descubrimiento y lectura: `walkFileTree` comunicará ese fallo mediante `visitFileFailed` cuando el proveedor no consiga obtener atributos. Un fallo de acceso a la raíz puede impedir comenzar y debe detenerse. Conserva el mensaje original junto a la ruta para poder diagnosticarlo.

<div class="mjs-task"><input type="checkbox" id="b1-p9"><label for="b1-p9">He probado entradas incorrectas y distingo un fallo total de una incidencia parcial.</label></div>

## 10. Retos de aplicación { .mjs-step-heading }

1. Ejecuta el mismo inventario con extensión `txt`. Razona qué filas y total esperas antes de abrir el informe; el filtro debe funcionar sin cambiar el código.
2. Cambia el archivo `historico/2025/marzo.CsV` para que también tenga 4 bytes. Ahora hay varios máximos empatados: predice cuál gana con la regla alfabética y compruébalo.
3. Usa una carpeta de entrada vacía recién creada. Produce un informe con cabecera y cero filas, total cero y «ninguno» como mayor; después añade un CSV dentro de un quinto nivel y comprueba que sigue fuera del límite de profundidad.

Estos retos exigen combinar selección, rutas relativas y estadísticas. Prueba cada uno con una entrada que distinga un resultado correcto de uno accidental. Consulta las clases de referencia **después** de intentar tu diseño; no hay un fragmento de respuesta inmediata bajo los retos.

<div class="mjs-task"><input type="checkbox" id="b1-p10"><label for="b1-p10">He probado al menos un reto con datos que permitan detectar un error.</label></div>

## 11. Contrastar con la solución ejecutable { .mjs-step-heading }

En **otro proyecto**, abre `proyectos/ficheros/solucion`. El primer punto de entrada es [`cli.OrganizarDirectorios`](https://github.com/MJSPlayground/fp-dam2-acda/blob/main/proyectos/ficheros/solucion/src/main/java/es/mjsplayground/acda/ficheros/cli/OrganizarDirectorios.java): recibe entrada y trabajo; añade `--reemplazar` si autorizas sustituir la copia. El segundo es [`cli.Inventariar`](https://github.com/MJSPlayground/fp-dam2-acda/blob/main/proyectos/ficheros/solucion/src/main/java/es/mjsplayground/acda/ficheros/cli/Inventariar.java): recibe entrada, extensión y archivo de salida. Este llama a [`InventarioArchivosService`](https://github.com/MJSPlayground/fp-dam2-acda/blob/main/proyectos/ficheros/solucion/src/main/java/es/mjsplayground/acda/ficheros/model/service/InventarioArchivosService.java) y utiliza [`ArchivoInventariado`](https://github.com/MJSPlayground/fp-dam2-acda/blob/main/proyectos/ficheros/solucion/src/main/java/es/mjsplayground/acda/ficheros/model/entity/ArchivoInventariado.java).

```powershell
.\mvnw.cmd test
.\mvnw.cmd -q exec:java '-Dexec.mainClass=es.mjsplayground.acda.ficheros.cli.OrganizarDirectorios' '-Dexec.args=../codelab-inicial/trabajo-codelab/entrada ../codelab-inicial/trabajo-codelab/referencia'
.\mvnw.cmd -q exec:java '-Dexec.mainClass=es.mjsplayground.acda.ficheros.cli.Inventariar' '-Dexec.args=../codelab-inicial/trabajo-codelab/entrada csv ../codelab-inicial/trabajo-codelab/referencia/inventario.csv'
```

Compara cantidad, suma, mayor, orden y contenido del CSV con tu programa. La [guía de ejecución de la solución](https://github.com/MJSPlayground/fp-dam2-acda/blob/main/proyectos/ficheros/solucion/README.md) explica el laboratorio común. Los proyectos están separados: el inicial no es una copia oculta de las clases resueltas.

<div class="mjs-task"><input type="checkbox" id="b1-p11"><label for="b1-p11">He ejecutado las dos entradas de referencia y comparado resultados con mi programa.</label></div>

<div class="mjs-resources" markdown="1">
[Ver solución completa y cómo ejecutarla](https://github.com/MJSPlayground/fp-dam2-acda/blob/main/proyectos/ficheros/solucion/README.md){ .md-button .md-button--primary }
[Código de organización](https://github.com/MJSPlayground/fp-dam2-acda/blob/main/proyectos/ficheros/solucion/src/main/java/es/mjsplayground/acda/ficheros/cli/OrganizarDirectorios.java){ .md-button }
[Código del inventario](https://github.com/MJSPlayground/fp-dam2-acda/blob/main/proyectos/ficheros/solucion/src/main/java/es/mjsplayground/acda/ficheros/cli/Inventariar.java){ .md-button }
</div>
