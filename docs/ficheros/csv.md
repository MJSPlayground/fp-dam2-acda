# Ficheros CSV

CSV representa registros tabulares en texto. Suele tener una primera fila de **encabezados**, un delimitador como coma o punto y coma y una fila por registro. No hay una única variante universal: el productor y el consumidor deben acordar delimitador, comillas, codificación y política para valores vacíos. Un campo `"Pamplona, centro"` contiene una coma que **no** separa columnas; `"Dijo ""hola"""` representa una comilla interna. Por eso `linea.split(",")` pierde información.

La [especificación RFC 4180](https://www.rfc-editor.org/rfc/rfc4180) describe una variante común. Para los proyectos utilizamos [Apache Commons CSV](https://commons.apache.org/proper/commons-csv/), cuya [`CSVFormat`](https://commons.apache.org/proper/commons-csv/apidocs/org/apache/commons/csv/CSVFormat.html) configura el dialecto y cuyo `CSVParser` devuelve `CSVRecord` por registro lógico, aunque haya saltos dentro de una celda entrecomillada.

## Encabezados y campos vacíos

El siguiente fragmento ilustrativo lee `id,nombre,ciudad` desde la primera fila. `setHeader()` sin argumentos toma los nombres del archivo; `setSkipHeaderRecord(true)` impide que se procese esa fila como dato. `record.get("ciudad")` usa el encabezado, sin depender de la posición de la columna.

```java
CSVFormat formato = CSVFormat.RFC4180.builder()
        .setHeader().setSkipHeaderRecord(true).get();
try (Reader lector = Files.newBufferedReader(entrada, StandardCharsets.UTF_8);
     CSVParser parser = formato.parse(lector)) {
    for (CSVRecord record : parser) {
        String ciudad = record.get("ciudad");
        System.out.println(record.getRecordNumber() + ": " + ciudad);
    }
}
```

Un campo vacío se lee como cadena vacía y **no** equivale automáticamente a `null` ni a cero. La regla comercial decide si se acepta. Antes de acceder, comprueba que los encabezados necesarios existen y que el registro tiene la cantidad esperada de columnas; después valida el contenido (por ejemplo, identificador no vacío y cantidad entera no negativa). El número de registro del parser ayuda a explicar el error. Un fallo de estructura puede impedir continuar; un valor inválido de un registro bien formado permite apartarlo y seguir.

## Exportar de forma simétrica

[`CSVPrinter`](https://commons.apache.org/proper/commons-csv/apidocs/org/apache/commons/csv/CSVPrinter.html) aplica el dialecto al escribir. `printRecord` coloca comillas cuando un valor incluye delimitador, comilla o salto, y evita concatenar manualmente columnas. Si el receptor espera `;`, configura `setDelimiter(';')` tanto en importación como en exportación.

```java
CSVFormat salidaCsv = CSVFormat.RFC4180.builder()
        .setHeader("id", "nombre", "ciudad").get();
try (Writer escritor = Files.newBufferedWriter(destino, StandardCharsets.UTF_8);
     CSVPrinter csv = new CSVPrinter(escritor, salidaCsv)) {
    csv.printRecord("C01", "Ana", "Pamplona, centro");
}
```

El resultado conserva tres columnas; la ciudad aparece entre comillas. `CSVPrinter` no valida que `C01` exista ni que la ciudad sea admisible: son reglas de servicio. Aprende el ciclo completo en la [guiada](guiada-csv.md) y aplícalo al [fusionar archivos](practica-fusion-csv.md).
