# Auditoría interna — Firestore y MongoDB

Fecha: 2026-11-08. Estado: correcciones preparadas para revisión docente; este informe no sustituye la revisión humana ni la integración web/PDF del agente principal.

## Alcance y fuentes

Revisión de las veinte páginas de ambos temas, cuatro variantes por tecnología, enunciados y requisitos finales. Fuentes: CONTENIDOS_ACDA.md, PLAN-DESARROLLO.md, AGENTS.md, TEACHING-STANDARD.md del clon autorizado y skills fp-revision-didactica, fp-contenidos y fp-practicas. Se contrastaron también las referencias oficiales de DocumentReference, Spring Data MongoDB y atomicidad MongoDB. No se modificaron estándares ni skills compartidas.

Frontend HTML/CSS/JS intacto. No se modificaron scripts globales, navegación, estado de desarrollo ni guía manual. Los índices se ajustaron tras la liberación del agente principal. No se construyó sitio ni se generaron ZIP/PDF; esa integración corresponde al principal.

## Hallazgos principales y corrección

1. **Importante, Firestore:** el TODO2 agrupaba conversión, CRUD y consultas; el alumno no podía comprobar una primera escritura sin terminar servicio y HTTP. Se dividió2a/2b/2c/2d y se entregaron fases independientes de conversión, alta-lectura, CRUD, consultas, servicio y REST. La limpieza de alta-lectura usa el SDK preparado, no un futuro TODO de borrado.
2. **Importante, Firestore:** Postman se describía como Runner completo desde el inicio, aunque sus escrituras y limpieza requerían todos los controladores. Se añadieron carpetas00 (catálogo y errores, sin TODO) y01 (GET tras repositorio), conservando íntegros CRUD y práctica final. La guía diferencia petición individual y Runner completo.
3. **Bloqueante técnico, Firestore:** un documento con inicio9.5 fue leído como9 por getLong. La nueva prueba real falló antes de corregirlo. Se comprueba ahora el objeto Long original mediante get y después Math.toIntExact en las tres variantes resueltas; la teoría explica que getLong no comprueba el tipo original. El starter conserva su tarea de conversión y una pista parcial correcta.
4. **Importante, MongoDB:** el starter exigía MongoTemplate/Criteria/Update/findAndModify pese a centrar el recorrido en Spring Data y servicios. EstadisticasRepository.sumar se entrega completo; el TODO4 pasa a LigaService para validar y delegar, sin perder incremento atómico, límites ni versión.
5. **Importante, MongoDB:** equipos, jugadores y estadísticas estaban mezclados en un TODO genérico. Se separaron3a/3b/4 y las fixtures permiten comprobar cada fase sin programar las futuras. Se explicó cómo reconstruir un record conservando plantilla y versión.
6. **Importante, finales:** faltaban pruebas utilizables por nivel en los starters y puntos de cambio en métodos heredados. Se añadieron niveles optativos, concurrencia preparada y marcas en Firestore crear/actualizar y Mongo eliminarJugador. No se resolvieron algoritmos finales.
7. **Menor, orientación pública:** se sustituyeron notas de QA/producción docente por instrucciones útiles de desarrollo local y transición cloud; repaso conserva exactamente cinco fichas, sin promesas de publicación futura. Se mantuvo la advertencia de revocar una credencial expuesta aunque se retire un commit y el significado de instantánea histórica.

## Nueve preguntas del recorrido

| Pregunta | Firestore | MongoDB |
| --- | --- | --- |
| Qué aprendo | Documentos→conversión→SDK→servicio→HTTP | Agregado→Spring Data→equipos→jugadores→estadísticas |
| Qué necesito | Spring/Java y mapas/listas; HTTP se enseña aquí | Spring/REST ya explicados; Java con colecciones |
| Dónde estoy | Mapa de fases y subpasos2a–4c, con enlaces a teoría | Fases consultas/equipos/jugadores/estadísticas |
| Qué está preparado | Modelo/DTO, esperar, conId, validación de apoyo, frontend, Postman | DTO/modelo, sedes, apoyos de validación y helper atómico completo |
| Dónde programo | Rutas repository/service/api y métodos concretos | EquipoRepository y métodos de LigaService |
| Qué resultado observo | Test de capa, documento, estado HTTP y GET posterior | Entidad recuperada, plantilla/cifras/versiones y UI |
| Qué error pruebo | Tipo incompatible, ausencia, capacidad,500 preparado | Dorsal, sede, límites, copia antigua y concurrencia |
| Qué pista tengo | Fragmentos parciales de mapa, tipo, SDK/query y ResponseEntity | Firma derivada/@Query, reconstrucción de record y llamada al helper |
| Cuándo avanzo | Fase verde y estado comparado; API completa solo después de4c | Fase verde sin exigir el siguiente TODO |

