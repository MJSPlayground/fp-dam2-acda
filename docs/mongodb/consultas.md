# Consultas, paginación e índices

Un repositorio no se limita al CRUD heredado. Spring Data puede construir consultas a partir de nombres de métodos o utilizar expresiones MongoDB declaradas explícitamente. La elección debe hacer visible la pregunta que queremos resolver.

## Consultas derivadas

El nombre `findByCategoriaOrderByNombreAsc` expresa un filtro de igualdad y una ordenación. El parámetro corresponde al valor buscado; el resultado contiene las entidades que coinciden:

```java
List<Equipo> findByCategoriaOrderByNombreAsc(String categoria);
```

El fragmento supone que la propiedad `categoria` del modelo es una cadena. Si el proyecto utiliza un enum, la firma empleará ese tipo. Spring interpreta nombres de propiedades Java: un error tipográfico no es una nueva columna, sino una consulta inválida que puede fallar al arrancar.

Un método como `findByNombreContainingIgnoreCase` expresa búsqueda textual parcial sin distinguir mayúsculas. Antes de añadirlo a una API hay que definir límites y orden: una búsqueda que devuelve miles de documentos no es un sustituto de la paginación. En el método real del guiado, el resultado es `Page<Equipo>` y se añade `Pageable` a la firma. Al sustituir el método `default` del starter por una declaración, Spring Data implementa la consulta: no escribas una búsqueda completa en memoria dentro de ese cuerpo. La [referencia de consultas derivadas](https://docs.spring.io/spring-data/mongodb/reference/mongodb/repositories/query-methods.html) enumera los operadores admitidos.

## @Query y operadores básicos

`@Query` permite escribir un filtro MongoDB manteniendo parámetros separados de la estructura. Por ejemplo, esta consulta selecciona equipos que tienen al menos un jugador en una posición:

```java
@Query("{ 'jugadores.posicion': ?0 }")
List<Equipo> conPosicion(String posicion);
```

`?0` representa el primer argumento. No se debe formar una cadena JSON concatenando el texto que llega de un formulario. Los valores deben enlazarse como parámetros y validarse en el servicio.

El resultado contiene equipos completos: **filtrar por un elemento del array no recorta automáticamente la lista de jugadores devuelta**. Si la pantalla necesita solo jugadores coincidentes, el servicio debe preparar una representación adecuada o utilizar una proyección diseñada para ello.

| Operador MongoDB | Significado ilustrativo |
| --- | --- |
| `$eq` | Igualdad |
| `$gte` / `$lte` | Mayor o igual / menor o igual |
| `$in` | Uno de los valores indicados |
| `$and` / `$or` | Combinación de condiciones |
| `$elemMatch` | Un mismo elemento de un array cumple varias condiciones |

Para exigir que exista un mismo jugador delantero y disponible, `$elemMatch` evita combinar la posición de uno con la disponibilidad de otro:

```json
{
  "jugadores": {
    "$elemMatch": {"posicion": "DELANTERO", "disponible": true}
  }
}
```

Es un filtro ilustrativo para Compass o para una consulta parametrizada. Dos condiciones separadas sobre campos de un array pueden cumplirse en elementos distintos. La [documentación de consultas sobre arrays de documentos](https://www.mongodb.com/docs/manual/tutorial/query-array-of-documents/) permite contrastar esa diferencia.

## Ordenación estable

Ordenar por nombre permite presentar un listado comprensible. Cuando hay nombres iguales, añadir el ID como segundo criterio evita depender de un orden no definido entre empates:

```java
Sort orden = Sort.by("nombre").ascending()
    .and(Sort.by("id").ascending());
```

Spring Data traduce la propiedad de identidad al campo persistido. No se deben aceptar nombres de ordenación arbitrarios del cliente: una lista de opciones permitidas hace explícito el contrato y evita consultas inesperadas.

Una ordenación sensible a mayúsculas o acentos puede no coincidir con el orden alfabético humano esperado. No hay que anunciar una comparación lingüística si el proyecto utiliza la comparación predeterminada del motor.

## Páginas y tamaño

Spring Data utiliza `Pageable` para expresar qué página y tamaño pedimos. En `PageRequest`, la primera página es la **cero**:

```java
Pageable solicitud = PageRequest.of(0, 5,
    Sort.by("nombre").ascending().and(Sort.by("id").ascending()));
Page<Equipo> pagina = repository.findAll(solicitud);
List<Equipo> equipos = pagina.getContent();
```

El ejemplo recupera hasta cinco equipos e incluye información de la página. Obtener totales puede requerir una consulta de recuento adicional. Si una API solo necesita saber si hay continuación, puede usar otra representación en lugar de calcular siempre el total.

Una página vacía es válida si no hay coincidencias o se solicita una posición posterior al final. El servicio rechaza páginas negativas y tamaños fuera del límite definido por el proyecto. No corrige esos valores en silencio.

La paginación basada en desplazamiento es sencilla para este listado, pero los desplazamientos grandes pueden resultar costosos y los cambios concurrentes pueden mover elementos entre peticiones. El [tema Firestore](../firestore/consultas.md#cursores-y-orden-estable) permite comparar esta estrategia con un cursor sin suponer que ambos contratos sean intercambiables.

## Índices comprobables

Un índice facilita determinadas búsquedas y ordenaciones, a cambio de espacio y trabajo adicional cuando cambian los documentos. El índice único de `_id` no acelera automáticamente todas las preguntas sobre categoría o jugadores.

Para un listado que filtra categoría y ordena por nombre e ID, un índice compuesto puede ser útil. Su definición debe corresponder al esquema real y al orden de la consulta. El proyecto puede crear los índices preparados al arrancar; una anotación `@Indexed` no debe darse por aplicada sin comprobar cómo está configurada la creación de índices.

En Compass se puede inspeccionar la pestaña **Indexes** de la colección y contrastarla con el código de configuración. No elimines índices para experimentar en una base compartida. [Ampliación](ampliacion.md) enseña a leer `explain` y comparar planes con datos representativos.

Los índices no imponen por sí solos el límite de veinte jugadores ni la unicidad de dorsales dentro de cada plantilla. Esas reglas pertenecen al agregado y sus operaciones seguras.

## Preguntas que deben responder las pruebas

- ¿La consulta derivada devuelve solo equipos de la categoría pedida?
- ¿La consulta por posición reconoce el jugador embebido correcto?
- ¿Los empates conservan un orden determinista?
- ¿Pedir una página vacía se distingue de un fallo del servidor?
- ¿El límite de página se valida también en una petición directa a la API?
- ¿Un filtro por equipo devuelve la plantilla completa o una selección de jugadores? ¿Coincide con lo que anuncia el endpoint?

Continúa con [actualizaciones atómicas y errores](atomicidad.md) para proteger los datos que esas consultas recuperan.
