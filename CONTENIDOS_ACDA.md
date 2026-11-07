# Acceso a Datos

> **Estado:** índice aprobado para la web pública. Fuente de estructura y alcance del módulo en este repositorio.
>
> **Ámbito:** DAM2 · Acceso a Datos (ACDA). Este documento fija el alcance de la web pública, el enfoque didáctico y los proyectos de aprendizaje. No es un enunciado evaluable ni sustituye a la programación didáctica o al seguimiento de resultados de aprendizaje.

## 1. Objetivo y planificación

El módulo enseña a **guardar, recuperar, consultar, modificar y transformar datos** mediante ficheros, bases relacionales, ORM, componentes de acceso a datos y bases documentales. Se trabajará sobre todo con Java y con aplicaciones cuya presentación ya esté preparada.

| Tema (denominación docente) | RA de referencia | Horas ordinarias aproximadas |
| --- | :---: | ---: |
| UT1 — Manejo de ficheros y directorios | RA1 | 7 |
| UT2 — Herramientas ORM y JPA con Hibernate | RA3 | 9 |
| UT3 — Acceso a datos mediante componentes Spring | RA6 | 6 |
| UT5 — Conexiones a bases de datos relacionales (JDBC) | RA2 | 11 |
| UT6 — Bases de datos documentales con Cloud Firestore | RA5 | 7 |
| UT7 — Bases de datos NoSQL con MongoDB | RA4 | 7 |
| **Total de enseñanza ordinaria** | | **47** |

Las 25 horas restantes de las 72 planificadas corresponden a los dos períodos de proyectos intermodulares (UT4 y UT8). **No se presupone que esas horas estén disponibles para introducir contenidos nuevos**. Las 47 horas incluyen trabajo en clase, acompañamiento, realización de actividades y evaluación; no son 47 horas libres para explicar teoría. El ritmo podrá ser exigente, pero los fundamentos deben entenderse antes de ampliar la complejidad.

El alcance tecnológico actualizado es una **decisión didáctica**. La trazabilidad de RA y criterios de evaluación se mantendrá separadamente en la documentación docente, contrastando la normativa y la programación aplicables sin transformar automáticamente los títulos de las unidades en tecnologías equivalentes a los RA. No afirmar una cobertura curricular que no se haya comprobado.

## 2. Organización de los contenidos y de la web

- La **web y sus PDF públicos se organizan exclusivamente por títulos de temas**, bloques, páginas y recursos. No mostrar códigos o números de UT, referencias a proyectos intermodulares ni organización por evaluaciones. No incluir `ut1`, `ut2`, etc. en slugs, rutas, enlaces o paquetes Java.
- Se permite usar **«Bloque 1», «Bloque 2»**, etc. como orientación local dentro de un tema, sin convertir la numeración en parte de la URL.
- El contenido original se redacta principalmente en **Markdown**, integrado en el Zensical existente. Conservar su diseño, configuración, navegación, accesibilidad, modo claro/oscuro y exportación PDF. Reutilizar estilos y componentes existentes antes de crear otros.
- La estructura de páginas se ajustará a la dificultad de cada concepto: **no imponer una página por punto del índice** ni una estructura idéntica en todos los temas.
- Cada explicación debe poder ser seguida por quien **no conoce todavía esa tecnología**. Introducir problema, concepto, API y resultado de manera progresiva; explicar el código **antes y después** de mostrarlo, sin relleno ni saltos de conocimientos.
- Incluir ejemplos pequeños ejecutables o fragmentos claros dentro del proyecto principal, documentación oficial contextualizada y **enlaces internos a conceptos ya tratados**; no repetir capítulos enteros en temas posteriores.
- El alumnado avanzará de forma autónoma siguiendo la web. El docente indicará hitos o páginas a alcanzar en cada sesión y resolverá dudas; las páginas deben indicar qué se va a aprender, qué debe completarse, cómo ejecutarlo y cómo comprobar que funciona.
- Mantener la distinción entre **contenido ordinario** y **Ampliación**. El nombre de la sección adicional será siempre **«Ampliación»**, sin calificativos. Las ampliaciones no se presuponen impartidas en las horas ordinarias.
- Las páginas públicas no indicarán qué entra en el examen ni etiquetarán una actividad como «nivel de examen». La equivalencia de dificultad se reserva a la planificación docente.
- No crear una página principal de «práctica guiada de cierre» por cada bloque. **Un único proyecto guiado incremental por tema** concentra la práctica de sus bloques.

## 3. Metodología de aprendizaje y actividades

### 3.1. Un proyecto incremental por tema

Cada tema tendrá **un solo proyecto conductor** que evoluciona de manera visible a lo largo de la teoría. Puede ofrecerse una base generosa, ya importable y ejecutable, con la interfaz gráfica y la infraestructura ajenas al objetivo de la actividad resueltas. Los pasos del proyecto se insertarán en los puntos adecuados de las páginas o en etapas enlazadas de una misma guía; **no se generan prácticas autónomas adicionales por bloque**.

En cada etapa:

1. Explicar el concepto y un ejemplo pequeño, con resultado interpretable.
2. Mostrar dónde encaja en el proyecto y qué partes del código ya existen.
3. Indicar con claridad los `TODO` del alumnado, preferentemente en repositorios, servicios, modelos y, cuando proceda, controladores REST; **nunca exigir cambios al frontend**.
4. Ofrecer una comprobación observable: prueba, registro, resultado en pantalla, consulta, fichero o petición Postman.
5. Añadir progresivamente casos límite y reglas de negocio que exijan razonar y usar conocimientos de programación de primero: objetos, colecciones, bucles, condicionales, algoritmos, cálculos, manejo de excepciones, etc.
6. Proporcionar pistas graduadas si se justifican, inicialmente ocultas, y acceso a la **solución pública completa** del proyecto de aprendizaje.

