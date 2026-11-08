# Texto, CSV y codificación

Un archivo contiene bytes. Para interpretar texto debemos acordar una **codificación**, que relaciona esos bytes con caracteres. Utilizaremos UTF-8 para conservar letras acentuadas y otros caracteres de los nombres del catálogo.

No es necesario aprender un bloque separado de lectura completa de cadenas: comenzaremos con las operaciones que necesita el procesamiento por líneas y después utilizaremos una biblioteca para los registros CSV.

## Leer líneas con BufferedReader

`Files.newBufferedReader` abre un lector de texto. [`BufferedReader`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/io/BufferedReader.html) ofrece `readLine()`: devuelve el contenido de la siguiente línea sin su salto final, o `null` cuando termina el archivo.

Este fragmento ilustrativo cuenta líneas no vacías de un archivo de notas; supone que `origen` es un `Path` y se ejecuta dentro de un método que trata `IOException`:

```java
int lineasConContenido = 0;
try (BufferedReader lector = Files.newBufferedReader(origen, StandardCharsets.UTF_8)) {
    String linea;
    while ((linea = lector.readLine()) != null) {
        if (!linea.isBlank()) lineasConContenido++;
    }
}
```

Una línea vacía produce `""`, no `null`. Por eso la comprobación de fin no debe ser `linea.isEmpty()`. El contador es una variable en memoria; el fragmento no modifica el archivo.

`try-with-resources` cierra el lector al salir, incluso si hay una excepción. No devuelve un lector abierto para que otra capa lo cierre sin saber quién es su propietario. Si falta el archivo o se deniega el acceso, el error debe comunicarse, no convertirse en un resultado cero engañoso.

## Escribir líneas con BufferedWriter

[`BufferedWriter`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/io/BufferedWriter.html) escribe caracteres y permite añadir un salto de línea mediante `newLine()`. Esta demostración crea un archivo nuevo; `CREATE_NEW` rechaza que ya exista:

```java
try (BufferedWriter escritor = Files.newBufferedWriter(
        destino, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
    escritor.write("Productos revisados");
    escritor.newLine();
    escritor.write("Pendiente: comprobar existencias");
    escritor.newLine();
}
```

El directorio padre debe existir. Cerrar el escritor entrega el contenido pendiente y libera recursos; `flush` no significa por sí solo que exista una copia durable frente a cualquier fallo físico. En el proyecto, un ayudante de escritura evita truncar el destino anterior durante la generación. Su técnica se explica en [Ampliación](ampliacion.md).

## CSV: registros, columnas y comillas

CSV representa información tabular en texto. El proyecto utiliza coma como delimitador y exige estos cuatro encabezados, en ese orden:

```csv
codigo,nombre,unidades,precio
P01,"Taza, blanca",3,4.50
P02,"Carpeta ""Azul""",10,2.75
```

La primera fila identifica las columnas. La coma dentro de `"Taza, blanca"` pertenece al nombre; las comillas dobles repetidas representan una comilla dentro del campo. Un campo entrecomillado también puede contener un salto de línea. Por eso **una línea física no equivale siempre a un registro CSV**.

