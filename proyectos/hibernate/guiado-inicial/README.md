# Catálogo de biblioteca — guiado-inicial

## Ejecutar desde IntelliJ y MySQL

Abre esta carpeta Maven, espera la sincronización y selecciona JDK 25. Con Docker Desktop activo, ejecuta `config.MySqlLocal.main`: Working directory debe ser esta carpeta, junto a compose.yaml. La configuración inicia MySQL local, con volumen persistente y comprobación SELECT 1. Si ya tienes la instancia de este proyecto en 3308, reutilízala; no arranques otra variante de Compose sobre el mismo puerto.

Luego ejecuta `app.Launcher.main` del paquete `es.mjsplayground.acda.hibernate.biblioteca`. La interfaz JavaFX está terminada; no debes editarla. SessionFactory se crea de forma diferida al consultar, se comparte durante la aplicación y se cierra al salir. Cada caso de uso abre/cierra Session y Transaction. No se guarda una Session en el controlador.

La base predeterminada de esta variante es biblioteca_guiado. Las variantes guiada/final usan bases distintas en el mismo servidor para evitar interferencias al evolucionar el proyecto. Hibernate usa `update` en la aplicación: no borra ni vuelve a sembrar al reiniciar. Nunca cambies a create/create-drop sobre tus datos. El script docker/init.sql solo se ejecuta cuando el volumen está vacío.

Variables opcionales: MYSQL_URL (URL JDBC completa), MYSQL_USER y MYSQL_PASSWORD. Predeterminados locales: acda/acda_local, host127.0.0.1:3308. Compose usa root/qa_root_local únicamente como administración de este entorno docente. Son credenciales de desarrollo explícitas, no secretos reales. El puerto se publica solo en loopback; no reutilices esas credenciales en un servicio expuesto ni introduzcas claves reales en Git.

Preparar demostración es una acción explícita confirmada, admitida solo si no hay editoriales, autores ni libros. Nunca se ejecuta automáticamente. Si hay datos, los conserva y muestra un mensaje. La demostración crea dos editoriales y dos autores compartidos por dos libros; uno tiene detalle y otro no, para practicar ambos estados.

## Capas y entidades

Hibernate puro, sin Spring. JPA/Jakarta Persistence define las anotaciones y Hibernate las implementa. Editorial, Autor, Libro y Detalle son clases normales con constructor protegido sin argumentos para el proveedor y constructores públicos para los casos de uso; no son los records empleados en las respuestas de la UI.

Editorial-libros es 1:N/N:1: el propietario es Libro.editorial (FK editorial_id); Editorial.libros usa mappedBy. Libro-autores es M:N: Libro.autores posee la tabla libro_autor y Autor.libros es inverso. Un libro requiere de uno a cinco autores existentes. No hay CascadeType.REMOVE hacia autores compartidos ni borrado en cascada desde editorial.

Libro-detalle es 1:1 opcional, con detalle_id único en Libro. Solo esta propiedad tiene cascade ALL y orphanRemoval: persistir un libro nuevo puede persistir su detalle, quitarlo elimina el detalle y borrar el libro elimina su ficha. Los autores y la editorial permanecen. No se permite borrar autor/editorial con libros vinculados.

Las asociaciones se configuran LAZY; no deben consultarse desde una UI con Session ya cerrada. El servicio construye LibroDatos dentro de su transacción, convirtiendo nombres/IDs/detalle a valores inmutables. La prueba de integración demuestra LazyInitializationException con una colección no inicializada fuera de sesión, y la diferencia con el DTO preparado. LAZY es una estrategia de carga; el comportamiento exacto de todas las asociaciones depende del proveedor y del contexto.

La tabla de la UI muestra el catálogo. Nuevo/Editar abren formularios y Guardar cierra al completar; Cancelar no modifica datos. Títulos, nombres de catálogo e idioma conservan saltos de línea mediante TextArea. La selección múltiple usa Ctrl para autores y, en la final, libros. Eliminar solicita confirmación segura por defecto. Las entradas inválidas mantienen el diálogo abierto.

## Reglas y consultas

Nombre de editorial/autor: 1–80 caracteres, único en su catálogo. ISBN: exactamente 13 dígitos y único; en este ejercicio se usa como identificador, sin cálculo de dígito de control. Título: 1–120. Precio BigDecimal: 0,01–10000 con hasta dos decimales. Detalle: idioma de 2–30 y páginas enteras de 1–10000; o ambos vacíos para quitar/no tener detalle. Se recortan extremos, sin eliminar saltos internos.

LibroRepository recibe la Session del servicio: persist/find/remove y HQL tipado. Las consultas HQL utilizan nombres de entidades/atributos, no tablas SQL; el filtro de título usa un parámetro LIKE y editorial opcional. Los parámetros evitan concatenar los valores del usuario en el HQL. Los comodines % y _ de LIKE conservan su significado.