Las etapas se diseñarán para que el proyecto inicial pueda ejecutarse desde IntelliJ; los `TODO` aún no resueltos pueden mostrar resultados ficticios o estados controlados en la interfaz, **nunca dar apariencia engañosa de persistencia real**. Evitar copiar clases completas sin contexto o exigir configuraciones repetidas que consuman la sesión.

### 3.2. Práctica final única por tema

Al acabar la teoría se ofrece **una sola práctica final autónoma**, vinculada al mismo dominio funcional del proyecto incremental, pero con funcionalidades **nuevas**. El alumnado recibirá como código inicial **el proyecto guiado completo y resuelto** (interfaz incluida), para no depender de que sus pasos anteriores hayan quedado perfectos.

El enunciado se plantea desde las necesidades de un cliente o usuario, expresa comportamientos, restricciones y criterios observables, y **no dicta la solución técnica**. Sus tres niveles pertenecen al mismo proyecto:

- **Nivel 1 — Base:** trabajo común que debe realizar todo el alumnado; exige comprender y aplicar lo esencial.
- **Nivel 2 — Aplicación:** nuevos requisitos de dificultad semejante a la exigencia prevista para una prueba práctica, con razonamiento, validaciones y combinación de conocimientos. En la web usar solo «Aplicación».
- **Nivel 3 — Reto avanzado:** más difícil que la evaluación ordinaria; integra casos límite, operaciones coordinadas o lógica adicional sin introducir como obligatoria una API que solo figure en Ampliación.

El material de la práctica final incluirá: enunciado; proyecto inicial específico derivado de la solución del guiado; conjunto de datos y preparación; forma de ejecutarlo; criterios de aceptación comprobables; y **solución pública independiente**. Los `TODO` de la práctica final serán menos prescriptivos que los de la guiada para que el alumno decida cómo programar. Las tres dificultades **no constituyen tres prácticas finales independientes**.

La práctica final es de aprendizaje y **no entregable** por defecto.

### 3.3. Tarea evaluable anual (Moodle, fuera de la web pública)

Cada tema tendrá **una tarea/proyecto evaluable** que podrá variar de un curso a otro. Su enunciado, código inicial específico, solución, rúbrica y materiales de corrección se gestionarán en **Moodle y en el repositorio docente privado**, nunca en la web o repositorio público. Se diseñará posteriormente a partir de los contenidos del tema. La implementación inicial de la web **no debe generar estas tareas ni sus soluciones**, ni acceder a Moodle.

### 3.4. Repaso opcional y Ampliación

- Cada tema tendrá una sección **«Prácticas de repaso»** con previsión de **cinco proyectos independientes**, completos y variados, cada uno con enunciado, proyecto inicial funcional con frontend preparado y solución pública.
- **En esta fase** se crearán las páginas de entrada y cinco fichas identificadas inequívocamente como **«En preparación»**, sin inventar proyectos, enunciados ni enlaces de descarga. Los proyectos se producirán progresivamente más adelante. No presentar contenido pendiente como completo.
- Cada tema tendrá otra sección distinta llamada simplemente **«Ampliación»**, con los temas adicionales especificados en este documento. Pueden incluir explicaciones, ejemplos y proyectos cuando merezca la pena, pero **no pertenecen al itinerario ordinario**.

## 4. Criterios técnicos y transversales obligatorios

### 4.1. Arquitectura y responsabilidades

Usar nomenclatura **Repository** desde los primeros temas, explicando brevemente que *DAO* es otro patrón/nombre válido y sin afirmar que esté obsoleto. Mantener una arquitectura coherente y proporcionada a la aplicación, por ejemplo:

```text
es.mjsplayground.acda.<tema>.<proyecto>
├── model
│   ├── entity        # Entidades u objetos de dominio
│   ├── repository    # Contratos y acceso a los datos
│   └── service       # Casos de uso, validaciones y reglas de negocio
├── config            # Conexiones y configuración, cuando corresponda
├── ui                # Interfaz ya preparada (JavaFX/web)
└── app               # Punto de entrada, cuando corresponda
```

En Spring, los controladores, la configuración y los repositorios se adaptarán a las convenciones reales del framework, conservando responsabilidades claras. **No obligar a crear todos los paquetes si no existen clases que los justifiquen.** Los nombres `es.mjsplayground.acda.ficheros`, `.hibernate`, `.spring`, `.jdbc`, `.firestore` y `.mongodb` son estables y no contienen numeración de UT.

- **Repositorio:** lectura, escritura, consultas, conversión técnica a objetos y gestión de fallos de persistencia; no decide reglas comerciales.
- **Servicio:** valida datos y reglas, orquesta repositorios y otros servicios cuando proceda, define casos de uso y transacciones. Evitar dependencias circulares.
- **UI/controlador:** muestra resultados y errores, recoge entradas y llama a servicios; no incorpora SQL, operaciones de fichero ni reglas comerciales. Puede hacer validación inmediata de formato por experiencia de uso, **pero el servicio vuelve a validar lo importante**.
- **Entidad/DTO:** representar datos del dominio o contratos de intercambio sin introducirles persistencia ni decisiones de interfaz.

### 4.2. Gestión profesional de errores, logging y validaciones

