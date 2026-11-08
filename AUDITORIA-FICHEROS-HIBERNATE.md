# Auditoría de aprendizaje: Ficheros e Hibernate

Fecha: 2026-11-08. Informe interno para integración y revisión docente; no constituye aprobación pedagógica. Fuentes: CONTENIDOS_ACDA.md, AGENTS.md, PLAN-DESARROLLO.md, TEACHING-STANDARD y skills fp-revision-didactica, fp-contenidos y fp-practicas. Se conserva el índice, la arquitectura y el frontend. No se han construido sitio, PDF ni ZIP en esta intervención.

## Resultado y cambios concretos

El material existente explicaba los contratos correctamente, pero algunas etapas exigían completar simultáneamente demasiados métodos antes de poder contrastar nada. Se han separado hitos y proporcionado oráculos optativos directamente en los starters. Las soluciones ejecutan esos mismos oráculos: no se deduce su corrección de que el starter compile con casos omitidos.

Ficheros introduce Maven, responsabilidades y logging antes del primer TODO. CSV pasa por lectura, escritura, CRUD y carga candidata por separado. La guía incorpora cuatro fragmentos Java orientativos (CSVRecord, lista candidata, Jackson y lectura filtrada), con entradas/salidas y trabajo pendiente. La introducción temprana de logging muestra el campo Logger preparado y llamadas INFO parametrizada y ERROR con causa. JSON y serialización mantienen su progresión de objetos a colecciones y apoyo preparado. RandomAccessFile continúa como demostración pequeña; XML permanece en Ampliación. Hibernate distingue tres pasos de mapeo, catálogos, CRUD, HQL, servicio y conservación transaccional. Su guía incluye fragmentos acotados de relación propietaria, find, consulta tipada y edición de entidad gestionada con apoyo de versión; no entrega guardarLibro completo. Se prepara `BibliotecaService.comprobarVersion` en las cuatro variantes; la lógica equivalente de las soluciones se extrae sin cambiar su contrato. Los alumnos utilizan ese apoyo, sin diseñar control de concurrencia.

No se resuelven los algoritmos de conciliación, aplicación, copiado, traslado ni fusión de los starters finales. Las pruebas crean entrada y comprueban resultado; los fixtures no contienen esos algoritmos. Los modelos, frontend, filtros seguros, escritura protegida y transacción siguen preparados.

## Cobertura de páginas, en orden curricular

| Página Ficheros | Hallazgo y actuación |
| --- | --- |
| index.md | Revisado el recorrido completo y prerrequisitos; enlace nuevo a comprobaciones por fases. |
| rutas.md | Maven/capas/logging aparecían demasiado tarde; introducción operativa antes de Path, con clases y responsabilidades concretas. Conservados Path/Files/listado sin recursión. |
| texto-csv.md | Lectura, escritura y validación se percibían como un bloque; hitos aislados y ejemplo -1 convertible pero inválido para negocio. Conservados UTF-8, recursos y Commons CSV/comillas. |
| json.md | Mapeo objeto-array precede árbol correctamente; añadido orden leer/guardar y comprobación de rango independiente de carga. Flags estrictos preparados, sin pedir diseñarlos. |
| binarios.md | Secuencia byte/Data/RAF/Serializable ya adecuada; pasos concretos Instantanea→writeObject→filtro→readObject y ensayo propio/corrupto. RAF no se amplía como requisito. |
| arquitectura.md | Buen reparto Repository/Service, causas y logging; nuevo orden ejecutable por capas y punto de interrupción antes de sustituir memoria. Simplificado lenguaje interno de QA. |
| proyecto.md | Etapa CSV demasiado acumulativa; separada en 2A/2B/2C/2D, con método, pistas, resultado y prueba. Ejecución JUnit con VM options; omitidos no equivalen a éxito. |
| practica-final.md | Reglas completas conservadas; faltaba mecanismo entregado para fallo/recuperación. Casos optativos por nivel y explicación de lote pequeño de test frente a lote público. No se dicta algoritmo. |
| ampliacion.md | Revisada íntegra; XML DOM/XXE, streaming, WatchService, escritura fiable y demos limitadas ya proporcionan entrada y expectativas. Sin cambio necesario de alcance ni de código. AmpliacionTest ejecutado esta ronda. |
| repaso.md | Conservadas cinco fichas En preparación; eliminada promesa interna de publicación futura y añadido itinerario guiado/final mientras no hay actividades. |

