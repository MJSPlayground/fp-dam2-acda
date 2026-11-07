# Atomicidad, conflictos y errores

MongoDB garantiza atomicidad al modificar un documento. Eso encaja con nuestro agregado: un equipo incluye su plantilla y las estadísticas de cada jugador. No significa que cualquier secuencia de lecturas y escrituras Java sea automáticamente una operación segura.

## Una escritura y una secuencia son cosas distintas

Imagina dos personas que leen una plantilla con diez jugadores. La primera añade uno y guarda la lista de once; la segunda guarda su propia lista de once, creada desde la lectura anterior. Sin protección, la segunda escritura podría eliminar al jugador añadido por la primera.

La [atomicidad de MongoDB](https://www.mongodb.com/docs/manual/core/write-operations-atomicity/) protege la escritura de cada documento; para coordinar una decisión previa necesitamos una condición que detecte si el estado ha cambiado o una actualización que exprese directamente la operación.

## Control optimista con @Version

El equipo utiliza una propiedad `@Version Long version`. Spring Data conserva la versión leída y la incluye como condición cuando guarda. Si otra operación cambió el documento, la escritura de la versión antigua no debe sobrescribirla.

```java
@Version Long version
```

Este fragmento representa una propiedad de la entidad, no una clase completa. En un record se declara como componente. Un equipo nuevo parte de versión nula; la infraestructura gestiona su evolución según su contrato.

El servicio crea el estado propuesto conservando la versión del documento recuperado y llama a `save`. Un conflicto se comunica como `409`: el cliente puede volver a cargar y decidir su cambio sobre datos actuales. No se debe capturar el conflicto y guardar otra vez la lista obsoleta sin revisar las condiciones.

En esta API el DTO de edición no recibe la versión que tenía el formulario: el servicio vuelve a leer el equipo antes de guardar. Por tanto, `@Version` protege la ventana entre esa lectura del servidor y su escritura; **no detecta por sí sola cualquier formulario que llevaba abierto mucho tiempo**. Dos cambios sucesivos sobre nombre o categoría pueden aceptarse y prevalecer el último. Las estadísticas y la plantilla se conservan porque no se reemplazan con una copia enviada por el formulario de equipo. Detectar también esa edición antigua del cliente requeriría otro contrato, como versión explícita o `If-Match`, que no forma parte de este proyecto.

Las [operaciones de actualización y bloqueo optimista de Spring Data](https://docs.spring.io/spring-data/mongodb/reference/mongodb/template-crud-operations.html) explican cómo participa la versión. Añadir la anotación sin utilizarla en las operaciones que reemplazan el agregado no resuelve por sí solo todo el problema.

## Incrementos que no pierden otras actualizaciones

Para sumar estadísticas no hace falta recuperar todo el equipo y reemplazarlo. Una operación `$inc` expresa cuánto debe aumentar el contador a partir del valor que haya en la base al ejecutarse.

El repositorio preparado utiliza `MongoTemplate`, `Query`, `Criteria` y `Update`. Este fragmento reducido muestra la idea sobre las estadísticas de un jugador; omite los límites del proyecto para centrarse en la escritura:

```java
Criteria jugador = Criteria.where("id").is(jugadorId);
Query consulta = Query.query(Criteria.where("_id").is(equipoId)
    .and("jugadores").elemMatch(jugador));
Update cambio = new Update()
    .inc("jugadores.$.estadisticas.partidos", 1)
    .inc("jugadores.$.estadisticas.goles", goles)
    .inc("jugadores.$.estadisticas.asistencias", asistencias)
    .inc("version", 1);
```

`$` identifica el elemento del array que coincide con la condición. El cambio incrementa los tres contadores y la versión dentro de la misma escritura. La versión también debe avanzar para que un `save` de plantilla basado en datos anteriores no borre esos incrementos.

El código real añade condiciones sobre los contadores para impedir superar los límites. Solo entonces ejecuta `findAndModify` con retorno del documento actualizado. Un resultado nulo significa que ninguna entidad cumplió todas las condiciones: hay que distinguirlo de una excepción del servidor.

No se envían valores absolutos calculados desde una lectura antigua si la intención es incrementar. Tampoco se separa el aumento de partidos, goles y asistencias en tres escrituras que puedan quedar parcialmente realizadas.

## Reglas y límites del proyecto

La operación guiada de estadísticas representa una participación: suma un partido y los goles y asistencias de esa participación. Admite incrementos de goles y asistencias entre 0 y 20. Cero goles no significa que el jugador no participó.

Los contadores acumulados están acotados: partidos hasta 10.000, goles y asistencias hasta 100.000. El repositorio incorpora esas condiciones a la actualización. Para comprobar el límite, una prueba debe observar que un rechazo conserva todos los contadores anteriores.

El alta y edición de plantilla mantienen como máximo veinte jugadores y dorsales distintos dentro de cada equipo. El servicio evalúa el estado propuesto; la versión impide confirmar si el documento cambió entretanto. No se requiere bloquear todo el servidor Java para hacerlo.

## Límites entre documentos

Añadir o eliminar un equipo no modifica automáticamente la sede referenciada ni los demás equipos. La atomicidad de un documento no ofrece una transacción sobre todas las colecciones.

La práctica final guarda una jornada y sus efectos sobre la plantilla **dentro del mismo equipo**. Por eso podemos confirmar el resultado y sus estadísticas en una única escritura protegida. Registrar una jornada de un equipo no actualiza el del rival: no se presenta como una gestión bilateral de partidos con consistencia entre dos documentos.

Si otra aplicación necesitara cambiar varios agregados de forma indivisible, habría que estudiar transacciones, su configuración y sus costes. No se introduce esa infraestructura como requisito oculto de esta práctica.

## Respuestas y causas técnicas

Los errores conservan el contrato REST explicado en [Firestore](../firestore/rest.md). En este proyecto, una entrada inválida produce `400`, un equipo o jugador inexistente `404`, un dorsal ocupado o versión obsoleta `409`, y un fallo inesperado del almacenamiento `500`.

Un nombre de equipo repetido se protege también mediante un índice único y debe traducirse a un conflicto comprensible. El stack trace y la causa del driver permanecen en el logging del servidor; la respuesta pública no incluye detalles de conexión ni credenciales.

Los estados `501` del starter indican TODO pendientes. La solución no utiliza ese estado para disimular una excepción técnica. En [diagnóstico y pruebas](../ayuda/diagnostico.md) encontrarás cómo interpretar el stack trace y comprobar la capa responsable.

## Verificaciones que aportan evidencia

1. Añadir dos jugadores con el mismo dorsal: solo uno debe quedar registrado.
2. Editar el nombre de un equipo: deben conservarse jugadores y estadísticas.
3. Aplicar dos incrementos válidos: el total debe incluir ambos, sin perder el primero.
4. En una prueba de persistencia, intentar guardar un agregado con versión antigua: debe rechazarse sin sustituir el actual. No equivale a enviar una versión por la API, cuyo DTO no la admite.
5. Forzar un límite estadístico: ni partidos ni goles ni asistencias deben cambiar parcialmente.
6. Reiniciar el backend manteniendo el volumen MongoDB: el estado confirmado debe seguir disponible.

Una prueba que solo cuenta respuestas HTTP no demuestra esas propiedades. Es necesario recuperar el documento y comparar el estado final.
