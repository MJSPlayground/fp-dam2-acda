# HTTP, REST y respuestas de la API

El navegador y Postman no llaman directamente a un método Java. Envían una petición HTTP a una dirección del servidor. Spring interpreta esa petición, llama al controlador y transforma la respuesta en un mensaje que puede entender el cliente.

En [JDBC](../jdbc/repositorios.md) una vista invocaba al servicio dentro del mismo proceso. Ahora añadimos una frontera de red: los datos deben tener una representación pública, los errores necesitan un formato estable y cada respuesta debe indicar qué ha ocurrido.

## Anatomía de una petición

Esta petición solicita crear una reserva en nuestra API local. La primera línea indica método y ruta; las cabeceras describen el formato del cuerpo y la respuesta aceptada. Después de una línea vacía aparece el JSON:

```http
POST /api/reservas HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Accept: application/json

{
  "espacio": "SALA-A",
  "fecha": "2027-02-15",
  "inicio": 10,
  "fin": 12,
  "responsable": "Coordinación",
  "asistentes": 8,
  "equipo": ["proyector"],
  "detalles": {"actividad": "Taller"}
}
```

La URL completa es `http://localhost:8080/api/reservas`. El host y el puerto identifican el servidor; `/api/reservas` identifica el recurso; un parámetro como `?espacio=SALA-A` filtra una consulta. El cuerpo transmite una representación. Una cabecera no se escribe como campo dentro del JSON.

Una respuesta tiene estado, cabeceras y, cuando corresponde, cuerpo. **El código de estado debe interpretarse antes que el contenido:** un JSON con un mensaje de error no es un alta correcta aunque la conexión HTTP haya funcionado.

## Recursos y métodos