| Página Hibernate | Hallazgo y actuación |
| --- | --- |
| index.md | Revisados prerrequisitos sin JDBC/Spring y recorrido; enlace a fases ejecutables. |
| arquitectura.md | ORM/proveedor/capas progresivos; corregidos plurales del esquema conceptual a nombres reales libro/editorial/detalle. |
| configuracion.md | Revisada íntegra; MySqlLocal, puertos, esquemas separados, entorno, logging y update/validate bien delimitados. No se requiere rediseño. |
| entidades.md | Constructores/ID/columnas/version/XML ya explicados; añadido checkpoint previo a CRUD y apoyo de versión preparado. |
| persistencia.md | Añadido recorrido buscar→persist→remove y explicación de lambda/tarea transaccional entregada. Fallo después de flush reproducible sin cortar servidor. |
| relaciones.md | Faltaban expectativas ligadas a datos concretos; tabla Horizonte/Norte/Alex/Luz, retirada de vínculos y detalle, borrado conservando compartidos y comprobación en sesión nueva. |
| consultas.md | Añadido hito LibroRepository.listar y resultados concretos 2/1/0; parametrización contrastada con texto de inyección tratado como dato. |
| proyecto.md | Cinco asociaciones se exigían en un salto; tres comprobaciones sin servidor y luego MySQL real. Catálogos/CRUD/HQL separados; guardarLibro dividido en subpasos. Se explica apoyo versión y fallo ya proporcionado. |
| practica-final.md | Todos los requisitos conservados. Oráculos de copia independiente, traslado y restricción tardía de fusión para rollback real, sin implementar sus algoritmos en starter. |
| ampliacion.md | Revisados joins/proyecciones/agregados/página/nativo/N+1/XML; demos y pruebas reales existentes adecuadas. Simplificado término QA; AmpliacionTest ejecutado con MySQL real. |
| repaso.md | Cinco fichas mantenidas; texto alumno sin promesa interna de publicación. |

## Cobertura de cada nivel final

| Tema/nivel | Requisitos revisados y mecanismos entregados |
| --- | --- |
| Ficheros Base | Texto código/nombre sin distinguir mayúsculas, mínimo inclusivo y rango, CSV/JSON/BIN, selección vacía, memoria intacta y error escritura. FasesFinalTest.base proporciona ejemplo mínimo; suite InventarioTest/ConciliacionTest contrasta recorridos adicionales. |
| Ficheros Aplicación | CSV/JSON, productos nuevos/límite5000, coincidencia exacta tras strip, suma y límite1000000, nuevo precio, duplicados todas apariciones incluso fila inválida, incidencias/fila, fallo estructural distinto de fila inválida, caracteres CSV. FasesFinalTest.aplicacion aporta lote reproducible de cuatro productos/dos incidencias; lote público conserva cuatro incidencias. |
| Ficheros Reto | Previsualización inmutable y versión, respaldo previo nuevo y no sobrescrito, fallo de copia sin mutación ni archivo vacío abandonado, recuperación validada, informe CSV. FasesFinalTest.reto incluye inyección de IOException preservada, plan obsoleto, lectura independiente de backup y recuperación. Los otros casos siguen en ConciliacionTest y enunciado. |
| Hibernate Base | Copia ISBN/título únicos conservando precio/editorial/autores; detalle nuevo con identidad independiente; origen intacto; filtro autor/franja inclusiva. FasesFinalTest.base observa identidades; MySqlIntegrationTest verifica filtros y copia/traslado completos. |
| Hibernate Aplicación | Selección1..100 sin repetidos, destino distinto existente, descuento0..30/escala2 y HALF_UP, precio mínimo0.01, preservación de relaciones y transacción conjunta. FasesFinalTest.aplicacion contrasta ID inválido y 29.90→26.91; suite existente amplía casos. |
| Hibernate Reto | Origen/destino distintos/existentes, fichas completas, traslado sin rebaja, borrado origen, renombrado único permitiendo nombre del origen, conservación de relaciones/precios. FasesFinalTest.reto provoca colisión con tercera editorial tras escrituras y verifica rollback en operación nueva antes de una fusión válida. |

