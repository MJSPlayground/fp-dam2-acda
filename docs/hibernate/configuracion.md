# MySQL, Maven y configuración de Hibernate

El proyecto necesita una aplicación Java y un servidor MySQL. La interfaz JavaFX no inicia una base embebida: al guardar se comunica con el servidor local mediante Hibernate y el driver preparado.

## Arranque del entorno

Abre el proyecto Maven en IntelliJ y espera la sincronización, como indica [preparación y Run](../ayuda/proyectos-maven.md). Con Docker Desktop disponible, ejecuta `config.MySqlLocal.main` para preparar el servicio del `compose.yaml`; después ejecuta `app.Launcher.main`.

El servidor MySQL se publica en `127.0.0.1:3308`; dentro del contenedor utiliza 3306. El proyecto guiado utiliza el esquema `biblioteca_guiado` y la práctica final `biblioteca_final`. Comparten servidor, pero mantienen sus datos separados.

Las nociones de imagen, contenedor y volumen están en [contenedores y herramientas](../ayuda/contenedores.md). Detener el backend o el servidor no equivale a borrar sus datos. No elimines el volumen para resolver un error de contraseña o de puerto.

## Datos preparados sin sobrescribir el trabajo

Para el desarrollo local, el proyecto utiliza el usuario `acda` y la contraseña `acda_local`. No reutilices esas credenciales en un servidor publicado.

La aplicación permite cargar datos de demostración de forma explícita y solo cuando el catálogo está vacío. No vuelve a sembrar libros ni editoriales al reiniciar. Una tabla sin datos y una operación pendiente del starter son situaciones distintas: lee el mensaje antes de concluir que falta la conexión.

La interfaz se abre con los controles preparados y el proveedor se obtiene cuando corresponde. Durante el desarrollo puede comunicar etapas pendientes; eso no representa un guardado correcto ni exige modificar las vistas.

## Observar con MySQL Workbench

Crea una conexión en Workbench con host `127.0.0.1`, puerto 3308 y las credenciales locales del proyecto. Utiliza **Test Connection** y selecciona el esquema correcto antes de inspeccionar tablas. La [guía de contenedores](../ayuda/contenedores.md#conectar-mysql-workbench) explica el recorrido común.

Después de una operación desde Java, comprueba `libro`, `editorial`, `autor`, `detalle` y la tabla de unión `libro_autor`. La anotación `@Table` del modelo determina el nombre SQL; no tiene por qué ser el plural de la clase.

Observa la clave `editorial_id`, la clave única `detalle_id` y las parejas de `libro_autor`. No cambies filas manualmente para hacer coincidir una captura con un resultado esperado: la prueba consiste en contrastar lo que guardó la aplicación.

## Qué aporta Maven

El POM reúne Hibernate ORM, la API Jakarta Persistence correspondiente, el driver MySQL, JavaFX y logging. El wrapper permite reproducir la compilación y las pruebas sin Maven global; la vía principal del alumnado sigue siendo Run/Debug en IntelliJ.

Las versiones compatibles se fijan y prueban en el proyecto. Un error `ClassNotFoundException` o `NoSuchMethodError` puede indicar una dependencia ausente o mezclada, no un problema de clave foránea. No se resuelve copiando JAR arbitrarios a la carpeta del código.

## Cómo se construye el proveedor

`HibernateFactory` registra las entidades, configura la conexión y construye la `SessionFactory`. Este fragmento ilustra el tipo de configuración que ya está preparado:

```java
Configuration configuracion = new Configuration()
    .addAnnotatedClass(Editorial.class)
    .addAnnotatedClass(Autor.class)
    .addAnnotatedClass(Libro.class)
    .addAnnotatedClass(Detalle.class);
```

Registrar una clase permite incorporar sus metadatos. Tenerla en un paquete llamado `entity` no garantiza por sí solo que esta configuración la conozca. La [introducción de arranque de Hibernate](https://docs.hibernate.org/orm/current/introduction/html_single/#configuration-and-bootstrap) describe las alternativas de configuración.

Las propiedades de conexión indican URL, usuario, contraseña y driver. El proyecto permite configuración externa mediante `MYSQL_URL`, `MYSQL_USER` y `MYSQL_PASSWORD`; no es necesario incrustar otras credenciales en las entidades.

## Generación del esquema

La configuración local utiliza actualización del esquema para acompañar el desarrollo del mapeo. Esto facilita la práctica, pero no sustituye un procedimiento de migraciones, copias y revisión para una base con datos importantes.

`validate` comprueba correspondencia sin crear las tablas que faltan. `create-drop` se utiliza exclusivamente en pruebas aisladas que admiten perder su contenido: no se selecciona para corregir una incidencia del catálogo de trabajo.

Si cambia una anotación y la tabla sigue teniendo otra forma, revisa el mensaje del proveedor y el esquema efectivo. No presupongas que cualquier modificación de Java reconstruye datos existentes de forma compatible.

## Logging y diagnóstico

El proyecto incluye SLF4J/Log4j2. Los errores técnicos conservan sus causas, mientras la interfaz muestra un mensaje breve. El SQL generado se observa mediante el registro configurado para Hibernate, sin utilizar `show_sql` como sustituto del logging de la aplicación.

| Síntoma | Primera comprobación |
| --- | --- |
| No conecta con Docker | Motor disponible y servicio iniciado |
| MySQL rechaza la conexión | Puerto, usuario, contraseña y permisos de esquema |
| Arranque falla por una entidad | Anotaciones, registro y tipos de asociaciones |
| Columna o tabla no coincide | Esquema seleccionado y mapeo efectivo |
| Restricción de unicidad | Dato duplicado y transacción revertida |
| Error al abrir una asociación | Datos necesarios y límite de la sesión |

Utiliza [diagnóstico y pruebas](../ayuda/diagnostico.md) para leer el stack trace antes de cambiar la configuración. Continúa con [entidades y anotaciones](entidades.md).
