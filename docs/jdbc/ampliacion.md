# Ampliación

Estas explicaciones profundizan en la [persistencia JDBC](consultas.md) y las [transacciones](transacciones.md). Los fragmentos son demostraciones con su contexto indicado; no añaden nuevas funcionalidades obligatorias al proyecto guiado.

## Metadatos: información sobre la base y los resultados

Una herramienta genérica de inspección no conoce de antemano el nombre del motor ni la estructura de cada consulta. JDBC ofrece dos tipos de metadatos:

- [`DatabaseMetaData`](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/DatabaseMetaData.html) describe el motor, el driver y capacidades de una conexión.
- [`ResultSetMetaData`](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/ResultSetMetaData.html) describe las columnas de un resultado concreto.

En una conexión abierta podemos identificar el entorno y consultar si el driver declara soporte para transacciones:

```java
DatabaseMetaData metadatos = conexion.getMetaData();
String motor = metadatos.getDatabaseProductName();
String version = metadatos.getDatabaseProductVersion();
boolean admiteTransacciones = metadatos.supportsTransactions();
```

Estos valores son útiles en diagnóstico. No se debe decidir todo el comportamiento solo buscando palabras dentro del nombre del motor: una capacidad específica se consulta con el método correspondiente y se prueba con el driver empleado.

Para inspeccionar una consulta, ejecutamos SQL fijo y recorremos sus columnas. El índice vuelve a empezar en 1:

```java
try (Statement sentencia = conexion.createStatement();
     ResultSet filas = sentencia.executeQuery("SELECT id, nombre FROM producto")) {
    ResultSetMetaData columnas = filas.getMetaData();
    for (int i = 1; i <= columnas.getColumnCount(); i++) {
        String etiqueta = columnas.getColumnLabel(i);
        String tipo = columnas.getColumnTypeName(i);
        // El depurador permite observar etiqueta y tipo para cada columna.
    }
}
```

`getColumnLabel` respeta alias de consulta. Si se escribe `SELECT nombre AS articulo`, la etiqueta que interesa para una tabla genérica es `articulo`. No es necesario que existan filas para describir las columnas del resultado.

## JOIN: datos relacionados convertidos en objetos

El historial de ventas guarda la clave del producto. Para presentar su nombre junto al importe, una consulta puede combinar ambas tablas:

```sql
SELECT v.id AS venta_id, p.nombre AS producto,
       v.cantidad, v.total, v.fecha
FROM venta v
JOIN producto p ON p.id = v.producto_id
ORDER BY v.id DESC;
```

Cada fila contiene ahora datos de dos tablas, pero no hace falta construir dos entidades completas. Un objeto de lectura como `VentaResumen(long id, String producto, int cantidad, long totalCentimos, String fecha)` representa directamente lo que necesita el historial.

En el repositorio, el mapeo utiliza los alias para evitar ambigüedades entre identificadores:

```java
VentaResumen resumen = new VentaResumen(
    filas.getLong("venta_id"),
    filas.getString("producto"),
    filas.getInt("cantidad"),
    filas.getLong("total"),
    filas.getString("fecha"));
```

El fragmento se coloca dentro del `while (filas.next())` explicado en [consultas](consultas.md). `JOIN` conserva solo parejas coincidentes. `LEFT JOIN` también conserva filas de la tabla izquierda sin pareja; las columnas de la derecha serán nulas y el mapeo tendrá que admitirlo.

Hay además una decisión de negocio: si cambia el nombre del producto, este `JOIN` mostrará el nombre actual. Si un documento histórico debe conservar el texto original, hará falta guardar una instantánea del nombre en la venta. Elegir el mapeo no sustituye a decidir qué historia queremos conservar.

## Procedimientos almacenados con CallableStatement

Un procedimiento es código almacenado y ejecutado en el servidor de base de datos. Puede agrupar SQL o encapsular una operación. SQLite no incorpora el mismo modelo de procedimientos almacenados que MySQL; esta demostración corresponde **a MySQL**.

Supongamos una tabla MySQL `producto` con `id` y `stock`. Desde una herramienta SQL con una cuenta autorizada se podría definir este procedimiento:

```sql
CREATE PROCEDURE consultar_stock(IN p_id BIGINT, OUT p_stock INT)
SELECT stock INTO p_stock FROM producto WHERE id = p_id;
```

El procedimiento recibe un identificador y entrega el stock en un parámetro de salida. Este ejemplo mínimo requiere un identificador existente y no representa por sí mismo una política completa de errores. En Java usamos [`CallableStatement`](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/CallableStatement.html), que extiende las posibilidades de una sentencia preparada:

```java
try (CallableStatement llamada = conexion.prepareCall("{call consultar_stock(?, ?)}")) {
    llamada.setLong(1, productoId);
    llamada.registerOutParameter(2, Types.INTEGER);
    llamada.execute();
    int stock = llamada.getInt(2);
    boolean ausente = llamada.wasNull();
    // Interpretar stock solo según el contrato del procedimiento.
}
```

Registrar el parámetro de salida informa al driver del tipo que esperamos recuperar. La sintaxis de escape `{call ...}` es JDBC; la definición y los permisos del procedimiento pertenecen al motor. La [guía de Connector/J sobre procedimientos](https://dev.mysql.com/doc/connector-j/en/connector-j-usagenotes-statements-callable.html) explica sus particularidades.

Mover reglas al servidor puede facilitar que varias aplicaciones las compartan, pero añade otro lugar que versionar, desplegar y probar. No es una mejora automática sobre un servicio Java sencillo.

## Pool de conexiones con HikariCP

En un servidor con muchas peticiones, abrir una conexión física para cada una puede resultar costoso. Un **pool** mantiene un conjunto limitado y presta conexiones a las operaciones. [`HikariCP`](https://github.com/brettwooldridge/HikariCP) es una implementación habitual.

El siguiente fragmento requiere añadir su dependencia Maven, además del driver MySQL. La aplicación crea un único pool al arrancar; el `try` exterior representa aquí su ciclo completo, no lo que se ejecutaría por petición:

```java
HikariConfig configuracion = new HikariConfig();
configuracion.setJdbcUrl(System.getenv("ACDA_DB_URL"));
configuracion.setUsername(System.getenv("ACDA_DB_USER"));
configuracion.setPassword(System.getenv("ACDA_DB_PASSWORD"));
configuracion.setMaximumPoolSize(4);

try (HikariDataSource pool = new HikariDataSource(configuracion)) {
    try (Connection conexion = pool.getConnection()) {
        // Ejecutar una operación breve; cerrar devuelve el préstamo al pool.
    }
    // En un servidor, el pool permanece disponible hasta el apagado.
}
```

El tamaño cuatro es ilustrativo. Debe ajustarse a la carga y al servidor, midiendo esperas y límites. Crear un pool por llamada anula su propósito. No cerrar los préstamos agota el pool aunque siga habiendo conexiones físicas abiertas.

El pool no sustituye las transacciones ni permite compartir una misma conexión entre hilos de forma arbitraria. La operación debe resolver su transacción antes de devolver el préstamo. En una aplicación SQLite local, añadir más conexiones tampoco elimina la limitación de escritores concurrentes.

## Operaciones por lotes

Cuando importamos muchas filas, ejecutar una llamada independiente por cada una añade trabajo repetido. `addBatch` acumula los parámetros de una misma plantilla y `executeBatch` solicita su ejecución conjunta. Por ejemplo, este fragmento recibe una lista de objetos con nombre, precio y stock:

```java
String sql = "INSERT INTO producto(nombre, precio, stock) VALUES (?, ?, ?)";
try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
    for (Producto producto : productos) {
        sentencia.setString(1, producto.nombre());
        sentencia.setLong(2, producto.precioCentimos());
        sentencia.setInt(3, producto.stock());
        sentencia.addBatch();
    }
    int[] resultados = sentencia.executeBatch();
}
```

Una importación que deba ser atómica ejecutará este bloque dentro de una transacción como la de [ventas](transacciones.md). **Lote no significa transacción**: con confirmaciones independientes un fallo podría dejar parte del trabajo guardado.

Los recuentos pueden contener `Statement.SUCCESS_NO_INFO` cuando se ejecutó una orden sin recuento disponible. Ante un fallo puede aparecer [`BatchUpdateException`](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/BatchUpdateException.html), cuyos recuentos ayudan a diagnosticar. No supongas que todos los drivers se detienen exactamente en la misma posición ni que el array siempre tendrá un `1` por elemento. Para importaciones grandes se procesan lotes limitados, evitando retener millones de valores en memoria, y se decide si la atomicidad abarca cada lote o el archivo completo.

## Índices y planes de ejecución

Un índice es una estructura auxiliar que ayuda a localizar u ordenar filas. Acelera determinadas lecturas a cambio de espacio y trabajo adicional en escrituras. El índice debe responder a una consulta real.

Por ejemplo, el historial suele filtrarse por producto. En una copia de prueba SQLite podemos comparar el plan antes y después de crear un índice:

```sql
EXPLAIN QUERY PLAN
SELECT id, cantidad, total FROM venta WHERE producto_id = 1;

CREATE INDEX IF NOT EXISTS idx_venta_producto ON venta(producto_id);

EXPLAIN QUERY PLAN
SELECT id, cantidad, total FROM venta WHERE producto_id = 1;
```

Según el esquema, los datos y la versión, el plan puede pasar de recorrer la tabla a buscar mediante el índice. El optimizador puede elegir otra estrategia, especialmente en tablas pequeñas: crear el índice no obliga a usarlo. La [documentación de SQLite sobre planes](https://sqlite.org/eqp.html) explica cómo interpretarlos y advierte que su formato no es una API estable.

Para evaluar la mejora hacen falta datos representativos, la misma consulta y condiciones comparables. Medir una sola ejecución con tres filas no permite concluir que una optimización sea eficaz. Antes de añadir índices también conviene evitar `SELECT *`, recuperar solo las filas necesarias y eliminar consultas repetidas dentro de un bucle cuando un `JOIN` resuelve el conjunto.

Un índice compuesto depende del orden de sus columnas y de los filtros utilizados. Indexar todas las columnas por intuición puede empeorar las escrituras sin mejorar las consultas relevantes.

## Concurrencia y niveles de aislamiento

Dos operadores pueden vender el mismo producto casi a la vez. Una lectura aislada seguida de una escritura no garantiza por sí sola que ambos vean existencias actuales. Ya usamos una actualización condicionada para impedir un descuento que viole el límite.

Los niveles de aislamiento describen qué interacciones permite una transacción con otras: `READ_UNCOMMITTED`, `READ_COMMITTED`, `REPEATABLE_READ` y `SERIALIZABLE`. Sus efectos concretos y los niveles admitidos dependen del motor y del driver. Una lectura sucia observa cambios sin confirmar; una lectura no repetible obtiene otro valor al releer una fila; una lectura fantasma encuentra un conjunto distinto al repetir una búsqueda por condición.

La API permite consultar soporte y configurar el nivel **antes de comenzar la transacción**:

```java
int nivel = Connection.TRANSACTION_SERIALIZABLE;
if (!conexion.getMetaData().supportsTransactionIsolationLevel(nivel)) {
    throw new IllegalStateException("El motor no admite el aislamiento solicitado");
}
conexion.setTransactionIsolation(nivel);
conexion.setAutoCommit(false);
```

Un aislamiento más fuerte puede aumentar bloqueos o abortos por conflicto. No se selecciona el nivel máximo para ocultar una operación mal diseñada. Hay que mantener la operación breve y definir qué ocurre si el motor no puede completarla.

SQLite serializa las escrituras y ofrece comportamientos distintos en diario tradicional y WAL; no se debe extrapolar sin más un experimento MySQL. Para estudiar concurrencia, utiliza dos conexiones en una base temporal, coordina explícitamente las operaciones y observa stock, historial y errores tras ambas. Reintentar solo es correcto si el caso de uso está preparado: repetir una venta ya confirmada tras perder su respuesta podría duplicarla. La [explicación oficial de aislamiento SQLite](https://sqlite.org/isolation.html) permite contrastar esas observaciones.
