# Guiada: importar CSV y generar un informe

Un centro registra solicitudes en `id,nombre,ciudad`. Se quiere contar las válidas y producir un informe legible, conservando las incidencias por separado. La guía utiliza la configuración de [Commons CSV](csv.md) y la lectura UTF-8 de [texto](texto.md).

## Datos de partida

Prepara un CSV con `C01,"Ana, María",Pamplona`, `C02,Luis,` y una fila con `id` vacío. La primera fila es el encabezado. La coma dentro del nombre y la ciudad vacía prueban dos casos que `split(",")` interpreta mal.

1. Abre el fichero mediante `Files.newBufferedReader(ruta, UTF_8)` y crea un `CSVParser` con encabezados leídos del archivo. Comprueba que existen `id`, `nombre` y `ciudad`; si falta alguno, detén la importación con un mensaje que cite el nombre del encabezado.
2. Recorre `CSVRecord`. Valida que el registro tiene tres campos, que `id` y `nombre` no están vacíos y que la ciudad puede quedar vacía. Acumula las filas válidas y una incidencia `registro N: motivo` para cada rechazo. ¿Se conserva `Ana, María` como un único nombre?
3. Crea la carpeta externa de salida y usa `BufferedWriter` para escribir `Válidos: N`, seguido de los nombres aceptados, y `Rechazados: M`. Cierra el escritor antes de abrir el informe para verificarlo.

```java
CSVFormat formato = CSVFormat.RFC4180.builder()
        .setHeader().setSkipHeaderRecord(true).get();
try (Reader lector = Files.newBufferedReader(entrada, StandardCharsets.UTF_8);
     CSVParser parser = formato.parse(lector)) {
    if (!parser.getHeaderMap().keySet().containsAll(Set.of("id", "nombre", "ciudad"))) {
        throw new IllegalArgumentException("Faltan encabezados id, nombre o ciudad");
    }
    // Continúa con la validación de cada CSVRecord.
}
```

El fragmento muestra la preparación, no una clase completa. Si un registro falla por una regla de dominio, el siguiente aún se puede procesar. Una comilla sin cerrar puede impedir recuperar la estructura: informa del error de parseo sin declarar que todas las filas posteriores se importaron.

??? tip "Pista para registrar incidencias"
    Usa `record.getRecordNumber()` en el mensaje y conserva el valor original problemático cuando pueda mostrarse con seguridad. No sustituyas un dato ausente por uno inventado.

Comprueba que el informe cuenta dos aceptados y un rechazado, y que mantiene la coma del nombre. La [solución de referencia](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/ficheros/solucion) permite contrastar la salida. Después aborda la [fusión autónoma](practica-fusion-csv.md).
