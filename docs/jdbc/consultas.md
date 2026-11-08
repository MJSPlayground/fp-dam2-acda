# Instrucciones SQL y resultados

Después de [abrir una conexión](conexiones.md), necesitamos enviar órdenes al motor. Una consulta devuelve filas; una modificación devuelve principalmente cuántas filas ha afectado. Distinguir esos resultados evita interpretar una ejecución sin errores como una modificación efectiva.

Los fragmentos de esta página son demostraciones para una tabla didáctica `producto`. No deben sustituir las clases completas del proyecto. Suponemos una `Connection conexion` abierta; los métodos que contienen estos fragmentos deben declarar o tratar `SQLException`.

## Statement para SQL controlado

[`Statement`](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/Statement.html) permite enviar un texto SQL. Es adecuado para una instrucción fija de inicialización, escrita por quien desarrolla la aplicación. Creamos una tabla de ejemplo con un identificador, nombre, precio expresado en céntimos y existencias:

```java
String ddl = """
    CREATE TABLE IF NOT EXISTS producto (
        id INTEGER PRIMARY KEY,
        nombre TEXT NOT NULL,
        precio INTEGER NOT NULL CHECK (precio > 0),
        stock INTEGER NOT NULL CHECK (stock >= 0)
    )
    """;
try (Statement sentencia = conexion.createStatement()) {
    sentencia.executeUpdate(ddl);
}
```

El esquema usa sintaxis SQLite. `NOT NULL` impide valores ausentes y `CHECK` limita los valores admitidos. Las restricciones protegen también frente a escrituras realizadas por otras herramientas. El servicio añadirá mensajes y reglas de negocio comprensibles.

`IF NOT EXISTS` evita un error por tabla existente; **no actualiza** una tabla que ya tenga otro diseño. Cambiar una definición en el código no migra automáticamente una base antigua.

| Método | Se utiliza cuando | Resultado |
| --- | --- | --- |
| `executeQuery` | Se esperan filas, como en `SELECT` | Un `ResultSet` |
| `executeUpdate` | Se insertan, modifican o eliminan filas; también DDL | Número de filas afectadas, o cero en instrucciones sin recuento |
| `execute` | No se conoce de antemano el tipo de resultado | Indica si el primer resultado es un conjunto de filas |

No construyas SQL concatenando valores procedentes de formularios. Incluso un nombre con apóstrofo puede romper la consulta, y un texto manipulado puede alterar su significado. Para esos datos utilizamos parámetros.

## Primera lectura: una columna antes del mapeo

Antes de añadir parámetros o construir productos, observamos una sola columna. `executeQuery` devuelve un `ResultSet`: un cursor situado antes de la primera fila. `next()` avanza a la siguiente fila y dice si existe; `getString("nombre")` lee esa columna de la fila actual. El siguiente fragmento usa SQL fijo, una conexión `conexion` ya abierta y el tratamiento de `SQLException` explicado al principio:

```java
try (Statement sentencia = conexion.createStatement();
     ResultSet filas = sentencia.executeQuery("SELECT nombre FROM producto ORDER BY id")) {
    while (filas.next()) {
        String nombre = filas.getString("nombre");
        // Inspecciona nombre con un breakpoint; cambia en cada iteración.
    }
}
```

Con los datos iniciales se leen Teclado, Ratón y Monitor, en ese orden. Si no hay filas, el cuerpo no se ejecuta. Leer antes del primer `next()` es un error; crear un objeto fuera del bucle y reutilizarlo no representa una fila nueva. Primero entendemos este recorrido; después añadimos parámetros y convertimos todas las columnas a un producto.

## PreparedStatement: SQL y datos separados

[`PreparedStatement`](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/PreparedStatement.html) conserva una plantilla con marcadores `?`. Se asigna un valor a cada posición antes de ejecutarla. Las posiciones empiezan en **1**, de izquierda a derecha.

Para guardar un producto utilizamos tres parámetros. El motor genera el identificador al omitirlo en este esquema SQLite:

```java
String sql = "INSERT INTO producto(nombre, precio, stock) VALUES (?, ?, ?)";
try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
    sentencia.setString(1, "Teclado compacto");
    sentencia.setLong(2, 2590);
    sentencia.setInt(3, 12);
    int insertadas = sentencia.executeUpdate();
    if (insertadas != 1) {
        throw new SQLException("No se ha insertado exactamente un producto");
    }
}
```

El precio guardado es 25,90 €, representado como 2590 céntimos. Este diseño evita errores de coma flotante en cantidades monetarias. La conversión de texto de formulario a dinero corresponde a la entrada preparada y la validación al servicio.

La consulta preparada distingue los datos del código SQL: un apóstrofo dentro del nombre forma parte del nombre. Los parámetros **no** sirven para nombres de tabla, columna o palabras como `ASC`. Si permitimos elegir una ordenación, traducimos opciones conocidas a fragmentos constantes mediante una lista de valores permitidos.

En este fragmento `executeUpdate()` no recibe otra vez el SQL: ejecuta la plantilla de la preparación, con los valores ya asignados.

## ResultSet: recorrer, leer y convertir

Una consulta no devuelve una `List<Producto>` automáticamente. Devuelve un [`ResultSet`](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/ResultSet.html), un cursor inicialmente situado **antes de la primera fila**. `next()` intenta avanzar; devuelve `false` al agotarse los resultados.

