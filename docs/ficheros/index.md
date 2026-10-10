# Manejo de ficheros y directorios

Una aplicación necesita conservar información entre ejecuciones, intercambiarla con otras herramientas y recuperarse cuando un archivo está incompleto o dañado. Este tema comienza con rutas y directorios; después estudia los formatos de texto, CSV, XML, JSON y binario. Termina con una arquitectura de importación y exportación que permite combinar esos formatos sin mezclar las reglas comerciales con los detalles de lectura.

Las prácticas guiadas acompañan la primera aplicación de cada técnica. Las prácticas de bloque proponen resolver un problema nuevo de forma autónoma. Al final, tres encargos integran los conocimientos con dificultad creciente. Las [ampliaciones](ampliacion-streaming.md) estudian archivos grandes, cambios en directorios, canales y escritura fiable.

## Recorrido recomendado

1. [Rutas y sistemas de ficheros](rutas.md) y [gestión y recorrido](gestion-recorrido.md): localizar, crear y explorar datos sin depender del equipo del profesor.
2. [Texto](texto.md) y [CSV](csv.md): codificación, lectura gradual y registros tabulares con comillas y campos vacíos.
3. [XML](xml.md) y [JSON](json.md): representar estructuras y convertirlas con analizadores adecuados.
4. [Binarios](binarios.md): conservar tipos compactos, actualizar registros de posición fija y comprender la serialización Java.
5. [Transformación](transformacion.md): separar importación, validación, cálculo y exportación antes de abordar las [prácticas finales](final-clientes.md).

Los proyectos usan JDK 25 y Maven Wrapper. Consulta [cómo preparar y ejecutar un proyecto](../ayuda/index.md) antes de abrirlos. Los datos de muestra del classpath son de lectura; el programa guarda sus resultados en una carpeta externa que se indica en cada actividad.

La [documentación de `java.nio.file`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/package-summary.html) sirve de referencia mientras estudias los dos primeros apartados. Las páginas posteriores enlazan las APIs específicas cerca de su explicación.
