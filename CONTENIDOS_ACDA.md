# Acceso a Datos

## Índice de contenidos

Cada tema se organiza en bloques temáticos, páginas de contenido, **una práctica guiada de cierre por bloque**, prácticas finales y una sección de **Ampliación**. Antes de cada práctica de cierre, las páginas explican gradualmente los conceptos nuevos con ejemplos pequeños e interpretados. Cada práctica de cierre adopta un formato *codelab*: contexto, preparación, pasos razonados y comprobables, código incremental, pistas o soluciones parciales desplegables cuando sean útiles, pequeños retos de integración y acceso destacado a la solución pública completa. No se crean simultáneamente otra «práctica de bloque» autónoma con los mismos objetivos; sus retos se integran en el codelab. **Las prácticas finales siguen siendo autónomas**, con enunciado, proyecto inicial cuando proceda y solución pública al final, sin resolverlas paso a paso. En las actividades de persistencia se suministran interfaces preparadas cuando su desarrollo no es el objetivo. La complejidad de las reglas de negocio progresa con los contenidos. Los números de bloques orientan dentro del tema; no se usan números de UT en el material público.

---

## Manejo de ficheros y directorios

**Propósito:** gestionar archivos y persistencia local, leer y escribir distintos formatos y transformar información entre ellos.

### Bloque 1. Ficheros y directorios en Java

- **Sistemas de ficheros y rutas.** Archivos, directorios, rutas absolutas y relativas, directorio de trabajo y permisos.
- **Gestión de ficheros con `Path` y `Files`.** Crear, copiar, mover, eliminar, inspeccionar propiedades y gestionar fallos.
- **Recorrido y búsqueda de archivos.** Listar, filtrar, recorrer árboles de directorios e inventariar archivos.
- **Orientación para leer los proyectos de ejemplo.** Reconocer de manera introductoria las clases de datos, lectura/escritura, reglas y ejecución de un programa Java sin adelantar la enseñanza formal de patrones, ORM ni Spring.
- **Práctica guiada de cierre del bloque:** Organizar e inventariar directorios con `Path` y `Files`, incorporando recorrido, filtros, tamaños, estadísticas, exportación y tratamiento de incidencias.

### Bloque 2. Ficheros de texto y CSV

- **Lectura y escritura de texto.** UTF-8, `Files.readString`, `Files.writeString` y flujos de caracteres.
- **Procesamiento eficiente de texto.** Lectura línea a línea, `BufferedReader`, `BufferedWriter` y `try-with-resources`.
- **Ficheros CSV.** Delimitadores, encabezados, comillas, campos vacíos, bibliotecas CSV y validación.
- **Práctica guiada de cierre del bloque:** Importar, validar y combinar CSV, generar un informe de texto y exportar registros filtrados respetando el formato.

### Bloque 3. Ficheros XML

- **Lectura, escritura y transformación XML con DOM.** Analizar documentos, recorrer elementos, modificar nodos, generar XML y configurar el analizador de forma segura frente a entidades externas (XXE).
- **Práctica guiada de cierre del bloque:** Leer XML con DOM seguro, exportar registros a CSV y construir después XML a partir de CSV con validaciones.

### Bloque 4. Ficheros JSON

- **Estructura y tipos de datos JSON.** Objetos, arrays, valores, estructuras anidadas y diferencias respecto a XML.
- **Lectura y escritura JSON con Jackson.** `ObjectMapper`, árboles JSON, conversión de objetos Java y manejo de errores.
- **Práctica guiada de cierre del bloque:** Leer estructuras JSON con Jackson y transformar registros entre JSONy y CSV, incluyendo anidamiento y filtros.

### Bloque 5. Ficheros binarios

- **Flujos de datos binarios.** `InputStream`, `OutputStream`, `DataInputStream` y `DataOutputStream`.
- **Acceso aleatorio.** `RandomAccessFile`, desplazamientos, registros de longitud fija y actualización de datos.
- **Serialización de objetos Java.** `Serializable`, streams de objetos, `serialVersionUID`, compatibilidad y riesgos de deserializar datos no confiables.
- **Práctica guiada de cierre del bloque:** Guardar, leer y modificar registros binarios de tamaño fijo; calcular informes de existencias y exportarlos a CSV o JSON.

### Bloque 6. Transformación de formatos y aplicación