Los tests de aprendizaje no sustituyen todos los casos del enunciado ni la revisión humana. Las suites de solución conservan escenarios adicionales. No se incorporan proyectos de repaso ni requisitos nuevos.

## Estimación interna de carga y dificultad

Estimaciones orientativas de diseño, no tiempo observado ni promesa de terminación. No se publican en las páginas. Ampliación queda como profundización posterior. El alumnado necesita Java básico, colecciones, excepciones, lectura de tests y en Hibernate claves relacionales. Conviene realizar primer Run y lectura del fallo en aula; los mecanismos ya proporcionados evitan dedicar estas horas a inventar infraestructura.

### Ficheros: referencia lectiva 7 h

| Etapa o nivel | Minutos orientativos | Dificultad y apoyo |
| --- | ---: | --- |
| Etapa1: entorno/capas/logging35 + rutas25 | 60 | Inicial-media; localizar archivos de código y listado sin recursión. Fase rutas. |
| Etapa2A: lectura CSV | 35 | Media; campos lógicos y conversiones, fixture con comillas/salto. |
| Etapa2B: escritura CSV | 25 | Media; CSVPrinter y lector independiente. |
| Etapa2C: CRUD/validación | 35 | Media; identidad/alta frente a edición y duplicados. |
| Etapa2D: carga completa | 30 | Media-alta; estado candidato, validación total y ausencia de importación parcial. Etapa2 total125. |
| Etapa3: Jackson | 35 | Media; array a objetos y configuración estricta ya preparada. |
| Etapa4: binario propio | 40 | Media-alta; Instantanea, streams, filtro preparado y causas. |
| Etapa5: errores/consolidación | 20 | Media; matriz de conservación y cancelaciones. |
| Final Base | 25 | Media; selección/exportación reusa contratos. |
| Final Aplicación | 45 | Alta; duplicados globales, suma y clasificación de incidencias. |
| Final Reto | 55 | Alta; plan/versiones/recuperación y fallo preparado. |
| Margen de acompañamiento | 15 | Depuración y contraste personal. |
| Referencia lectiva | 420 | Incluye explicaciones y acompañamiento; no es la suma obligatoria de guiado y tres niveles finales. |

La conciliación y el Reto son los puntos de mayor carga. La estimación presupone dominio de colecciones; si el grupo no lo tiene, requerirá acompañamiento o más trabajo autónomo. No se han rebajado requisitos para ajustar la cifra.

### Hibernate: referencia lectiva 9 h

| Etapa o nivel | Minutos orientativos | Dificultad y apoyo |
| --- | ---: | --- |
| Preparación/arquitectura dentro etapa1 | 45 | Media; Docker/ORM y configuración entregados. |
| Etapa1A: 1:N | 25 | Media; propietario y mappedBy. Fase uno-muchos. |
| Etapa1B: M:N | 30 | Media-alta; identidad compartida y unión. Fase muchos-muchos. |
| Etapa1C: 1:1 | 20 | Media; dependencia/cascada/huérfano. Fase uno-uno. Etapa1 total120. |
| Etapa2A: catálogos y CRUD | 35 | Media; Session recibida y entidades gestionadas. |
| Etapa2B: HQL | 30 | Media; parámetros y resultados2/1/0. Etapa2 total65. |
| Etapa3: servicio/edición/demo | 65 | Alta; varias entidades y validaciones en un caso. Versión y transacción preparadas. |
| Etapa4: relaciones/ciclo de vida | 45 | Media-alta; comprobar qué desaparece y qué permanece en sesión nueva. |
| Etapa5: errores/rollback | 30 | Alta; flush frente a commit, fallo ya inyectado. |
| Final Base | 40 | Media-alta; copia de dependiente y filtro combinado. |
| Final Aplicación | 55 | Alta; conjunto, redondeo y atomicidad. |
| Final Reto | 70 | Alta; secuencia de cambios, restricción tardía y rollback completo. |
| Margen de acompañamiento | 50 | Primer entorno, lectura de SQL y depuración. |
| Referencia lectiva | 540 | Incluye explicaciones y acompañamiento; no implica terminar los tres niveles finales en clase. |

## Pruebas realmente ejecutadas

