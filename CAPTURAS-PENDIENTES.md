# Recursos visuales pendientes · ACDA

Este archivo recoge **capturas auténticas, GIF y otros recursos visuales** que requieren intervención del docente o verificación humana. No constituye un índice de contenidos ni implica que deban producirse todas las propuestas. Se revisa cuando Codex desarrolla o valida un bloque.

**Criterios:** mostrar procesos que se comprenden mejor visualmente; no fabricar capturas de IntelliJ ni dejar enlaces a recursos inexistentes. Los esquemas conceptuales originales reproducibles podrán crearse en SVG y versionarse directamente, una vez comprobado que corresponden al código. Los GIF deben tener explicación textual y fotograma estático equivalente para PDF y movimiento reducido. Evitar datos personales y rutas privadas del equipo.

## Solicitudes concretas al docente

### [Opcional · Pendiente] Hibernate: ejecutar e inspeccionar las tres relaciones

- **Páginas:** `docs/hibernate/proyecto.md` y `docs/hibernate/relaciones.md`.
- **Objetivo:** relacionar los objetos de la aplicación con claves foráneas y tabla de unión.
- **Pasos:** abrir el proyecto Maven resuelto, ejecutar `es.mjsplayground.acda.hibernate.biblioteca.config.MySqlLocal.main` y después `app.Launcher.main`. Crear o abrir el libro indicado en la guía con editorial, varios autores y detalle. En Workbench conectar al servidor local del README e inspeccionar las filas relacionadas y la tabla de unión, sin alterar los datos.
- **Herramientas:** IntelliJ IDEA y MySQL Workbench; registrar versiones exactas del aula. No mostrar credenciales ni datos ajenos.
- **Destinos:** `docs/assets/capturas/hibernate/intellij-biblioteca.webp` y `docs/assets/capturas/hibernate/workbench-relaciones.webp`.
- **Alt/pies:** «Catálogo JavaFX iniciado desde Launcher en IntelliJ»; «Libro, editorial, detalle y autores relacionados en las tablas de MySQL».
- **Estado:** pendientes dentro de IntelliJ y Workbench. Las pruebas realizadas utilizan JavaFX y MySQL reales, pero no simulan capturas de esas herramientas.

### [Opcional · Pendiente] Ficheros: ejecutar y cargar un catálogo desde IntelliJ

- **Página:** `docs/ficheros/proyecto.md`, «Preparar y ejecutar».
- **Objetivo:** reconocer el punto de entrada, la carpeta externa y el estado del catálogo tras cargarlo.
- **Pasos:** abrir `inventario-guiado-solucion.zip` como Maven, seleccionar el JDK y ejecutar `es.mjsplayground.acda.ficheros.inventario.app.Launcher.main`. Usar una carpeta de trabajo nueva, copiar los ejemplos desde el botón preparado, seleccionar el CSV y cargarlo. Mostrar la tabla, la ruta externa y la configuración Run elegida; evitar rutas personales visibles.
- **Herramienta:** IntelliJ IDEA; anotar la versión exacta del aula.
- **Destino:** `docs/assets/capturas/ficheros/intellij-inventario.webp`.
- **Alt/pie:** «Inventario JavaFX iniciado desde Launcher en IntelliJ, con un catálogo CSV cargado desde una carpeta externa».
- **Estado:** pendiente dentro de IntelliJ. La aplicación y sus flujos se han probado con JavaFX real y eventos automatizados; eso no sustituye esta comprobación en el IDE.

### [Opcional · Pendiente] MongoDB: arranque y documento embebido en Compass

- **Páginas:** `docs/mongodb/configuracion.md` y `docs/mongodb/proyecto.md`, tras el arranque.
- **Objetivo:** relacionar la aplicación con el documento que guarda equipos, jugadores y estadísticas.
- **Pasos:** con Docker disponible, abrir el proyecto Maven y ejecutar `es.mjsplayground.acda.mongodb.liga.config.MongoLocal.main`; después `LigaApplication.main`. Conectar Compass a `mongodb://localhost:27018`, abrir `acda_liga` para el guiado o `acda_liga_final` para la final y expandir un documento de `equipos` con su array de jugadores y estadísticas. Mostrar también la referencia `sedeId` sin confundirla con una clave foránea automática.
- **Herramientas:** IntelliJ IDEA y MongoDB Compass; registrar versiones exactas del aula.
- **Destino:** `docs/assets/capturas/mongodb/compass-equipo-plantilla.webp`.
- **Alt/pie:** «Documento de equipo con jugadores y estadísticas embebidos y referencia a la sede en MongoDB Compass».
- **Estado:** pendiente de captura desde las herramientas del aula. Las pruebas automatizadas utilizan MongoDB real y navegador; no representan una comprobación visual de Compass ni de IntelliJ.