- **Organización básica del código de ejemplo.** Distinguir modelos de datos (`model.entity`), clases de lectura/escritura (`model.dao`), reglas de aplicación (`model.service`) y código de presentación o ejecución, con ejemplos sencillos y sin adelantar el estudio formal de ORM, JPA o inyección de dependencias.
- **Importadores y exportadores reutilizables.** Separar lectura, transformación, validación y escritura en clases con responsabilidades claras.
- **Validación y recuperación ante errores.** Registros incompletos, codificaciones, escrituras fallidas y conservación de información válida.
- **Práctica guiada de cierre del bloque:** Construir progresivamente un conversor multiformato con importadores, validaciones, informes de incidencias y exportadores reutilizables.

### Prácticas finales

- **Final fácil — Conversor de datos de clientes.** Leer un formato, comprobar reglas sencillas y generar otro formato (No incluir XML).
- **Final intermedia — Importador y exportador de inventario.** Leer y combinar archivos, validar existencias y precios, generar informes y convertir entre varios formatos (No incluir XML).
- **Final avanzada — Gestor universal de información comercial.** Aplicación con JavaFX preparado, conversión multiformato, filtros, cálculos, errores y persistencia (No incluir XML).

### Ampliación

- **Procesamiento streaming de ficheros grandes.** Procesamiento incremental sin cargar archivos completos en memoria.
- **Monitorización de directorios con `WatchService`.** Detección y tratamiento de cambios en el sistema de archivos.
- **Canales de ficheros y NIO avanzado.** `FileChannel` y operaciones especializadas.
- **Escrituras fiables.** Archivos temporales, sustitución atómica y recuperación ante interrupciones.

---

## Herramientas de mapeo objeto-relacional (ORM) y JPA con Hibernate

**Propósito:** persistir entidades Java en MySQL mediante Hibernate, diseñando repositorios y servicios que apliquen reglas de negocio.

### Bloque 1. Fundamentos de ORM

- **Desfase objeto-relacional y herramientas ORM.** Tablas frente a objetos, ventajas y limitaciones del mapeo.
- **JPA, Jakarta Persistence e Hibernate.** Especificación, implementación, API de persistencia y papel del proveedor.
- **Práctica guiada de cierre del bloque:** Reconocer entidades, claves y relaciones de un caso de negocio antes de realizar el mapeo.

### Bloque 2. Instalación y configuración

- **MySQL con Docker y MySQL Workbench.** Contenedor persistente, conexión, esquemas y publicación del puerto interno 3306 como 3306 o 3308 en el equipo.
- **Primer proyecto Hibernate con Maven.** Dependencias, configuración, conexión, registro de entidades y mención breve al mapeo XML (`orm.xml`) como alternativa, sin protagonismo en las prácticas.
- **POJO, JavaBean y entidad persistente.** Constructores, propiedades, accesores y requisitos de persistencia.
- **Mapeo de entidades mediante anotaciones.** `@Entity`, `@Table`, `@Id`, `@Column`, identificadores, tipos Java modernos y restricciones.
- **Práctica guiada de cierre del bloque:** Configurar una aplicación Hibernate, persistir una entidad en MySQL y ampliar el modelo con otra entidad y sus restricciones.

### Bloque 3. Persistencia, servicios y logging

- **Sesiones y estados de las entidades.** Contexto de persistencia, estados transitorio, persistente y separado.
- **Repositorios y operaciones CRUD.** Guardar, buscar, modificar y eliminar mediante la API de persistencia.
- **Transacciones con Hibernate.** Inicio, `commit`, `rollback` y manejo de recursos.
- **Capa de servicio y reglas de negocio.** Validar invariantes y coordinar repositorios sin poner lógica comercial en la interfaz.
- **Logging profesional con SLF4J y Log4j2.** Fachada e implementación, niveles, configuración, mensajes parametrizados y registro de excepciones.
- **Práctica guiada de cierre del bloque:** Implementar un repositorio y un servicio con CRUD, reglas de negocio, transacciones, recuperación de errores y logging.

### Bloque 4. Relaciones entre entidades

