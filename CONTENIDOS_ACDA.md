# Acceso a Datos

## Índice de contenidos

Cada tema se organiza en bloques temáticos, páginas de contenido, prácticas de aprendizaje y una sección final de **Ampliación**. Las prácticas guiadas pueden integrarse en una explicación si son breves o disponer de página propia cuando tengan entidad suficiente. En las actividades de persistencia se suministran las interfaces preparadas cuando su desarrollo no es el objetivo del ejercicio. Las prácticas incorporan reglas de negocio de complejidad progresiva.

---

## Manejo de ficheros y directorios

**Propósito:** gestionar archivos y persistencia local, leer y escribir distintos formatos y transformar información entre ellos.

### Bloque 1. Ficheros y directorios en Java

- **Sistemas de ficheros y rutas.** Archivos, directorios, rutas absolutas y relativas, directorio de trabajo y permisos.
- **Gestión de ficheros con `Path` y `Files`.** Crear, copiar, mover, eliminar, inspeccionar propiedades y gestionar fallos.
- **Recorrido y búsqueda de archivos.** Listar, filtrar, recorrer árboles de directorios e inventariar archivos.
- **Práctica guiada:** organizar y explorar directorios con `Path` y `Files`.
- **Práctica de bloque:** inventario de archivos con filtros, estadísticas y reglas de selección.

### Bloque 2. Ficheros de texto y CSV

- **Lectura y escritura de texto.** UTF-8, `Files.readString`, `Files.writeString` y flujos de caracteres.
- **Procesamiento eficiente de texto.** Lectura línea a línea, `BufferedReader`, `BufferedWriter` y `try-with-resources`.
- **Ficheros CSV.** Delimitadores, encabezados, comillas, campos vacíos, bibliotecas CSV y validación.
- **Práctica guiada:** importar un CSV, validar campos y generar un informe de texto.
- **Práctica de bloque:** fusionar CSV, descartar entradas no válidas y exportar registros filtrados.

### Bloque 3. Ficheros XML

- **Lectura, escritura y transformación XML con DOM.** Analizar documentos, recorrer elementos, modificar nodos, generar XML y configurar el analizador de forma segura frente a entidades externas (XXE).
- **Práctica guiada:** leer XML y exportar los registros a CSV.
- **Práctica de bloque:** transformar CSV en XML y aplicar validaciones a los datos.

### Bloque 4. Ficheros JSON

- **Estructura y tipos de datos JSON.** Objetos, arrays, valores, estructuras anidadas y diferencias respecto a XML.
- **Lectura y escritura JSON con Jackson.** `ObjectMapper`, árboles JSON, conversión de objetos Java y manejo de errores.
- **Práctica guiada:** importar JSON y generar CSV.
- **Práctica de bloque:** transformar información entre CSV, XML y JSON, incluyendo datos anidados y filtros.

### Bloque 5. Ficheros binarios

- **Flujos de datos binarios.** `InputStream`, `OutputStream`, `DataInputStream` y `DataOutputStream`.
- **Acceso aleatorio.** `RandomAccessFile`, desplazamientos, registros de longitud fija y actualización de datos.
- **Serialización de objetos Java.** `Serializable`, streams de objetos, `serialVersionUID`, compatibilidad y riesgos de deserializar datos no confiables.
- **Práctica guiada:** guardar, consultar y modificar registros binarios.
- **Práctica de bloque:** leer registros binarios y generar un informe JSON o CSV con reglas de negocio.

### Bloque 6. Transformación de formatos y aplicación

- **Importadores y exportadores reutilizables.** Separar lectura, transformación, validación y escritura en clases con responsabilidades claras.
- **Validación y recuperación ante errores.** Registros incompletos, codificaciones, escrituras fallidas y conservación de información válida.
- **Práctica de bloque:** conversor multiformato capaz de importar, validar y exportar una misma colección en diferentes formatos.

### Prácticas finales