Los cambios sobre entidades gestionadas se detectan al flush/commit. No se llama a merge para cada modificación. @Version en Libro impide que una edición antigua sobrescriba otra versión; la UI también envía la versión que abrió. La restricción de versión del proveedor protege carreras reales entre dos sesiones.

Una excepción provoca rollback y se conserva su causa técnica. BibliotecaException distingue mensajes de negocio y fallos de persistencia, con SLF4J/Log4j2 para el diagnóstico. La UI no llama al repositorio directamente. El pool incorporado de Hibernate es suficiente para esta práctica local; no se presenta como configuración de producción.

## QA opcional

Desde esta carpeta: `.\mvnw.cmd clean test` sin MySQL para pruebas de reglas; con el servidor local activo: `.\mvnw.cmd -Dqa.mysql=true test`. Las pruebas crean un esquema NUEVO qa_hibernate_UUID mediante la cuenta administrativa y lo eliminan al terminar, comprobando el prefijo; nunca vacían biblioteca_guiado/biblioteca_final. Variables QA_MYSQL_ADMIN_URL/QA_MYSQL_ADMIN_USER/QA_MYSQL_ADMIN_PASSWORD permiten otra instancia. Los valores locales de QA por defecto son root/qa_root_local en3308.

Interfaz real, con sesión gráfica disponible: `.\mvnw.cmd -Dqa.mysql=true -Dqa.ui=true test`. Sin esas propiedades, las pruebas gráficas o de MySQL se omiten explícitamente: no equivale a haberlas ejecutado. Las demos de Ampliación están en el paquete demo de la solución guiada y tienen pruebas independientes; no forman otro proyecto obligatorio.

Versiones comprobadas en POM: Java25, Hibernate ORM7.4.15.Final, Jakarta Persistence3.2 transitivo, MySQL Connector/J9.5.0, JavaFX25.0.2, SLF4J/Log4j2. Compose fija MySQL8.4.13 por digest. Fuentes: https://hibernate.org/orm/releases/7.4/ ; https://docs.hibernate.org/orm/7.4/introduction/html_single/ ; https://dev.mysql.com/doc/connector-j/en/ ; https://jakarta.ee/specifications/persistence/3.2/ .

## Alcance de esta variante

Etapa1: reemplaza los @Transient de las asociaciones por mapeos JPA: Editorial.libros inverso1:N, Autor.libros inversoM:N y Libro.editorial/autores/detalle propietarios. Los campos, constructores, getters/setters y mapeos simples ya están preparados. @Transient permite arrancar la interfaz mientras trabajas, sin simular esas relaciones. Etapa2: completa LibroRepository listar/buscar/insertar/eliminar y las consultas de catálogos en CatalogoRepository. Usa la Session recibida, sin abrir transacciones propias. Etapa3: completa BibliotecaService.guardarLibro/datosDemo con validación y el coordinador transaccion preparado. Los demás apoyos de servicio están resueltos. Las operaciones pendientes muestran un mensaje claro; la demostración no se ejecuta como persistencia ficticia. Respeta el orden antes de empezar a registrar libros. Las pruebas por defecto del inicial comprueban apoyos preparados; las fases optativas comprueban cada TODO al completarlo, con los mismos resultados esperados que en la solución.

## Pruebas optativas de aprendizaje por fases

El proyecto entrega `FasesGuiadoTest` en `src/test/java`. Abre la clase en IntelliJ, crea una configuración JUnit y selecciona en **VM options** `-Dfase=mapeos`. Valores disponibles: uno-muchos, muchos-muchos, uno-uno, mapeos, catalogos, repositorio, consultas, servicio, rollback. El valor `completo` activa todas las fases de esta clase. Añade también `-Dqa.mysql=true` para las fases con MySQL (todas salvo las comprobaciones de mapeos). El apoyo crea y elimina solo un esquema UUID propio; requiere una cuenta de desarrollo con CREATE/DROP DATABASE, nunca credenciales de producción. La conexión administrativa por defecto es `jdbc:mysql://127.0.0.1:3308/mysql?sslMode=DISABLED&allowPublicKeyRetrieval=true&serverTimezone=UTC`, root/qa_root_local. Puedes sobreescribirla mediante `QA_MYSQL_ADMIN_URL`, `QA_MYSQL_ADMIN_USER` y `QA_MYSQL_ADMIN_PASSWORD` en Environment variables de Run.

Alternativa con Wrapper: `.\mvnw.cmd '-Dtest=FasesGuiadoTest' '-Dfase=completo'  '-Dqa.mysql=true' test`. En Unix utiliza `./mvnw`. Sin `fase`, estos casos se omiten; esa omisión no verifica los TODO. En el starter se espera que fallen las fases cuyos métodos siguen pendientes. Los mismos oráculos están en la solución para contrastar los resultados, sin incluir sus algoritmos en el starter.