### [Opcional · Pendiente] Firestore: dos configuraciones Run y colección Postman

- **Páginas:** `docs/firestore/proyecto.md`, «Arranque desde IntelliJ» e «Importar la colección Postman».
- **Objetivo:** reconocer que el emulador y la aplicación son dos procesos distintos; importar la colección preparada sin crear peticiones manualmente.
- **Pasos:** abrir `reservas-guiado-solucion.zip` como Maven, ejecutar `config.Emulador.main` y mantenerlo activo en 8085; ejecutar `ReservasApplication.main` y abrir localhost:8080. Importar `postman/reservas.postman_collection.json` y mostrar una petición CRUD con su respuesta JSON. No mostrar credenciales ni una consola cloud privada.
- **Herramientas:** IntelliJ IDEA y Postman; registrar las versiones exactas de aula.
- **Destinos:** `docs/assets/capturas/firestore/intellij-emulador-aplicacion.webp` y `docs/assets/capturas/firestore/postman-reserva.webp`.
- **Alt/pies:** «Configuraciones separadas de emulador y aplicación de reservas en IntelliJ»; «Colección preparada de reservas y respuesta de creación en Postman».
- **Estado:** pendiente de captura dentro de las herramientas del aula; instrucciones textuales y colección importable disponibles. La validación automatizada utiliza el emulador real, Chromium y Newman, sin simular capturas de estas herramientas.

### [Opcional · Pendiente] JDBC: importar y arrancar desde IntelliJ

- **Página y posición:** `docs/jdbc/proyecto.md`, «Preparar el proyecto».
- **Objetivo:** contrastar el recorrido de importación Maven y Run con la instalación real del aula. La compilación, los tests y el arranque JavaFX sí se han comprobado; no se ha ejecutado IntelliJ en esta sesión.
- **Pasos:** descargar `almacen-guiado-inicial.zip`, extraer en carpeta nueva, abrir su `pom.xml` como proyecto, seleccionar JDK 25 y ejecutar `es.mjsplayground.acda.jdbc.almacen.app.Launcher.main` mediante Run. Mostrar el aviso de etapa pendiente del starter; contrastar después con la tabla de tres productos de la solución en otra carpeta.
- **Herramienta:** IntelliJ IDEA, registrar versión exacta usada; Java 25.
- **Destino:** `docs/assets/capturas/jdbc/intellij-almacen.webp`.
- **Alt y pie:** «Proyecto Maven de almacén abierto en IntelliJ y aplicación JavaFX iniciada desde Launcher».
- **Estado:** pendiente; instrucciones textuales disponibles sin enlaces a capturas inexistentes. Las capturas reales de las pruebas gráficas están en `target/qa` y no se distribuyen en los ZIP.

### [Opcional · Pendiente] Spring: arranque MVC y perfiles desde IntelliJ

- **Páginas:** `docs/spring/proyecto.md` y `docs/spring/ampliacion.md`, tras el arranque y en la demostración de seguridad.
- **Objetivo:** identificar los procesos MySQL/aplicación y configurar argumentos de programa sin editar el frontend.
- **Pasos:** abrir `catalogo-guiado-solucion.zip` como Maven; ejecutar `es.mjsplayground.acda.spring.catalogo.config.MySqlLocal.main` con la carpeta del proyecto como Working directory y después `CatalogoApplication.main`. Abrir `http://127.0.0.1:8080`. Detener la aplicación y añadir `--spring.profiles.active=seguro` a sus argumentos Run para mostrar el login local preparado.
- **Herramienta:** IntelliJ IDEA y navegador; registrar versiones reales del aula.
- **Destino:** `docs/assets/capturas/spring/intellij-catalogo-perfiles.webp`.
- **Alt/pie:** «Configuración Run del catálogo Spring con perfil seguro y formulario de acceso local».
- **Estado:** pendiente de comprobación manual dentro del IDE; no enlazar la imagen hasta disponer de la captura real. La ejecución y las pruebas automatizadas se registran separadamente en `ESTADO-DESARROLLO.md`.

## Referencia histórica

Las solicitudes del antiguo codelab de directorios se conservan en [el archivo histórico](referencias/ficheros-historico/CAPTURAS-HISTORICAS.md). Se sustituirán por solicitudes concretas del nuevo inventario cuando proceda.

## Reglas de actualización

Cada vez que se desarrolle un bloque o tema, **revisar si hacen falta visuales adicionales** y registrar solo solicitudes concretas (prioridad, ubicación, objetivo, secuencia/estado a captar, herramienta/versión, ruta/nombre, alt, pie, estado/responsable). Codex puede generar diagramas conceptuales si las fuentes bastan, pero no capturas falsas del IDE. Si un recurso llega, validar su correspondencia con la versión actual de la herramienta y marcarlo realizado; no dejar imágenes enlazadas que no existan.