- **Relaciones uno a uno (1:1).** `@OneToOne`, claves externas y persistencia relacionada.
- **Relaciones uno a muchos y muchos a uno (1:N / N:1).** `@OneToMany`, `@ManyToOne`, lado propietario y colecciones.
- **Relaciones muchos a muchos (M:N).** `@ManyToMany`, tablas intermedias y entidades de asociación con datos propios.
- **Carga y ciclo de vida de las relaciones.** `LAZY`, `EAGER`, cascadas, `orphanRemoval` y errores habituales.
- **Práctica guiada de cierre del bloque:** Relacionar entidades y gestionar operaciones sobre relaciones con restricciones de negocio, comprobando los resultados en MySQL.

### Bloque 5. Consultas y recuperación

- **Consultas HQL y JPQL.** Parámetros, filtros, ordenación y consultas tipadas.
- **Consultas sobre relaciones.** Joins, proyecciones, agregaciones sencillas y paginación.
- **Consultas SQL nativas con Hibernate.** Cuándo acceder a tablas directamente y cómo recuperar resultados.
- **Práctica guiada de cierre del bloque:** Crear consultas parametrizadas y ampliar el repositorio con filtros, joins, informes y validaciones basadas en consultas.

### Prácticas finales

- **Final fácil — Catálogo de productos.** CRUD con servicio y reglas de precios, existencias y duplicados.
- **Final intermedia — Gestión de biblioteca.** Relaciones, disponibilidad, préstamos, devoluciones, reglas de negocio y consultas.
- **Final avanzada — Sistema de gestión de pedidos.** Varias entidades, cálculos, consultas, transacciones y restricciones comerciales, con interfaz JavaFX preparada.

### Ampliación

- **Rendimiento de Hibernate.** Problema N+1, `JOIN FETCH`, consultas generadas y diagnóstico.
- **Bloqueo optimista y concurrencia.** `@Version` y conflictos de actualización.
- **Migraciones de esquemas con Flyway.** Versionado de cambios de base de datos.
- **Pruebas avanzadas de persistencia.** Verificación automatizada de repositorios, relaciones y consultas.

---

## Acceso a datos mediante componentes (Spring)

**Propósito:** construir componentes de persistencia reutilizables con Spring; trabajar IoC, inyección, servicios, repositorios, Spring Data JPA y aplicaciones MVC con Thymeleaf. Las API REST se estudian más adelante al construir servicios sobre Cloud Firestore.

### Bloque 1. Programación orientada a componentes

- **Componentes software y reutilización.** Interfaces, encapsulación, composición, ventajas e inconvenientes.
- **Spring Framework y Spring Boot.** Ecosistema, starters, autoconfiguración y ejecución.
- **Práctica guiada de cierre del bloque:** Crear y ejecutar una aplicación sencilla con componentes Spring, entendiendo su responsabilidad y reutilización.

### Bloque 2. Componentes y configuración Spring

- **Contenedor IoC y ciclo de vida de beans.** Registro, creación, inicialización y gestión.
- **Inyección de dependencias.** `@Component`, `@Service`, `@Repository`, `@Bean` e inyección por constructor.
- **Propiedades, perfiles y eventos.** `application.properties`, configuración externa y comunicación entre componentes.
- **Práctica guiada de cierre del bloque:** Configurar componentes y sustituir implementaciones de un servicio mediante inyección de dependencias.

### Bloque 3. Componentes de acceso a datos

- **Arquitectura por capas en Spring.** Controlador, servicio, repositorio, entidad y responsabilidades.
- **Spring Data JPA.** `JpaRepository`, CRUD y consultas derivadas.
- **Servicios, validación y transacciones.** Reglas de negocio, coordinación y `@Transactional`.
- **Componentes de persistencia reutilizables.** Interfaces e implementaciones sobre ficheros y ORM.
- **Práctica guiada de cierre del bloque:** Conectar un servicio con Spring Data JPA e incorporar reglas de negocio sobre una aplicación parcialmente preparada.

### Bloque 4. Aplicaciones web con Thymeleaf

- **Spring MVC y Thymeleaf.** Modelo, vista y controlador; lectura y adaptación de plantillas existentes.
- **Formularios y visualización de datos.** Listados, formularios, mensajes y validación de entrada.
- **Práctica guiada de cierre del bloque:** Integrar servicios con una interfaz MVC/Thymeleaf preparada y completar persistencia, formularios y validaciones.

### Bloque 5. Composición y reutilización