Es un **objetivo transversal desde UT1**, no un temario nuevo que se imparta íntegramente en cada tema:

- Validar en el servicio requisitos como campos vacíos, rangos, duplicados, disponibilidad, existencias, permisos de operación y coherencia de estados; introducir cada regla cuando el alumnado tenga herramientas para comprenderla.
- Crear **excepciones de dominio propias cuando aporten semántica**, por ejemplo `EntidadNoEncontradaException`, `StockInsuficienteException` o `OperacionNoPermitidaException`, sin inventar una clase distinta por cada posible error.
- Distinguir errores **esperables de negocio** de errores **técnicos** (`IOException`, `SQLException`, problemas de conexión o serialización). Encapsular fallos técnicos en el límite adecuado y **conservar la causa original**. No usar excepciones para el flujo normal ni tragarlas con `catch` vacío.
- El servicio comunica fallos de negocio; la capa de entrada/UI o el manejador centralizado transforma esos fallos en **mensajes comprensibles y acciones posibles**. No exponer contraseñas, consultas internas, credenciales ni stack traces al usuario.
- Donde haya HTTP/REST, devolver códigos correctos y consistentes: por ejemplo **400** para petición mal formada o validación de entrada, **404** cuando el recurso no existe, **409** ante conflictos de estado y **500** para fallos internos inesperados; **201** en creaciones y **204** cuando una operación exitosa no devuelve cuerpo. Definir y reutilizar un formato de error coherente; no responder siempre 200 ante fallos. No convertir automáticamente cualquier fallo técnico en 404.
- Registrar hechos técnicos mediante **SLF4J y Log4j2 desde UT1**, distinguiendo `DEBUG`, `INFO`, `WARN` y `ERROR` y usando mensajes parametrizados. Evitar `System.out.println` como método habitual de diagnóstico, registros duplicados y exposición de secretos. En Spring, gestionar correctamente el backend de logging para no tener implementaciones SLF4J en conflicto.
- Introducir depuración, lectura de stack traces, pruebas manuales y, cuando proceda, tests automatizados. Verificar tanto casos correctos como fallos y reglas de negocio.
- Aplicar buenas prácticas **sin sobreingeniería**: primero entender una excepción, una validación y un caso de uso real; después introducir jerarquías y manejadores compartidos cuando estén justificados.

### 4.3. Proyectos y entornos

- **Java 25** como objetivo inicial, **Maven y Maven Wrapper** en proyectos Java, con verificación de compatibilidad real de dependencias antes de distribuirlos. Utilizar IntelliJ IDEA como flujo principal de importación, ejecución y depuración.
- Datos de solo lectura de ejemplo preferentemente en `src/main/resources/data/` y cargados como recursos; archivos que el programa vaya a crear/modificar en una **ubicación externa y configurable**, nunca en recursos empaquetados.
- Interfaces gráficas **completamente construidas, atractivas, accesibles y funcionales** desde la base del ejercicio: **JavaFX** para ficheros, Hibernate y JDBC; **web preparada** para Spring, Firestore y MongoDB, salvo una justificación técnica mejor. El alumnado **no tendrá que crear o editar vistas, JavaScript, FXML, CSS ni componentes frontend**.
- En REST, los controladores/backend **sí pueden ser parte del aprendizaje** porque enrutan peticiones y definen respuestas. El frontend web permanece terminado, con contratos y estados de error previstos; las páginas deben indicar qué backend se completa. No llamar «frontend» a los endpoints REST que deben programar.
- Para **Firestore** entregar además una colección **Postman importable con todas las peticiones, variables, cuerpos y ejemplos de error ya preparados**, para ejecutar sin construir manualmente las peticiones. Usar la misma idea donde aporte valor a otras API.
- Proporcionar datos de ejemplo, modo de reinicio/restablecimiento, README, pruebas de funcionamiento y solución íntegra. Mantener coherencia entre frontend, controladores, servicios, repositorios, datos y tests.
- Las credenciales y claves privadas nunca se incluyen en Git, archivos ZIP públicos, ejemplos en claro ni logs. Preparar `.env.example` o configuración equivalente sin secretos.
- Aplicar el estándar `MJSPlayground/ai-workspace/TEACHING-STANDARD.md` a los criterios generales **excepto donde este índice establezca explícitamente la nueva metodología específica de ACDA**. Actualizar posteriormente los pasajes específicos obsoletos del estándar y las skills, sin cambiar en silencio las pautas de otros módulos.

## 5. Temario y proyectos conductores

---

## Manejo de ficheros y directorios

**Dedicación orientativa:** 7 horas.  
**Propósito:** gestionar almacenamiento local mediante rutas, texto, CSV, JSON y persistencia binaria, separando responsabilidades y manejando errores.

### Bloque 1. Ficheros, directorios y entorno Java

- **Sistemas de ficheros y rutas.** Archivos y directorios, rutas absolutas/relativas, directorio de trabajo y permisos.
- **`Path` y `Files`.** Comprobar propiedades y crear, copiar, mover y borrar archivos/directorios con gestión adecuada de excepciones.
- **Recorrido y búsqueda básica integrados.** Listar archivos de un directorio y aplicar un filtro simple dentro de los ejemplos del bloque y del proyecto; **no** enseñar recursión compleja, inventarios extensos o estadísticas de árboles como contenido obligatorio.
- **Maven desde el primer tema.** `pom.xml`, dependencias y sincronización en IntelliJ, aprovechando la posterior integración de Jackson y logging.
- **Primeros pasos con SLF4J y Log4j2.** Logger y niveles, sustitución de `sout`, registro de incidencias sin datos sensibles y guía de configuración reutilizable.

