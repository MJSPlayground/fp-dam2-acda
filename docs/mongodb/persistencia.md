# Persistencia con Spring Data MongoDB

En [JDBC](../jdbc/consultas.md) escribíamos SQL y convertíamos cada fila. En [Firestore](../firestore/persistencia.md) transformábamos documentos con el SDK. **Spring Data MongoDB** automatiza parte del acceso y del mapeo mediante entidades y repositorios; seguimos siendo responsables de elegir un modelo correcto y validar sus operaciones.

## La entidad y la colección

`@Document` identifica una entidad persistida y puede indicar su colección. `@Id` señala la propiedad asociada con `_id`. Las demás propiedades se convierten según sus tipos y metadatos.

Este modelo reducido es ilustrativo: omite campos del proyecto para mostrar únicamente el mapeo del identificador y una lista embebida:

```java
@Document("equipos")
public record Equipo(
    @Id String id,
    String nombre,
    List<Jugador> jugadores
) {}

public record Jugador(String id, String nombre, int dorsal) {}
```

Cada `Equipo` ocupa un documento. La lista de jugadores forma parte de ese documento; la ausencia de `@Document` en `Jugador` no impide persistirlo como embebido. El [mapeo de objetos de Spring Data](https://docs.spring.io/spring-data/mongodb/reference/mongodb/mapping/mapping.html) describe cómo se construyen los objetos y se convierten sus propiedades.

Un ID Java `String` no implica necesariamente que `_id` se almacene como texto: las convenciones de Spring Data pueden convertir valores compatibles a `ObjectId`. En el proyecto los IDs de equipo tienen 24 caracteres hexadecimales y los jugadores embebidos utilizan identificadores propios. Hay que usar el ID correcto para cada ruta.

## Un repositorio generado

Una interfaz que extiende `MongoRepository<Equipo, String>` hereda operaciones de persistencia. El primer tipo representa la entidad y el segundo su identificador:

```java
public interface EquipoRepository extends MongoRepository<Equipo, String> {
}
```

Spring crea la implementación al arrancar dentro de su contexto; no debemos construir la interfaz con `new`. El servicio la recibe por constructor, como las dependencias que utilizamos en el tema de [API REST](../firestore/rest.md).

| Método | Significado |
| --- | --- |
| `findById(id)` | Devuelve un `Optional<Equipo>` |
| `findAll()` | Recupera documentos; no promete un orden de negocio |
| `insert(equipo)` | Crea un documento y rechaza una clave duplicada |
| `save(equipo)` | Inserta o guarda según el estado de identidad y la estrategia de mapeo |
| `existsById(id)` | Consulta existencia, sin bloquear una operación posterior |
| `deleteById(id)` | Elimina por identidad; el servicio define el contrato de ausencia |

La [documentación de repositorios MongoDB](https://docs.spring.io/spring-data/mongodb/reference/mongodb/repositories/repositories.html) explica esa implementación. Heredar métodos no convierte todos ellos en operaciones públicas de nuestra API: el servicio decide cuáles tienen sentido y con qué condiciones.

## Crear y recuperar

Para un alta, la entidad puede partir de un ID nulo y dejar que la persistencia genere su identidad. Después se utiliza el objeto devuelto para obtenerla. Este fragmento ilustra el modelo reducido anterior:

```java
Equipo creado = repository.insert(new Equipo(null, "Atlético Norte", List.of()));
String id = creado.id();
Equipo recuperado = repository.findById(id)
    .orElseThrow(() -> new NoEncontradoException("El equipo no existe"));
```

`NoEncontradoException` representa aquí una excepción de dominio ilustrativa. El proyecto contiene su tipo propio. Una consulta sin coincidencias produce ausencia; un servidor inaccesible debe producir un error técnico, nunca un equipo vacío.

Si el servidor genera la identidad, el cliente no debe enviarla para decidir qué documento reemplazar durante un alta. El DTO público y la entidad persistida tienen responsabilidades distintas, como vimos en [Firestore](../firestore/rest.md#dto-datos-publicos-y-modelo-persistido).

## Equipos, jugadores y estadísticas

El proyecto almacena una plantilla dentro del equipo. Cada jugador contiene identificación, nombre, dorsal, posición, disponibilidad y estadísticas. Estas últimas agrupan partidos, goles y asistencias en un subdocumento.

Un alta de jugador no crea por ello un documento independiente en una colección `jugadores`. El servicio modifica el agregado del equipo y el repositorio realiza una escritura que respeta sus condiciones. Los IDs de jugador permiten localizar un elemento sin depender de su índice dentro de la lista.

La lista está acotada y los dorsales no pueden repetirse dentro de la misma plantilla. Guardar una lista completa obtenida hace tiempo podría borrar el alta concurrente de otra persona. La página de [atomicidad](atomicidad.md) explica por qué leer–modificar–guardar necesita protección cuando intervienen varios clientes.

## La sede como referencia explícita

Las sedes tienen una colección y repositorio propios. El equipo conserva `sedeId`; el servicio comprueba que corresponde a una sede existente antes de aceptar el dato.

No se añade automáticamente un `JOIN` ni una clave foránea. Para presentar el nombre de la sede, la aplicación puede resolver su ID con el catálogo preparado. El repositorio del equipo no debe llamar al servicio de sedes ni a la UI; la coordinación pertenece al servicio o a una composición de lectura diseñada para la respuesta.

Al ser un catálogo preparado, el alumnado puede concentrarse en el modelo del equipo sin desarrollar otro sistema de mantenimiento independiente. Los ejemplos de `$lookup` se reservan a [Ampliación](ampliacion.md).

## Validación en el servicio

Las reglas no se trasladan a la entidad persistida por el mero hecho de añadir `@Document`. El servicio comprueba nombre, categoría, sede, tamaño de plantilla, dorsales y valores de estadísticas. La entrada HTTP añade comprobaciones de formato; una llamada directa de prueba al servicio no debe saltarse las invariantes.

También hay que distinguir un equipo que no existe de uno al que no se puede añadir otro jugador. El primero produce `404`; un dorsal ocupado o un cambio incompatible con el estado puede producir `409`. Un campo fuera de sus límites se comunica como `400`, conservando el [formato común de errores REST](../firestore/rest.md#estados-de-exito-y-de-error).

## Errores técnicos y pruebas

Spring traduce fallos del driver a su jerarquía `DataAccessException`, conservando las causas. No debe capturarse cualquier fallo para devolver `List.of()`: eso mostraría una plantilla vacía cuando el servidor está caído. El manejador común registra el detalle y responde con un error seguro, sin exponer URI con credenciales ni stack traces.

Para comprobar persistencia hay que recuperar los datos después del alta y comparar también los embebidos. Una prueba con un repositorio simulado comprueba parte de las reglas, pero no demuestra que anotaciones, consultas o actualizaciones funcionen en MongoDB real.

La comprobación de edición debe verificar que cambiar un nombre no reinicia estadísticas ni elimina otros jugadores. La de error debe observar que el estado anterior permanece. Continúa con [consultas y paginación](consultas.md).