- **Integración de varios servicios.** Coordinación de operaciones y colaboración sin dependencias circulares.
- **Diseño de componentes reutilizables.** Interfaces, sustitución de implementaciones, bajo acoplamiento y empaquetado conceptual.
- **Práctica guiada de cierre del bloque:** Coordinar varios servicios y reglas de negocio en un sistema MVC preparado, evitando acoplamiento innecesario.

### Prácticas finales

- **Final fácil — Gestor web de artículos.** Con interfaz y controladores preparados, implementar servicios, repositorio y validaciones.
- **Final intermedia — Gestión de reservas.** Entidades relacionadas, disponibilidad y coordinación de servicios.
- **Final avanzada — Aplicación web de gestión empresarial.** Servicios reutilizables, persistencia, reglas complejas y operaciones coordinadas; interfaz suministrada.

### Ampliación

- **Pruebas con JUnit y Spring Boot Test.** Pruebas de servicios, repositorios e integración.
- **Diagnóstico avanzado.** Logging, excepciones, perfiles y resolución de incidencias.
- **Empaquetado y ejecución de Spring Boot.** JAR ejecutable, configuración y distribución.
- **Spring Security para aplicaciones MVC.** Autenticación y autorización.
- **Spring Boot Actuator.** Estado, indicadores y métricas.
- **Despliegue de Spring con Docker.** Imagen, contenedor y configuración de ejecución.
- **Testcontainers.** Pruebas de integración utilizando bases de datos reales en contenedores.

---

## Conexiones a bases de datos relacionales (JDBC)

**Propósito:** programar manualmente el acceso a bases relacionales mediante JDBC. Spring puede proporcionar componentes e interfaz MVC, pero no sustituirá la implementación JDBC del repositorio por Spring Data.

### Bloque 1. Conectores y bases relacionales

- **JDBC frente a herramientas ORM.** Drivers, arquitectura, control directo, ventajas y límites.
- **Bases embebidas y de servidor.** SQLite y MySQL; características, elección y configuración.
- **Práctica guiada de cierre del bloque:** Preparar las conexiones iniciales a SQLite y MySQL con Docker, comparar su configuración y probar operaciones sencillas.

### Bloque 2. Conexiones y operaciones SQL

- **Conexiones JDBC.** `DriverManager`, `Connection`, URL, credenciales y ciclo de vida.
- **Ejecución de instrucciones SQL.** `Statement`, scripts, DDL, inserciones, actualizaciones y eliminaciones.
- **Recuperación de resultados con `ResultSet`.** Filas, columnas, tipos y conversión a objetos Java.
- **Consultas seguras con `PreparedStatement`.** Parámetros, reutilización y prevención de inyección SQL.
- **Práctica guiada de cierre del bloque:** Desarrollar consultas parametrizadas y CRUD, importar registros y realizar búsquedas seguras mediante JDBC.

### Bloque 3. Repositorios y servicios JDBC

- **Repositorios JDBC manuales.** Contratos, implementaciones y encapsulación de SQL.
- **Servicios y reglas de negocio.** Verificaciones, coordinación, validación y errores.
- **Integración con Spring MVC.** Usar repositorios JDBC creados desde cero mediante servicios Spring e interfaz web preparada.
- **Práctica guiada de cierre del bloque:** Completar una aplicación con vistas preparadas, repositorios JDBC manuales y reglas comerciales en servicios.

### Bloque 4. Metadatos y consultas complejas

- **Metadatos de JDBC.** `DatabaseMetaData` y `ResultSetMetaData`.
- **Consultas de varias tablas.** Joins, claves generadas y transformación de relaciones a objetos.
- **Procedimientos almacenados.** `CallableStatement`, parámetros de entrada/salida y resultados en MySQL.
- **Práctica guiada de cierre del bloque:** Recuperar datos relacionados y metadatos para construir consultas e informes con reglas de negocio.

### Bloque 5. Transacciones y fiabilidad

- **Transacciones JDBC.** ACID, `autoCommit`, `commit`, `rollback` y coordinación entre operaciones.
- **Gestión de recursos y errores.** `try-with-resources`, `SQLException` y logging profesional.
- **Pruebas de persistencia y consistencia.** CRUD, errores, fallos parciales e invariantes.
- **Práctica guiada de cierre del bloque:** Ejecutar operaciones compuestas y provocar `rollback`; ampliar el caso a una gestión de inventario con cambios atómicos.