MySQL compartido coordinado con root: acda-auditoria-mysql-20261108, loopback3308, MySQL8.4.13. Variables: QA_MYSQL_ADMIN_URL=jdbc:mysql://127.0.0.1:3308/mysql?sslMode=DISABLED&allowPublicKeyRetrieval=true&serverTimezone=UTC, QA_MYSQL_ADMIN_USER=root, QA_MYSQL_ADMIN_PASSWORD=qa_root_local. Cada prueba usa su esquema UUID; este agente no detiene el contenedor ni elimina esquemas ajenos.

| Variante | Comando desde su carpeta | Resultado |
| --- | --- | --- |
| Ficheros guiado-inicial | `.\mvnw.cmd -q package` | exit0; StarterTest1; fases7 omitidas y UI1 omitida. |
| Ficheros guiado-solucion | `.\mvnw.cmd -q '-Dfase=completo' package` | exit0; Ampliacion5 + Inventario9 + FasesGuiado7; UI1 omitida. |
| Ficheros final-inicial | `.\mvnw.cmd -q package` | exit0; StarterTest1; fases3 y UI1 omitidas. |
| Ficheros final-solucion | `.\mvnw.cmd -q '-Dfase=completo' package` | exit0; Conciliacion6 + Inventario9 + FasesFinal3; UI1 omitida. |
| Hibernate guiado-inicial | `.\mvnw.cmd -q package` | exit0; StarterTest1; fases8 y UI1 omitidas. |
| Hibernate guiado-solucion | `.\mvnw.cmd -q '-Dfase=completo' '-Dqa.mysql=true' package` | exit0; Ampliacion3 + BibliotecaService3 + MySqlIntegration5 + FasesGuiado8; UI1 omitida. |
| Hibernate final-inicial | `.\mvnw.cmd -q package` | exit0; StarterTest1; fases3 y UI1 omitidas. |
| Hibernate final-solucion | `.\mvnw.cmd -q '-Dfase=completo' '-Dqa.mysql=true' package` | exit0; BibliotecaService3 + MySqlIntegration7 + FasesFinal3; UI1 omitida. |

Logs locales: target/auditoria.log; informes Surefire en cada target/surefire-reports. Algunos informes pueden quedar actualizados por la ejecución UI posterior; la tabla distingue las rondas.

### Regresión de las pruebas UI del starter

Se encontró un defecto adicional: los tests UI antiguos exigían literalmente «Etapa pendiente» en los iniciales, de modo que completar correctamente el backend podía romperlos. Ahora reconocen el estado realmente pendiente y siguen el recorrido funcional cuando ya está completado. Se protege además el acceso al Scene de un diálogo que ya se cerró al guardar. No se modifica la UI de producción.

Ejecutado en los cuatro starters: `.\mvnw.cmd -q '-Dqa.mysql=true' '-Dqa.ui=true' '-Dtest=*UiTest' test`: cuatro exit0, un test real por variante. Para demostrar compatibilidad tras completar TODO, se crearon cuatro copias temporales de las soluciones y se sustituyó ÚNICAMENTE su test UI por el test correspondiente del starter. Misma orden: cuatro exit0. El primer ensayo de Hibernate final detectó NullPointerException al consultar la escena del diálogo ya cerrado; se corrigió la guardia y se repitió con exit0. Copias propias en `%TEMP%/acda-auditoria-tests-completados-_45w0z61`, logs target/auditoria-ui.log.

No se simula interacción con IntelliJ. Son controles JavaFX reales mediante Platform/Stage, con capturas en target/qa. Producción/frontend intactos; las comprobaciones web/PDF/ZIP/Pages corresponden a root tras integrar.

## Comandos para integración automática de los oráculos

Desde cada carpeta de solución, activar todos los hitos con una orden:

- Ficheros guiado: `.\mvnw.cmd '-Dtest=FasesGuiadoTest' '-Dfase=completo' test` (7 casos).
- Ficheros final: `.\mvnw.cmd '-Dtest=FasesFinalTest' '-Dfase=completo' test` (3 casos).
- Hibernate guiado: `.\mvnw.cmd '-Dtest=FasesGuiadoTest' '-Dfase=completo' '-Dqa.mysql=true' test` (8 casos).
- Hibernate final: `.\mvnw.cmd '-Dtest=FasesFinalTest' '-Dfase=completo' '-Dqa.mysql=true' test` (3 casos).