## Revisión por página

| Página | Hallazgo/corrección o decisión de conservar | Evidencia y salida esperada |
| --- | --- | --- |
| firestore/index | Recupera prerrequisitos y anuncia las capas/fases sin presentar HTTP como conocido | Enlaces a guía y teoría; no requiere cloud |
| firestore/documentos | Se concretan Reserva/Espacio/ReservaRequest y campos de cliente frente a servidor | Identificar ID/estado y dos entradas de mapa sin perder datos |
| firestore/configuracion | Emulador vs cloud explicado por utilidad del alumno; se mantiene separación de procesos/IAM/secretos | Emulador8085, aplicación8080; proyecto demo sin credenciales |
| firestore/persistencia | Lectura descompuesta en referencia/futuro/esperar/Optional; conversión estricta y ruta inversa | Fases conversión, alta-lectura y repositorio |
| firestore/consultas | Se vincula TODO2d con fixture que discrimina sala/rango, sin ocultar fallos con[] | Fase consultas y GET preparado |
| firestore/rest | Respuesta201/Location y204 explicadas; Postman por pasos, error seguro reproducible | Fase rest y peticiones de carpeta00/01/CRUD |
| firestore/proyecto | Subpasos alcanzables con ruta/método, fragmentos/pistas, comprobación y criterio de avance | Fases optativas sin futuros TODO; interfaz sin editar |
| firestore/practica-final | Se conserva cada reto; se añaden comprobaciones por nivel y puntos heredados que cambian | Niveles base/aplicacion/reto y coordinación preparada |
| firestore/ampliacion | Se conserva OpenAPI/batch/transacciones/Auth/listeners/seguridad como fragmentos conceptuales contextualizados | No se presenta autenticación/listeners como función de la app local |
| firestore/repaso | Se retira promesa futura, orientación a repasar lo disponible | Cinco En preparación, sin nuevas descargas |
| mongodb/index | Se hace visible el helper avanzado preparado y la separación equipos/jugadores | No exige MongoTemplate ni Aggregation |
| mongodb/documentos | Se alinea referencia con sedeId del proyecto, sin sugerir otra colección de jugadores | Equipo contiene plantilla; sede vive aparte |
| mongodb/configuracion | Se especifica reset por datos propios/fixture, no por borrar volumen | Bases acda_liga y acda_liga_final separadas |
| mongodb/persistencia | Se explica reconstrucción del record, preservación de plantilla y versión | Fases equipos/jugadores recuperan el agregado completo |
| mongodb/consultas | Firma Page/Pageable real, eliminación de default, @Query sin concatenación | Fixture lista/filtra sin depender de crear desde servicio |
| mongodb/atomicidad | API avanzada interpretada, no exigida; copia antigua/concurrencia reproducibles | Helper cambia versión y conserva incrementos |
| mongodb/proyecto | Equipos3a y jugadores3b separados; estadísticas4 delega en helper preparado | Cuatro fases con fixtures que evitan futuros TODO |
| mongodb/practica-final | Requisitos íntegros; fixtures de cálculo no requieren registrar jornadas antes | Niveles y cálculos de desempates/racha sin pipeline |
| mongodb/ampliacion | Conservados cinco ejemplos y explicación de resultado; no confundir goles personales con puntos | Cinco bloques ejecutados realmente por checker existente |
| mongodb/repaso | Orientación actual, cinco fichas conservadas | Sin promesa de otra entrega ni proyecto opcional creado |

## Firestore: cada requisito final

Los identificadores siguientes son internos. B=Base, A=Aplicación, R=Reto. Los tres niveles siguen acumulándose; no se rebajaron requisitos.

