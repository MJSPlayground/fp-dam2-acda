# Proyecto guiado: almacén y ventas

Una tienda necesita mantener su catálogo y registrar ventas sin perder el control de las existencias. Trabajarás siempre sobre el mismo proyecto: la interfaz JavaFX ya está preparada y no debes modificarla. El backend crecerá conforme avances por las páginas de teoría.

## Preparar el proyecto

[Descargar proyecto inicial](/fp-dam2-acda/descargas/codigo/almacen-guiado-inicial.zip){ .md-button .md-button--primary download="almacen-guiado-inicial.zip" }

Descomprime el ZIP en una carpeta nueva. En IntelliJ selecciona **Open**, abre la carpeta que contiene `pom.xml`, espera la sincronización Maven y selecciona el JDK indicado en [preparación del entorno](../ayuda/proyectos-maven.md). Localiza `es.mjsplayground.acda.jdbc.almacen.app.Launcher` y ejecuta su `main` con Run. Conserva esa configuración para volver a ejecutar el proyecto.

La conexión, el esquema y los datos de arranque están resueltos. El programa crea `datos/almacen.db` dentro del directorio de trabajo de la configuración. No guarda datos modificables en `src/main/resources`. Para realizar otra prueba sin alterar tus datos, puedes indicar una ruta nueva con `-Dalmacen.db=C:/trabajo/prueba.db` en **VM options**.

Al principio, la interfaz mostrará que faltan etapas por implementar. No confundas ese aviso con un error de instalación ni con un catálogo guardado correctamente. La solución sí debe mostrar los productos iniciales.

## Cómo se reparten las responsabilidades

```text
Launcher → AlmacenApp → AlmacenView
                           ↓
                    AlmacenService
                           ↓
           JdbcProductoRepository / JdbcVentaRepository
                           ↓
                    Connection → SQLite
```

La vista recoge los campos y presenta resultados. El servicio valida las reglas y decide qué operaciones deben confirmarse juntas. Los repositorios traducen entre filas SQL y objetos Java. `Database` proporciona conexiones. Una conexión recibida por un repositorio pertenece a quien la abrió: el repositorio no la cierra ni confirma una transacción por su cuenta.

Las cantidades monetarias se almacenan como **céntimos enteros**: 25,90 € son `2590`. Así evitamos aproximaciones de coma flotante en cobros y totales. La interfaz se ocupa de convertir entre el texto en euros y el valor que recibe el servicio.

## Pruebas preparadas por fase

En el starter tienes `src/test/java/.../AprendizajeGuiadoTest.java`. Abre esa clase en IntelliJ, crea su configuración Run de JUnit y añade **VM options** `-Daprendizaje.fase=2`. Cambia el valor a `3`, `4` o `5` cuando completes esa fase. Cada caso prepara una base SQLite temporal nueva y no toca `datos/almacen.db`. Sin elegir fase se omiten: no significa que el TODO funcione.

## Etapa 1. Entender la conexión

Lee [conectores y conexiones](conexiones.md). Abre `config.Database` e identifica la URL, `DriverManager.getConnection` y el cierre de recursos. Esta parte está preparada para que el starter pueda arrancar: no tienes que sustituirla.

Comprueba dónde se crea la base y localiza las tablas `producto` y `venta`. Explica qué cambia respecto a MySQL: en SQLite la base es un archivo local; en MySQL hay un servidor, puerto y credenciales. El CRUD del proyecto se desarrollará una sola vez sobre SQLite.

Si la apertura falla, comprueba el directorio, permisos y mensaje de `SQLException`. No reemplaces un fallo de apertura por una lista vacía: significaría algo distinto.

## Etapa 2. Leer el catálogo

Tras [SQL y resultados](consultas.md), abre `model/repository/JdbcProductoRepository.java`, método `listar(Connection c)`. **No abras otra conexión**.

1. Escribe un `SELECT id,nombre,precio,stock FROM producto ORDER BY id` fijo y crea la sentencia usando `c`.
2. Declara una lista vacía antes del `try`, ejecuta la consulta y avanza con `while (filas.next())`.
3. Mapea primero `id` y `nombre`; añade después precio y stock al constructor `Producto`. No devuelvas el `ResultSet`.
4. Devuelve la lista al salir del cierre de recursos.

