# Estado del desarrollo

## Encargo y punto actual

Encargo integral autorizado en main. Orden obligatorio: JDBC → Firestore → MongoDB → Ficheros → Hibernate → Spring.

**JDBC: publicado y preparado para revisión docente. Firestore: publicado y preparado para revisión docente. MongoDB: publicado y preparado para revisión docente. Ficheros: en desarrollo. Hibernate y Spring pendientes.**

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
- Ejemplos MySQL, procedimiento almacenado y HikariCP ilustrativos, contrastados con documentación oficial pero sin integración contra un servidor MySQL en esta sesión. El proyecto principal reproducible utiliza SQLite.
- Sin aprobación pedagógica humana: la revisión final corresponde al docente.
- La web completa de seis temas NO está terminada. No generar aún los treinta repasos opcionales.

## Siguiente paso concreto

Cerrar Ficheros completo (inventario, CSV/JSON/serialización y final) y comprobar Actions/Pages antes de comenzar Hibernate. Mantener commits y push directos en main según autorización vigente, sin pedir nueva aprobación entre temas.

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

Ficheros comienza después de esa comprobación. Se trasladaron27 Markdown históricos desde `docs/ficheros/` a `referencias/ficheros-historico/`, con rutas absolutas verificadas dentro del repositorio y sin sobrescribir destinos. Así se conservarán en Git sin aparecer como apuntes actuales en publicación/búsqueda. Las capturas antiguas también quedan identificadas como históricas. Se preparan cuatro proyectos nuevos reutilizando componentes válidos del código anterior; no se borran los proyectos antiguos.

QA JDBC adicional local, pendiente de consolidar con este hito: misma prueba JavaFX ampliada para cancelar/confirmar eliminación, Cancel seguro, evento Escape, vender2, actualizar stock/historial y rechazar borrar producto vendido. Suite `qa.ui=true`:8servicio+1UI correctas. Escape es evento JavaFX automatizado, no una tecla física ni Run en IntelliJ.