| Requisito | Implementación/contraste y comprobación |
| --- | --- |
| B1 Cancelar sin perder ID/datos | ReservaService/FirestoreReservaRepository; nivelbase compara mapa/lista y estado |
| B2 Motivo1–120 y error400/404 | Validación de servicio; pruebas motivo vacío/121 e inexistente |
| B3 Repetir conserva primer motivo | Operación preparada para repetición; nivelbase usa otro motivo y compara |
| B4 Consultar/borrar cancelada y prohibir PUT/reactivar | Punto heredado actualizar marcado en starter; nivelbase409; CRUD/limpieza conserva contrato |
| B5 Estado permitido y responsable exacto sensible a mayúsculas | filtrar y servicio listar; nivelbase contrasta Recepción/recepción y estado inválido |
| A1 Solo activas de misma sala/fecha ocupan | Revisión de predicado; nivelaplicacion prueba ocupación y cancelación que libera |
| A2 Intervalo semiabierto y adyacencia | nivelaplicacion crea8–10 y10–11; edición9–11 rechazada |
| A3 Disponibilidad válida y entrada inválida | Validación reutilizada; nivelaplicacion prueba intervalo invertido400 |
| A4 Crear y editar, excluirse y conservar tras rechazo | coordinar preparado; nivelaplicacion edita sobre sí y compara edición fallida |
| A5 Decisión al guardar y concurrencia | Dos tareas sincronizadas confirman solo una; helper SDK sigue resuelto |
| R1 Máximo2 activas/día en todas las salas y normalización | nivelreto usa mayúsculas/espacios y tercera reserva en otra sala |
| R2 Cancelar libera y cambiar fecha/responsable respeta destino | nivelreto libera cupo y rechaza mover desde otro día; compara el original |
| R3 Páginas1–20, orden porID, cursor nulo final | nivelreto tamaño20/2 y límites0/21; comparación de páginas; prueba previa de tamaño1 |
| R4 Continuar sin repetición, cursor eliminado válido | nivelreto elimina el documento que marca cursor y comprueba continuación estable |
| R5 Cursor inválido400 y páginas independientes de filtros | validación/pagina separadas del listado; nivelreto ../x400, contrato revisado en controlador |
| Integración Errores/CRUD anterior | TestsAPI/servicio y emulador anteriores conservados; UI y Postman se vuelven a ejecutar |

## MongoDB: cada requisito final

| Requisito | Implementación/contraste y comprobación |
| --- | --- |
| B1 Filtrar goles0–100000 y disponibilidad opcional | filtrarJugadores; nivelbase prueba mínimo/disponibilidad, negativo400; límites superiores revisados en servicio |
| B2 Equipo ausente404 y lista vacía válida | obtener/filtrar; pruebas de ausencia y filtro sin coincidencias |
| B3 Cambiar disponibilidad sin reiniciar ni afectar jugador ajeno | nivelbase compara identidad/cifras y repetición; ausente404 |
| A1 Jornada identifica/fecha/rival/marcador | registrarJornada valida campos; tests de marcador inconsistente; bordes textuales revisados en código/DTO |
| A2 Identificador único por equipo sin doble suma | nivelaplicacion y prueba real concurrente; solo una jornada/participación |
| A3 Hasta20 aportaciones distintas, jugador existente/disponible | validación en servicio; no disponible probado en integración; duplicados/límite revisados y caso manual abajo |
| A4 Goles/asistencias0–100 y suma coherente | validación y acumulación antes de persistir; rechazo de marcador incoherente compara documento completo |
| A5 Máximo30 jornadas y estadísticas acumuladas acotadas | condiciones revisadas; límite estadístico del guiado probado en Mongo real; caso30+1 queda en guía manual |
| A6 Incluido suma partido incluso0; ausente no suma; vacía solo con0 | construcción de jugador/jornada revisada; las fixtures distinguen participantes; caso0–0 manual abajo |
| A7 Jugador histórico no se elimina | marcado el punto heredado del starter; nivelaplicacion comprueba409 |
| A8 Escritura indivisible y sin efecto sobre rival | un agregado con versión; nivelaplicacion compara tras rechazo y concurrencia, sin transacciones multidocumento |
| R1 Clasificación puntos3/1/0 y campos calculados | nivelreto conV/E/D obtiene4p y diferencia-1; no suma goles personales del guiado |
| R2 Orden puntos/diferencia/favor/nombre/ID | CalculosAprendizajeTest con lecturas preparadas cubre todos los desempates sin quitar índice |
| R3 Equipo sin jornadas tiene0; todas las categorías juntas | nivelreto compara equipo vacío; revisión de findAll sin filtro de categoría |
| R4 Últimos5 porfecha/ID, reciente alfinal; vacío | CalculosAprendizajeTest desordena6 fechas/IDs y esperaV,E,V,D,V; nivelreto verifica vacío |
| R5 Java sin Aggregation y no propagación bilateral | solución utiliza bucles/Comparator sobre equipo; Ampliación independiente, sin nuevas infraestructuras para el alumno |
| Integración CRUD/errores/versiones | Suite real previa y nuevas fases; UI conserva jugadores/cifras y permite cancelar |

