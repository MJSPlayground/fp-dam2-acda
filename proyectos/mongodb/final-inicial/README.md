# Equipos y jugadores — final-inicial

## Ejecutar con IntelliJ

Abre esta carpeta como proyecto Maven, espera la sincronización y selecciona JDK25. Con Docker Desktop activo, ejecuta `config.MongoLocal.main` para iniciar MongoDB con Compose; configura como Working directory esta carpeta (contiene compose.yaml). Si ya tienes la instancia MongoDB local de este proyecto en27018, reutilízala. No arranques otra variante de Compose a la vez.

Luego ejecuta `LigaApplication.main` en `es.mjsplayground.acda.mongodb.liga`. Abre http://127.0.0.1:8080. No hay que editar HTML/CSS/JavaScript. Los datos persisten en el volumen Docker liga-data: parar Spring no los borra. No elimines volúmenes para repetir un ejercicio; crea equipos nuevos y elimina solo los de prueba desde la aplicación.

Configuración: MONGODB_URI cambia la conexión; default mongodb://127.0.0.1:27018/acda_liga_final con timeout selección/conexión3s. PORT cambia puerto HTTP. Compose fija MongoDB Community8.0.34 por digest oficial y publica solo127.0.0.1:27018. El servidor HTTP también escucha loopback. Este entorno docente local carece de autenticación: no exponer fuera del equipo.

## Modelo documental

Cada Equipo es un documento con ObjectId, nombre único1–60, categoria JUNIOR/SENIOR, sedeId, jugadores embebidos y version. Cada Jugador embebido tiene UUID, nombre1–60, dorsal1–99 único en su equipo, posición PORTERO/DEFENSA/MEDIO/DELANTERO, disponible y subdocumento Estadisticas(partidos,goles,asistencias). Plantilla máxima20. Los jugadores no tienen colección independiente: el equipo es el agregado que limita tamaño y conserva sus invariantes.

Sede es una colección sencilla, con referencias por id: CENTRO=Pabellón Centro/Bilbao y NORTE=Campo Norte/Bilbao. La infraestructura prepara estas sedes al arrancar; no las recrea ni sobrescribe en cada petición. No hay @DBRef ni JOIN automático. Las sedes se consultan como catálogo preparado, no se editan desde este proyecto.

## API e interacción

GET /api/sedes. GET/POST /api/equipos. GET/PUT/DELETE /api/equipos/{id}. POST /api/equipos/{id}/jugadores; PUT/DELETE /api/equipos/{id}/jugadores/{jugadorId}; PATCH /api/equipos/{id}/jugadores/{jugadorId}/estadisticas con {goles,asistencias}0–20: suma un partido y esos incrementos.

POST equipo responde201+Location; cambios de plantilla/estadísticas responden200 con el agregado actualizado; DELETE equipo204. GET equipos usa categoria/posicion opcionales y pagina0..10000, tamanio1..20. Respuesta {elementos,totalElementos,totalPaginas,pagina}, orden estable nombre,id. Consulta derivada por categoría y @Query sobre posición embebida. Se incluyen índices nombre único, categoría/nombre y posición/nombre.

Nuevo/Editar abren formularios, Guardar los cierra y actualiza datos; Cancelar conserva el estado anterior. Eliminar pide confirmación con Cancelar seguro. Ver plantilla abre jugadores del equipo. Los mensajes se muestran sin insertar HTML de entrada; teclado y tema claro/oscuro preparados. Si una actualización concurrente produce409, actualiza la lista y vuelve a intentar con los datos vigentes.

Errores estructurados {estado,mensaje,ruta,instante}:400 entrada inválida (incluidos enteros fraccionarios),404 ausencia,409 nombre/dorsal/capacidad/conflicto de versión,500 fallo técnico con detalle solo en log,501 etapas pendientes. No se ocultan fallos de persistencia ni se sustituye MongoDB por un mapa.

## Atomicidad y límites

@Version hace que save de una instantánea antigua falle: no sobrescribe otra modificación. Las reglas de plantilla viven en servicio y el guardado con versión cambia un único documento. El cliente puede volver a intentarlo después de actualizar; no se oculta el conflicto con reintentos ciegos.