- **Final fácil — Conversor de datos de clientes.** Leer un formato, comprobar reglas sencillas y generar otro formato.
- **Final intermedia — Importador y exportador de inventario.** Leer y combinar archivos, validar existencias y precios, generar informes y convertir entre varios formatos.
- **Final avanzada — Gestor universal de información comercial.** Aplicación con JavaFX preparado, conversión multiformato, filtros, cálculos, errores y persistencia.

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
- **Práctica de bloque:** identificar entidades, claves y relaciones de un caso de negocio.

### Bloque 2. Instalación y configuración

- **MySQL con Docker y MySQL Workbench.** Contenedor persistente, conexión, esquemas y publicación del puerto interno 3306 como 3306 o 3308 en el equipo.
- **Primer proyecto Hibernate con Maven.** Dependencias, configuración, conexión, registro de entidades y mención breve al mapeo XML (`orm.xml`) como alternativa, sin protagonismo en las prácticas.
- **POJO, JavaBean y entidad persistente.** Constructores, propiedades, accesores y requisitos de persistencia.
- **Mapeo de entidades mediante anotaciones.** `@Entity`, `@Table`, `@Id`, `@Column`, identificadores, tipos Java modernos y restricciones.
- **Práctica guiada:** crear una entidad y comprobar su persistencia en MySQL.
- **Práctica de bloque:** modelar otra entidad con tipos, restricciones y valores coherentes.

### Bloque 3. Persistencia, servicios y logging

- **Sesiones y estados de las entidades.** Contexto de persistencia, estados transitorio, persistente y separado.
- **Repositorios y operaciones CRUD.** Guardar, buscar, modificar y eliminar mediante la API de persistencia.
- **Transacciones con Hibernate.** Inicio, `commit`, `rollback` y manejo de recursos.
- **Capa de servicio y reglas de negocio.** Validar invariantes y coordinar repositorios sin poner lógica comercial en la interfaz.
- **Logging profesional con SLF4J y Log4j2.** Fachada e implementación, niveles, configuración, mensajes parametrizados y registro de excepciones.
- **Práctica guiada:** repositorio y servicio para una entidad con validaciones y logs.
- **Práctica de bloque:** CRUD con reglas de negocio y recuperación de errores.

### Bloque 4. Relaciones entre entidades

- **Relaciones uno a uno (1:1).** `@OneToOne`, claves externas y persistencia relacionada.
- **Relaciones uno a muchos y muchos a uno (1:N / N:1).** `@OneToMany`, `@ManyToOne`, lado propietario y colecciones.
- **Relaciones muchos a muchos (M:N).** `@ManyToMany`, tablas intermedias y entidades de asociación con datos propios.
- **Carga y ciclo de vida de las relaciones.** `LAZY`, `EAGER`, cascadas, `orphanRemoval` y errores habituales.
- **Práctica guiada:** relacionar entidades y verificar el resultado en MySQL.
- **Práctica de bloque:** gestionar entidades relacionadas con restricciones de negocio.

### Bloque 5. Consultas y recuperación

- **Consultas HQL y JPQL.** Parámetros, filtros, ordenación y consultas tipadas.
- **Consultas sobre relaciones.** Joins, proyecciones, agregaciones sencillas y paginación.
- **Consultas SQL nativas con Hibernate.** Cuándo acceder a tablas directamente y cómo recuperar resultados.
- **Práctica guiada:** consultas parametrizadas desde un repositorio y su servicio.
- **Práctica de bloque:** búsquedas, informes y validaciones basadas en consultas.

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
- **Práctica guiada:** crear y ejecutar una aplicación Spring sencilla con componentes.

### Bloque 2. Componentes y configuración Spring

- **Contenedor IoC y ciclo de vida de beans.** Registro, creación, inicialización y gestión.
- **Inyección de dependencias.** `@Component`, `@Service`, `@Repository`, `@Bean` e inyección por constructor.
- **Propiedades, perfiles y eventos.** `application.properties`, configuración externa y comunicación entre componentes.
- **Práctica de bloque:** sustituir implementaciones de un servicio mediante inyección de dependencias.

### Bloque 3. Componentes de acceso a datos

