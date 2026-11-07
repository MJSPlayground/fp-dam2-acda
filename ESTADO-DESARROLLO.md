# Estado del desarrollo

## Encargo y punto actual

Encargo integral autorizado en main. Orden obligatorio: JDBC → Firestore → MongoDB → Ficheros → Hibernate → Spring.

**JDBC: publicado y preparado para revisión docente. Firestore: publicado y preparado para revisión docente. MongoDB: en desarrollo. Ficheros, Hibernate y Spring pendientes.**

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

Desarrollar Cloud Firestore completo (reservas, web preparada, API REST y colección Postman desde el primer hito), sin avanzar a MongoDB hasta pasar su puerta de calidad. Mantener commits y push directos en main según autorización vigente, sin pedir nueva aprobación entre temas.

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

## MongoDB en curso

Producción de teoría, cuatro proyectos y QA real Docker coordinada. Aún no hay un hito MongoDB listo para publicar. Siguiente paso: cerrar contrato equipos/jugadores y competición, construir y probar persistencia real e interfaz antes del cierre del tema.