REST organiza una API alrededor de recursos y sus representaciones. En nuestro caso hay una colección de reservas y recursos individuales identificados por un ID. Los [significados de los métodos HTTP](https://www.rfc-editor.org/rfc/rfc9110.html#name-method-definitions) permiten que los clientes sepan qué tipo de operación están solicitando.

| Petición | Necesidad en el proyecto | Respuesta prevista |
| --- | --- | --- |
| `GET /api/espacios` | Consultar espacios conocidos | `200` y lista |
| `GET /api/reservas` | Listar o filtrar reservas | `200` y lista, incluso si está vacía |
| `GET /api/reservas/{id}` | Consultar una reserva | `200`, o `404` si no existe |
| `POST /api/reservas` | Crear una reserva con ID generado | `201` y reserva creada |
| `PUT /api/reservas/{id}` | Sustituir los datos editables de una existente | `200`, o `404` si no existe |
| `DELETE /api/reservas/{id}` | Borrar una reserva existente | `204`, o `404` si no existe |

El proyecto trata `PUT` sobre una reserva inexistente como error, en lugar de crearla. Es parte del contrato de esta API y debe comprobarse en sus tests y colección Postman.

Un `GET` no debe crear, borrar ni cancelar reservas. `DELETE` no necesita un cuerpo para identificar qué borrar: utiliza la ruta. `PATCH` se utiliza para una modificación parcial definida por el contrato, como la cancelación que añade la práctica final.

### Idempotencia

Una operación es idempotente si repetirla tiene el mismo efecto final pretendido que realizarla una sola vez. No significa que devuelva necesariamente el mismo código ni que no registre actividad. Un segundo `DELETE` puede devolver `404` porque el recurso ya desapareció, y el estado final sigue siendo «no existe».

Repetir un `PUT` con la misma representación debe dejar los mismos datos editables. Repetir un `POST` que genera un identificador puede crear otra reserva. Si un cliente pierde la respuesta de creación, reintentar automáticamente podría duplicarla. La aplicación necesitaría una estrategia adicional de idempotencia antes de hacerlo en producción.

## DTO: datos públicos y modelo persistido

Un DTO es un objeto destinado a transportar datos a través de una frontera. El cliente puede proponer espacio, fecha y horario, pero no debe decidir el ID generado ni imponer un estado interno arbitrario. Por eso `ReservaRequest` representa la entrada, mientras la reserva devuelta incluye datos controlados por el servidor.

No se envía un `DocumentSnapshot` del SDK al navegador. El repositorio lo transforma en una entidad, y el controlador devuelve una representación de la API. Así evitamos acoplar al cliente a clases de persistencia y exponer información interna.

Las validaciones de entrada comprueban que el JSON pueda interpretarse y que los campos tengan forma válida. El servicio vuelve a comprobar las reglas del caso de uso. Un cuerpo mal formado y una reserva con más asistentes que la capacidad del espacio son fallos diferentes, aunque ambos impidan crearla.

## Cómo conecta Spring una ruta con Java

[`@RestController`](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann.html) identifica una clase cuyos métodos devuelven datos para la respuesta. `@RequestMapping` fija un prefijo común; `@GetMapping`, `@PostMapping` y las demás anotaciones seleccionan método y ruta.

Este fragmento ilustrativo devuelve una reserva ya obtenida por el servicio:

```java
@GetMapping("/reservas/{id}")
public ResponseEntity<Reserva> obtener(@PathVariable String id) {
    Reserva reserva = servicio.obtener(id);
    return ResponseEntity.ok(reserva);
}
```

El método pertenece al controlador con prefijo `/api`. `@PathVariable` recoge el segmento que sustituye a `{id}`. `ResponseEntity.ok` expresa estado `200` y cuerpo. Si el servicio lanza una excepción de recurso inexistente, el manejador común la convertirá en `404`; no se devuelve `200` con un objeto vacío. La solución también puede devolver directamente la reserva para que Spring construya esa respuesta `200`.

Para leer un filtro opcional utilizamos `@RequestParam`; para convertir el JSON de entrada, `@RequestBody`. Las [firmas de métodos MVC](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods.html) admiten estas fuentes de datos sin que tengamos que analizar manualmente el mensaje HTTP.

```java
@GetMapping("/reservas")
public List<Reserva> listar(@RequestParam(required = false) String espacio) {
    return servicio.listar(espacio, null, null);
}
```

Este fragmento simplifica la firma real para mostrar un solo filtro. Si la URL no lo incluye, `espacio` será `null`. No es lo mismo que una cadena con contenido inválido. El servicio debe decidir qué filtros acepta y el controlador real recoge todos los previstos por el contrato.

No hay que modificar HTML ni JavaScript. El frontend ya conoce rutas y estructuras; implementar una ruta distinta por comodidad rompería ese acuerdo aunque el nuevo endpoint respondiese.

## Construir la respuesta de una escritura

Una creación no se expresa con `ok` por costumbre. Tras recibir del servicio una reserva `creada`, el controlador puede construir la ubicación y el cuerpo:

```java
URI destino = URI.create("/api/reservas/" + creada.id());
return ResponseEntity.created(destino).body(creada);
```

`created` fija `201` y la cabecera `Location`; `body` incorpora la representación con su ID. `URI` procede de `java.net`. La escritura ya debe haber terminado: el controlador no devuelve un futuro del SDK como si fuera la reserva.

Para el borrado, después de llamar al servicio, `ResponseEntity.noContent().build()` devuelve `204` sin JSON. Una actualización que devuelve directamente `Reserva` produce `200` y cuerpo. No copies el mismo tipo de respuesta a todas las operaciones.

## Estados de éxito y de error

| Estado | Interpretación en nuestra API |
| --- | --- |
| `200 OK` | Consulta o actualización completada con representación |
| `201 Created` | Se creó una reserva |
| `204 No Content` | Operación completada sin cuerpo de respuesta |
| `400 Bad Request` | JSON, tipos, campos o límites de entrada inválidos |
| `404 Not Found` | El ID solicitado no corresponde a una reserva |
| `409 Conflict` | La operación contradice el estado actual, como una ocupación incompatible |
| `500 Internal Server Error` | Fallo inesperado del servidor o la persistencia |
| `501 Not Implemented` | Operación pendiente en el proyecto inicial |

El `501` identifica operaciones pendientes de la versión de aprendizaje; la solución completa no debe utilizarlo para ocultar errores. Una lista sin coincidencias se devuelve con `200` y `[]`, no con `404`.

`@RestControllerAdvice` concentra el tratamiento común de excepciones. El manejador traduce un error de dominio a su estado correspondiente; registra el detalle técnico de un fallo inesperado y devuelve un mensaje seguro. No se incluyen claves, rutas privadas ni stack traces en las respuestas públicas. El [tratamiento de excepciones de Spring MVC](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-exceptionhandler.html) permite separar esta tarea de cada controlador.

La clase `ApiErrors` devuelve los campos `estado`, `mensaje`, `ruta` e `instante`. `estado` repite el código HTTP, `ruta` identifica el endpoint y `instante` sitúa el fallo. No compares solo una frase que pueda cambiar de redacción: observa también el estado HTTP y estos campos estructurados.

## Probar con Postman

La colección del proyecto ya contiene las peticiones: no tienes que construirlas manualmente. Tras importarla, comprueba `baseUrl = http://localhost:8080`. La carpeta **00 · Primer contacto HTTP** contiene consultas y errores que puedes ejecutar antes de los TODO. La **01 · Lectura tras completar el repositorio** comprueba el `GET` preparado. La carpeta CRUD se usa petición a petición al completar `POST`, `PUT` y `DELETE`; ejecuta su Runner completo al terminar esos tres métodos. El [guiado](proyecto.md) relaciona cada petición con su subpaso.

Una prueba de alta debe comprobar `201`, el ID devuelto y los valores persistidos mediante un `GET` posterior. Una prueba negativa debe comprobar el estado esperado **y que no haya aparecido una reserva inválida**. Borrar una reserva y volver a consultarla comprueba una propiedad distinta de limitarse a inspeccionar el código de borrado.

Si aparece una página HTML en vez de JSON, revisa la URL y el cuerpo: puede haberse solicitado la portada, una ruta incorrecta o un recurso estático. Si el servidor no responde, comprueba primero su proceso y puerto; si responde `500`, consulta el logging del backend y la disponibilidad del emulador.

## Etapa del proyecto: completar los controladores

Una vez resueltos modelo, repositorio y servicio, completa los **TODO 4a–4c** de `ReservaController`, en ese orden: `POST`, `PUT` y `DELETE`. Mantén las rutas de la tabla y delega las reglas al servicio. Los `GET` de apoyo y el frontend están preparados.

La fase `rest` de `FasesAprendizajeTest` usa un servicio de prueba preparado: permite comprobar estados, `Location` y errores sin depender de Firestore. El caso técnico provoca una excepción con causa y comprueba que el JSON público no la revela. Después prueba creación, modificación, borrado y sus errores con la colección importada y el emulador real. Ejecuta también las mismas operaciones desde la web: dos clientes distintos deben recibir las mismas decisiones del servicio. Continúa con [la guía del proyecto](proyecto.md) para comprobar el recorrido completo.