`split(",")` no interpreta esas reglas y no sirve como analizador de este formato. La [RFC 4180](https://www.rfc-editor.org/rfc/rfc4180) describe la variante utilizada como referencia. Otros productores pueden usar punto y coma: el dialecto debe acordarse, no adivinarse silenciosamente.

## Interpretar registros con Commons CSV

Apache Commons CSV se incluye como dependencia Maven del proyecto. `CSVFormat` define el dialecto; `CSVParser` produce registros lógicos; cada `CSVRecord` permite acceder por encabezado.

```java
CSVFormat formato = CSVFormat.RFC4180.builder()
    .setHeader().setSkipHeaderRecord(true).get();
try (Reader lector = Files.newBufferedReader(origen, StandardCharsets.UTF_8);
     CSVParser parser = formato.parse(lector)) {
    if (!parser.getHeaderNames()
            .equals(List.of("codigo", "nombre", "unidades", "precio"))) {
        throw new IOException("Los encabezados no corresponden al catálogo");
    }
    for (CSVRecord fila : parser) {
        String codigo = fila.get("codigo");
        String nombre = fila.get("nombre");
        int unidades = Integer.parseInt(fila.get("unidades"));
        BigDecimal precio = new BigDecimal(fila.get("precio"));
        // Construir un producto candidato; el servicio validará el catálogo.
    }
}
```

El fragmento muestra el recorrido; el código completo también comprueba estructura y convierte errores con contexto. `setHeader()` sin argumentos utiliza la primera fila como encabezado; `setSkipHeaderRecord(true)` evita procesarla como producto. La [API de Commons CSV](https://commons.apache.org/proper/commons-csv/apidocs/index.html) documenta las clases utilizadas.

`Integer.parseInt` rechaza texto no entero y valores que no caben. Un campo vacío no equivale automáticamente a cero. `BigDecimal` construye un importe decimal desde texto; no se pasa primero por `double`. El número de registro del parser ayuda a localizar el error, aunque un registro ocupe varias líneas físicas.

## Separar conversión y reglas de producto

El repositorio interpreta la estructura técnica: encabezados, columnas y tipos. El servicio comprueba el significado:

- Código único de 1 a 20 caracteres, con letras mayúsculas, dígitos, guion o guion bajo.
- Nombre con contenido, de hasta 100 caracteres.
- Unidades enteras entre 0 y 1.000.000.
- Precio entre 0,01 y 1.000.000 euros, con un máximo de dos decimales.

El nombre puede contener comas, comillas y saltos: son contenido válido y el formato debe conservarlos. Un `-1` puede convertirse a entero, pero incumple la regla de existencias. Un identificador duplicado requiere comparar productos, no solo leer correctamente una fila.

En el guiado, cargar un archivo sustituye el catálogo completo **solo cuando todo es válido**. Leer tres productos y fallar en el cuarto no debe dejar una sustitución parcial. La [práctica final](practica-final.md) define una operación diferente para conciliar filas con incidencias.

## Exportar sin concatenar columnas

`CSVPrinter` escribe los campos y añade las comillas necesarias. Este ejemplo conserva el nombre con coma y el importe decimal:

```java
CSVFormat formato = CSVFormat.RFC4180.builder()
    .setHeader("codigo", "nombre", "unidades", "precio").get();
try (Writer escritor = Files.newBufferedWriter(destino,
        StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
     CSVPrinter csv = new CSVPrinter(escritor, formato)) {
    csv.printRecord("P01", "Taza, blanca", 3, "4.50");
}
```

El resultado contiene cuatro columnas, no cinco. Al exportar la lista completa se repite `printRecord` para cada producto, sin escribir manualmente comas entre sus campos.

## Comprobaciones del proyecto

Completa el **TODO 2** de lectura y escritura CSV. Prueba el recorrido guardar → cargar y compara todos los campos. Incluye un nombre con coma y comillas, unidades cero y una fila inválida al final de un archivo.

Comprueba también encabezado ausente, número no entero, precio inválido y código duplicado. En todos los errores de carga del guiado debe conservarse el catálogo anterior. Continúa con [JSON y Jackson](json.md).

## Del registro leído al catálogo aceptado

Trabaja primero con `CsvProductoRepository.leer`, sin depender del formulario: la fase `csv-lectura` de `FasesGuiadoTest` entrega una fila cuyo nombre contiene coma, comillas y un salto. Espera un solo producto con el texto íntegro. Después implementa `guardar` y ejecuta `csv-escritura`; el test comprueba el archivo con Commons CSV, independientemente de tu lector.

Solo después conecta la lectura con `InventarioService.cargar`. La cadena `-1` es un entero que el repositorio puede convertir, pero no un stock admisible: el servicio debe rechazarlo sin sustituir el catálogo anterior. La fase `carga` proporciona una fila correcta seguida de otra inválida para comprobar que no se importó parcialmente. [La etapa 2 del guiado](proyecto.md#etapa-2-primer-recorrido-completo-con-csv) divide lectura, escritura, mantenimiento y carga en hitos separados.