Las filas que dicen «revisado» no se presentan como ejecución exhaustiva de todos sus valores frontera. La guía manual concreta los casos pendientes de interacción, sin cambiar los requisitos ni inventar resultados.

## Carga estimada interna (no publicar en web/PDF)

Estimaciones de planificación, no medidas con alumnado. Incluyen lectura/explicación del concepto y el trabajo guiado. No se fuerza una suma exacta a7 h: el extremo superior revela cuándo habrá que continuar fuera de clase o adaptar el ritmo. Requieren Java básico, colecciones y Spring previos.

| Firestore, tramo guiado | Rango |
| --- | --- |
| Modelo y distinción cliente/servidor |25–35 min |
| Arranque/emulador y configuración |25–40 min |
| Conversión2a |35–55 min |
| Alta/lectura2b y edición/borrado2c |50–75 min |
| Consultas2d |25–40 min |
| Servicio3a–3b |30–45 min |
| HTTP/controladores4a–4c/Postman |70–100 min |
| Integración, depuración y recapitulación |30–45 min |

Guiado y explicaciones: estimación 290–435 min. Las7 h son un marco de secuenciación, no una promesa de que cualquier grupo resolverá también la práctica final. Si HTTP es nuevo por completo o faltan Optional/record/mapas, habrá que reforzar esos prerrequisitos.

| MongoDB, tramo guiado | Rango |
| --- | --- |
| BSON/agregado/referencias |25–35 min |
| Docker/Compass/arranque |20–30 min |
| Repositorios/consultas |35–50 min |
| Equipos3a |35–55 min |
| Jugadores3b |50–75 min |
| Servicio estadísticas4/helper preparado |35–50 min |
| Atomicidad/versiones y análisis de pruebas |40–60 min |
| Integración/depuración |30–45 min |

Guiado y explicaciones: estimación 270–400 min. La ayuda atómica preparada evita convertir esta sesión en un curso adicional de MongoTemplate.

| Final, trabajo adicional al guiado | Base | Aplicación | Reto avanzado |
| --- | --- | --- | --- |
| Firestore |60–90 min |90–150 min |120–180 min |
| MongoDB |45–75 min |100–160 min |110–180 min |

Base puede comenzarse parcialmente en clase según el grupo. Aplicación/Reto probablemente necesitan trabajo autónomo y puesta en común posterior. No se han simplificado reglas para hacerlos caber dentro de las7 h ordinarias.

## Pruebas y comandos reales

Ejecutados desde cada carpeta de solución con Maven Wrapper Windows y servicios propios. Equivalente CI: sh mvnw con los mismos argumentos.

- Firestore/guiado-solucion: `.\mvnw.cmd -q -Dqa.emulador=true -Daprendizaje.fase=todas test`.
- Firestore/final-solucion: `.\mvnw.cmd -q -Dqa.emulador=true -Daprendizaje.nivel=todos test`.
- MongoDB/guiado-solucion: `.\mvnw.cmd -q -Dqa.mongo=true -Daprendizaje.fase=todas test`.
- MongoDB/final-solucion: `.\mvnw.cmd -q -Dqa.mongo=true -Daprendizaje.nivel=todos test`.

Regresión real final, incluyendo alta-lectura: **Firestore guiado16/final15; MongoDB guiado16/final23**, cero fallos, errores y omisiones. Las nuevas pruebas activadas son6/3/4/6, respectivamente. Prueba de dato corrupto9.5: rojo antes de corregir getLong, verde con comprobación estricta. Los tests de fase/nivel se ejecutan también contra soluciones, no solo omitidos en starters.

Maven package de las ocho variantes ejecutado con éxito; después del último ajuste de pruebas se repitió `mvnw.cmd -q test` en las ocho con éxito.

| Tests default | Total | Omitidos explícitamente | Fallos/errores |
| --- | --- | --- | --- |
| Firestore guiado inicial/solución |7 /16 |6 /7 |0 |
| Firestore final inicial/solución |4 /15 |3 /6 |0 |
| Mongo guiado inicial/solución |5 /16 |4 /8 |0 |
| Mongo final inicial/solución |7 /23 |6 /13 |0 |