Buscamos productos cuyo stock sea menor o igual que un límite. El orden estable facilita comprobar el resultado:

```java
String sql = """
    SELECT id, nombre, precio, stock
    FROM producto
    WHERE stock <= ?
    ORDER BY id
    """;
List<Producto> productos = new ArrayList<>();
try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
    sentencia.setInt(1, 5);
    try (ResultSet filas = sentencia.executeQuery()) {
        while (filas.next()) {
            Producto producto = new Producto(
                filas.getLong("id"),
                filas.getString("nombre"),
                filas.getLong("precio"),
                filas.getInt("stock"));
            productos.add(producto);
        }
    }
}
```

Para entender el fragmento, `Producto` sería un `record Producto(long id, String nombre, long precioCentimos, int stock)`. El proyecto puede organizar su entidad de otra forma: lo esencial es que el **mapeo** asigna cada columna al campo correcto. Cada iteración crea un objeto independiente. Una consulta sin coincidencias produce una lista vacía y ninguna iteración; no es un fallo técnico.

Los recursos se cierran desde dentro hacia fuera: primero resultado, después sentencia y finalmente la conexión que los contiene. El repositorio devuelve objetos ya materializados, no un cursor dependiente de una conexión cerrada.

### Tipos y valores ausentes

Los métodos de lectura deben corresponder al significado de la columna: `getString` para texto, `getInt` para cantidades pequeñas y `getLong` para identificadores o importes enteros. No conviertas todo a texto para volver a analizarlo después.

Una columna SQL puede contener `NULL`. `getInt` y `getLong` devuelven cero para un nulo; para distinguirlo debe llamarse a `wasNull()` inmediatamente después de esa lectura. Si el dato es obligatorio, `NOT NULL` simplifica el contrato. Para un texto opcional, `getString` puede devolver `null`. Una aplicación debe decidir qué significa esa ausencia, en lugar de sustituirla silenciosamente por un valor inventado.

## Actualizar y eliminar con un resultado verificable

Una modificación debe delimitar las filas con `WHERE`. Omitirlo cambia todos los productos. Este ejemplo modifica únicamente el precio de un identificador:

```java
String sql = "UPDATE producto SET precio = ? WHERE id = ?";
try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
    sentencia.setLong(1, nuevoPrecioCentimos);
    sentencia.setLong(2, id);
    int afectadas = sentencia.executeUpdate();
    if (afectadas != 1) {
        throw new IllegalArgumentException("El producto no existe");
    }
}
```

En SQLite, para este esquema, un identificador inexistente da cero filas. En otros motores y configuraciones el recuento de filas modificadas frente a coincidentes puede variar; el contrato del repositorio debe tenerlo en cuenta.

Eliminar utiliza la misma estructura con `DELETE FROM producto WHERE id = ?`. Si una venta referencia el producto, puede existir una restricción que impida el borrado. No borres primero el historial para conseguir que desaparezca el error: hay que decidir qué operación admite el negocio.

## Claves generadas y búsquedas

Cuando la UI necesita el identificador insertado, JDBC permite solicitar claves generadas con `Statement.RETURN_GENERATED_KEYS` y leerlas con `getGeneratedKeys()`. El soporte y el comportamiento dependen del driver. Otra opción SQLite es consultar `last_insert_rowid()` **en la misma conexión**, justo tras la inserción; no equivale a buscar el máximo global del identificador.

Buscar un registro por su clave puede devolver `Optional<Producto>`: `Optional.empty()` expresa que la consulta funcionó pero no existe esa clave. Una excepción expresa que no se pudo consultar. El servicio decide si la ausencia se convierte en «Producto no encontrado».

## Comprobaciones que revelan errores habituales

- Introducir un nombre con apóstrofo: debe almacenarse sin modificar el SQL.
- Buscar una clave inexistente: debe distinguirse de un fallo de conexión.
- Consultar con límite cero: solo deben aparecer productos agotados.
- Actualizar un identificador concreto: los demás productos deben conservar sus datos.
- Reiniciar la aplicación: los datos deben seguir en el mismo archivo.

Continúa con [repositorios, servicios y errores](repositorios.md), donde estas instrucciones adquieren un contrato reutilizable.


## Etapas del proyecto: listado y mantenimiento

En `JdbcProductoRepository`, el **TODO 2** corresponde a `listar`. Recibe una conexión prestada: prepara la consulta, convierte las filas en productos y cierra sentencia y resultado. La conexión la cierra el servicio que la abrió. Al pulsar **Actualizar**, la interfaz debe mostrar los tres productos iniciales: Teclado con 10 unidades, Ratón con 20 y Monitor con 5.

El **TODO 3** incorpora `buscar`, `guardar` y `eliminar`. En este proyecto concreto `buscar` devuelve `null` si no encuentra la clave; el servicio debe comprobarlo antes de utilizar el producto. `guardar` distingue un alta con identificador cero de la edición de uno existente. Consulta las firmas del starter y conserva sus contratos.

Completa también las validaciones de `AlmacenService.guardar` descritas en la página de [servicios](repositorios.md). Prueba un alta con apóstrofo, modifica solo ese producto y elimínalo mientras no tenga ventas. La interfaz se entrega preparada y no debe editarse. El historial empieza a tener contenido cuando completes la transacción de venta, no al terminar únicamente el listado.
