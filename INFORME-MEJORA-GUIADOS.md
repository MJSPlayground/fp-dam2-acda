# Mejora de los seis proyectos guiados

Fecha: 2026-11-15. Informe interno, fuera de la web, los PDF y los proyectos descargables.

## Alcance y fuentes

Se retoma el árbol de trabajo interrumpido sin descartar sus cambios. Base: `508aa67`, en `main`, coincidente con GitHub al comenzar. Consultados el índice, AGENTS, plan, estado, guía manual e informes de auditoría del repositorio. El estándar docente y las cinco skills se consultaron en `origin/main` de AI Workspace después de actualizar las referencias de GitHub (`302dd46`); el clon de estándares no se modificó. Se aplicó también la skill PDF.

Se conservan proyectos, tecnologías, dominios, funcionalidades, dependencias, navegación, componentes e identidad visual. No se han añadido imágenes públicas, nuevos proyectos, prácticas de repaso ni material de evaluación reservado. Las seis páginas de prácticas finales no se han modificado. Sus README se han corregido únicamente por mensajes internos, incoherencias o pistas algorítmicas excesivas.

## Cambios por tema

| Tema | Referencias resueltas y observación | Trabajo que conserva el estudiante |
| --- | --- | --- |
| Ficheros | `copiarEjemplos` preparado para distinguir Path y operación; registro CSV independiente con el dialecto real; `eliminar` y validadores preparados; mapeador Jackson e Instantanea explicados. | Listado, lectura/escritura CSV, alta/edición y carga completa; adaptar JSON y BIN conservando recursos, causas y estado. Se mantienen cinco etapas y CSV 2A–2D. |
| Hibernate | Campos de Detalle preparados; mapeo de `Libro.editorial` resuelto para integrar; búsqueda de editorial preparada; consulta `editoriales` resuelta para añadir; patrón de servicio/transacción ya preparado. | Inverso 1:N, M:N y 1:1; CRUD de libros, consulta de autores y filtros HQL; alta/edición de libros y demostración atómica. Versiones y helper transaccional permanecen preparados. |
| Spring | Constructor y `EditorialService.guardar` preparados y explicados; primera consulta derivada resuelta para integrar; reglas de stock y publicación de eventos existentes como referencia. | Consulta combinada, validaciones del libro, alta/edición y demostración transaccional. Perfiles, eventos y colaboradores se observan; no se añade infraestructura. |
| JDBC | Conexión prestada de `AlmacenService.listar`; SELECT/ResultSet del historial preparado; método `existenPara` completo para enseñar parámetros; fragmento de INSERT y ensayo manual de rollback. | Listado y búsqueda de productos; altas, edición y eliminación; validaciones; descuento y registro separados; venta atómica con rollback. |
| Firestore | Catálogo y GET de espacios preparados; conversión de un campo y comprobación de tipos; referencia/futuro/snapshot; condición simple de consulta y construcción de respuesta HTTP. | Completar conversión del modelo, CRUD del SDK, filtros restantes, colaboración del servicio y POST/PUT/DELETE. Las peticiones ya preparadas se ejecutan por subpasos, no se construyen desde cero. |
| MongoDB | Modelo documental explicado; consulta por categoría y consulta por posición resueltas para integrar; `obtener` y `conJugadores` preparados; diferencia entre documento completo versionado e incremento atómico. | Consulta combinada, casos de uso de equipos, jugadores y coordinación/validación de estadísticas. La operación avanzada MongoTemplate sigue íntegramente preparada. |

Todos los guiados explican objetivo, preparación, prerrequisitos, partes resueltas y pendientes, capas y primera comprobación anterior al código. Las tareas identifican archivos y métodos. Las pistas permanecen cerradas en la web. Los cierres incluyen éxito, errores, estado conservado, reinicio cuando corresponde y preguntas conceptuales. La solución completa continúa separada al final.

En Spring y Firestore se reduce el trabajo incidental mediante referencias preparadas, integración por capas y comprobaciones parciales. No se rebajan algoritmos ni funcionalidades ni se presentan las seis/siete horas como un tiempo medido con alumnado. La adecuación final al ritmo del grupo requiere revisión humana.

## Incidencias corregidas