- **Arquitectura por capas en Spring.** Controlador, servicio, repositorio, entidad y responsabilidades.
- **Spring Data JPA.** `JpaRepository`, CRUD y consultas derivadas.
- **Servicios, validación y transacciones.** Reglas de negocio, coordinación y `@Transactional`.
- **Componentes de persistencia reutilizables.** Interfaces e implementaciones sobre ficheros y ORM.
- **Práctica guiada:** conectar un servicio con un repositorio Spring Data JPA.
- **Práctica de bloque:** implementar reglas de negocio sobre una aplicación preparada.

### Bloque 4. Aplicaciones web con Thymeleaf

- **Spring MVC y Thymeleaf.** Modelo, vista y controlador; lectura y adaptación de plantillas existentes.
- **Formularios y visualización de datos.** Listados, formularios, mensajes y validación de entrada.
- **Práctica guiada:** integrar un servicio con una interfaz Thymeleaf suministrada.
- **Práctica de bloque:** completar la persistencia y validaciones de una aplicación cuyo frontal y controladores están preparados.

### Bloque 5. Composición y reutilización

- **Integración de varios servicios.** Coordinación de operaciones y colaboración sin dependencias circulares.
- **Diseño de componentes reutilizables.** Interfaces, sustitución de implementaciones, bajo acoplamiento y empaquetado conceptual.
- **Práctica de bloque:** aplicar varias reglas de negocio coordinadas en un sistema MVC preparado.

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
- **Práctica guiada:** conexiones iniciales a SQLite y MySQL preparado con Docker.

### Bloque 2. Conexiones y operaciones SQL

- **Conexiones JDBC.** `DriverManager`, `Connection`, URL, credenciales y ciclo de vida.
- **Ejecución de instrucciones SQL.** `Statement`, scripts, DDL, inserciones, actualizaciones y eliminaciones.
- **Recuperación de resultados con `ResultSet`.** Filas, columnas, tipos y conversión a objetos Java.
- **Consultas seguras con `PreparedStatement`.** Parámetros, reutilización y prevención de inyección SQL.
- **Práctica guiada:** consultas y CRUD de una tabla con parámetros.
- **Práctica de bloque:** importación de registros y búsquedas seguras.

### Bloque 3. Repositorios y servicios JDBC

- **Repositorios JDBC manuales.** Contratos, implementaciones y encapsulación de SQL.
- **Servicios y reglas de negocio.** Verificaciones, coordinación, validación y errores.
- **Integración con Spring MVC.** Usar repositorios JDBC creados desde cero mediante servicios Spring e interfaz web preparada.
- **Práctica de bloque:** completar un sistema con vistas preparadas, repositorio JDBC y lógica comercial en el servicio.

### Bloque 4. Metadatos y consultas complejas

- **Metadatos de JDBC.** `DatabaseMetaData` y `ResultSetMetaData`.
- **Consultas de varias tablas.** Joins, claves generadas y transformación de relaciones a objetos.
- **Procedimientos almacenados.** `CallableStatement`, parámetros de entrada/salida y resultados en MySQL.
- **Práctica de bloque:** consultas relacionadas e informes con reglas sobre los datos.

### Bloque 5. Transacciones y fiabilidad

- **Transacciones JDBC.** ACID, `autoCommit`, `commit`, `rollback` y coordinación entre operaciones.
- **Gestión de recursos y errores.** `try-with-resources`, `SQLException` y logging profesional.
- **Pruebas de persistencia y consistencia.** CRUD, errores, fallos parciales e invariantes.
- **Práctica guiada:** ejecutar una operación compuesta y provocar un rollback.
- **Práctica de bloque:** gestión de inventario con varias operaciones atómicas.

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
- **Práctica de bloque:** identificar colecciones y documentos para un caso de negocio.

### Bloque 2. Configuración y acceso seguro

- **Creación y configuración de Cloud Firestore.** Proyecto, base, región y consola.
- **SDK de administración de Firebase con Spring Boot.** Dependencias, inicialización e integración.
- **Credenciales y seguridad de acceso.** Cuentas de servicio, IAM, variables de entorno y protección de secretos.
- **Práctica guiada:** conectar una aplicación de servicio a Firestore con configuración externa.

### Bloque 3. Modelado y persistencia

