# Manejo de ficheros y directorios

Los ficheros permiten conservar datos fuera de la ejecución de una aplicación. Aquí aprenderás a gestionar rutas y directorios, leer y escribir información y convertirla entre formatos.

## Recorrido

El tema comienza con **ficheros y directorios en Java**. Después trabaja **ficheros de texto y CSV**, **XML**, **JSON** y **ficheros binarios**. Termina con **transformación de formatos y aplicación**, prácticas finales y **Ampliación**.

Antes de empezar, prepara el [entorno Java](../ayuda/index.md). Los contenidos detallados de cada bloque se incorporarán progresivamente.

Una ruta identifica dónde buscar un archivo. Este fragmento construye una ruta relativa; todavía no abre el archivo:

```java
Path archivo = Path.of("datos", "clientes.csv"); // (1)!
```

1. `Path.of` une los segmentos en una ruta relativa al directorio de trabajo del proceso. La lectura y el tratamiento de errores se explicarán en el primer bloque.

El siguiente tema es [Herramientas de mapeo objeto-relacional (ORM) y JPA con Hibernate](../hibernate/index.md).