- Cuatro README de Ficheros: eliminadas historias de reutilización y comentarios internos; corregida correspondencia de sus etapas con la guía. También retirado el comentario histórico de CsvProductoRepository en las cuatro variantes.
- Final de Ficheros: conservados requisitos y contratos de modelos/infraestructura; retirada la secuencia operativa de reserva/limpieza/aplicación que revelaba parte del algoritmo autónomo.
- Cuatro README de Hibernate: entorno local de prácticas, comprobaciones opcionales y conexión a un esquema existente; retiradas pistas de bloqueo/flush de la final y referencias arrastradas a lotes/recuperación.
- JDBC: contrastado `Database.preparar()`: crea tablas y si la tabla producto es nueva inserta los tres productos. La interfaz pendiente no implica base vacía. Retirados informes internos sobre la sesión de IntelliJ/CI; aclarada configuración preparada.
- Spring y documentales: depurados mensajes internos y pistas técnicas excesivas de finales, conservando comandos, contratos de ayudantes y seguridad.
- `check_student_voice.py`: incluye README del manifiesto real de descargas, también anidados; excluye artefactos y proyectos históricos no publicados. `--packages` inspecciona los README dentro de los 24 ZIP. Workflow ejecuta el control de fuentes y después el de ZIP, además de HTML/PDF.
- Cinco pruebas del control de lenguaje distinguen notas internas de autoría, seguridad, commit SQL, historial de negocio, auditoría de datos y nombres de propiedades técnicas. Se comprueba también la selección de README realmente distribuibles.
- Revisiones independientes corrigieron un breakpoint inalcanzable en Ficheros, un enlace de Spring, textos internos residuales y una frase de MongoDB que podía hacer obligatorias las pruebas optativas.
- La inspección visual encontró nombres de fase partidos dentro de tablas PDF. El exportador marca esas tablas y la hoja de impresión conserva su primera columna sin partir identificadores; no cambia el sitio.

## Verificaciones ejecutadas

Java local: Temurin 25.0.4.1, Maven Wrapper de cada descarga. Los servicios de ensayo se crean aparte; no se utilizan ni detienen los contenedores de otros proyectos.

| Verificación | Resultado |
| --- | --- |
| `scripts/test_learning_local.sh ficheros jdbc` | Ficheros guiado 7/final 3; JDBC guiado 10/final 11. Cero fallos, errores u omisiones en estas suites seleccionadas. |
| Fragmento CSV independiente de la guía | Ejecutado con Java 25 y el classpath del starter: `Taza, blanca \| 3`, conforme al resultado explicado. |
| `scripts/test_hibernate_real.sh`, MySQL real 3308 | Guiado 20 y final 14 casos totales; una UI omitida por variante en esa ejecución. Cero fallos/errores. |
| `scripts/test_spring_real.sh`, MySQL real | Guiado 14/final 23; cero fallos, errores u omisiones. Incluye consultas, reglas, MVC, versiones, eventos y rollback. |
| `scripts/test_mongodb_real.sh`, MongoDB real 27018 | Guiado 16/final 23; cero fallos, errores u omisiones. Incluye fases, atomicidad y cálculos de finales. |
| `scripts/test_firestore_emulator.sh`, emulador oficial | Guiado 16/final 15; cero fallos, errores u omisiones. Incluye conversión, repositorio, servicio, REST y niveles finales. |
| `-Dqa.ui=true -Dqa.mysql=true -Dtest=*UiTest test` | Seis recorridos JavaFX reales, guiado/final solución de Ficheros, JDBC e Hibernate: un caso por variante, cero fallos/errores/omisiones. No es ejecución manual dentro de IntelliJ. |
| Aplicaciones web reales, tres JAR del guiado | `check_spring_app.py`, `check_firestore_app.py` y `check_mongodb_app.py`: CRUD, errores, cancelaciones, versiones, conservación de listas/mapas/estadísticas y cuatro combinaciones de tamaño/color correctos. Mongo incluye 20 incrementos concurrentes. Puertos de ensayo 8084/8081/8082, MySQL/Mongo propios y emulador oficial; procesos cerrados al terminar. |
| `scripts/test_project_packages.py` | 24/24 ZIP extraídos en carpetas temporales limpias, compilados y probados mediante su Wrapper. Las integraciones/gráficos/fases optativas omitidas en este modo no se cuentan como superadas; se ejecutaron por separado como consta arriba. |
| `package_projects.py --check` | Integridad, estructura limpia y correspondencia byte a byte de los 24 ZIP con sus fuentes. |
| `test_student_voice.py` y control de fuentes/HTML/PDF/ZIP | Cinco pruebas correctas; 98 fuentes, incluido README de las 24 descargas. |
| Web local, `check_site_ui.py` y seis controles por tema | Navegación, teclado/foco, progreso existente, móvil/escritorio, claro/oscuro y cuatro descargas reales por tema correctos. Sin rediseño. |
| `check_links.py` | 65 páginas, 1418 enlaces internos/proyectos, 1070 fragmentos HTML y 515 entradas de búsqueda correctos. |
| `export_pdfs.py` local | Ocho PDF: Ficheros 31, Hibernate 31, Spring 30, JDBC 31, Firestore 34, MongoDB 28, Ayuda 8 y global 183 páginas. |
| PDF descargados de Pages | Ocho PDF públicos: Ficheros 31, Hibernate 31, Spring 30, JDBC 31, Firestore 33, MongoDB 28, Ayuda 8 y global 182 páginas. La generación Linux redistribuye algunas líneas respecto a Windows; se inspeccionó también su renderizado real. |