Dentro de `listar(Connection c)`, `sql` contiene el SELECT anterior y `datos` es la lista que devolverás. Este fragmento muestra únicamente el ciclo de lectura; completa el mapeo de las otras columnas y añade el producto a la lista:

```java
try (PreparedStatement sentencia = c.prepareStatement(sql);
     ResultSet filas = sentencia.executeQuery()) {
    while (filas.next()) {
        long id = filas.getLong("id");
        String nombre = filas.getString("nombre");
        // Lee precio y stock y construye un Producto para esta fila.
    }
}
```

Una iteración corresponde a un producto. `next()` debe ejecutarse antes de leer y no hay marcadores que enlazar en este SELECT fijo. Con tres filas habrá tres iteraciones; la lista no se llena hasta que añadas cada objeto construido.

Como pista parcial, el campo de stock se obtiene con `filas.getInt("stock")`; debe ser el cuarto argumento del record. Con esa estructura completa **TODO 2** en `JdbcProductoRepository.listar`. Prepara la consulta, recorre el `ResultSet` con `next()` y construye un `Producto` por fila. Cierra sentencia y resultado con `try-with-resources`.

Pulsa **Actualizar**. En una base nueva aparecerán Teclado con 10 unidades, Ratón con 20 y Monitor con 5. Observa que una lista vacía sin error es válida cuando no quedan productos; un fallo técnico debe comunicar un error.

Ejecuta la fase `2`: deben recuperarse exactamente los tres datos iniciales. **Avanza cuando los tres aparecen y el resultado no depende de una conexión que ya esté cerrada.**

## Etapa 3. Mantener productos

### 3a. CRUD en el repositorio

Lee [repositorios y servicios](repositorios.md). En `JdbcProductoRepository`, completa primero `buscar(Connection c,long id)`: enlaza la clave y usa `if (filas.next())`, porque solo esperas una fila. Devuelve `null` si no existe: ese es el contrato de este proyecto.

En `guardar`, separa alta (`id == 0`) de edición. Enlaza los valores, ejecuta y comprueba el recuento; en el alta solicita la clave con `Statement.RETURN_GENERATED_KEYS` y recupérala con `getGeneratedKeys()` en esa misma conexión. Después completa `eliminar` con una condición por identificador. No añadas validaciones de formularios ni transacciones propias al repositorio.

Para el alta, una sentencia preparada enlaza los datos sin mezclarlos con el SQL. Dentro de la rama de inserción de `guardar(Connection c,Producto v)`, este fragmento deja deliberadamente pendiente el tercer parámetro y la ejecución:

```java
String sql = "INSERT INTO producto(nombre,precio,stock) VALUES(?,?,?)";
try (PreparedStatement sentencia = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
    sentencia.setString(1, v.nombre());
    sentencia.setLong(2, v.precioCentimos());
    // Enlaza stock en la posición 3 y ejecuta la inserción.
}
```

Los valores proceden del producto recibido. El precio ya está en céntimos: no vuelvas a multiplicarlo por100. El segundo argumento al preparar solicita la clave generada. La inserción correcta afecta a una fila; después `getGeneratedKeys()` devuelve un resultado cuyo primer `next()` permite leer esa clave con `getLong(1)`, en la misma conexión. La edición utiliza otro SQL con condición por clave y no debe insertar otra fila.

### 3b. Validaciones en el servicio

Abre `model/service/AlmacenService.java`, método `guardar(Producto p)`. Primero rechaza entradas inválidas; después abre la conexión con `try-with-resources`, normaliza el nombre y llama al repositorio. Conserva el tratamiento preparado de errores SQL. Así distingues el mecanismo CRUD del caso de uso que decide si se permite guardar. Los parámetros se enlazan mediante `PreparedStatement`; no concatenes nombres recibidos de la interfaz.

El nombre debe tener entre 1 y 80 caracteres tras eliminar espacios en los extremos. El precio debe estar entre 0,01 y 1.000.000 euros y las existencias no pueden ser negativas. Un nombre duplicado debe rechazarse conservando la causa técnica. La interfaz no sustituye estas reglas en el servicio.

Comprueba **Nuevo → Guardar**, **Editar → Cancelar**, edición guardada y eliminación cancelada o confirmada. Un nombre como `Cable d'oficina` debe guardarse sin romper SQL. Prueba también nombre vacío, precio cero y stock negativo. Después de guardar, verifica que el formulario se cierre y la tabla refleje los datos.