### Bloque 2. Texto, CSV y JSON

- **Procesamiento por líneas.** `BufferedReader`, `BufferedWriter`, codificación UTF-8, `try-with-resources` y errores. Se explicarán las operaciones mínimas de lectura/escritura necesarias **dentro de este apartado**, sin reintroducir como bloque separado la teoría general de `Files.readString`/`writeString`.
- **CSV.** Encabezados, delimitadores, comillas, campos vacíos, importación/exportación y validación. Evitar analizadores caseros incorrectos ante comillas o delimitadores; usar una biblioteca sencilla si el caso lo requiere y explicar su papel.
- **JSON.** Objetos, arrays, valores, anidamiento y casos de uso.
- **Jackson.** `ObjectMapper`, objetos Java ↔ JSON, lectura, escritura y manejo de errores. Las operaciones sobre árboles JSON se introducirán solo cuando sean necesarias para el proyecto.

### Bloque 3. Binarios, acceso aleatorio y serialización

- **Flujos binarios:** `InputStream`, `OutputStream`, `DataInputStream` y `DataOutputStream`; diferencias respecto a texto.
- **Acceso aleatorio:** `RandomAccessFile`, posiciones y registros de tamaño fijo; **explicación y demostración pequeña**, sin exigir un subsistema de registros aleatorios en la práctica central.
- **Serialización Java:** `Serializable`, `ObjectInputStream`, `ObjectOutputStream`, compatibilidad y precauciones al deserializar. **Este será el mecanismo binario que utilizará principalmente el alumnado en la práctica**, solo sobre datos generados y controlados por la propia aplicación. Aclarar que la serialización nativa de Java no es el formato de intercambio recomendado para datos externos o no confiables.

### Bloque 4. Arquitectura de acceso a ficheros y transformación

- **Capas iniciales:** `model.entity`, `model.repository`, `model.service`, configuración y entrada de la aplicación. Explicar sus responsabilidades mientras evoluciona el proyecto, sin adelantar JPA ni Spring.
- **Importadores/exportadores reutilizables.** Separación entre lectura/escritura técnica, transformación y validación de negocio; uso de clases y métodos comprensibles.
- **Validaciones y errores.** Datos incompletos o repetidos, formatos incorrectos, errores de lectura/escritura, excepciones propias útiles y logging. Mantener información válida y evitar pérdidas accidentales.

### Proyecto guiado incremental — Inventario local de productos

**Interfaz JavaFX preparada.** Aplicación que muestra productos y permite cargarlos, filtrarlos, editarlos y guardarlos en distintos formatos. El proyecto parte de un catálogo y vistas ya diseñadas.

Hitos didácticos integrados en la teoría: seleccionar una ubicación y listar archivos; leer y escribir CSV; importar/exportar JSON con Jackson; guardar y recuperar el catálogo en un fichero binario mediante serialización; separar repositorio y servicio; aplicar reglas sobre identificadores, precios y existencias; informar de fallos mediante excepciones y logs. El acceso aleatorio se muestra en una demostración breve, no se convierte en requisito del proyecto.

### Práctica final — Nuevas funciones del inventario

El **starter** es la solución completa del proyecto guiado; se añaden requisitos de cliente que obligan a crear código nuevo.

- **Base:** una ampliación funcional de importación/exportación y validación con interfaz ya preparada.
- **Aplicación:** conciliación de datos con duplicados, reglas de stock/precios, incidencias y resultados verificables.
- **Reto avanzado:** recuperación ante fallos, transformaciones combinadas y lógica adicional de inventario.

### Prácticas de repaso

Cinco proyectos opcionales **en preparación**; publicar inicialmente sus cinco fichas sin enlaces ficticios.

### Ampliación

- XML con DOM: lectura, escritura, transformación y defensa frente a XXE.
- Procesamiento streaming de archivos grandes.
- Monitorización de cambios con `WatchService`.
- Escrituras fiables con temporales, sustitución atómica y recuperación.

---

## Herramientas de mapeo objeto-relacional (ORM) y JPA con Hibernate

**Dedicación orientativa:** 9 horas.  
**Propósito:** mapear objetos Java en una base relacional, ejecutar CRUD y consultas, gestionar transacciones y relaciones, y organizar repositorios y servicios.

### Bloque 1. Arquitectura de persistencia y fundamentos ORM

- **Arquitectura desde el principio.** Estructura real de paquetes y responsabilidades de `entity`, `repository`, `service` y `config/connection`; flujo de llamadas. Reaprovechar el concepto iniciado en Ficheros y formalizarlo con un ejemplo de BD.
- **Desfase objeto-relacional y ORM.** Tablas, objetos, asociaciones y ventajas/limitaciones de la abstracción.
- **JPA, Jakarta Persistence e Hibernate.** Especificación frente a proveedor, API y alcance de la herramienta.

### Bloque 2. Configuración y entidades

- **MySQL con Docker y Workbench.** Conexión, esquema, puerto y comprobación del contenedor. Preparar configuración reutilizable para evitar perder tiempo en instalaciones.
- **Proyecto Hibernate con Maven.** Dependencias, conexión, configuración de sesiones/entidades y ejecución.
- **Anotaciones de mapeo:** `@Entity`, `@Table`, `@Id`, `@Column`, tipos, identificadores y restricciones. Explicar los constructores/accesores necesarios dentro del mapeo, **sin una página independiente de POJO/JavaBean**.
- **Mapeo XML:** mención y ejemplo corto como alternativa a las anotaciones, sin convertirse en un segundo itinerario práctico.

