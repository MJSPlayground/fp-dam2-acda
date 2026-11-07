# Estado del desarrollo

## Encargo y punto actual

Encargo integral autorizado en main. Orden obligatorio: JDBC → Firestore → MongoDB → Ficheros → Hibernate → Spring.

**JDBC: publicado y preparado para revisión docente. Firestore: publicado y preparado para revisión docente. MongoDB: publicado y preparado para revisión docente. Ficheros: publicado y preparado para revisión docente. Hibernate: publicado y preparado para revisión docente. Spring: verificado localmente, publicación pendiente.**

## Avances confirmados

- `ba97967`: entrega JDBC. Actions [42408701956](https://github.com/MJSPlayground/fp-dam2-acda/actions/runs/42408701956) build y deploy completados con éxito. Pages real: tres páginas JDBC, cuatro ZIP íntegros e importables y PDF JDBC/global con HTTP 200.

- `a60e79e`: navegación inicial de seis temas, avisos En construcción, material histórico de Ficheros fuera de navegación sin eliminar sus fuentes. Actions [42408265936](https://github.com/MJSPlayground/fp-dam2-acda/actions/runs/42408265936) completado con éxito. GitHub Pages real comprobado con Chromium: Ficheros HTTP 200 y aviso visible.
- JDBC: conexiones (SQLite y ejemplo MySQL), SQL/CRUD/ResultSet, repositorios/servicios/errores/decimales, transacciones. Un proyecto guiado de almacén y ventas con etapas 2–5 TODO (conexión inicial resuelta para permitir arranque).
- Una práctica final Operaciones de almacén: Base búsqueda/precios; Aplicación recepción; Reto entrega conjunta con presupuesto y rollback. Inicial derivado del guiado resuelto; funciones nuevas sin resolver. Solución pública separada.
- Ampliación: metadatos, JOIN, CallableStatement, HikariCP, lotes, índices/EXPLAIN y concurrencia/aislamiento. Exactamente cinco fichas de repaso En preparación.
- Cuatro ZIP JDBC reproducibles e importables, con wrapper, sin target/BD/logs. Icono oficial incluido (corregido filtro que excluía todo PNG).
- Se reutilizan Zensical, estilos, navegación, exportador PDF y wrapper. No se han editado estándares ni skills compartidos.

## Pruebas realmente ejecutadas

Entorno: Windows, Temurin 25.0.4.1, Python 3.13.7; herramientas web mediante `.venv/Scripts/python.exe`.

- Guiado inicial: `.\mvnw.cmd -q clean test`, 1 prueba correcta.
- Guiado solución: `.\mvnw.cmd -q '-Dqa.ui=true' test`, 8 pruebas servicio y 1 JavaFX correctas.
- Final inicial: `.\mvnw.cmd '-Dqa.ui=true' test --batch-mode -q`, 10 correctas sin omitidas.
- Final solución: mismo comando, 17 correctas sin omitidas.
- JavaFX real: alta, edición, cancelación, limpieza del formulario, error de decimales, redimensionado; operaciones finales, presupuesto y avisos. Capturas reales inspeccionadas en `target/qa`; no se empaquetan.
- `scripts/test_project_packages.py`: seis ZIP extraídos en carpetas temporales y compilados/probados con su wrapper; cuatro de JDBC y dos históricos. Las pruebas gráficas son optativas y se omiten en esta ejecución sin `qa.ui`.
- `scripts/export_pdfs.py`: siete PDF de sección y global; JDBC 25 páginas y global 33. Inspección visual de muestras renderizadas de código, teoría y práctica final. No equivale a revisión humana exhaustiva de todas las páginas.
- `scripts/check_links.py`: 43 páginas y 112 enlaces internos/proyectos correctos.
- `scripts/check_site_ui.py`: navegación principal, teclado, claro/oscuro, responsive, recursos históricos y descargas PDF correctos.
- `scripts/check_jdbc_ui.py`: ocho páginas JDBC en 1366/390 px, claro/oscuro, sin desbordamiento; cuatro botones descargan ZIP con nombre correcto y estructura/importación comprobada.
- Revisión didáctica independiente con skill `fp-revision-didactica`: requisitos ↔ starter ↔ solución ↔ pruebas; corregidos contratos de búsqueda, casos de stock independientes y explicación BigDecimal. SQL ilustrativo contrastado con SQLite y ejemplo decimal ejecutado con JShell.

## Límites concretos

- No se ha ejecutado Run desde IntelliJ: recorrido documentado y captura opcional registrada en `CAPTURAS-PENDIENTES.md`. Sí se ejecutaron JavaFX y su interfaz real.
- El proyecto JDBC principal utiliza SQLite. Sus fragmentos MySQL/Callable/Hikari se han ejecutado también contra servidor MySQL real, como detalla la comprobación adicional al final del registro. No se ha realizado una medición de rendimiento ni una prueba exhaustiva de aislamientos.
- Sin aprobación pedagógica humana: la revisión final corresponde al docente.
- La web completa de seis temas NO está terminada. No generar aún los treinta repasos opcionales.

## Siguiente paso concreto

Desarrollar Spring completo: componentes, DI, Spring Data JPA y servicios del catálogo, con frontend MVC preparado; cerrar proyectos, pruebas, PDF y publicación. Mantener commits y push directos en main según autorización vigente, sin pedir nueva aprobación entre temas.

## Firestore: entrega técnica y comprobaciones

- Diez páginas conforme al índice: documentos, configuración, persistencia, consultas, REST, guía, práctica final, repaso5 y Ampliación5. Interfaz web preparada y cuatro proyectos en `proyectos/firestore/`.
- Spring Boot 3.5.7, Firebase Admin 9.7.0 y Java 25 comprobados contra emulador oficial 1.22.0 (CLI 15.35.0), proyecto demo sin claves. Launcher `config.Emulador.main` verifica SHA256 oficial y evita instalar Maven global.
- Final: cancelación idempotente, conflictos parciales de horario, cupo diario, consultas paginadas; infraestructura de coordinación resuelta, reglas en servicio. Starter final conserva CRUD guiado resuelto y no incluye soluciones de requisitos nuevos.
- Fuentes Java formateadas. Cuatro `package` correctos. Default: guiado-solución9 pruebas más1 integración omitida; final-solución9 más3 omitidas; starters1 cada uno.
- Suite con `qa.emulador=true`: 10 pruebas guiado y 12 final, sin omitidas; dos solicitudes simultáneas incompatibles admiten exactamente una y devuelven409 en la otra. Script CI exacto `scripts/test_firestore_emulator.sh` ejecutado con Git `sh`, correcto.
- Arranque real de starters: guiado catálogo espacios200 y persistencia pendiente501; final conserva listado200/alta201/borrado204 y deja nuevas funciones en501.
- `scripts/check_firestore_app.py` sobre guiado8080 y final8081: CRUD, formularios, cancelación/errores, JSON inválido y conservación exacta de lista con coma/salto de línea y mapa con varias claves al editar. Móvil390/escritorio1366, claro/oscuro; capturas reales inspeccionadas.
- `npx --yes newman@6.2.3 run proyectos/firestore/guiado-solucion/postman/reservas.postman_collection.json --reporters cli --timeout-request 15000`:12 peticiones/19 aserciones correctas. Colección final:29 peticiones/48 aserciones correctas, incluidos filtros reales, min0, páginas/cursor, cupo, conflictos y cancelación. Todas las descargas incluyen colecciónv2.1 preparada.
- HTTP real:400 JSON decimal rechazado,404 recurso/ruta,405 con Allow conservado,415 y errores estructurados. Corregida pérdida de filtros en URL Postman y añadido control para evitar regresión.
- `scripts/check_firestore_ui.py`:9 páginas en cuatro combinaciones de tamaño/tema y4ZIP/Postman correctos. `scripts/check_jdbc_ui.py` sigue correcto. Enlaces53 páginas/161 destinos correctos.
- PDF Firestore27 páginas, global59 y Ayuda6; muestras reales renderizadas de teoría, código, tablas, guía y seguridad inspeccionadas. Descarga con filename explícito corregida.
- Auditoría didáctica independiente: corregidos TODO inexistente, listado del starter final, soluciones nuevas filtradas, HTTP405, tipos decimales y edición destructiva de mapas/listas. Sin defectos materiales residuales en última ronda de lectura.
- Ayuda ampliada: JDK25, configuración Run/autoimportación/atajos, depuración, logging y pruebas; fuentes oficiales enlazadas y captura IDE pendiente registrada.

Fallo técnico real comprobado: emulador detenido → HTTP 500 estructurado en 0,34 s, sin lista vacía ni stack trace; `config.Emulador.main` verificó SHA256 y arrancó el proceso Java en 8085, recuperándose la API. Editor JSON y calendario oscuro comprobados de nuevo. `package_projects.py --check` compara ahora nombres y bytes contra las fuentes para detectar ZIP obsoletos.

Última ronda limpia terminada: `test_project_packages.py` correcto para los diez ZIP exactos, cada uno extraído y probado con su Wrapper; checkers global/JDBC/Firestore/enlaces correctos. UI final también comprobada: ocupado → cancelar con motivo → libre, filtros de estado/fecha/responsable y botones de paginación.

`b5dd0b4`: Actions [42410875882](https://github.com/MJSPlayground/fp-dam2-acda/actions/runs/42410875882) build y deploy correctos. Pages real comprobado con Chromium: tres páginas principales, cuatro ZIP íntegros con Postman v2.1, PDF Firestore27 páginas y global59 con HTTP200. IAM, Firebase Authentication, consola, índices cloud y ejemplos de Ampliación remotos NO se han ejecutado contra un proyecto real ni con credenciales. No declarar esas comprobaciones realizadas.

**Siguiente tema:** MongoDB, únicamente después de confirmar el hito Firestore publicado. Docker Desktop ya responde (servidor29.9.2); no se han creado todavía contenedores Mongo/MySQL para esos temas.

## MongoDB: entrega y comprobaciones locales

- Diez páginas del índice, guiado y final de tres niveles, cuatro proyectos Java25/Spring Data MongoDB con frontend preparado. Equipo agrega jugadores/estadísticas; Sede referenciada. Final añade jornadas, disponibilidad, consultas, clasificación y racha calculadas en Java.
- MongoDB real8.0.34, imagen oficial `mongodb/mongodb-community-server@sha256:db3c73bd50998abdfe17c80755597fc9a0601dedef1dfb7812dbc4133fc63a08`; QA tmpfs aislada, sin montajes de datos ajenos.
- Bases predeterminadas separadas: `acda_liga` y `acda_liga_final`, para evitar pérdida de campos al comparar modelos. Starter final conserva las funciones resueltas del guiado; nuevos algoritmos no filtrados.
- Cuatro package correctos. Suite con `.\mvnw.cmd -q '-Dqa.mongo=true' test`: guiado12 (8 unit/API y4Mongo), final17 (10 y7), sin omitidas ni fallos. CAS, incrementos concurrentes, dorsal concurrente, jornada repetida sin duplicar contadores, límites20/10000/100000, conservación de jornadas y desempates comprobados.
- Arranque real starters: guiado listado/sedes200 con operaciones pendientes501; final alta201/plantilla200/estadísticas200 y jornada pendiente501. No se han sustituido por persistencia ficticia.
- `scripts/check_mongodb_app.py`: ambos proyectos reales correctos, CRUD, filtros, tipos/errores,20 incrementos HTTP concurrentes, edición que preserva datos embebidos, cancelación de formulario, disponibilidad, jornadas, racha y clasificación. Escritorio/móvil, claro/oscuro; capturas reales revisadas.
- Newman exit0 incluidas comprobaciones de scripts: guiado15 peticiones/25 aserciones; final25/43. Corregido error de sintaxis en script y añadida compilación JavaScript de las ocho colecciones Postman actuales para evitar regresión.
- `scripts/check_mongodb_examples.py`: cinco bloques JS extraídos de Ampliación y ejecutados en base QA aislada; pipelines con9 goles/8 intervenciones/lookup3 y explain/índices correctos, sin afirmar mejoras de rendimiento no medidas.
- Catorce ZIP comparados contra nombres/bytes de fuentes y probados con Wrapper desde carpetas limpias. Checkers global/JDBC/Firestore/Mongo correctos. `check_links.py` ampliado:63 páginas,950 enlaces y737 fragmentos HTML válidos; corregida ancla con tilde que no existía en HTML.
- PDF Mongo23 páginas, global81 y Ayuda8 generados sin incidencias; muestras renderizadas de teoría, código y final revisadas visualmente.
- Fallo real DB detenida → HTTP500 estructurado en3,08s, sin lista vacía ni stack trace. `MongoLocal.main` ejecutado realmente desde Maven arrancó Compose y alcanzó HEALTHY27018; imagen8.0.34 y ping comprobados. Se usó proyecto QA nuevo `acda-liga-qa-launcher-20261107`, verificado ausente antes, sin modificar servicios/volúmenes ajenos.
- Auditoría didáctica independiente: corregida separación de bases, precisión del alcance de @Version y un fragmento roto. Sin pruebas dentro de IntelliJ ni Compass: capturas opcionales registradas.

**Pendiente:** Actions y Pages del hito MongoDB. Después continuar Ficheros, sin nuevas aprobaciones intermedias.

## Preparación para Ficheros (sin implementación todavía)

Inventario histórico en solo lectura: reutilizables ProductoFormatos (CSV/Jackson/DOM seguro), ComercialJsonDao (temporal y sustitución), SerializacionDemo (filtro), componentes de GestorApp/gestor.css/branding. Evitar trasladar el gestor comercial entero, tres finales, recursión profunda ordinaria, DAO y saltos UI→persistencia. Revisar conversiones JSON grandes sin truncar. Al sustituir páginas antiguas, impedir que materiales contradictorios huérfanos continúen en búsqueda pública; conservarlos como referencia en Git.

## Incidencia CI MongoDB y corrección

El primer despliegue `883a380`, Actions [42412766847](https://github.com/MJSPlayground/fp-dam2-acda/actions/runs/42412766847), falló en Initialize containers, antes de tests: fatal12257600 por comprobación de kernel/TCMalloc. La imagen UBI8 probada localmente no podía arrancar en el runner.

Fuentes primarias consultadas en origen: [incidencia MongoDB](https://jira.mongodb.org/browse/SERVER-121912), [guardia de arranque8.0.34](https://github.com/mongodb/mongo/blob/r8.0.34/src/mongo/db/startup_check_rseq.cpp), [TCMalloc y registro rseq](https://www.mongodb.com/docs/manual/administration/tcmalloc-performance/) y [corrección específica Ubuntu](https://jira.mongodb.org/browse/SERVER-131779).

Primer experimento negativo: misma imagen UBI8 con `GLIBC_TUNABLES=glibc.pthread.rseq=1` seguía devolviendo `usingPerCPUCaches=true`, por lo que no se publicó como arreglo válido. Prueba positiva: Docker Official Image Ubuntu del mismo MongoDB8.0.34, digest `sha256:b9924c330a8bbc5800fe9c69f04a84e221eca7b28fd049a0354147d9908fa142`, glibc2.39 y el ajuste anterior: ping/CRUD correctos, `usingPerCPUCaches=false`, suite12/17 correcta sin omisiones y cinco ejemplos JS correctos. Se cambia solo CI; Compose del alumnado ya validado permanece igual. El workflow comprueba explícitamente ese modo y registra el kernel.

Comando real de integración de esta prueba: `$env:MONGODB_URI='mongodb://127.0.0.1:27019/acda_mongodb_ci_rseq?serverSelectionTimeoutMS=3000&connectTimeoutMS=3000'; & 'C:/Program Files/Git/bin/sh.exe' scripts/test_mongodb_real.sh`. Contenedores temporales propios de esta incidencia detenidos, sin modificar recursos ajenos.

Pendiente confirmar el nuevo commit en Actions/Pages: la prueba local no reproduce el kernel del runner y no se declara despliegue correcto hasta observarlo.

## MongoDB publicado y comienzo de Ficheros

`7eabc8f` resolvió la compatibilidad de CI: Actions [42413255221](https://github.com/MJSPlayground/fp-dam2-acda/actions/runs/42413255221) build y deploy correctos. Pages real comprobado: tres páginas MongoDB, cuatro ZIP íntegros con Compose y PDF Mongo23/global81 páginas con HTTP200. La publicación tardó más de lo habitual, pero terminó correctamente; no se omitieron pruebas ni se reescribió historial.

Ficheros comienza después de esa comprobación. Traslado consolidado con el hito Ficheros de27 Markdown históricos desde `docs/ficheros/` a `referencias/ficheros-historico/`, con rutas absolutas verificadas dentro del repositorio y sin sobrescribir destinos. Así se conservarán en Git sin aparecer como apuntes actuales en publicación/búsqueda. Las capturas antiguas también quedan archivadas como referencia histórica. Se preparan cuatro proyectos nuevos reutilizando componentes válidos del código anterior; no se borran los proyectos antiguos.

QA JDBC adicional local, pendiente de consolidar con este hito: misma prueba JavaFX ampliada para cancelar/confirmar eliminación, Cancel seguro, evento Escape, vender2, actualizar stock/historial y rechazar borrar producto vendido. Suite `qa.ui=true`:8servicio+1UI correctas. Escape es evento JavaFX automatizado, no una tecla física ni Run en IntelliJ.

Al retomar, comprobar primero estado Git y el último workflow: el código Ficheros está consolidado en este hito, pero su publicación solo se considera confirmada cuando Actions y Pages estén comprobados.

## Ficheros: entrega técnica consolidada

- Diez páginas conformes al índice; un inventario JavaFX incremental y una final Base/Aplicación/Reto. CSV RFC4180/UTF8, Jackson con objetos/array y binario principal Serializable; Repository/Service, validaciones, causas y SLF4J/Log4j2 desde el comienzo. Listado simple sin recursión compleja; RandomAccessFile queda en demostración breve.
- XML/DOM seguro, streaming, WatchService y escrituras fiables desarrollados en Ampliación. Dos clases de demostración y cinco pruebas reales correctas; cinco fichas de repaso En preparación.
- Cuatro proyectos nuevos `proyectos/ficheros/{guiado,final}-{inicial,solucion}`. Históricos preservados sin presentarlos como actuales. Inicial final conserva núcleo guiado y deja pendientes exportación filtrada, conciliación/incidencias, preview/aplicación/recuperación. Datos y criterios verificables4 productos/4 incidencias/P01=12.
- Paquetes y recursos reales comprobados: CSV con comillas, Unicode y LF; muestras CSV/JSON3 productos y lote correcto; corregido CRCRLF. JSON rechaza rangos, fracciones, campos ausentes/null, coerciones y claves duplicadas; lectura simplificada a Producto[].class coherente con teoría. Binario rechaza clases ajenas/arrays grandes; valida catálogo antes de reemplazarlo.
- Fallos de carga/escritura conservan catálogo/archivo anterior; no se sobrescriben respaldos previos. Recuperación y respaldo fallido comprobados. Corregida selección de último BIN al recuperar tras aplicar sin exigir deseleccionar CSV.
- JavaFX real en cuatro variantes: `.\mvnw.cmd -q '-Dqa.ui=true' test` → guiado inicial2, guiado solución15, final inicial2, final solución16 correctas sin omisiones. Default desde ZIP suma los mismos casos pero omite una UI por proyecto, de forma explícita.
- UI: alta, edición, cancelación, confirmaciones de carga/guardado/borrado y Escape automatizado, formatos3, precio/stock, nombres CSV multilínea preservados, JSON inválido, filtros, preview/aplicar/recovery/exportincidencias. Formulario contextual desplazable tras detectar recorte real al reducir ventana; captura compacta886×523 y normal revisadas. Cuatro Launcher.main ejecutados vía Maven y detenidos; no Run dentro IntelliJ.
- Figura propia de rutas vertical400×444 y fuente18, móvil/PDF revisados. Cinco casillas del recorrido usan el componente existente, con persistencia y reinicio; se corrigió Markdown que se mostraba como texto.
- Exportador sin incidencias: Ficheros26 páginas, global105, Ayuda8. Muestras renderizadas de figura, código, teoría, práctica y Ampliación revisadas.
- `package_projects.py` genera16ZIP exactos; guardias contra ZIP obsoletos y diferencias con fuentes. Todos compilados/probados desde copias limpias. `check_site_ui.py` conserva navegación/foco/figuras/reduced-motion y adapta controles al itinerario actual, sin depender del codelab histórico. `check_links.py`:46 páginas/867 enlaces/662 fragmentos válidos,315 entradas de búsqueda exclusivamente del nav vigente. Las27 páginas archivadas no están publicadas ni indexadas.
- QA adicional JDBC de diálogos se consolida también en este hito; no cambia producción.

**Pendiente:** Actions y Pages del hito Ficheros; después Hibernate. Revisión pedagógica humana final, ejecución manual en IntelliJ y capturas de aula siguen pendientes donde está indicado.

## Ficheros publicado y comienzo de Hibernate

`f46bc9d`: Actions [42417163135](https://github.com/MJSPlayground/fp-dam2-acda/actions/runs/42417163135) build y deploy correctos. Pages real comprobado en navegador: tres páginas actuales y cuatro ZIP íntegros. Búsqueda pública315 entradas, con rutas de Ficheros limitadas al itinerario vigente; PDF Ficheros26/global105 páginas, HTTP200. El primer script de consulta pública asumió erróneamente clave docs en search.json; corregido a items y verificación completada (no fue un fallo del sitio).

Hibernate comienza después de ese control. Se producirán cuatro proyectos JavaFX/Hibernate puro y diez páginas conforme al índice; MySQL real aislado para probar relaciones1:1/1:N/M:N y rollback. Los agentes existentes mantienen sus permisos y modelo; no se ha cambiado configuración global por la preferencia de cuota. Nuevos subagentes rutinarios, si fueran necesarios, usarán la preferencia solicitada cuando la herramienta admita elegirla.

## Hibernate — cierre local (2026-11-07)

- Once páginas cubren ORM/JPA, configuración, entidades, sesiones, transacciones, tres cardinalidades, HQL, guiado, final, repaso y Ampliación. Cinco fichas de repaso En preparación. Auditoría didáctica posterior de solo lectura sin nuevos defectos materiales; no sustituye revisión docente.
- Cuatro proyectos Java 25/JavaFX con Hibernate 7.4.15.Final y MySQL 8.4.13 real, Maven Wrapper y frontend preparado. Guiado: catálogo con editoriales, autores, libros y detalles. Final: nuevas ediciones, traslado/descuento y fusión atómica; starter conserva guiado resuelto sin resolver requisitos nuevos.
- `mvnw.cmd -q -Dqa.ui=true -Dqa.mysql=true test`: inicial guiado 2, solución guiado 12, inicial final 2, solución final 11; sin fallos, errores u omisiones. Relaciones, versiones de formulario, conservación de autores, retirada del detalle, restricciones, causas y rollback múltiple comprobados.
- `sh scripts/test_hibernate_real.sh`: ambas soluciones con MySQL; 12/11 pruebas totales, una UI omitida por proyecto en esta ejecución. Tres pruebas de Ampliación activas: XML, consultas y diagnóstico N+1 (6 sentencias frente a 1, mismos dos libros).
- `config.MySqlLocal.main` y cuatro `app.Launcher.main` ejecutados realmente contra Compose de alumno. Esquemas separados, persistencia tras reinicio y arranque sin borrado automático. Sin ejecución manual de IntelliJ ni Workbench.
- Corregido mensaje de error oculto en formulario desplazable; verificados flujos JavaFX y ventana compacta. Capturas reales no incluidas en ZIP.
- `scripts/export_pdfs.py`: Hibernate 26 páginas y global 128; muestras de portada, código, relaciones, guiado y Ampliación renderizadas e inspeccionadas. Corregidas dimensiones intrínsecas del SVG para evitar página casi vacía. No revisión visual exhaustiva de cada página.
- Pendiente inmediato: terminar comprobaciones de ZIP/web, commit/push y Actions/Pages antes de iniciar Spring.
- Cierre de descargas: `scripts/package_projects.py --check` comprueba 20 ZIP exactos y sus bytes contra fuentes; `scripts/test_project_packages.py` extrae y prueba 20/20 con Wrapper, salida 0. En este modo las pruebas UI/MySQL optativas se omiten; ya ejecutadas por separado como consta arriba.
- Web local: `check_site_ui.py` y `check_hibernate_ui.py` correctos (10 páginas internas, cuatro descargas, escritorio/móvil, claro/oscuro); guía de cinco casillas conserva progreso y permite reinicio real. `check_links.py`: 56 páginas, 1045 enlaces, 783 fragmentos y 397 entradas de búsqueda correctos.
- Compose y contenedor de QA propios detenidos, volumen de demostración propio conservado. Sin intervenciones en servicios ajenos.

### Publicación Hibernate comprobada

- `092bc87`, sincronizado con origin/main. Actions [42419950876](https://github.com/MJSPlayground/fp-dam2-acda/actions/runs/42419950876): build y deploy SUCCESS, incluyendo MySQL real.
- Chromium sobre GitHub Pages: portada, guiado y final Hibernate HTTP 200, sin aviso En construcción; cuatro ZIP descargados e íntegros con POM/README/Wrapper. PDF Hibernate 26 páginas y global 128, HTTP 200. Capturas reales de las tres páginas.
- Se inicia Spring después de superar esta puerta de calidad. No quedan comprobaciones técnicas bloqueantes conocidas de Hibernate; IntelliJ/Workbench y revisión pedagógica final mantienen las limitaciones indicadas.

### Comprobación adicional JDBC/MySQL durante el cierre del módulo

- Ejecutado realmente con Java 25, MySQL 8.4.13, Connector/J 9.5.0 y HikariCP 6.3.3 desde la caché Maven: metadatos/alias de columnas, procedimiento `consultar_stock` y parámetro OUT=42, lote con escritura inicial correcta seguida de clave duplicada y rollback completo, préstamo Hikari devuelto (cero conexiones activas). Salida 0.
- Comando: `java -cp $taskJdbcClasspath qa-captures/JdbcMysqlSmoke.java`; classpath incluye esos JAR y SLF4J 2.0.17/Log4j2 2.25.3. Clase de comprobación local en qa-captures (no distribuida como un nuevo proyecto). El manifiesto de Hikari no informa ImplementationVersion; la versión procede del JAR explícito del classpath.
- Esquema temporal propio `acda_jdbc_qa_UUID`, nombre validado por expresión fija antes de DDL, eliminado al terminar; sin afectar a esquemas Spring ni datos de aplicaciones. Esto completa la ejecución MySQL/Callable/Hikari que anteriormente figuraba como limitación, sin cambiar los proyectos JDBC publicados. No se ha medido rendimiento ni probado cada nivel de aislamiento.

## Spring — cierre local (2026-11-07)

- Diez páginas cubren componentes/IoC/ciclo de vida, DI por constructor, Boot/configuración/perfiles, Spring Data JPA, servicios/reglas/eventos y transacciones/conflictos. Una figura de colaboración; cinco fichas En preparación. Ampliación desarrolla MVC/Thymeleaf, formularios, JUnit/BootTest, diagnóstico/perfiles, JAR, Security y Docker.
- Cuatro proyectos Java25/Boot3.5.16/Thymeleaf/JPA/MySQL8.4.13. Guiado: servicios para libros y editoriales del catálogo. Final: reposición, presupuesto sin mutación y pedido atómico con instantánea. Inicial final conserva el guiado completo, sin resolver las operaciones nuevas. Frontend y controladores MVC preparados.
- Cuatro `mvnw.cmd package` superados. Soluciones `mvnw.cmd -Dqa.mysql=true test`: guiado9/final13, sin fallos ni omisiones, repetidos tras cambios de UI. Iniciales: ComponentesTest2; final inicial añade ReglasCatalogoTest (4 en total); el flag no inventa integraciones ausentes. README iniciales corregidos para explicarlo.
- Pruebas MySQL: consulta/CRUD, versiones de formulario, reglas/perfiles/DI, evento AFTER_COMMIT, claves `Pedido_A`/`pedido_a` diferentes mediante ascii_bin, repetición exacta rechazada, concurrencia y rollback después de actualizaciones de stock reales antes de una restricción UNIQUE. Snapshot histórico e importes inmutables.
- Navegador real sobre soluciones: formularios, títulos con LF/tildes, errores de tipo, validaciones, confirmaciones, presupuesto sin mutación, reposición y pedido. Starters arrancados realmente en8090/8091: guiado comunica TODO; final mantiene CRUD y no inventa presupuesto.
- Seguridad: login, roles lector/gestor, CSRF y logout. Corregidos menú seguro desbordado en móvil, acción POST visible para lector y separación/área táctil de Cancelar. Inspección independiente parent de capturas escritorio/móvil claro/oscuro.
- Auditoría didáctica posterior de solo lectura contra cuatro proyectos/README/TODO sin defectos materiales residuales. Corregidas variables/perfiles de ejemplos, secuencia de datos de prueba, constructor real del evento, explicación de copias defensivas y redondeo desde perspectiva del cliente. Sin aprobación pedagógica humana.
- `scripts/export_pdfs.py`: Spring25 páginas, global150, siete secciones más global. Muestras reales renderizadas e inspeccionadas: portada, Java/configuración, figura, guía y Security/JAR. No inspección visual exhaustiva de todas las páginas.
- `scripts/check_published_site.py --topic hibernate`: comprobación reproducible ya ejecutada sobre Pages real (portada +11 páginas,4ZIP,2PDF). Se ejecutará completa después del despliegue final, fuera de la construcción CI.
- Pendiente para publicar Spring: terminar JAR/Docker/ZIP24/web, commit/push y comprobar Actions/Pages reales.

### Cierre de QA Spring (2026-11-08)

- `check_spring_app.py --base http://127.0.0.1:8084 --label guiado` y equivalentes final8086/final-inicial8091 correctos; guiado-inicial8090 revisado aparte con TODO visible, formulario y datos conservados. Cuatro `package` repetidos tras cambios de UI.
- `check_spring_security.py --base http://127.0.0.1:8087` y Docker8089 correctos: login fallido/correcto, roles, POST sin CSRF403 y con token, logoutPOST y móvil. Regresiones cubren menú autenticado, acciones por rol y Cancelar44px/separación.
- `sh scripts/test_spring_real.sh` correcto. Suites optativas con MySQL9/13 sin omisiones; modo ordinario por variante2/9/4/13 totales, con0/5/0/9 omitidas explícitamente. No confundir los modos.
- `config.MySqlLocal.main` real y `docker compose --profile app up --build -d` en ambas soluciones correctos, UI completa guiado8088/final8089. Docker USER10001, Temurin25.0.5; digest base construido ecdc0adb37bde1a281c4d245a3cd535004b1fc8322d560f5b09463b248c81df0.
- Reinicio JAR final conservó exactamente4 instantáneas; reinicio Compose conservó2. Caída controlada del MySQL propio: GET/pedidos devuelve500 real sin stack; recuperación conserva las2 instantáneas. **Limitación medida:** espera30,1s por el timeout de conexión Hikari ante esa caída. Captura real spring-docker-db-caida-500.png.
- Descargas:24/24ZIP comparados byte a byte con fuentes y Maven Wrapper desde carpetas limpias, salida0. Global y Spring:9 subpáginas×escritorio/móvil×claro/oscuro,4descargas, guía5casillas con persistencia/reinicio y figura343×531legible. `check_links.py`:65páginas/1197enlaces/890fragmentos/462entradas de búsqueda correctos.
- Censo transversal: seis temas con cuatro proyectos y cuatro descargas cada uno; cinco fichas En preparación por tema, sin desarrollar los treinta proyectos opcionales.
- Procesos y servicios propios detenidos; sin listeners3309/8084/8086/8087/8088/8089/8090/8091. Volumen propio acda-spring_spring-data conservado. Sin tocar servicios ajenos. IntelliJ no ejecutado manualmente.
- Pendiente únicamente para cierre público: commit/push, Actions y comprobación integral de Pages.
