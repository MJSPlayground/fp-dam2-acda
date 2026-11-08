# Conectores y conexiones

Una aplicación de almacén necesita conservar sus productos aunque se cierre la ventana. Una base relacional organiza esa información en tablas; Java debe enviarle consultas y convertir las filas recibidas en objetos. **JDBC** es la API que utilizaremos para realizar ese intercambio.

Antes de empezar conviene recordar `SELECT`, `INSERT`, `UPDATE`, `DELETE`, claves primarias y condiciones `WHERE`. En [Ayuda sobre proyectos Maven](../ayuda/proyectos-maven.md) puedes repasar cómo abrir un proyecto, seleccionar el JDK y ejecutar su clase principal desde IntelliJ.

## La API y el driver cumplen funciones distintas

La API JDBC pertenece al JDK: sus interfaces están en [`java.sql`](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/package-summary.html). El **driver** es una biblioteca que implementa esas interfaces para comunicarse con un motor concreto. Tener Java instalado no significa disponer del driver de SQLite o de MySQL.

```text
Interfaz preparada → Servicio Java → Repositorio JDBC → Driver → Base de datos
                            ↑              ↑
                      reglas de negocio   instrucciones SQL
```

El servicio decide si una venta es válida; el repositorio ejecuta las consultas necesarias. El driver traduce las llamadas JDBC a operaciones que comprende el motor. El motor comprueba sus restricciones y devuelve resultados o errores.

Con JDBC escribimos SQL y el mapeo entre columnas y objetos. Un ORM automatiza parte de ese trabajo mediante metadatos y entidades. JDBC ofrece control explícito sobre consultas y transacciones, a cambio de más código repetitivo. Un ORM tampoco elimina la necesidad de comprender SQL, índices o transacciones; escoger uno depende del problema y del proyecto.

## SQLite y MySQL

| Aspecto | SQLite | MySQL |
| --- | --- | --- |
| Dónde se ejecuta el motor | Dentro del proceso de la aplicación | En un servidor independiente |
| Persistencia habitual | Un archivo local | Archivos gestionados por el servidor |
| Acceso | Ruta del archivo y permisos del sistema | Red, puerto, usuario y permisos SQL |
| Encaje en este tema | Proyecto principal reproducible | Demostración de conexión a servidor |

El proyecto utiliza SQLite para que cada estudiante pueda arrancar sin un servidor adicional. MySQL permite comprender qué cambia cuando la base pertenece a otro proceso. **Compartir JDBC no hace que todo el SQL sea portable:** tipos, autoincrementos, funciones y algunos detalles de transacciones varían.

El archivo SQLite debe estar fuera de `src/main/resources`: es un dato modificable. Una URL relativa se interpreta desde el directorio de trabajo del proceso. Dos ejecuciones con directorios distintos pueden abrir dos bases diferentes aunque usen el mismo texto de URL.

## Abrir y cerrar una conexión

[`DriverManager`](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/DriverManager.html) localiza un driver apropiado para la URL. `getConnection` intenta abrir una sesión y devuelve una `Connection`. La URL `jdbc:sqlite:almacen-demo.db` selecciona SQLite; si el archivo no existe, el driver puede crearlo, pero eso **no crea automáticamente las tablas del almacén**.

Este fragmento ilustra el ciclo de vida; requiere el driver SQLite en las dependencias del proyecto:

```java
try (Connection conexion = DriverManager.getConnection("jdbc:sqlite:almacen-demo.db")) {
    boolean abierta = !conexion.isClosed();
    // En el depurador, abierta será true si la conexión se ha establecido.
}
```

`try-with-resources` cierra la conexión incluso si dentro del bloque ocurre una excepción. El objeto no se debe guardar para utilizarlo después de ese cierre. En las páginas siguientes añadiremos instrucciones dentro del bloque.

Una conexión ocupa recursos. Abrirla al entrar en una operación y cerrarla al terminar hace visible su propietario. Para una operación atómica con varias consultas, todas deberán compartir la misma conexión; abrir una distinta por cada escritura rompería esa unidad.

Los drivers modernos distribuidos correctamente se descubren automáticamente. No se añade `Class.forName(...)` por costumbre: si aparece `No suitable driver`, primero hay que comprobar la dependencia, la sincronización Maven y el prefijo de la URL.

## Conectar a MySQL sin publicar contraseñas

Para MySQL se necesita Connector/J en Maven, el servidor arrancado y una base ya creada para la cuenta utilizada. La [sintaxis de URL de Connector/J](https://dev.mysql.com/doc/connector-j/en/connector-j-reference-jdbc-url-format.html) distingue servidor, puerto y base. Este ejemplo recibe la configuración desde variables de entorno:

```java
String url = System.getenv("ACDA_DB_URL");
String usuario = System.getenv("ACDA_DB_USER");
String clave = System.getenv("ACDA_DB_PASSWORD");
if (url == null || usuario == null || clave == null) {
    throw new IllegalStateException("Falta configurar el acceso a MySQL");
}
try (Connection conexion = DriverManager.getConnection(url, usuario, clave)) {
    // La aplicación ya dispone de una sesión con el servidor.
}
```

Una URL local de ejemplo es `jdbc:mysql://localhost:3306/almacen`. `localhost` se refiere al equipo o contenedor donde corre Java. En Docker hay que distinguir el puerto publicado para el equipo del nombre de servicio disponible entre contenedores. No incluyas credenciales en el repositorio ni las escribas en los logs. Para conexiones remotas configura la verificación TLS según el servidor; no desactives la seguridad para silenciar un error.

Esta conexión requiere un usuario con los permisos necesarios sobre esa base. No es preciso utilizar una cuenta administradora para trabajar con productos y ventas.

## Diagnosticar antes de cambiar código

| Síntoma | Primera comprobación |
| --- | --- |
| `No suitable driver` | Dependencia cargada y URL JDBC del motor correcto |
| SQLite muestra una lista inesperadamente vacía | Ruta absoluta y directorio de trabajo; puede haberse creado otra base |
| `unable to open database file` | Carpeta padre existente y permisos de escritura |
| MySQL rechaza la conexión | Servidor, puerto, red y mapeo de Docker |
| `Access denied` | Cuenta, contraseña y permisos para el origen de la conexión |
| `no such table` / tabla inexistente | Base seleccionada e inicialización del esquema |

La excepción debe conservarse para poder distinguir esos casos. No conviertas todos los fallos en una lista vacía: una consulta correcta sin productos y una base inaccesible son situaciones diferentes.

Al cerrar esta página debes poder localizar qué URL abre el almacén, explicar quién cierra la conexión y distinguir un archivo SQLite de una sesión MySQL. Todavía no necesitas escribir el CRUD ni una transacción. Continúa con [instrucciones SQL y resultados](consultas.md).

## Etapa del proyecto: reconocer la conexión preparada

En el proyecto inicial abre `config.Database`. La conexión y el esquema se entregan resueltos para que la interfaz pueda arrancar desde el primer momento. Identifica `abrir`, la URL que recibe el constructor y el bloque que activa claves foráneas; después localiza quién cierra la conexión en `AlmacenService`.

La base de trabajo está en `datos/almacen.db`, fuera del classpath. Antes de interpretar una tabla vacía, comprueba que estás ejecutando con el directorio de trabajo previsto. No tienes que modificar JavaFX ni completar un TODO de conexión: el primer trabajo de persistencia del alumnado será el listado.