### Bloque 3. CRUD, sesiones, servicios y transacciones

- **Sesiones y estados de entidades.** Transitorio, persistente y separado; cuándo importa cada uno.
- **CRUD mediante repositorios Hibernate.** Alta, consulta, modificación y borrado; cierre de recursos.
- **Transacciones:** inicio, `commit`, `rollback`, límites y errores.
- **Servicios y reglas de negocio.** Algunas validaciones y casos de error representativos, sin distraer con lógica comercial desmesurada; reutilizar las convenciones de excepción y logging de UT1.

### Bloque 4. Relaciones y ciclo de vida

- **Relación 1:1:** `@OneToOne`, propiedad y persistencia.
- **Relaciones 1:N / N:1:** `@OneToMany` y `@ManyToOne`, claves foráneas y lado propietario.
- **Relación M:N:** `@ManyToMany`, tabla de unión y operaciones básicas.
- **Ciclo de vida:** `LAZY`/`EAGER`, cascadas, `orphanRemoval` y errores habituales, con profundidad proporcional al caso de uso. Mantener **las tres cardinalidades** en el itinerario ordinario.

### Bloque 5. Consultas HQL/JPQL

- **HQL/JPQL básico.** Consultas parametrizadas, filtros, condiciones y ordenación, recuperando entidades desde repositorios.

### Proyecto guiado incremental — Catálogo de biblioteca

**Interfaz JavaFX preparada.** Entidades: editoriales, libros, autores y detalles de publicación. El ejemplo permite visualizar relaciones **1:N** (editorial-libros), **M:N** (libros-autores) y **1:1** (libro-detalle), con mapeo gradual, CRUD, transacciones y consultas HQL sencillas. La interfaz, datos semilla y configuración de ejecución estarán preparados; los `TODO` se concentran en entidades, repositorios y servicios.

### Práctica final — Ampliación del catálogo

- **Base:** nuevas operaciones CRUD y reglas sencillas sobre entidades ya mapeadas.
- **Aplicación:** operaciones con varias entidades relacionadas y consultas HQL parametrizadas.
- **Reto avanzado:** restricciones de negocio y transacciones que coordinen varias modificaciones.

### Prácticas de repaso

Cinco proyectos opcionales **en preparación**.

### Ampliación

- Consultas HQL/JPQL sobre relaciones: joins, proyecciones, agregaciones y paginación.
- SQL nativo con Hibernate.
- Rendimiento de Hibernate: N+1, `JOIN FETCH`, consultas generadas y diagnóstico.

---

## Acceso a datos mediante componentes Spring

**Dedicación orientativa:** 6 horas.  
**Propósito:** desarrollar y reutilizar componentes de persistencia con Spring Boot, inyección de dependencias y Spring Data JPA, sin invertir horas en enseñar frontend.

### Bloque 1. Componentes y Spring Boot

- **Componentes software.** Interfaces, encapsulación, composición, reutilización, ventajas y límites.
- **Spring Framework y Spring Boot.** Ecosistema, starters, autoconfiguración y ejecución de una aplicación preparada.
- **IoC y ciclo de vida de beans.** Contenedor, creación y gestión; detalles suficientes para entender los componentes.
- **Inyección de dependencias.** `@Component`, `@Service`, `@Repository`, `@Bean` e inyección por constructor.
- **Propiedades, perfiles y eventos.** `application.properties`, configuración externa, perfiles y una comunicación por eventos a nivel introductorio, integrados con ejemplos y sin framework adicional.

### Bloque 2. Componentes de acceso a datos

- **Arquitectura Spring por capas:** controlador preparado, servicio, repositorio y entidad.
- **Spring Data JPA:** `JpaRepository`, CRUD y consultas derivadas.
- **Servicios, reglas y validaciones:** aprovechamiento de lo aprendido con Hibernate, excepciones de dominio y colaboración entre componentes.
- **Transacciones:** `@Transactional` y sus límites básicos.
- **Reutilización de persistencia:** contratos e implementaciones intercambiables **integrados en la explicación de repositorios y DI**, sin página propia ni práctica redundante para ficheros/ORM.

### Bloque 3. Composición de servicios

- **Colaboración entre varios servicios:** coordinación de operaciones y responsabilidades.
- **Diseño de componentes reutilizables:** interfaces, sustitución de implementaciones, bajo acoplamiento y prevención de dependencias circulares.

### Proyecto guiado incremental — Servicios para el catálogo

**Aplicación Spring Boot con interfaz web y controladores de UI ya preparados**, que reutiliza el dominio o parte de los datos del catálogo Hibernate. Transformar progresivamente el acceso a datos en repositorios Spring Data JPA, introducir DI y servicios, añadir consultas derivadas, operaciones coordinadas, reglas y transacciones. El alumnado **no modifica Thymeleaf, HTML, CSS ni JavaScript**. Se pueden usar componentes falsos iniciales para explicar sustitución por implementaciones reales.

### Práctica final — Reglas y servicios del catálogo

- **Base:** un nuevo caso de uso que precise repositorios y servicio.
- **Aplicación:** coordinación de varios componentes, consultas y validaciones de negocio.
- **Reto avanzado:** varias operaciones relacionadas con una transacción y gestión de conflictos.

### Prácticas de repaso

Cinco proyectos opcionales **en preparación**.

### Ampliación

