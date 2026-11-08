# Conexiones a bases de datos relacionales (JDBC)

JDBC permite controlar directamente las conexiones y las instrucciones SQL desde Java. Aprenderás a implementar repositorios propios, recuperar resultados y mantener operaciones consistentes.

## Antes de empezar

Revisa tablas, claves y consultas SQL sencillas. Descarga el [almacén inicial](proyecto.md) y prepara [Maven e IntelliJ](../ayuda/proyectos-maven.md). La base principal es un archivo SQLite: puedes empezar sin Docker ni MySQL. El ejemplo MySQL sirve para comparar una base de servidor.

## Recorrido

1. [Conectores y conexiones](conexiones.md): JDBC frente a ORM, SQLite y MySQL, apertura y cierre.
2. [SQL y resultados](consultas.md): sentencias, filas, parámetros y operaciones CRUD.
3. [Repositorios y servicios](repositorios.md): separar consultas, validaciones y diagnóstico.
4. [Transacciones](transacciones.md): vender sin dejar operaciones a medias.

El [proyecto guiado de almacén y ventas](proyecto.md) avanza con estas páginas sobre una única aplicación JavaFX ya preparada. Descarga el starter antes de comenzar; cada página indica la etapa correspondiente.

Después encontrarás una sola [práctica final de operaciones de almacén](practica-final.md), con niveles Base, Aplicación y Reto avanzado; [cinco fichas de repaso en preparación](repaso.md); y [Ampliación](ampliacion.md) sobre metadatos, JOIN, procedimientos, pool, lotes, índices y concurrencia.

En [Ayuda y herramientas](../ayuda/index.md) encontrarás la preparación general de Java y bases de datos.

Continúa con [Bases de datos documentales con Cloud Firestore](../firestore/index.md).
