# Guiada: organizar y explorar directorios

Una oficina recibe documentos en una carpeta `entrada` y necesita preparar `trabajo/copias` y conocer los CSV recibidos. El objetivo es construir una pequeña herramienta que cree la salida, copie un archivo sin sobrescribir y liste los CSV con su tamaño.

## Preparación

Crea una carpeta externa `entrada` con `clientes.csv`, `notas.txt` y un subdirectorio `historico` que contenga otro CSV. Abre un proyecto Maven con el [entorno preparado](../ayuda/index.md). En esta guía los fragmentos se incorporan a un `main` que declara `throws IOException`; en una aplicación para usuarios se capturaría el error para mostrar un mensaje.

1. Define `Path origen = Path.of(args[0]);` y `Path trabajo = Path.of(args[1]);`. Ejecuta con dos argumentos externos; muestra `toAbsolutePath()` para identificar el directorio de trabajo si una ruta relativa no se resuelve donde esperabas.
2. Construye `trabajo.resolve("copias")` y usa `Files.createDirectories`. Después copia `origen.resolve("clientes.csv")` al destino **sin** `REPLACE_EXISTING`. ¿Qué excepción se obtiene al repetir la operación? ¿Por qué es preferible en este caso a sobrescribir sin aviso?
3. Recorre `origen` con `Files.walk(origen, 3)` dentro de `try-with-resources`. Filtra archivos normales cuyo nombre termina en `.csv`; imprime `origen.relativize(ruta)` y `Files.size(ruta)`. Ordena la salida para que dos ejecuciones sean comparables.

```java
try (Stream<Path> rutas = Files.walk(origen, 3)) {
    for (Path ruta : rutas.filter(Files::isRegularFile).sorted().toList()) {
        if (ruta.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            System.out.printf("%s: %d bytes%n", origen.relativize(ruta), Files.size(ruta));
        }
    }
}
```

El listado debe incluir `clientes.csv` y `historico/…csv`, pero no `notas.txt`. El `try` cierra el recorrido. Si la carpeta inicial no existe, la operación lanza `NoSuchFileException`; lee su ruta en la traza antes de modificar código.

??? tip "Comprobación del segundo paso"
    `Files.copy` lanza `FileAlreadyExistsException` al repetirlo. Borra la copia o elige un nombre nuevo para volver a ejecutar; no añadas reemplazo hasta que esa sea una decisión funcional explícita.

El [código de referencia](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/ficheros/solucion) incorpora la herramienta y sus datos. En el [inventario autónomo](practica-inventario.md) tendrás que añadir reglas de selección y estadísticas sin pasos detallados.