- Spring MVC y Thymeleaf: estructura, funcionamiento y adaptación de plantillas.
- Formularios, listados, mensajes y validación de entrada en aplicaciones web.
- JUnit y Spring Boot Test.
- Diagnóstico avanzado con logging, excepciones y perfiles.
- Empaquetado y ejecución de Spring Boot como JAR.
- Spring Security para MVC: autenticación y autorización.
- Despliegue de Spring Boot con Docker.

---

## Conexiones a bases de datos relacionales con JDBC

**Dedicación orientativa:** 11 horas.  
**Propósito:** entender el acceso SQL directo desde Java, construir repositorios manuales y coordinar operaciones de forma segura mediante servicios y transacciones.

### Bloque 1. Conectores y conexiones

- **JDBC frente a ORM.** Drivers, arquitectura, ventajas, límites y control directo.
- **Bases de datos embebidas y de servidor.** SQLite y MySQL, usos y configuración; reutilizar conocimientos de MySQL/Docker.
- **Conexión JDBC.** `DriverManager`, `Connection`, URL, credenciales, apertura y cierre.

### Bloque 2. Operaciones SQL y recuperación de resultados

- **`Statement`:** ejecución de SQL de definición y modificación; usar ejemplos controlados y explicar sus limitaciones con datos externos.
- **`ResultSet`:** lectura de filas/columnas, tipos y conversión a entidades Java.
- **`PreparedStatement`:** parámetros, operaciones CRUD y prevención de inyección SQL.

### Bloque 3. Repositorios, servicios, errores y transacciones

- **Repositorios JDBC manuales:** encapsulación de consultas y mapeo de resultados.
- **Servicios y reglas de negocio:** validación, disponibilidad, existencias, cálculos y operaciones coordinadas; el backend Java es el trabajo del alumnado.
- **Transacciones JDBC:** ACID básico, `autoCommit`, `commit`, `rollback` y recuperación ante fallos.
- **Recursos y errores:** `try-with-resources`, `SQLException`, excepciones propias pertinentes y logging; no dejar conexiones abiertas ni ocultar errores.
- **Integración con interfaz:** controladores/presentación Spring MVC o JavaFX **se entregan ya implementados**; solo deben integrar correctamente el servicio y repositorios que construye el alumnado, sin enseñar un nuevo frontend.

### Proyecto guiado incremental — Gestión de almacén y ventas

**Interfaz JavaFX preparada**, con tablas, formularios y acciones ya diseñadas. Usar SQLite para ilustrar una BD embebida y MySQL para una conexión de servidor; el proyecto principal tendrá un almacenamiento definido y reproducible, evitando que cada paso requiera reimplementar dos veces el mismo CRUD. Completar conexiones, repositorios `Jdbc`, consultas parametrizadas, mapeo de resultados, reglas de stock y una operación de venta atómica con `commit`/`rollback`.

### Práctica final — Operaciones de almacén

- **Base:** nuevas búsquedas y modificaciones seguras, con validaciones.
- **Aplicación:** compra o venta con cálculos y condiciones de stock, reutilizando transacciones.
- **Reto avanzado:** operación coordinada que deba revertirse si falla alguna condición o una escritura.

### Prácticas de repaso

Cinco proyectos opcionales **en preparación**.

### Ampliación

- `DatabaseMetaData` y `ResultSetMetaData`.
- Consultas `JOIN` y transformación de datos relacionados a objetos.
- Procedimientos almacenados con `CallableStatement`.
- Pool de conexiones HikariCP.
- Operaciones JDBC por lotes.
- Índices, planes de ejecución y optimización SQL.
- Concurrencia y niveles de aislamiento.

---

## Bases de datos documentales con Cloud Firestore

**Dedicación orientativa:** 7 horas.  
**Propósito:** modelar documentos y colecciones, implementar persistencia en Firestore y exponerla mediante una API REST comprensible, segura y verificable.

### Bloque 1. Modelo documental y Firestore

- **Bases relacionales frente a documentales:** modelos, flexibilidad, ventajas, límites y casos de uso.
- **Firebase y Cloud Firestore:** ecosistema, arquitectura y características.
- **Colecciones y documentos:** identificadores, campos, tipos, mapas, listas y conversiones.
- **Relaciones documentales:** mapas, subcolecciones, referencias, duplicación y criterios de elección.

### Bloque 2. Configuración y acceso seguro

- **Proyecto y base Firestore:** consola, región y configuración inicial.
- **Firebase Admin SDK desde Spring Boot:** dependencias, inicialización y conexión.
- **Credenciales y permisos:** cuentas de servicio, IAM, variables de entorno y protección de secretos. Diferenciar los privilegios del SDK Admin de las reglas de seguridad de clientes; **nunca** poner claves privadas en recursos públicos ni en el frontend.

### Bloque 3. Persistencia, consultas y servicios

- **Repositorio/servicio Firestore:** CRUD, traducción entre documentos y objetos y gestión de errores.
- **Reglas de negocio y validación en servicio.**
- **Consultas:** igualdad, rangos, condiciones combinadas y ordenación.
- **Índices y paginación:** límites, cursores e interpretación de requisitos de índices; nivel introductorio, aprovechando los resultados reales del proyecto.

### Bloque 4. API REST y pruebas

- **HTTP:** peticiones, respuestas, cabeceras, JSON, métodos y códigos de estado.
- **Diseño REST:** recursos, rutas, GET/POST/PUT/PATCH/DELETE, idempotencia y operaciones.
- **Controladores Spring Boot:** `@RestController`, rutas, parámetros, cuerpos y `ResponseEntity`.
- **DTO y validación de entrada:** distinguir representación pública de modelo persistido.
- **Errores REST:** excepciones de negocio, manejador común (por ejemplo `@RestControllerAdvice`), formato de error y uso coherente de 400, 404, 409 y 500.
- **Postman:** importar una **colección completa ya preparada** y modificar únicamente variables, IDs o datos de petición; observar tanto respuestas correctas como errores.

