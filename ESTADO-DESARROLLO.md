# Estado del desarrollo

## Encargo y punto actual

Encargo integral autorizado en main. Orden obligatorio: JDBC → Firestore → MongoDB → Ficheros → Hibernate → Spring.

**JDBC: publicado y preparado para revisión docente. Firestore: en desarrollo. Los otros cuatro temas siguen pendientes.**

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

## Firestore en curso

Producción coordinada de cuatro proyectos con Firebase Admin SDK, emulador sin claves, web/REST/Postman; teoría completa y adaptación de QA/descargas. Docker instalado pero motor no disponible; no se asume persistencia verificada hasta ejecutar emulador real. No hay aún un hito Firestore listo para publicar.
