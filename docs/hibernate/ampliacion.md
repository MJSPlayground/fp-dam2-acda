# Ampliación

El recorrido ordinario utiliza entidades, transacciones y HQL sencillo. Aquí profundizamos en consultas sobre relaciones y en la observación de lo que ejecuta Hibernate, con demostraciones incluidas en la solución del mismo proyecto.

## JOIN sobre asociaciones

Un `join` recorre una asociación mapeada. Para obtener parejas de título y autor:

```java
List<Object[]> filas = sesion.createSelectionQuery(
    "select l.titulo, a.nombre from Libro l join l.autores a order by l.titulo, a.nombre",
    Object[].class).getResultList();
```

Cada fila tiene dos valores y no representa un libro completo. Si un libro tiene dos autores, aparecen dos parejas. Un join interno conserva coincidencias; un `left join` permite conservar también la entidad de la izquierda cuando no existe la relación opcional.

En HQL se utiliza `l.autores`, no una tabla inventada con ese nombre. El proveedor conoce `libro_autor` por el mapeo. La [guía de joins HQL](https://docs.hibernate.org/orm/current/querylanguage/html_single/#explicit-association-joins) describe estas asociaciones.

## Proyecciones: recuperar solo lo necesario

Una proyección selecciona propiedades concretas. Puede ser adecuada para una lista que solo muestra título y precio:

```java
List<Object[]> filas = sesion.createSelectionQuery(
    "select l.titulo, l.precio from Libro l order by l.titulo, l.id",
    Object[].class).getResultList();
```

El array contiene un `String` y un `BigDecimal`. La demostración transforma esos valores en un record de lectura `Resumen`; no intenta persistir ese resumen como otra entidad.

```java
Resumen resumen = new Resumen((String) fila[0], (BigDecimal) fila[1]);
```

Las conversiones corresponden a los tipos seleccionados. Una consulta que cambie el orden o el tipo de sus expresiones debe cambiar también su representación de salida. Para proyecciones más amplias existen alternativas tipadas y expresiones constructoras; no es necesario añadirlas todas al primer listado.

## Agregaciones y agrupación

`count`, `sum`, `avg`, `min` y `max` calculan resultados sobre conjuntos. Esta consulta agrupa libros por editorial:

```java
List<Object[]> filas = sesion.createSelectionQuery(
    "select e.nombre, count(l), sum(l.precio) from Libro l join l.editorial e "
        + "group by e.id, e.nombre order by e.nombre",
    Object[].class).getResultList();
```

Cada fila contiene nombre, número de libros e importe sumado. `group by` establece la agrupación; no ordena el resultado. Al partir de libros, esta consulta solo produce grupos de editoriales que tienen libros.

`sum` sobre importes mantiene un tipo decimal apropiado. Hay que tratar los resultados vacíos o nulos de cada agregación según su contrato; no convertir todo a `double` para volver a dar formato. La [referencia de agregaciones HQL](https://docs.hibernate.org/orm/current/querylanguage/html_single/#aggregate-functions) desarrolla estos casos.

## Paginación con orden explícito

Para recuperar una parte del listado utilizamos desplazamiento y máximo de resultados:

```java
List<String> titulos = sesion.createSelectionQuery(
    "select l.titulo from Libro l order by l.titulo, l.id", String.class)
    .setFirstResult(0)
    .setMaxResults(5)
    .getResultList();
```

`setFirstResult` comienza en cero y `setMaxResults` limita cuántos valores devuelve. El orden incluye un desempate por ID. La demostración valida página y tamaño antes de calcular el desplazamiento.

Un listado por páginas no representa necesariamente una instantánea compartida entre peticiones: otro usuario podría cambiar datos entre ellas. Tampoco conviene combinar un límite con un fetch de colección suponiendo que cada fila SQL corresponde a un libro: un libro con varios autores produce varias filas. Para escenarios más grandes se diseña la consulta y su estrategia de paginación de forma expresa.

## SQL nativo

Hibernate también permite ejecutar SQL del motor. Esta consulta utiliza el nombre real de tabla, no el de entidad:

```java
Long cantidad = sesion.createNativeQuery("select count(*) from libro", Long.class)
    .getSingleResult();
```

La API sigue gestionando la sesión, pero el texto pertenece a SQL. Una función específica de MySQL puede limitar la portabilidad. Los valores externos siguen utilizándose como parámetros; no se concatenan para formar instrucciones.

Una consulta nativa no es automáticamente más rápida. Hay que comprobar su resultado y su plan, y conocer sus efectos sobre el contexto de persistencia si modifica datos. Aquí la demostración es de lectura y no altera el catálogo.

## N+1: una navegación que genera consultas adicionales

Una consulta obtiene N libros. Después, al recorrerlos, el programa necesita sus autores y editoriales. Si esas asociaciones están pendientes, puede ejecutar consultas adicionales para cada entidad o colección. El número concreto depende del mapeo, identidades compartidas y cachés, pero el patrón puede crecer con el listado.

No se detecta solo mirando la cantidad de métodos Java. Hay que observar el SQL y las estadísticas. `ConsultasDemo.medirCarga` abre una sesión nueva, recupera libros y accede a las mismas asociaciones en ambas variantes para comparar el mismo trabajo.

## JOIN FETCH: preparar datos en la consulta

`join fetch` indica que una asociación debe cargarse junto a la consulta. Esta variante incluye relaciones opcionales con `left join fetch`:

```java
List<Libro> libros = sesion.createSelectionQuery(
    "select distinct l from Libro l join fetch l.editorial "
        + "left join fetch l.autores left join fetch l.detalle order by l.titulo, l.id",
    Libro.class).getResultList();
```

El fetch no cambia la cardinalidad del modelo ni hace que el detalle deje de ser opcional. La consulta debe conservar los libros esperados y preparar las asociaciones que realmente necesita la lectura.

Cargar todas las relaciones en cualquier operación también tiene costes: multiplica filas y memoria y puede ser innecesario para una proyección simple. La solución se elige según la pantalla y la consulta, sin convertir todas las anotaciones en EAGER para silenciar un fallo.

## Medir sentencias y diagnosticar

Hibernate permite habilitar estadísticas y consultar `getPrepareStatementCount`. La demostración reinicia sus contadores y realiza una comparación secuencial en una `SessionFactory` dedicada. No mezcla el tráfico de una UI abierta con la prueba.

`AmpliacionTest` comprueba que el conjunto de libros permanece y que el fetch reduce las sentencias para sus datos preparados. Ese resultado es una observación concreta, no una promesa de velocidad en cualquier base. No se utiliza un cronómetro sobre dos filas para afirmar una optimización general.

El SQL se consulta con el logging de Hibernate. Los parámetros pueden resultar útiles en un entorno de diagnóstico, pero los registros deben evitar datos sensibles y no sustituir la explicación de qué operación se ejecutó.

## Demostración breve de mapeo XML

`demo.NotaXml` no tiene anotaciones de entidad. `demo/nota-orm.xml` describe su identidad, tabla y columna, y `demo.XmlDemo` registra ese recurso en una configuración propia.

Puedes ejecutar `demo.XmlDemo.main` desde IntelliJ con MySQL disponible. Crea una nota de demostración en la tabla `demo_notas_xml` y la recupera desde otra sesión. No crea otra aplicación de biblioteca ni exige sustituir los mapeos por anotaciones del guiado.

[Ver clase y ejecución XML](https://github.com/MJSPlayground/fp-dam2-acda/blob/main/proyectos/hibernate/guiado-solucion/src/main/java/es/mjsplayground/acda/hibernate/biblioteca/demo/XmlDemo.java) · [Ver el recurso de mapeo](https://github.com/MJSPlayground/fp-dam2-acda/blob/main/proyectos/hibernate/guiado-solucion/src/main/resources/demo/nota-orm.xml)

## Ejecutar los ejemplos de consulta

En la solución, ejecuta `demo.ConsultasDemo.main` desde IntelliJ. Lee el catálogo existente, muestra joins, proyecciones, totales, una página y el recuento nativo, y registra la comparación de carga. No siembra datos ni modifica los libros; puedes preparar la demostración desde la interfaz en una base vacía.

Los métodos `fromTransaction` utilizados por esas clases son ayudantes del proveedor que abren, resuelven y cierran la transacción del bloque. Equivalen en propósito al patrón explícito explicado en [sesiones y transacciones](persistencia.md); no hay una sesión global compartida indefinidamente.

[Ver ConsultasDemo](https://github.com/MJSPlayground/fp-dam2-acda/blob/main/proyectos/hibernate/guiado-solucion/src/main/java/es/mjsplayground/acda/hibernate/biblioteca/demo/ConsultasDemo.java) · [Ver las pruebas reales de la demostración](https://github.com/MJSPlayground/fp-dam2-acda/blob/main/proyectos/hibernate/guiado-solucion/src/test/java/es/mjsplayground/acda/hibernate/biblioteca/AmpliacionTest.java)

Las pruebas utilizan un esquema QA temporal propio y MySQL real. Las opciones de ejecución técnica y variables están en el README; no deben apuntar a una base de trabajo importante.