### Proyecto guiado incremental — API de reservas de espacios

**Interfaz web terminada y API base preparada**, con rutas y operaciones iniciales de ejemplo para poder usar la aplicación desde el primer paso. Completar progresivamente repositorio Firestore, servicio, modelos, validaciones y nuevos endpoints REST. Debe comprenderse qué ruta, método, cuerpo y respuesta corresponde a cada operación, y el alumno implementará parte de esos controladores. Incluir desde el comienzo una **colección Postman importable**, peticiones CRUD, variables, ejemplos correctos e incorrectos y documentación breve para ejecutarlas; el frontend no se modifica.

### Práctica final — Ampliación del sistema de reservas

- **Base:** nuevos endpoints y persistencia documental con validaciones.
- **Aplicación:** disponibilidad, conflictos, consultas y códigos HTTP acordes al resultado.
- **Reto avanzado:** restricciones de negocio más complejas, consultas paginadas y coordinación sin asumir transacciones avanzadas no impartidas.

### Prácticas de repaso

Cinco proyectos opcionales **en preparación**.

### Ampliación

- Documentación OpenAPI y Swagger UI.
- Escrituras agrupadas y transacciones Firestore.
- Firebase Authentication.
- Firestore en tiempo real y listeners.
- Seguridad de API: tokens, autenticación, autorización y protección de endpoints.

---

## Bases de datos NoSQL con MongoDB

**Dedicación orientativa:** 7 horas.  
**Propósito:** persistir objetos simples y estructuras documentales en MongoDB mediante Spring Data, modelar relaciones y resolver consultas, errores y reglas de negocio.

### Bloque 1. Modelo de datos MongoDB

- **MongoDB y bases documentales:** arquitectura, casos de uso y comparación con Firestore y SQL, sin repetir toda la introducción de UT6.
- **BSON y documentos:** colecciones, `ObjectId`, tipos, listas y estructuras anidadas.

### Bloque 2. Configuración y diseño

- **MongoDB con Docker y Compass:** conexión, volúmenes, exploración y operaciones visuales.
- **Modelado documental:** embebidos, referencias, esquemas flexibles, validación y criterios de elección.

### Bloque 3. Spring Data MongoDB y persistencia

- **Spring Boot + MongoDB:** dependencias y configuración.
- **Entidades documentales:** `@Document`, `@Id`, mapeo, `MongoRepository` y CRUD.
- **Servicios y reglas de negocio:** operaciones, validaciones, excepciones y coordinación.
- **Objetos estructurados:** listas, subdocumentos, referencias y recuperación de información.

### Bloque 4. Consultas, integridad y comprobación

- **Consultas personalizadas:** derivadas, `@Query`, filtros y operadores básicos.
- **Ordenación, paginación e índices:** uso introductorio y comprobable.
- **Atomicidad:** actualizaciones seguras sobre un documento y límites de consistencia.
- **Pruebas de persistencia:** comprobación de CRUD, consultas, validaciones, errores y resultados, combinando UI y pruebas de servicio/repositorio que aporten valor.

### Proyecto guiado incremental — Gestión de equipos y jugadores

**Interfaz web preparada.** Persistir jugadores, equipos y estadísticas con documentos simples y embebidos; introducir referencias cuando el modelo lo justifique. Construir entidades, repositorios, servicios y consultas, implementar reglas sobre plantillas y estadísticas, probar operaciones y errores. Reaprovechar la arquitectura Spring aprendida y concentrarse en lo nuevo de MongoDB.

### Práctica final — Gestión de competición

- **Base:** nuevas consultas y actualizaciones documentales.
- **Aplicación:** cálculos sobre jugadores/equipos, restricciones y operaciones seguras.
- **Reto avanzado:** estadísticas y reglas combinadas que exijan programar, sin depender del Aggregation Pipeline de Ampliación.

### Prácticas de repaso

Cinco proyectos opcionales **en preparación**.

### Ampliación

- Aggregation Pipeline: `$match`, `$group`, `$project`, `$sort`, `$lookup` y cálculos.
- Optimización de consultas: `explain`, índices compuestos y análisis de rendimiento.

---

## 6. Ayuda y herramientas

Estas guías son **consultables desde todos los temas**, no constituyen horas lectivas añadidas. Integrar enlaces contextuales y reutilizarlas en lugar de duplicar explicaciones.

### Entorno Java y proyectos

- **Java 25:** instalación, JDK, selección y diagnóstico.
- **IntelliJ IDEA:** importar proyecto Maven, seleccionar JDK, descargar dependencias, autoimportar clases, optimizar imports, refactorizar, ejecutar y solucionar problemas habituales.
- **Run Configurations:** establecer un punto de entrada `main` reutilizable, ejecutar la configuración elegida mediante el botón Run, configurar directorio de trabajo y argumentos cuando proceda. Aclarar que el botón contextual de ejecución de otra clase **puede crear o elegir otra configuración**; no prometer que cualquier icono Run ejecute siempre el mismo `main`.
- **Maven y Maven Wrapper:** `pom.xml`, dependencias, sincronización, compilación, importación y uso básico desde IntelliJ. Comandos opcionales para desarrolladores/QA.
- **Depuración:** breakpoints, variables, paso a paso, excepciones y stack traces.
- **JavaFX en proyectos preparados:** importar, ejecutar, usar la interfaz y resolver incidencias, sin enseñar a diseñarla.
- **Thymeleaf en aplicaciones preparadas:** estructura y ejecución, sin requerir modificar vistas.

