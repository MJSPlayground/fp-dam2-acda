# Gestión de competición

La gestión ya mantiene equipos, plantillas y estadísticas. La organización necesita consultar jugadores según su rendimiento, registrar resultados y presentar la evolución de los equipos. Amplía el mismo proyecto conservando sus funciones anteriores.

## Preparación

[Descargar proyecto inicial](/fp-dam2-acda/descargas/codigo/jugadores-final-inicial.zip){ .md-button .md-button--primary download="jugadores-final-inicial.zip" }

Abre la carpeta Maven y sigue [el arranque del guiado](proyecto.md#arranque-desde-intellij): Docker disponible, `config.MongoLocal.main` y después `LigaApplication.main`. La interfaz incorpora los controles nuevos. El backend del guiado está resuelto; no debes editar el frontend para completar la actividad.

Utiliza los datos que prepares para esta práctica y conserva sus identificadores. Las sedes y los equipos no son lo mismo: el catálogo de sedes está preparado, mientras los equipos y jugadores se crean durante el trabajo. No borres el volumen de MongoDB para repetir un escenario; utiliza la limpieza y el entorno indicados en el README.

## Base: localizar jugadores y señalar disponibilidad

La organización quiere consultar los jugadores de un equipo que alcanzan un mínimo de goles acumulados entre 0 y 100.000. Debe poder combinarlo con su disponibilidad: sin esa condición aparecen todos los coincidentes, con `true` solo los disponibles y con `false` solo los no disponibles. Un equipo existente sin coincidencias devuelve una lista vacía; un equipo inexistente produce `404`.

También necesita marcar un jugador como disponible o no disponible, conservando su identidad, dorsal, posición y estadísticas. Repetir la misma selección deja el mismo estado, sin reiniciar ningún contador. Un jugador que pertenece a otro equipo no puede modificarse usando la ruta de este.

Comprueba un equipo con jugadores de diferente rendimiento y disponibilidad. Cambia el estado de uno, vuelve a consultar y verifica que sus estadísticas siguen intactas. Un mínimo de goles inválido debe rechazarse con `400`, sin sustituirse silenciosamente por cero.

## Aplicación: registrar una jornada

Cada equipo guarda sus propios resultados. Una jornada contiene identificador, fecha, rival, goles a favor, goles en contra y aportaciones de jugadores. El rival se indica por su nombre: este registro **no modifica automáticamente otro equipo**.

Las condiciones son:

- Identificador de jornada de 1 a 60 letras, dígitos, guiones o guiones bajos. Repetirlo dentro del mismo equipo se rechaza con `409`, sin sumar estadísticas otra vez.
- Fecha válida en formato ISO y rival de 1 a 60 caracteres.
- Marcador entre 0 y 100 goles por cada lado.
- Como máximo veinte aportaciones, cada una de un jugador distinto, existente y disponible en esa plantilla.
- Goles y asistencias de cada aportación entre 0 y 100. Los goles de todas las aportaciones deben coincidir con los goles a favor del equipo, y las asistencias totales no pueden superarlos.
- Como máximo treinta jornadas por equipo. Un rechazo conserva el historial y todos los contadores anteriores.

Cada jugador incluido en las aportaciones suma una participación, incluso cuando sus goles y asistencias son cero. Los jugadores que no aparecen no suman un partido por el mero hecho de pertenecer a la plantilla. Una lista de aportaciones vacía solo puede cuadrar con un marcador de cero goles a favor. Un jugador referenciado por una jornada histórica no puede eliminarse de la plantilla: se rechaza con `409` para conservar ese historial.

Guardar la jornada debe conservar el resultado y sumar sus estadísticas de forma indivisible. No puede quedar un resultado guardado sin sus incrementos ni incrementos de una jornada rechazada. El sistema mantiene también los límites acumulados de estadísticas del guiado.

Por ejemplo, un 2–1 con dos jugadores que aportan un gol cada uno y una asistencia total es válido: ambos suman un partido y sus aportaciones individuales. Un marcador de dos goles con aportaciones que solo suman uno se rechaza. Repetir el identificador de la jornada válida tampoco añade un segundo resultado ni vuelve a incrementar contadores.

Comprueba además jugador inexistente, no disponible, repetido y un fallo de límite acumulado. Después de cada rechazo recupera el equipo y compara el estado completo, no solo el mensaje de error.

## Reto avanzado: clasificación y racha

La organización necesita una clasificación calculada a partir de las jornadas guardadas: una victoria concede tres puntos, un empate uno y una derrota cero. Cada fila muestra equipo, jornadas jugadas, puntos, goles a favor, goles en contra y diferencia de goles.

El orden es: más puntos, mejor diferencia, más goles a favor y nombre ascendente. Si persiste el empate, el identificador da un orden estable. Un equipo sin jornadas debe tener ceros, sin provocar divisiones por cero ni valores ausentes. En esta aplicación la clasificación reúne todos los equipos, sin separar categorías: es una tabla de demostración, no un calendario de liga federada.

La clasificación no se obtiene sumando directamente los goles personales que ya pudieran existir del guiado: esos contadores pueden proceder de participaciones anteriores sin un resultado de equipo registrado. Solo las jornadas de competición determinan puntos y marcadores de esta consulta.

Además, cada equipo ofrece su racha de los últimos cinco resultados. Las jornadas se ordenan por fecha y, a igualdad de fecha, por identificador. La respuesta utiliza `V`, `E` y `D` en orden cronológico, con el más reciente al final. Con menos de cinco jornadas se muestran únicamente las disponibles; sin jornadas, una lista vacía.

Una secuencia de resultados 2–1, 0–0 y 1–3, en ese orden cronológico, produce una racha `V, E, D` y cuatro puntos. Añadir resultados con fechas anteriores no debe confundirse con añadirlos al final de la racha solo por ser la última petición recibida.

Resuelve los cálculos con Java y los conocimientos del tema. No se exige utilizar el Aggregation Pipeline de Ampliación ni incorporar transacciones entre documentos.

## Comprobaciones de integración

La interfaz ya conoce las operaciones de consultas, disponibilidad, jornadas, clasificación y racha. El backend debe respetar sus rutas y estructuras. Reutiliza [el contrato de errores HTTP](../firestore/rest.md) y el diagnóstico de [conflictos y atomicidad](atomicidad.md).

Comprueba el recorrido guiado después de incorporar las funciones: edición del equipo, mantenimiento de jugadores, filtros y suma ordinaria de estadísticas. Los cambios nuevos no deben reiniciar plantillas ni perder actualizaciones realizadas por otra operación.

El starter marca las funciones nuevas como pendientes y conserva las anteriores. Los tests deben observar el estado posterior a un rechazo y una versión obsoleta, además de sus respuestas. No se considera resuelto un requisito porque la interfaz muestre un mensaje de éxito.

## Solución completa

[Descargar solución de los tres niveles](/fp-dam2-acda/descargas/codigo/jugadores-final-solucion.zip){ .md-button download="jugadores-final-solucion.zip" }

[Ver código, README y pruebas](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/mongodb/final-solucion)

La solución conserva el agregado del equipo y separa persistencia, reglas y presentación. El resultado de un equipo no se propaga a un rival mediante escrituras ocultas.