Las omisiones default corresponden a fases/niveles/integración elegidos mediante propiedad. En las soluciones se activaron realmente con los comandos anteriores, no se presentan como comprobaciones superadas por omitirlas. Default de los starters comprueba infraestructura, no exige que UnsupportedOperationException siga existiendo. No se añaden pruebas que penalicen completar un TODO.

Ampliación Mongo: `.venv/Scripts/python.exe scripts/check_mongodb_examples.py --container acda-auditoria-mongo-20261108` PASS: cinco bloques documentados, goles9, intervenciones8, lookup3, índice y explain2resultados; base propia temporal.

Postman: las cuatro colecciones se validaron con los verificadores existentes de URLs y sintaxis JavaScript, sin modificar scripts globales. Ejecución real con Newman6.2.1 y EXIT0, sin fallos de aserciones ni scripts: inicial carpeta00 **3 peticiones/3 aserciones**, solución guiada completa **16/23**, solución final completa **33/52**. InformesJSON en qa-captures/auditoria-postman-{inicial,guiado,final}.json. Comando guiado: `npx --yes newman@6.2.1 run proyectos/firestore/guiado-solucion/postman/reservas.postman_collection.json --env-var baseUrl=http://127.0.0.1:8084 --bail --reporters json --reporter-json-export qa-captures/auditoria-postman-guiado.json`; final utiliza su colección y puerto8086; inicial añade `--folder "00 · Primer contacto HTTP (antes de los TODO)"` y puerto8087.

Recorrido real de aplicación, sin editar frontend: `scripts/check_firestore_app.py --base http://127.0.0.1:8084 --label guiado` y8086/final PASS; `scripts/check_mongodb_app.py --base http://127.0.0.1:8082 --label guiado` y8083/final PASS. Comprueban CRUD, entrada incorrecta, errores, conservación de listas/mapas/estadísticas, cancelación, móvil/escritorio y claro/oscuro; los finales añaden disponibilidad/cancelación/páginas o jornadas/racha/clasificación. Scripts globales solo ejecutados, no modificados.

## Datos para la guía manual del principal

### Descargas y puntos de entrada

- Firestore: reservas-{guiado,final}-{inicial,solucion}.zip. Ejecutar config.Emulador.main y después ReservasApplication.main; aplicación8080, emulador8085. Clase base: es.mjsplayground.acda.firestore.reservas.
- MongoDB: jugadores-{guiado,final}-{inicial,solucion}.zip. Docker Desktop, config.MongoLocal.main y después LigaApplication.main. Aplicación8080, Mongo27018; base acda_liga oacda_liga_final. Clase base: es.mjsplayground.acda.mongodb.liga.
- IntelliJ: abrir carpeta conpom.xml, JDK25, sincronizar Maven y comprobar Working directory del lanzador junto a compose.yaml. Run real de IntelliJ no ejecutado en esta auditoría: los comandos CLI no se presentan como prueba de botones/ventanas del IDE.

### Entorno y reset seguro

Emulador propio8085: JAR oficial1.22.0 reutilizado de caché; PID 3460, proyecto localdemo-acda-reservas. Sin credenciales. Las pruebas nuevas crean proyectosdemo-fase-.../demo-niveles-... separados de la interfaz. Reiniciar emulador pierde sus datos salvo exportación configurada; no se indica hacerlo como solución de un TODO.

Mongo propio: acda-auditoria-mongo-20261108, loopback27018, imagenmongo@sha256:b9924c330a8bbc5800fe9c69f04a84e221eca7b28fd049a0354147d9908fa142,8.0.34, GLIBC_TUNABLES=glibc.pthread.rseq=1; comprobado usingPerCPUCaches=false. Tmpfs /data/db y/data/configdb, sin volúmenes ajenos. Nuevos tests usan basesacda_qa_fases_UUID/acda_qa_niveles_UUID y comprueban su nombre antes de borrar. Para repetir por interfaz, eliminar solo datos del escenario; no borrar un volumen.

### Fases desde IntelliJ