### Contenedores y bases de datos

- Docker y Docker Compose.
- MySQL y Workbench.
- SQLite.
- MongoDB Compass.
- Firebase Console.

### Calidad, API y seguridad

- SLF4J y Log4j2: configuración, niveles y diagnóstico.
- Pruebas con JUnit: ejecutar y comprender tests.
- Postman: importar colecciones, variables, ejecutar y comprobar respuestas.
- OpenAPI y Swagger UI.
- Variables de entorno, configuración, credenciales y `.gitignore`.
- Documentación oficial de Java, Maven, Hibernate, Spring, Firebase y MongoDB, enlazada desde las páginas pertinentes.
- Solución de problemas frecuentes: JDK, Maven, rutas, puertos, Docker, bases de datos, conectividad, recursos, interfaces y ejecución.

### Autonomía de aprendizaje

- Cómo seguir la web, practicar y comprobar cada hito.
- Cómo interpretar y completar `TODO` sin modificar la UI.
- Cómo leer un mensaje de error, depurar e investigar documentación antes de pedir ayuda.
- Cómo usar las soluciones públicas como contraste del aprendizaje, no como sustituto del trabajo.
- Enlaces cruzados entre contenidos anteriores, con navegación estable.

## 7. Publicación, calidad y entrega de los materiales

- **Repositorio público `MJSPlayground/fp-dam2-acda`:** teoría, guías, ejemplos, proyecto incremental inicial y resuelto, práctica final inicial y resuelta, Ampliación pública y fichas de repaso. Preservar el proyecto Zensical ya existente y adaptar sus rutas, documentación y scripts sin reconstruirlo desde cero.
- **Repositorio privado del profesorado:** evaluaciones Moodle, soluciones, rúbricas y recursos de corrección. No generarlo ni publicarlo como parte de esta fase de desarrollo público.
- **Fuentes existentes:** `CONTENIDOS_ACDA.md` aprobado como alcance principal, programación y hoja de presentación para trazabilidad curricular; antiguos PDF como fuentes históricas secundarias, nunca como estructura predominante.
- **Estándar común:** `MJSPlayground/ai-workspace/TEACHING-STANDARD.md`; habilidades `fp-contenidos`, `fp-practicas`, `mjs-interfaces`, `fp-ut-completa`, `fp-revision-didactica` y `mjs-docs-ux` según su disponibilidad y compatibilidad. Cuando alguna skill presuponga una práctica por bloque o tres finales independientes, **la metodología específica aprobada en este documento prevalece para ACDA**, y debe actualizarse esa referencia explícitamente antes de generar contenido masivo.
- **QA mínimo por cada tema:** trazabilidad entre teoría y etapas; ejecución limpia de los starters; compilación con Maven Wrapper; pruebas de CRUD, servicios, excepciones y casos límite; coherencia entre datos, código y frontend; importación y funcionamiento de Postman donde corresponda; enlaces de descarga existentes; revisión visual de la interfaz, Zensical y PDF; capturas reales o registro `CAPTURAS-PENDIENTES.md`; contraste de documentación técnica y dependencias.
- **Regla de disponibilidad:** no afirmar que una actividad, descarga, solución o ejemplo está terminado hasta haberlo creado y verificado. Para los cinco repasos pendientes, **«En preparación»**; no enlaces rotos ni simulaciones engañosas.
- **Publicación (decisión docente 2026-11-07):** conservar la configuración GitHub Pages existente y desarrollar directamente en `main`, con commits y pushes trazables por hitos completos, validación previa y comprobación del despliegue. Los temas sin terminar muestran «En construcción», sin presentar descargas inexistentes como válidas. El docente revisará los materiales publicados antes de difundirlos al alumnado mediante Moodle. No realizar operaciones destructivas, exponer secretos ni declarar los contenidos pedagógicamente aprobados sin revisión humana.

## 8. Inventario mínimo de entregables por tema

Se considera listo para revisión docente cuando existe:

1. Teoría de todos los bloques obligatorios, explicada de forma autónoma y con referencias cruzadas.
2. Un proyecto incremental con **starter funcional**, interfaz completa y etapas `TODO` alineadas con el temario.
3. Una **solución completa** y ejecutable del proyecto incremental, con explicación o vínculo desde la web.
4. Una **práctica final** con niveles Base, Aplicación y Reto avanzado, cuyo starter es la solución del guiado más la infraestructura de nuevas funciones, con interfaz ya preparada.
5. Solución íntegra y ejecutable de los tres niveles de la práctica final, claramente identificados.
6. Una sección **«Prácticas de repaso»** y cinco fichas «En preparación» sin proyectos simulados.
7. Sección **«Ampliación»** con las páginas previstas, cuando procedan, distinguidas del itinerario ordinario.
8. Descargas, instrucciones de importación/Run, datos de ejemplo y comprobaciones manuales o automatizadas.
9. Cuando el tema sea REST, colección Postman preparada y validada; en Firestore, obligatoria desde la primera entrega.
10. Navegación web y PDF, enlaces, responsive, modo oscuro, interfaz y accesibilidad revisados; incidencias y capturas pendientes registradas.

**No forman parte de esta entrega inicial:** las seis tareas evaluables anuales de Moodle, los treinta proyectos completos de repaso ni sus soluciones. Solo sus espacios públicos de repaso estarán creados.