### Prácticas finales

- **Final fácil — Gestor de clientes.** CRUD JDBC, servicios y validaciones de datos.
- **Final intermedia — Gestión de almacén.** Relaciones, control de existencias, consultas y reglas comerciales.
- **Final avanzada — Sistema de gestión de ventas.** Transacciones, cálculos, relaciones, consultas y restricciones comerciales, con interfaz preparada.

### Ampliación

- **Pool de conexiones con HikariCP.** Reutilización de conexiones y configuración.
- **Procesamiento por lotes con JDBC.** Inserciones y actualizaciones batch.
- **Optimización SQL.** Índices, planes de ejecución y análisis de consultas.
- **Concurrencia y aislamiento.** Problemas entre transacciones y niveles de aislamiento.
- **Tipos compuestos y persistencia objeto-relacional con PostgreSQL.** Definición de tipos mediante `CREATE TYPE`, atributos compuestos, consultas y una demostración breve de acceso desde Java.
- **Bases de datos orientadas a objetos con ObjectDB.** Diferencias respecto a ORM y bases documentales; demostración breve de persistencia, consultas y transacciones. Preparar el entorno con Docker, sin instalación adicional de ObjectDB en el equipo. Antes de distribuir el ejemplo, comprobar la compatibilidad real con Java 25 y las condiciones de la licencia gratuita; evitar imágenes de terceros sin mantenimiento.

---

## Bases de datos documentales con Cloud Firestore

**Propósito:** gestionar colecciones y documentos en Cloud Firestore y diseñar API REST con Spring Boot que intercambien JSON, incorporando validación, tratamiento de errores, pruebas y documentación OpenAPI/Swagger UI.

### Bloque 1. Persistencia documental

- **Bases relacionales y documentales.** Modelos, flexibilidad, ventajas, limitaciones y usos.
- **Firebase y Cloud Firestore.** Servicios, arquitectura y componentes de una solución documental.
- **Práctica guiada de cierre del bloque:** Diseñar un modelo documental de colecciones, documentos y relaciones para un caso empresarial, razonando decisiones básicas.

### Bloque 2. Configuración y acceso seguro

- **Creación y configuración de Cloud Firestore.** Proyecto, base, región y consola.
- **SDK de administración de Firebase con Spring Boot.** Dependencias, inicialización e integración.
- **Credenciales y seguridad de acceso.** Cuentas de servicio, IAM, variables de entorno y protección de secretos.
- **Práctica guiada de cierre del bloque:** Configurar y conectar una aplicación de servicio a Firestore con credenciales y configuración externas seguras.

### Bloque 3. Modelado y persistencia

- **Colecciones, documentos y tipos.** Identificadores, campos, mapas, listas y conversiones.
- **Modelado de relaciones documentales.** Mapas, subcolecciones, referencias, duplicación y elección de estructura.
- **Repositorios y servicios Firestore.** CRUD, reglas de negocio y gestión de fallos.
- **Práctica guiada de cierre del bloque:** Implementar CRUD documental y completar operaciones sobre documentos relacionados con validaciones comerciales.

### Bloque 4. HTTP y REST

- **Fundamentos de HTTP.** Peticiones, respuestas, cabeceras, JSON y códigos de estado.
- **Diseño de API REST.** Recursos, rutas, GET/POST/PUT/PATCH/DELETE, idempotencia y convenciones.
- **Controladores REST con Spring Boot.** `@RestController`, mappings, parámetros, cuerpos y `ResponseEntity`.
- **Práctica guiada de cierre del bloque:** Publicar el CRUD de Firestore como API REST y ampliar sus endpoints a un segundo recurso con reglas de negocio.

### Bloque 5. Buenas prácticas de API

- **DTO y validación de entrada.** Contratos independientes del modelo persistente, Bean Validation y validación de servicio.
- **Respuestas y tratamiento de errores.** Estados HTTP, `@RestControllerAdvice`, excepciones y formato coherente.
- **OpenAPI y Swagger UI.** Esquemas, operaciones, respuestas y documentación interactiva.
- **Pruebas manuales de API con Postman.** Colecciones, variables, escenarios normales y errores.
- **Práctica guiada de cierre del bloque:** Documentar y probar una API con OpenAPI y Postman, incorporando validaciones y respuestas de error consistentes.