EstadisticasRepository.sumar usa MongoTemplate.findAndModify, predicados sobre jugador/límites y $inc; incrementa también version para invalidar instantáneas previas. Un jugador admite hasta10000 partidos y100000 goles/asistencias acumulados. Consultar y después guardar sin CAS perdería incrementos; la solución lo prueba con operaciones concurrentes y copia antigua.

No hay transacciones multidocumento. Este modelado tiene documentos acotados; no extrapolar la plantilla embebida a colecciones sin límite. La referencia de sede no implica cascadas.

## Postman y pruebas

Importa postman/liga.postman_collection.json y ejecuta carpetas en orden. baseUrl es la única variable de conexión; IDs se capturan automáticamente. El Runner crea dos equipos y los elimina al terminar. Los TODO del inicial deben completarse antes de esperar que pasen esos escenarios. No ejecutar sobre datos de producción.

QA opcional desde esta carpeta: `.\mvnw.cmd clean test` sin Mongo; `.\mvnw.cmd -Dqa.mongo=true test` con Mongo real local; `.\mvnw.cmd package` genera JAR. Los tests de integración usan una base nueva acda_qa_UUID y eliminan solo esa base al terminar, comprobando nombre para evitar otra ubicación. MONGODB_URI permite cambiar host, pero la base de tests se sobreescribe deliberadamente. Las pruebas unitarias sí aíslan dependencias para errores; la aplicación real usa Spring Data MongoDB.

Versiones: Java25, Spring Boot3.5.7, Spring Data/driver gestionados por ese BOM, MongoDB Community8.0.34 por digest de Compose. Logging SLF4J/Log4j2. Fuentes: https://www.mongodb.com/docs/v8.0/tutorial/install-mongodb-community-with-docker/ ; https://docs.spring.io/spring-data/mongodb/reference/ ; https://docs.spring.io/spring-data/mongodb/reference/mongodb/template-crud-operations.html .

## Alcance de esta variante

Parte del guiado resuelto, con jornadas/modelos/DTO y frontend nuevos preparados. TODO generales en filtrarJugadores, disponibilidad, registrarJornada, clasificacion y racha. Las operaciones del guiado siguen funcionando. No se distribuyen los algoritmos nuevos dentro del starter ni pruebas que los implementen. Consulta enunciado para restricciones de historial y razonamiento de clasificación; no añadas Aggregation como requisito.

## Competición

El agregado añade jornadas embebidas (máximo30): id estable1–60 alfanumérico/guion/guionbajo, fecha ISO, rival1–60, golesFavor/Contra0–100 y hasta20 aportaciones sin repetir jugador. Una aportación tiene jugadorId,goles,asistencias0–100; cada participante disponible suma un partido, incluso con0goles. La suma de goles debe coincidir con el marcador y las asistencias no superarlo. Se permite lista vacía cuando golesFavor0: nadie suma participación.

PATCH .../jugadores/{jugadorId}/disponibilidad {disponible:boolean}. GET .../jugadores?minGoles=0&disponibles=true/false (omitido=todos). POST /api/equipos/{id}/jornadas guarda marcador y estadísticas de participantes juntos, con @Version sobre un solo equipo. Repetir id produce409 sin duplicar estadísticas. No se edita ni elimina una jornada en este alcance. Un jugador con historial de jornadas no se elimina (409).

GET /api/clasificacion calcula en Java, sin Aggregation:3puntos victoria,1empate,0derrota; desempata por diferencia, goles a favor, nombre,id. Incluye todos los equipos incluso sin jornadas, sin separar categorías. Devuelve equipoId,nombre,jugados,puntos,golesFavor,golesContra,diferencia. GET /api/equipos/{id}/racha devuelve {resultados:[V,E,D]}, últimas5 por fecha/id, orden cronológico con la más reciente al final.

Cada jornada describe la participación de UN equipo frente a un rival textual. No actualiza simultáneamente al rival ni modela una competición federada bilateral: así el alumnado puede garantizar atomicidad sin transacciones multidocumento. El frontend de jornada prepara IDs y permite introducir los datos JSON de aportaciones; no hay que programar vistas.

Al abrir datos antiguos del guiado, jornadas ausente se interpreta como lista vacía; al editar equipo/jugador se conservan todas las jornadas. Las variantes usan bases distintas por defecto: guiado acda_liga y final acda_liga_final, en el mismo servidor. Así guardar con el modelo guiado no puede borrar jornadas desconocidas. No anules esta separación al configurar MONGODB_URI.
