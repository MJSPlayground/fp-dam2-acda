# Práctica de bloque: inventario de archivos

Una productora necesita un inventario de su carpeta de entregas. Recibe una carpeta raíz y una extensión elegida por el usuario. La herramienta debe recorrer hasta cuatro niveles, seleccionar solo archivos normales con esa extensión sin distinguir mayúsculas, ordenar las rutas relativas y generar `inventario.csv` fuera de la carpeta examinada.

Cada fila del informe contiene ruta relativa, tamaño en bytes y fecha de modificación. Al final se muestran número de archivos, suma de tamaños y el archivo más grande; en empate gana la ruta alfabéticamente menor. Un subdirectorio vacío no cuenta. Si un archivo desaparece durante el recorrido, informa de la incidencia sin perder los datos ya obtenidos. Rechaza una raíz inexistente o que no sea directorio.

Prueba con dos archivos `.csv` de distinto tamaño, uno `.CSV`, un `.txt`, un subdirectorio y dos archivos empatados. La salida debe ser estable entre ejecuciones y no incluirse a sí misma.

[Solución pública](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/ficheros/solucion).
