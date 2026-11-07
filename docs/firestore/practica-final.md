# Ampliación del sistema de reservas

El centro ya puede registrar, consultar, modificar y borrar reservas. Ahora necesita conservar las cancelaciones, informar de la disponibilidad y evitar que una misma persona concentre la agenda. Amplía el mismo sistema manteniendo las funciones anteriores.

## Preparación

[Descargar proyecto inicial](/fp-dam2-acda/descargas/codigo/reservas-final-inicial.zip){ .md-button .md-button--primary download="reservas-final-inicial.zip" }

Abre la carpeta Maven en IntelliJ y sigue el [arranque del proyecto guiado](proyecto.md#arranque-desde-intellij): primero `config.Emulador.main`, después `ReservasApplication.main`. El frontend incorpora los controles de las funciones nuevas; no debes modificarlo. El backend del guiado se entrega resuelto para que puedas partir de él, aunque tus etapas anteriores hayan quedado incompletas.

No conectes la práctica a un proyecto cloud con datos reales. Para repetir escenarios utiliza los identificadores creados por la colección Postman y su limpieza final, sin borrar reservas ajenas. Consulta el README de la descarga para conocer el ciclo de vida de los datos del emulador.

## Base: conservar las cancelaciones y encontrarlas

La recepción necesita cancelar una reserva sin perder sus datos. Debe constar un motivo de entre 1 y 120 caracteres, sin contar espacios exteriores. Un texto vacío no es una explicación válida. La cancelación conserva el mismo identificador y deja el estado `CANCELADA`.

Repetir una petición de cancelación sobre esa misma reserva no debe cambiar el primer motivo ni crear otro registro. La respuesta sigue mostrando la reserva cancelada. Intentar cancelar una reserva inexistente produce `404`; un motivo inválido produce `400`.

Una reserva cancelada puede consultarse y borrarse, pero no se modifica como si siguiera activa ni se reactiva mediante la edición ordinaria: esa petición se rechaza con `409`.

El listado permite combinar sus filtros anteriores con estado y responsable. Los únicos estados aceptados son `ACTIVA` y `CANCELADA`; un valor distinto produce `400`. La búsqueda por responsable es exacta y distingue mayúsculas: `Recepción` y `recepción` son filtros diferentes. Un resultado sin coincidencias es una lista vacía, no un error.

La colección preparada incorpora `PATCH /api/reservas/{id}/cancelacion` con cuerpo `{"motivo":"Cambio de actividad"}` y consultas de listado. Comprueba una cancelación válida, una repetida con otro motivo, una inexistente y un listado de canceladas. El motivo original debe seguir disponible tras reiniciar el backend mientras el emulador conserve los datos.

## Aplicación: consultar disponibilidad y evitar ocupaciones incompatibles

Una sala no puede tener dos reservas activas que ocupen parte del mismo horario en la misma fecha. Las canceladas liberan su horario. Los espacios diferentes y los días distintos tienen agendas independientes.

Dos actividades consecutivas son compatibles: una de 10:00 a 12:00 y otra de 12:00 a 13:00. En cambio, una actividad de 11:00 a 13:00 no cabe en esa sala si sigue activa la primera.

El sistema ofrece una consulta de disponibilidad indicando espacio, fecha, inicio y fin. La respuesta contiene `disponible: true` o `disponible: false`. Los datos inválidos se rechazan igual que en una reserva; no se interpretan como «sala libre».

Tanto el alta como la modificación deben comprobar la compatibilidad. Una reserva existente no debe chocar consigo misma al editarse, pero moverla sobre otra actividad activa se rechaza con `409`. El rechazo conserva íntegramente sus valores anteriores.

Comprueba este recorrido con una misma sala y fecha:

1. Crea una reserva de 10 a 12: consulta esa franja y observa que está ocupada.
2. Intenta otra de 11 a 13: debe rechazarse y no aparecer en el listado.
3. Crea otra de 12 a 13: debe admitirse.
4. Cancela la primera: la franja de 10 a 12 vuelve a estar disponible.
5. Intenta modificar la reserva de 12 a 13 sobre una tercera reserva incompatible: la edición fallida no debe alterar su horario anterior.

Una consulta de disponibilidad informa del estado en ese momento; no aparta la sala ni garantiza que siga libre hasta una creación futura. La decisión definitiva se toma al guardar.

## Reto avanzado: reparto de agenda y consulta por páginas

Para repartir oportunidades de uso, una persona solo puede mantener **dos reservas activas por día**, sumando todos los espacios. Las canceladas no cuentan. Para esta regla, los espacios exteriores se ignoran y las mayúsculas no distinguen personas: `Coordinación` y `coordinación` deben compartir el mismo cupo.

Con dos reservas activas de esa persona en una fecha, intentar una tercera produce `409` sin añadir datos. Cancelar una libera una plaza. Editar una reserva existente no debe contarla dos veces, y cambiar responsable o fecha exige respetar el cupo de destino. Una reserva de otra persona o de otro día no consume ese límite.

La recepción necesita además recorrer la colección por páginas de entre 1 y 20 reservas. Cada respuesta contiene `elementos` y `siguienteCursor`; el cursor es nulo cuando no quedan más resultados. Deben aparecer en orden ascendente y estable de identificador. Repetir una página sobre datos sin cambios conserva sus elementos y orden.

El cursor entregado permite pedir la continuación sin repetir el último elemento. Los límites inválidos y los cursores con formato no permitido producen `400`. Si el identificador que marca la posición dejó de existir, sigue sirviendo como posición del orden; no se exige que exista una reserva con ese ID. Este recorrido por páginas es independiente de los filtros del listado ordinario.

La colección contiene las peticiones de `GET /api/reservas/pagina?limite=...&cursor=...`. Con al menos tres reservas y tamaño dos, la primera página debe ofrecer una continuación y la siguiente no debe repetir sus elementos. Repite con tamaño cero, 21 y un cursor mal formado.

## Comprobar el sistema completo

Desde la interfaz y la colección Postman, comprueba los casos correctos y sus rechazos. Las respuestas deben mantener el formato de [errores de la API](rest.md), sin filtrar detalles técnicos. Los fallos del almacenamiento se distinguen de un conflicto de agenda.

El proyecto entrega infraestructura de coordinación para guardar una reserva junto con la comprobación de sus condiciones. Tu trabajo se concentra en las reglas de negocio y las nuevas operaciones del backend; no necesitas implementar el protocolo de transacciones Firestore estudiado en Ampliación. No sustituyas esa coordinación por una consulta seguida de una escritura independientes.

Comprueba también el guiado: edición válida, listas y mapas, borrado y errores de capacidad o fecha. Una mejora no debe romper los contratos que ya utiliza el frontend. Las pruebas de servicio y API deben observar el estado posterior a un rechazo, además de su código HTTP.

## Solución completa

[Descargar solución de los tres niveles](/fp-dam2-acda/descargas/codigo/reservas-final-solucion.zip){ .md-button download="reservas-final-solucion.zip" }

[Ver código, README y pruebas](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/firestore/final-solucion)

La solución mantiene el frontend preparado y separa persistencia, reglas de agenda y controladores REST. La colección Postman de la descarga contiene las peticiones de los tres niveles.