- **Colecciones, documentos y tipos.** Identificadores, campos, mapas, listas y conversiones.
- **Modelado de relaciones documentales.** Mapas, subcolecciones, referencias, duplicación y elección de estructura.
- **Repositorios y servicios Firestore.** CRUD, reglas de negocio y gestión de fallos.
- **Práctica guiada:** CRUD de documentos desde un servicio Spring.
- **Práctica de bloque:** modelar documentos relacionados y validar operaciones comerciales.

### Bloque 4. HTTP y REST

- **Fundamentos de HTTP.** Peticiones, respuestas, cabeceras, JSON y códigos de estado.
- **Diseño de API REST.** Recursos, rutas, GET/POST/PUT/PATCH/DELETE, idempotencia y convenciones.
- **Controladores REST con Spring Boot.** `@RestController`, mappings, parámetros, cuerpos y `ResponseEntity`.
- **Práctica guiada:** publicar el CRUD de Firestore como API HTTP.
- **Práctica de bloque:** diseñar endpoints para un segundo recurso con reglas de negocio.

### Bloque 5. Buenas prácticas de API

- **DTO y validación de entrada.** Contratos independientes del modelo persistente, Bean Validation y validación de servicio.
- **Respuestas y tratamiento de errores.** Estados HTTP, `@RestControllerAdvice`, excepciones y formato coherente.
- **OpenAPI y Swagger UI.** Esquemas, operaciones, respuestas y documentación interactiva.
- **Pruebas manuales de API con Postman.** Colecciones, variables, escenarios normales y errores.
- **Práctica guiada:** documentar y probar endpoints con OpenAPI y Postman.
- **Práctica de bloque:** incorporar validaciones, errores consistentes y documentación a una API.

### Bloque 6. Consultas y consistencia

- **Consultas y filtros Firestore.** Condiciones de igualdad, rangos y filtros combinados.
- **Índices y paginación con cursores.** Ordenación, índices, límites, cursores y costes de lectura.
- **Escrituras agrupadas y transacciones.** Operaciones coordinadas, consistencia y recuperación ante fallos.
- **Práctica de bloque:** búsqueda paginada y operaciones coordinadas con reglas comerciales.

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
- **Práctica de bloque:** diseñar documentos para una necesidad empresarial.

### Bloque 2. Instalación y modelado

- **MongoDB standalone con Docker y Compass.** Contenedor, volúmenes, conexión y exploración visual.
- **Diseño de documentos MongoDB.** Embebidos, referencias, esquemas flexibles y validación.
- **Práctica guiada:** crear y consultar documentos desde Compass.
- **Práctica de bloque:** elegir entre embebidos y referencias según consultas y reglas reales.

### Bloque 3. Persistencia con Spring Data MongoDB

- **MongoDB con Spring Boot.** Dependencias, conexión y configuración.
- **Documentos Java y `MongoRepository`.** `@Document`, `@Id`, mapeo y CRUD.
- **Servicios y reglas de negocio.** Validación, coordinación y errores de persistencia.
- **Persistencia de estructuras relacionadas.** Listas, documentos embebidos, referencias y recuperación.
- **Práctica guiada:** persistir entidades documentales mediante un repositorio.
- **Práctica de bloque:** completar reglas de negocio sobre documentos relacionados.

### Bloque 4. Consultas y agregaciones

- **Consultas personalizadas.** Consultas derivadas, `@Query`, filtros y operadores.
- **Ordenación, paginación e índices.** Resultados paginados y búsquedas eficientes.
- **Aggregation Pipeline.** `$match`, `$group`, `$project`, `$sort`, `$lookup` y cálculos.
- **Práctica guiada:** filtros y agregaciones sobre una colección.
- **Práctica de bloque:** construir informes y estadísticas a partir de agregaciones.

### Bloque 5. Integridad y pruebas

- **Atomicidad y operaciones seguras sobre documentos.** Actualizaciones atómicas de un documento y límites de consistencia.
- **Pruebas de persistencia MongoDB.** CRUD, consultas, validaciones, fallos y resultados.
- **Práctica de bloque:** verificar invariantes y corregir errores en operaciones documentales.

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