### Bloque 6. Consultas y consistencia

- **Consultas y filtros Firestore.** Condiciones de igualdad, rangos y filtros combinados.
- **Índices y paginación con cursores.** Ordenación, índices, límites, cursores y costes de lectura.
- **Escrituras agrupadas y transacciones.** Operaciones coordinadas, consistencia y recuperación ante fallos.
- **Práctica guiada de cierre del bloque:** Construir consultas paginadas y operaciones coordinadas en Firestore, comprobando índices, costes y reglas comerciales.

### Prácticas finales

- **Final fácil — API de catálogo de libros.** CRUD REST, DTO, servicio, validaciones y respuestas HTTP.
- **Final intermedia — API de reservas.** Documentos relacionados, disponibilidad, consultas y reglas de negocio.
- **Final avanzada — API de gestión comercial en la nube.** Recursos relacionados, filtros, transacciones, validación, errores y OpenAPI.

### Ampliación

- **Firebase Authentication.** Identidad y autorización de usuarios.
- **Firestore en tiempo real.** Listeners y sincronización de cambios.
- **Firebase Emulator Suite.** Pruebas y desarrollo local aislado.
- **Pruebas automatizadas de API.** MockMvc y pruebas de integración.
- **Seguridad profesional de API.** Tokens, autenticación, autorización y protección de recursos.

---

## Bases de datos NoSQL con MongoDB

**Propósito:** trabajar con MongoDB standalone mediante Docker y Spring Data MongoDB, modelando documentos y realizando consultas y agregaciones. Las arquitecturas replicadas y las transacciones multidocumento se introducen en Ampliación.

### Bloque 1. Fundamentos MongoDB

- **MongoDB y bases documentales.** Arquitectura, casos de uso y comparación con el modelo relacional y Firestore.
- **BSON, documentos e identificadores.** `ObjectId`, tipos, listas y estructuras anidadas.
- **Práctica guiada de cierre del bloque:** Diseñar documentos MongoDB para una necesidad empresarial y justificar el modelo elegido.

### Bloque 2. Instalación y modelado

- **MongoDB standalone con Docker y Compass.** Contenedor, volúmenes, conexión y exploración visual.
- **Diseño de documentos MongoDB.** Embebidos, referencias, esquemas flexibles y validación.
- **Práctica guiada de cierre del bloque:** Crear y consultar documentos con Compass, analizando después embebidos frente a referencias según las consultas requeridas.

### Bloque 3. Persistencia con Spring Data MongoDB

- **MongoDB con Spring Boot.** Dependencias, conexión y configuración.
- **Documentos Java y `MongoRepository`.** `@Document`, `@Id`, mapeo y CRUD.
- **Servicios y reglas de negocio.** Validación, coordinación y errores de persistencia.
- **Persistencia de estructuras relacionadas.** Listas, documentos embebidos, referencias y recuperación.
- **Práctica guiada de cierre del bloque:** Persistir documentos mediante Spring Data MongoDB y completar reglas de negocio sobre documentos relacionados.

### Bloque 4. Consultas y agregaciones

- **Consultas personalizadas.** Consultas derivadas, `@Query`, filtros y operadores.
- **Ordenación, paginación e índices.** Resultados paginados y búsquedas eficientes.
- **Aggregation Pipeline.** `$match`, `$group`, `$project`, `$sort`, `$lookup` y cálculos.
- **Práctica guiada de cierre del bloque:** Construir consultas y agregaciones sobre colecciones y generar informes y estadísticas de sus resultados.

### Bloque 5. Integridad y pruebas

- **Atomicidad y operaciones seguras sobre documentos.** Actualizaciones atómicas de un documento y límites de consistencia.
- **Pruebas de persistencia MongoDB.** CRUD, consultas, validaciones, fallos y resultados.
- **Práctica guiada de cierre del bloque:** Detectar y corregir problemas de integridad, validando invariantes y probando operaciones documentales.

### Prácticas finales

- **Final fácil — Gestor de jugadores.** CRUD, repositorio, servicio y validaciones.
- **Final intermedia — Gestión de equipos y competiciones.** Datos relacionados, filtros y reglas de negocio.
- **Final avanzada — Sistema de análisis deportivo.** Estructuras documentales, cálculos, consultas, agregaciones y validaciones complejas.

### Ampliación