Los tests de aprendizaje se contrastaron con los fixtures y TODO del starter: las primeras fases aportan sus datos y no exigen operaciones posteriores. Las comprobaciones en interfaz, Postman o depurador permiten avanzar sin activar JUnit. Las propiedades y ubicaciones de Run se describen para IntelliJ.

Se renderizaron e inspeccionaron todos los tramos de los seis guiados, además de muestras de portada, teoría, figuras existentes, tablas, código y Ampliación. Referencias de la inspección: Ficheros e Hibernate 1,4,8,19–25,31; Spring 1,4,8,16–24,30; JDBC 1,4,8,17–24,31; Firestore 1,4,8,19–27,34; MongoDB 1,4,8,15–22,28; Ayuda 1,3,5,8; global 1,3,92,183. Capturas y logs son locales, fuera de los ZIP y de Git.

Se repitió la inspección sobre los ocho PDF descargados de la publicación real: mismos rangos salvo Firestore 1,4,8,19–26,33 y global 1,3,92,182. No se observaron defectos materiales de lectura, tablas, código ni solapamientos. Las tablas de fases corregidas mantienen sus valores completos también en la salida de CI.

Incidencias del proceso ya resueltas: los comandos iniciales Firebase no encontraban `sh` y no interpretaban una ruta relativa con barras en Windows; se repitieron con Git Bash y Python disponibles en PATH y terminaron correctamente. El empaquetado coincidió inicialmente con la reconstrucción que limpia `site`; se repitió después de esa reconstrucción y los 24 proyectos terminaron desde limpio. No se atribuye éxito a esos intentos fallidos.

## Límites y revisión humana

- No se ha ejecutado gráficamente IntelliJ, Workbench, Compass ni Postman. Sí se ejecutaron comandos Wrapper, JavaFX real, navegador y servicios reales. La validación del procedimiento dentro del IDE queda para la revisión manual.
- Firestore se verificó exclusivamente con el emulador. Cloud real, IAM, credenciales e índices remotos siguen pendientes de entorno y autorización específica.
- La revisión PDF cubre todos los guiados y muestras del resto, no cada página del módulo completo.
- La revisión pedagógica con alumnado, el ajuste empírico de tiempos y las capturas de aula siguen pendientes. No se declara aprobación pedagógica automática.

## Git y publicación

- `8a395f2`: README, separación de destinatarios, control de lenguaje en fuentes/ZIP y corrección de columnas PDF.
- `428cf24`: guiado Ficheros.
- `4e2b28e`: guiado Hibernate.
- `6abee53`: guiado Spring.
- `e8cdbaf`: guiado JDBC.
- `2fc1c99`: guiado Firestore.
- `8d8c3e5`: guiado MongoDB.

Los cambios públicos se subieron en dos hitos coherentes en `main`, con los seis guiados en commits individuales. La ejecución inicial de Actions fue sustituida automáticamente por la configuración de concurrencia al subir el conjunto de guiados; no se considera una validación terminada.

La ejecución integrada [43652906566](https://github.com/MJSPlayground/fp-dam2-acda/actions/runs/43652906566) terminó con **build y deploy correctos**. Después, `scripts/check_published_site.py` terminó con salida 0: portada y 64 páginas de navegación, 24 ZIP íntegros comparados con sus fuentes y ocho PDF de la URL pública. Además, se comparó el texto completo del artículo publicado de cada guiado con el HTML local: seis coincidencias exactas y todas las pistas inicialmente cerradas.

El cierre de documentación interna se versiona por separado con `[skip ci]`: no cambia el artefacto público validado. Los procesos y contenedores propios de ensayo están cerrados; los servicios de otros proyectos se conservan.

Estado: **preparado para revisión pedagógica y ejecución manual del docente**, sin incidencias técnicas bloqueantes conocidas dentro del alcance verificado.