Fases independientes: Ficheros guiado rutas/csv-lectura/csv-escritura/crud/carga/json/bin; Hibernate guiado uno-muchos/muchos-muchos/uno-uno (o mapeos para agruparlas)/catalogos/repositorio/consultas/servicio/rollback. Finales: base/aplicacion/reto. Las tres fases de mapeos no necesitan MySQL. Omitir fase evita ejecutar estos casos: CI debe activarlos explícitamente al menos en soluciones. En shell Unix, `./mvnw` sustituye a `mvnw.cmd`.

## Datos para guía manual de root

1. Abrir un starter con pom.xml, esperar Maven y ejecutar Launcher. Frontend preparado, ninguna edición visual requerida. Ficheros muestra tres productos en memoria; Hibernate puede avisar relaciones/etapa pendientes, sin simular persistencia.
2. Mostrar una configuración JUnit de FasesGuiadoTest con VM options -Dfase=csv-lectura (Ficheros) o -Dfase=uno-muchos (Hibernate). Antes del TODO se espera fallo; al completar solo esa etapa debe poder contrastarse sin implementar todo.
3. Contrastar fases completas en solución. En Hibernate, activar qa.mysql y variables administrativas exclusivamente de desarrollo; revisar esquema UUID propio, nunca vaciar biblioteca_guiado/final.
4. Ficheros: editar solo precio de producto con coma/comillas/salto en nombre y comprobar conservación; carga cuya segunda fila tiene stock negativo mantiene catálogo entero.
5. Hibernate: registrar libro, retirar detalle conservando libro, retirar Luz conservando autor, consultar Alex en ambos libros. Verificar tablas singulares libro/editorial/autor/detalle y tabla libro_autor.
6. Ejecutar fallo preparado rollback: tercer libro visible dentro de transacción antes del error; sesión posterior vuelve a cantidad anterior. No cortar Docker para demostrarlo.
7. Final Ficheros: lotes público (4productos/4incidencias) y test pequeño (4/2) son distintos. Reto rechaza propuesta obsoleta y copia fallida; recupera estado previo desde backup propio.
8. Final Hibernate: copiar con detalleId distinto; traslado29.90→26.91; fusión que colisiona con tercera editorial revierte todos los cambios, seguida de fusión válida.
9. Ejecutar acciones/cancelaciones y tamaños compactos con las pruebas UI existentes y revisar visualmente el sitio/PDF al integrar. No marcar estas salidas de publicación como revisadas por este informe.

## Límites y revisión humana pendiente

La carga de conciliación y coordinación transaccional sigue siendo alta para principiantes: los pasos y ensayos reducen la barrera de depuración, no sustituyen el razonamiento del alumno. La valoración docente del nivel real del grupo y la suficiencia de 7 h/9 h permanece pendiente. No se han añadido exámenes, rúbricas, repaso opcional desarrollado, migraciones ni otra arquitectura. No hay commits/push de este agente.

## Contraste independiente de carga y lectura — root

Las cifras centrales por tarea de la tabla no constituyen una agenda ajustada a420/540minutos. Ficheros suma aproximadamente280minutos de trabajo guiado del alumno, e Hibernate325: la explicación, lectura de ejemplos, primer entorno y ayuda pueden añadir tiempo sustancial. Para planificación inicial se propone un rango de6–8h para Ficheros guiado+explicación y7,5–10h para Hibernate, dependiendo de Java previo y entorno. No es una medición con estudiantes.

Los niveles finales suponen esfuerzo adicional orientativo: Ficheros2–4h y Hibernate3–5,5h, con Base candidata a una primera comprobación en aula y Aplicación/Reto habitualmente prolongados como trabajo autónomo. No se garantiza completar todas las fases y finales dentro de7h/9h. La infraestructura preparada y las pistas reducen trabajo incidental; no se ha rebajado el razonamiento de conciliación, copia independiente, traslado ni fusión.

Hallazgo corregido por root tras el cierre del autor: persistía «configuración docente local» en hibernate/configuracion.md; sustituido por desarrollo local sin alterar configuración. Root comprobó también que las guías incluyen cuatro fragmentos Java parciales cada una, no solo nuevos párrafos de TODO.