- Guiado Firestore: Run JUnit de FasesAprendizajeTest con VM options `-Daprendizaje.fase=conversion`, alta-lectura, repositorio, consultas, servicio orest. Las fases que acceden al SDK necesitan emulador. La limpieza dealta-lectura no depende de borrado pendiente.
- Guiado Mongo: Run JUnit de FasesAprendizajeTest con consultas, equipos, jugadores oestadisticas. Mongo abierto; fixture y concurrencia preparadas.
- Final Firestore: Run JUnit de NivelesAprendizajeTest con `-Daprendizaje.nivel=base|aplicacion|reto` (elegir un valor, no el texto con barras).
- Final Mongo: RunTests del paquete con la misma propiedad; incluyeNivelesAprendizajeTest y CalculosAprendizajeTest. No hay código de clasificación resuelto en fixtures.

### Casos manuales concretos

| Firestore | Datos y resultado esperado |
| --- | --- |
| Guiado | SALA-A, fecha2027-02-15, 10–12, responsableCoordinación, asistentes8. Equipo: `["Proyector, HD"]`. Detalles: `{"actividad":"Taller","contacto":"Recepción"}`. Alta201+Location; GET conserva todos los valores. Editar solo responsable conserva lista/mapa.13 asistentes produce400 sin cambiar la reserva; DELETE204 y GET404. |
| Base | Cancelar con «Cambio» y repetir con «Otro»: mismoID y primer motivo. Motivo vacío o121 caracteres→400; ID inexistente→404; PUT sobre cancelada→409. Filtro exacto distingue Recepción/recepción. |
| Aplicación | Misma sala/día: 10–12 aceptada, 11–13 rechazada, 12–13 adyacente aceptada. Cancelar libera la primera franja. Edición rechazada conserva horario y demás campos. |
| Reto | Dos activas de Coordinación/coordinación en un día, sumando salas; tercera→409. Cancelar libera cupo. Tres documentos, tamaño2: continuación sin repetir; eliminar el ID cursor no invalida la posición. Límites0/21 y cursor../x→400. La concurrencia se ejecuta con la prueba preparada. |

| MongoDB | Datos y resultado esperado |
| --- | --- |
| Guiado | Equipo SENIOR/CENTRO, Alex dorsal7 MEDIO y Luz dorsal8 DEFENSA. Incremento2 goles/1 asistencia produce1 partido/2 goles/1 asistencia. Editar nombre/dorsal conserva cifras; dorsal repetido→409. |
| Base | Marcar Luz no disponible y filtrar por mínimo de goles/disponibilidad true o false. Repetir selección conserva estadísticas. Jugador ajeno→404. |
| Aplicación | Jornadaj1, fecha2027-02-15, rivalNorte, marcador2–1. Alex1 gol/1 asistencia y Luz1 gol/0 asistencias. Rechazar mientras Luz no esté disponible y aceptar después: ambos suman1 partido. Repetirj1 no suma. Marcador2 con aportaciones que suman1 rechaza sin cambios.0–0 sin aportaciones solo añade resultado. |
| Reto | Secuencia V/E/D produce4 puntos. Los últimos cinco se ordenan por fecha/ID, no llegada. El test de cálculos prepara empates de todos los criterios, incluidos ID y nombres que no se guardan para no quitar el índice único. |

Los bordes de30 jornadas y la participación acumulada se revisaron en el código; no se declara ejecutada una prueba específica30+1 nueva. Para ejecutarlos manualmente, la futura guía debe usar una fixture/base propia y comparar documento completo, nunca modificar una base compartida de Compass. El límite acumulado de estadísticas sí se ejecutó en la suite real ya existente. Las dos peticiones simultáneas se reproducen con los tests preparados, sin exigir al alumno crear un lanzador concurrente.

### Cierre de procesos de esta auditoría

Se detuvieron solo los procesos propios: aplicaciones PID27400/26060/28308/11524/18256 y emuladorPID 3460, comprobando sus líneas de comando. Se validó la etiqueta de propietario antes de detener acda-auditoria-mongo-20261108. Los puertos27018 y8082–8087 quedaron sin listeners. No se tocaron MySQL3308 ni servicios/volúmenes ajenos. Los datos temporales del emulador y tmpfs no representan trabajo de alumnado.

### Límites de evidencia

No se verifican IAM/credenciales cloud reales ni login Firebase: los ejemplos de Ampliación son conceptuales y sus dependencias están indicadas. No se afirman capturas reales de IDE/Compass ni generación de ZIP/PDF por este agente. Los rangos de carga no están medidos con alumnado. Integración de enlaces/HTML/PDF/publicación pendiente del principal. Fronteras finales indicadas como lectura de código deben distinguirse de ejecución real; no se declaran pasadas por inferencia.