Ejecuta la fase `3` y revisa un nombre con apóstrofo, alta/edición/eliminación y rechazos del servicio. **Avanza cuando todos conservan los demás productos y no se insertan datos inválidos.**

## Etapa 4. Preparar las dos escrituras de una venta

Completa **TODO 4**: `JdbcProductoRepository.descontar` y `JdbcVentaRepository.registrar`. El descuento debe efectuarse únicamente si hay unidades suficientes y comprobar cuántas filas cambiaron. Registrar una venta no debe modificar existencias por segunda vez.

En `descontar(Connection c,long id,int cantidad)`, el bloque de recursos cierra **solo la sentencia**, porque `c` es un préstamo. Tras declarar el SQL condicionado mostrado en teoría, el inicio es:

```java
try (PreparedStatement sentencia = c.prepareStatement(sql)) {
    sentencia.setInt(1, cantidad);
    // Enlaza id y el límite de stock en las posiciones 2 y 3.
    int afectadas = sentencia.executeUpdate();
    // Traduce el recuento al boolean que promete este método.
}
```

El servicio interpretará ese boolean para decidir si continúa. No añadas `c.close()` ni `c.commit()` aquí: la fase4 comprueba que el propietario aún puede hacer rollback sobre ambas escrituras.

Ambos métodos reciben una conexión. Todavía no llames a `commit` en ellos: la venta solo tiene sentido cuando las dos operaciones terminan correctamente. No añadas una llamada desde un repositorio al otro.

En `descontar`, utiliza el `UPDATE` condicionado de [transacciones](transacciones.md#descontar-sin-perder-una-condicion-de-negocio). En `registrar`, enlaza producto, cantidad y total según su firma; el esquema asigna la fecha con `DEFAULT CURRENT_TIMESTAMP`. Ejecuta la fase `4`: presta una conexión, invoca ambas escrituras y hace rollback desde fuera. Deben volver 10 teclados y cero ventas. **No avances si un repositorio cierra o confirma ese préstamo.**

## Etapa 5. Vender de forma atómica

Lee [transacciones y rollback](transacciones.md). En el método `vender`, completa por separado estos puntos: rechazo de cantidad; apertura y `setAutoCommit(false)`; búsqueda y cálculo; descuento condicionado y registro; `commit`; bloque de rollback que conserva el fallo original. Una operación debe pasar siempre por uno de los dos finales antes del cierre. Completa **TODO 5** en `AlmacenService.vender`. Valida una cantidad positiva y la existencia del producto; calcula el total y coordina descuento e historial con una misma conexión y una transacción.

Sobre una base nueva, vender dos teclados debe dejar ocho unidades y registrar **51,80 €**. Vender once teclados cuando hay diez debe conservar las diez unidades y no añadir una venta. No compares ese resultado después de haber vendido dos: en ese caso el estado de partida ya es ocho.

Comprueba también cantidad cero, negativa, producto inexistente y eliminación de un producto con ventas. Esta última debe rechazarse para conservar el historial. Si una escritura falla después del descuento, el rollback debe restaurar el stock. El starter incluye la fase `5`, con `falloSegundaEscrituraRevierteDescuentoYConservaCausa`: ya está preparada la avería al registrar, después del descuento real. Espera stock 10, historial vacío y causa SQL conservada. **Termina cuando ese caso y la venta correcta pasan; rechazar antes de escribir no demuestra rollback.**

## Depurar y contrastar

Pon un breakpoint al entrar en `vender`. Inspecciona el producto, la cantidad y el total; sigue el flujo hasta `commit` o `rollback`. Usa el stack trace y el registro técnico para distinguir un incumplimiento de negocio de un fallo de base de datos. No captures cualquier excepción para fingir que se ha guardado.

Las pruebas preparadas del starter y la solución crean bases temporales: no alteran tu catálogo. Activa cada fase como se explica arriba; las demás se omiten hasta que las elijas. El README conserva una alternativa técnica de ejecución.

## Solución completa

La solución contiene la misma interfaz y la implementación de todas las etapas. Su punto de entrada es también `app.Launcher`. Contrasta especialmente la propiedad de la conexión y el test que falla en la segunda escritura.

[Descargar solución completa](/fp-dam2-acda/descargas/codigo/almacen-guiado-solucion.zip){ .md-button download="almacen-guiado-solucion.zip" }

[Ver código y README de la solución](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/jdbc/guiado-solucion)
