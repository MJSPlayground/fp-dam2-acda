# Consultas HQL y JPQL básicas

Recuperar un libro por ID no responde todas las preguntas de la pantalla. Para buscar títulos o seleccionar una editorial utilizamos consultas sobre las entidades mapeadas.

**JPQL** pertenece al contrato de Jakarta Persistence. **HQL** es el lenguaje de consulta de Hibernate, que incorpora capacidades adicionales. En el recorrido inicial utilizamos una forma sencilla, cercana al conjunto compartido.

## Entidades y propiedades, no tablas y columnas

Esta consulta selecciona libros y los ordena:

```java
List<Libro> libros = sesion.createSelectionQuery(
    "from Libro l order by l.titulo, l.id", Libro.class).getResultList();
```

`Libro` es el nombre de la entidad y `titulo`, una propiedad Java. La tabla real se llama `libro`; HQL utiliza el modelo mapeado y Hibernate produce el SQL necesario. `Libro.class` indica el tipo de resultado esperado.

`l` es un alias que permite identificar de qué entidad procede cada propiedad. `getResultList` devuelve una lista, que puede estar vacía sin que exista un error de consulta.

La [guía de HQL](https://docs.hibernate.org/orm/current/querylanguage/html_single/) describe nombres, expresiones y selección de resultados. No debe interpretarse HQL como un texto SQL al que siempre baste cambiar el nombre de la tabla.

## Parámetros con nombre

Para filtrar no concatenamos el texto del usuario dentro de la consulta. La plantilla declara un parámetro y `setParameter` asigna su valor:

```java
List<Libro> libros = sesion.createSelectionQuery(
    "from Libro l where l.editorial.id = :editorial order by l.titulo, l.id", Libro.class)
    .setParameter("editorial", editorialId)
    .getResultList();
```

El parámetro `:editorial` corresponde al ID buscado. Se utiliza el nombre sin dos puntos al asignarlo. Una editorial válida sin libros puede producir una lista vacía; si el caso de uso exige comprobar que existe, esa validación se realiza aparte en el servicio.

Los parámetros representan valores. No convierten un nombre de propiedad o un fragmento arbitrario de ordenación en una opción segura del cliente. Las estructuras de consulta deben estar controladas por el código.

## Condiciones y búsqueda parcial

`where` establece condiciones. Podemos combinar igualdad, comparaciones y condiciones con `and` u `or`. Para buscar títulos sin distinguir mayúsculas, el proyecto utiliza `lower` y `like`:

```java
List<Libro> libros = sesion.createSelectionQuery(
    "from Libro l where lower(l.titulo) like :titulo order by l.titulo, l.id", Libro.class)
    .setParameter("titulo", "%" + texto.toLowerCase(Locale.ROOT) + "%")
    .getResultList();
```

`%` representa una secuencia de caracteres en el patrón y `_`, un carácter. La aplicación rodea el texto con `%` para buscar en cualquier posición. Por tanto, si se permiten esos símbolos dentro de la entrada, siguen teniendo significado de patrón; parametrizar evita que el valor se interprete como otra estructura HQL, pero no elimina los comodines de `like`.

`lower` normaliza mayúsculas para esta comparación. El tratamiento de acentos y otras diferencias también puede depender de las reglas de comparación de la base; no se promete una búsqueda lingüística que no esté configurada.

## Orden y tipos de resultado

`order by l.titulo, l.id` utiliza el ID para un desempate estable. Sin un orden explícito, no debemos deducir que el motor siempre devolverá los libros en el orden en que se crearon.

Una consulta que selecciona entidades devuelve entidades gestionadas por la sesión. Otra que selecciona un contador o un precio tiene otro tipo de resultado. Las proyecciones, agregaciones y consultas sobre relaciones se desarrollan en [Ampliación](ampliacion.md), sin convertirlas en requisitos ocultos del primer filtro.

## Sesión y preparación de la respuesta

El repositorio ejecuta la consulta con la sesión del caso de uso. El servicio transforma los datos que necesita la UI antes de cerrar esa sesión. Si la tabla muestra autores y editorial, esas propiedades deben estar disponibles en su representación de lectura.

No se corrige un fallo de inicialización abriendo sesiones desde una celda JavaFX ni haciendo EAGER toda la biblioteca. La explicación de [relaciones y carga](relaciones.md#lazy-y-eager) ayuda a identificar la causa; la Ampliación enseña a observar N+1 y las estrategias de consulta.

## Comprobaciones del proyecto

Completa el listado de `LibroRepository` y comprueba búsqueda vacía, título parcial, editorial y combinación de filtros. Un valor con apóstrofo no debe romper la consulta. Una entrada que no coincide devuelve una lista vacía, mientras un fallo del servidor se comunica como error técnico.

Vuelve al [proyecto guiado](proyecto.md) para contrastar el recorrido completo y sus relaciones.