- **Replica sets y transacciones multidocumento.** Descripción conceptual de arquitectura y requisitos, sin despliegue práctico obligatorio.
- **MongoDB Atlas.** Servicio gestionado y conexiones seguras.
- **Change Streams.** Procesamiento de eventos de cambios.
- **Optimización de consultas.** `explain`, índices compuestos y análisis de rendimiento.
- **`MongoTemplate`.** Operaciones especializadas que superan los métodos habituales de repositorio.

---

## Ayuda y herramientas

Páginas operativas comunes, enlazadas desde los temas donde sean útiles para evitar duplicaciones.

### Entorno Java y proyectos

- **Java 25: instalación y configuración.** Instalar y seleccionar JDK, comprobar versión y solucionar problemas habituales.
- **IntelliJ IDEA.** Importar y ejecutar proyectos, autoimportación, optimización de imports, atajos, búsqueda y refactorización.
- **Maven y Maven Wrapper.** `pom.xml`, dependencias, `mvnw`/`mvnw.cmd`, compilación, ejecución, pruebas y descarga reproducible.
- **Depuración de Java.** Breakpoints, inspección de variables, paso a paso, evaluación y stack traces.
- **JavaFX en proyectos preparados.** Arranque, vistas, formularios, tablas y diagnóstico de problemas de interfaz.
- **Thymeleaf en aplicaciones preparadas.** Ubicación de plantillas y recursos, ejecución y pequeñas modificaciones.

### Contenedores y bases de datos

- **Docker y Docker Compose.** Imágenes, contenedores, puertos, volúmenes, redes, comandos y archivos Compose; ejemplos MySQL y MongoDB.
- **MySQL y Workbench.** Conexión, esquemas, importación de scripts y puerto anfitrión 3308/puerto del contenedor 3306.
- **SQLite.** Bases en archivos locales y herramientas de exploración.
- **MongoDB Compass.** Conexión al contenedor, exploración de documentos y ejecución de consultas.
- **Firebase Console.** Gestión de proyectos, bases y credenciales sin exponer secretos.

### Calidad, API y seguridad

- **Logging profesional.** SLF4J, Log4j2, configuración, niveles y lectura de registros.
- **Pruebas con JUnit.** Ejecutar, interpretar y depurar tests.
- **Postman.** Colecciones, entornos, parámetros, cuerpos y pruebas de respuestas.
- **OpenAPI y Swagger UI.** Localizar documentación y probar endpoints.
- **Configuración y secretos.** Variables de entorno, perfiles, credenciales, `.gitignore` y datos sensibles.
- **Documentación oficial de APIs y dependencias.** Localizar clases, métodos, manuales y versiones.
- **Solución de problemas frecuentes.** Errores de JDK, Maven, puertos, conexiones, recursos, Docker y ejecución.

---

## Criterios técnicos compartidos

- Los proyectos Java utilizan **Java 25** como objetivo inicial, **Maven Wrapper** y paquetes bajo `es.mjsplayground`, con versiones verificadas antes de su distribución. Los paquetes vinculados a actividades utilizarán nombres temáticos estables (`es.mjsplayground.acda.<tema>.<proyecto>`), sin depender del orden de impartición.
- Las interfaces estarán preparadas cuando desarrollarlas no sea el objetivo de la actividad. En aplicaciones Spring MVC predominan las vistas **Thymeleaf** suministradas; en aplicaciones de escritorio de persistencia, **JavaFX**.
- Las **reglas de negocio residen en servicios**. Los repositorios encapsulan persistencia, y las vistas o controladores gestionan interacción y validaciones inmediatas de formato.
- En los proyectos de persistencia y componentes se emplearán **SLF4J y Log4j2** y técnicas de depuración profesional, en lugar de `System.out.println` como mecanismo habitual de diagnóstico.
- Las prácticas de bloque y finales combinarán, progresivamente, validaciones, consultas, errores, cálculos, invariantes y coordinación de operaciones.
- Cada tema incluye **prácticas finales fácil, intermedia y avanzada**, seguidas de **Ampliación**. Las soluciones de aprendizaje públicas y los proyectos iniciales se desarrollarán posteriormente conforme al estándar docente.
- La web mantendrá accesibles los contenidos de aprendizaje y sus ampliaciones de forma autónoma respecto de la organización docente de cada curso.
