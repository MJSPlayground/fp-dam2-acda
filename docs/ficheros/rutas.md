# Sistemas de ficheros y rutas

Un sistema de ficheros organiza archivos en directorios y decide cómo se nombran, protegen y almacenan. Java no necesita conocer todos sus detalles para trabajar con ellos: [`Path`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Path.html) representa una ubicación y [`Files`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Files.html) realiza operaciones sobre esa ubicación. Un `Path` por sí solo no crea ni abre nada.

## De un nombre a una ubicación

La ruta `datos/clientes.csv` es **relativa**: comienza en el directorio de trabajo del proceso. En IntelliJ, ese directorio se configura en la ejecución y puede no coincidir con `src/main/resources`. Una ruta como `C:\datos\clientes.csv` es absoluta en Windows, pero no será válida en Linux. Por eso conviene recibir del usuario o de una opción de configuración la carpeta de datos modificables.

Este fragmento ilustrativo construye una ruta relativa y muestra dónde se resolvería. `resolve` añade un segmento; `toAbsolutePath` usa el directorio de trabajo actual, sin comprobar que el archivo exista.

```java
Path carpeta = Path.of("salida"); // (1)!
Path informe = carpeta.resolve("informe.txt");
System.out.println(informe.toAbsolutePath().normalize());
```

1. `Path.of` construye una ruta; no comprueba existencia ni abre un archivo.

El resultado es una ruta absoluta terminada en `salida/informe.txt`. `normalize` elimina componentes léxicos redundantes como `.` y `..`; no sigue enlaces simbólicos ni valida permisos. [`toRealPath`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/Path.html#toRealPath(java.nio.file.LinkOption...)) sí consulta el sistema de ficheros y puede lanzar `IOException` si la ubicación no se puede resolver.

Para localizar el punto de partida de una ruta relativa, `Path.of("").toAbsolutePath()` muestra el directorio de trabajo. El separador visual depende del sistema operativo. `Path.of("datos", "clientes.csv")` deja que Java coloque el separador adecuado.

## Ruta externa y recurso empaquetado

Un archivo de ejemplo en `src/main/resources/data/clientes.csv` termina dentro del JAR. Se abre como recurso del **classpath** mediante `getResourceAsStream("/data/clientes.csv")`. La ruta lógica `/data/clientes.csv` no es una ubicación editable del disco. Para guardar cambios, elige una ruta externa configurada, por ejemplo `Path.of(args[0]).resolve("clientes.csv")`.

```java
try (InputStream entrada = Ejemplo.class.getResourceAsStream("/data/clientes.csv")) {
    if (entrada == null) throw new FileNotFoundException("Falta /data/clientes.csv en el proyecto");
    // La lectura se desarrolla en el bloque siguiente.
}
```

El fragmento ilustra la distinción, no constituye un programa completo. [`Class.getResourceAsStream`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Class.html#getResourceAsStream(java.lang.String)) devuelve `null` cuando no encuentra el recurso; por eso se comprueba antes de usar el flujo.

## Permisos y fallos

Un proceso puede ver un nombre y aun así no poder leerlo, modificarlo o atravesar su directorio. Los permisos varían por sistema y pueden cambiar entre dos instrucciones. `Files.exists(ruta)` puede ayudar a mostrar información, pero **no evita** que la lectura posterior falle: ambas operaciones están separadas en el tiempo. La lectura y escritura deben tratar `IOException`, en especial `NoSuchFileException` y `AccessDeniedException`, sin convertir cualquier fallo en «el archivo no existe».

En el [siguiente apartado](gestion-recorrido.md) usarás la ruta para operar y recorrer directorios.
