# MongoDB con Docker y Compass

El entorno tiene dos procesos: MongoDB conserva documentos y Spring Boot ofrece la interfaz y la API. Compass es una herramienta de inspección; no sustituye a ninguno de ellos. Reutilizamos los conceptos de [contenedores, puertos y volúmenes](../ayuda/contenedores.md) en lugar de volver a explicar Docker desde cero.

## Preparación reproducible

El proyecto incluye la configuración del servidor local y un lanzador `config.MongoLocal`. Con Docker Desktop iniciado, ejecuta ese `main` desde IntelliJ para preparar el servicio. Después ejecuta `LigaApplication.main` en otra configuración Run y abre `http://localhost:8080`.

MongoDB utiliza el puerto publicado **27018** del equipo; su puerto interno habitual es 27017. La URI local comienza por `mongodb://127.0.0.1:27018/` y debe seleccionar la base definida en el README. No copies el puerto 8080 del navegador como puerto de MongoDB.

Al cambiar de starter a solución, detén el backend anterior para liberar el puerto HTTP. El servidor de base de datos puede seguir disponible; no hay que crear un MongoDB diferente para cada etapa. Consulta [Maven y configuraciones Run](../ayuda/proyectos-maven.md) si IntelliJ aún no reconoce las dependencias o el JDK.

## Qué conserva el volumen

El volumen de Docker guarda los datos del servidor fuera del sistema de archivos efímero del contenedor. Detener MongoDB y volver a arrancarlo no equivale a vaciar la base. El proyecto prepara un catálogo de sedes, pero no debe volver a insertar o sobrescribir todos los equipos cada vez que se inicia Java.

Para un escenario limpio usa la base de pruebas o los datos temporales que indique el README. No elimines un volumen para resolver una conexión fallida: es una operación distinta que puede perder trabajo. Cambiar la URI puede seleccionar otra base y mostrar un listado vacío sin que la anterior haya desaparecido.

## Dependencias y configuración Spring

El starter utiliza el módulo de Spring Data MongoDB junto con Spring Web y validación de entrada. La versión se controla desde el POM del proyecto; no se mezclan versiones de driver elegidas al azar con las que gestiona Spring Boot.

La configuración de conexión identifica host, puerto y base. Cuando se utiliza otra URI mediante variables de entorno o una opción de ejecución, debe conservarse coherencia entre el backend y Compass. Las pruebas de integración utilizan datos aislados; una prueba que modifica documentos no debe apuntar a una base compartida con información importante.

En el proyecto, `EquipoRepository` y el repositorio de sedes se descubren dentro del contexto Spring. Si aparece un fallo de creación de bean, revisa primero el mensaje: una propiedad mal escrita en el nombre de una consulta derivada puede impedir arrancar aunque MongoDB esté disponible.

## Inspeccionar con Compass

Abre Compass y crea una conexión utilizando la URI del proyecto, con el puerto local 27018. Después selecciona la base y la colección de equipos. La [guía oficial de conexión](https://www.mongodb.com/docs/compass/connect/) explica los campos y opciones de conexión.

Tras crear un equipo desde la aplicación, actualiza la vista de documentos y compara:

- `_id` y su tipo BSON.
- Nombre, categoría y referencia de sede.
- Lista de jugadores y sus identificadores propios.
- Subdocumento de estadísticas y valores numéricos.
- Versión utilizada para detectar cambios concurrentes.

Un jugador embebido aparece dentro de su equipo; no tiene por qué aparecer en una colección independiente. Cambiar un documento manualmente puede saltarse las reglas Java: Compass se utiliza aquí para observar, no para fabricar una plantilla que la API debería rechazar.

Para estudiar consultas, el panel de filtro admite expresiones como `{ "categoria": "SENIOR" }`. El panel de ordenación tiene otra función; escribir una ordenación dentro del filtro no equivale a configurar ambos. La [consulta de documentos en Compass](https://www.mongodb.com/docs/compass/query/filter/) detalla ese recorrido.

## Identidad, red y credenciales

El entorno del tema se limita al equipo local. Una base local sin autenticación no debe publicarse en una red accesible. Para otro entorno habría que configurar usuarios, permisos mínimos, red y transporte seguro, y mantener las credenciales fuera del código y de los ZIP.

La autenticación de MongoDB identifica a la aplicación frente a la base; la [autenticación y autorización de la API](../firestore/ampliacion.md#seguridad-de-la-api) identifica y limita a sus usuarios. No son la misma protección. Tampoco se debe confiar en CORS como permiso de acceso a los datos.

## Diagnóstico por capas

| Síntoma | Comprobación inicial |
| --- | --- |
| Docker no conecta | Motor Docker disponible |
| MongoDB no responde | Servicio, salud y puerto publicado 27018 |
| Compass conecta pero la web no | Backend Spring, puerto HTTP y URI efectiva |
| Una base parece vacía | Nombre de base, colección y configuración utilizada |
| El starter muestra una etapa pendiente | TODO por completar; no es un problema de credenciales |
| Un cambio devuelve conflicto | Datos y versión actuales, no reinstalar el servidor |

Lee la causa del error en el logging y utiliza [diagnóstico y pruebas](../ayuda/diagnostico.md) para seguir el recorrido. Continúa con [Spring Data y persistencia](persistencia.md).
