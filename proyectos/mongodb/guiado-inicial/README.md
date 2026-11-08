# Equipos y jugadores — guiado-inicial

## Ejecutar con IntelliJ

Abre esta carpeta como proyecto Maven, espera la sincronización y selecciona JDK25. Con Docker Desktop activo, ejecuta `config.MongoLocal.main` para iniciar MongoDB con Compose; configura como Working directory esta carpeta (contiene compose.yaml). Si ya tienes la instancia MongoDB local de este proyecto en27018, reutilízala. No arranques otra variante de Compose a la vez.

Luego ejecuta `LigaApplication.main` en `es.mjsplayground.acda.mongodb.liga`. Abre http://127.0.0.1:8080. No hay que editar HTML/CSS/JavaScript. Los datos persisten en el volumen Docker liga-data: parar Spring no los borra. No elimines volúmenes para repetir un ejercicio; crea equipos nuevos y elimina solo los de prueba desde la aplicación.

Configuración: MONGODB_URI cambia la conexión; default mongodb://127.0.0.1:27018/acda_liga con timeout selección/conexión3s. PORT cambia puerto HTTP. Compose fija MongoDB Community8.0.34 por digest oficial y publica solo127.0.0.1:27018. El servidor HTTP también escucha loopback. Este entorno de desarrollo local carece de autenticación: no exponer fuera del equipo.

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

Etapa1 examina entidades/DTO y sedes preparados. TODO2 EquipoRepository: cambia métodos default de apoyo a consultas derivadas/@Query reales conservando las firmas. TODO3 LigaService: crear/actualizar equipo y agregar/editar/eliminar jugador con reglas y guardado optimista; validar/validarJugador y conJugadores son apoyos preparados. TODO4 LigaService.sumarEstadisticas: comprobar equipo/jugador e incrementos y delegar en el helper atómico preparado. EstadisticasRepository.sumar se entrega COMPLETO: no se programa MongoTemplate, Criteria ni findAndModify. REST y frontend preparados. El inicial lista equipos sin filtros nuevos mediante MongoRepository heredado, y muestra501 al invocar etapas pendientes. Las pruebas del inicial incluyen comprobaciones optativas por fases y no obligan a conservar los TODO pendientes.

## Comprobaciones optativas por fases

En IntelliJ abre `FasesAprendizajeTest` dentro de src/test/java/es/mjsplayground/acda/mongodb/liga, crea su configuración JUnit y añade VM options `-Daprendizaje.fase=consultas`. Mantén MongoDB abierto. Cambia el valor al completar cada paso:

| Valor | Qué comprueba | Lo que todavía no exige |
| --- | --- | --- |
| consultas | categoría/posición/combinación y página vacía | crear equipos desde servicio |
| equipos | crear/actualizar conservando plantilla y versión | operaciones de jugadores |
| jugadores | altas/ediciones/bajas con dorsal único y cifras conservadas | servicio de estadísticas |
| estadisticas | delegación segura, copia antigua y ocho incrementos concurrentes | implementar MongoTemplate o hilos |

Los tests cargan sus fixtures sin utilizar los TODO futuros y limpian solo una base acda_qa_fases_UUID con nombre comprobado. No borran acda_liga. Sin propiedad quedan omitidos. `todas` corresponde al final del recorrido. Primero completa consultas en EquipoRepository (TODO2), después equipos y jugadores en LigaService (TODO3a/3b) y finalmente la coordinación de estadísticas en ese mismo servicio (TODO4). La actualización atómica avanzada está terminada en todas las variantes.

Alternativa técnica opcional: `.\mvnw.cmd -Daprendizaje.fase=consultas test`; recorrido completo con Mongo real: `.\mvnw.cmd -Daprendizaje.fase=todas test`. La prueba de copia antigua y concurrencia ya proporciona sincronización, incrementos y comparación del estado final; no hay que programar una herramienta adicional ni manipular versiones por HTTP.
