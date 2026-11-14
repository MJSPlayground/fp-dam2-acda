# Proyecto guiado: almacén y ventas

Una tienda necesita mantener su catálogo y registrar ventas sin perder el control de las existencias. Trabajarás siempre sobre el mismo proyecto: la interfaz JavaFX ya está preparada y no debes modificarla. El backend crecerá conforme avances por las páginas de teoría.

Al terminar podrás consultar, crear, editar y eliminar productos y vender con descuento de stock e historial como una sola operación. Necesitas Java con records, listas, excepciones y SQL básico (`SELECT`, `INSERT`, `UPDATE`, `DELETE`); [SQL y resultados](consultas.md) conecta esos conocimientos con JDBC. Tú completas los repositorios y las reglas del servicio; la conexión SQLite, el modelo, la interfaz y el arranque están preparados. No necesitas Docker para este guiado.

## Preparar el proyecto

[Descargar proyecto inicial](/fp-dam2-acda/descargas/codigo/almacen-guiado-inicial.zip){ .md-button .md-button--primary download="almacen-guiado-inicial.zip" }

Descomprime el ZIP en una carpeta nueva. En IntelliJ selecciona **Open**, abre la carpeta que contiene `pom.xml`, espera la sincronización Maven y selecciona el JDK indicado en [preparación del entorno](../ayuda/proyectos-maven.md). Localiza `es.mjsplayground.acda.jdbc.almacen.app.Launcher` y ejecuta su `main` con Run. Conserva esa configuración para volver a ejecutar el proyecto.

La conexión, el esquema y los datos de arranque están resueltos. El programa crea `datos/almacen.db` dentro del directorio de trabajo de la configuración. No guarda datos modificables en `src/main/resources`. Para realizar otra prueba sin alterar tus datos, puedes indicar una ruta nueva con `-Dalmacen.db=C:/trabajo/prueba.db` en **VM options**.

**Primera comprobación, sin programar:** ejecuta `Launcher`, observa el aviso de etapa pendiente y abre **Nuevo → Cancelar**. La ventana y el formulario deben responder aunque aún no funcione el listado. En el directorio de trabajo debe aparecer `datos/almacen.db`: `Database.preparar()` ya ha creado las tablas y los tres productos iniciales. El aviso significa que todavía no puedes consultarlos desde el repositorio, no que la base esté vacía. Reiniciar conserva los cambios; solo una base nueva recibe los datos iniciales.

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

Observa este **fragmento ya existente** en `AlmacenService.listar`:

```java
try (Connection c = db.abrir()) {
    return productos.listar(c);
}
```

El servicio adquiere la conexión y la presta al repositorio. El `try` la cierra después de recibir los objetos Java; el `catch` del método, omitido en el fragmento, conserva la causa SQL mediante `fallo`. No copies el bloque como un método nuevo. En esta consulta no hay dos escrituras que coordinar; la transacción explícita aparecerá al vender.

Si la apertura falla, comprueba el directorio, permisos y mensaje de `SQLException`. No reemplaces un fallo de apertura por una lista vacía: significaría algo distinto.

## Etapa 2. Leer el catálogo

Tras [SQL y resultados](consultas.md), abre `model/repository/JdbcProductoRepository.java`, método `listar(Connection c)`. **No abras otra conexión**.

Antes de programar, observa `JdbcVentaRepository.listar`, **ya implementado**. Su `SELECT` devuelve identificador, nombre del producto, cantidad, total y fecha. `executeQuery()` produce un `ResultSet` situado antes de la primera fila; cada `next()` permite leer otra y construir un `Venta`. La sentencia y el resultado se cierran dentro del repositorio; los objetos de la lista siguen siendo utilizables fuera. Aquí las columnas se leen por posición: la posición 2 corresponde al nombre seleccionado. En productos utilizarás etiquetas como `"nombre"`, para que el mapeo sea más fácil de seguir.

**Ahora te toca:** aplica ese patrón a `JdbcProductoRepository.listar`, **TODO 2**. El contrato y `Producto` ya existen; falta recuperar todas las filas de productos y devolver objetos independientes del resultado SQL.

1. Escribe un `SELECT id,nombre,precio,stock FROM producto ORDER BY nombre` fijo y crea la sentencia usando `c`.
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

### 3a. Buscar una sola fila con un parámetro

Abre `model/repository/JdbcVentaRepository.java` y observa **el método completo ya preparado** `existenPara`. Este es su cuerpo; no tienes que añadirlo:

```java
try (PreparedStatement p = c.prepareStatement("SELECT 1 FROM venta WHERE producto_id=?")) {
    p.setLong(1, id);
    try (ResultSet r = p.executeQuery()) {
        return r.next();
    }
}
```

El `?` representa un valor y `setLong(1, id)` enlaza el primer parámetro. `SELECT 1` solo permite saber si existe alguna venta: no reconstruye un producto. `next()` devuelve `false` cuando no hay filas. Los valores nunca se concatenan al SQL. La [API de PreparedStatement](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/PreparedStatement.html) distingue las consultas que devuelven filas de las escrituras que devuelven un recuento.

Lee [repositorios y servicios](repositorios.md). En `JdbcProductoRepository`, completa primero `buscar(Connection c,long id)`: enlaza la clave y usa `if (filas.next())`, porque solo esperas una fila. Devuelve `null` si no existe: ese es el contrato de este proyecto.

**Ahora te toca:** adapta la consulta parametrizada al identificador de un producto y combina su resultado con el mapeo de la etapa 2. Para comprobarlo antes de guardar, coloca un breakpoint en `AlmacenService.listar`, dentro del `try` y antes del `return`. Ejecuta con Debug y usa **Evaluate Expression**: `productos.buscar(c, 1)` debe recuperar Teclado en una base nueva; `productos.buscar(c, 999999)` debe devolver `null` si no has creado ese identificador. Continúa la ejecución para cerrar la conexión normalmente.

??? tip "Pista — Una fila o ninguna"

    El `SELECT` de productos debe pedir sus cuatro columnas, en lugar del literal `1` del ejemplo. Reutiliza los getters de `listar`; no intentes leer una columna si `next()` devolvió `false`.

### 3b. Escribir y recuperar la identidad

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

**Ahora te toca:** completa `guardar(Connection, Producto)` y `eliminar(Connection, long)` del **TODO 3** en ese mismo archivo. `executeUpdate()` devuelve cuántas filas cambiaron, no las filas de un `SELECT`. La edición conserva su ID; un ID inexistente debe comunicar fallo. Puedes comprobar ambos métodos con Debug y una conexión abierta como en 3a, usando solo un producto de prueba nuevo. La interfaz de guardado seguirá pendiente hasta 3c.

??? tip "Pista — Evitar una actualización global"

    Antes de ejecutar un `UPDATE` o `DELETE`, identifica su condición `WHERE` y la posición del ID entre los parámetros. Comprueba una fila afectada. Para el alta, el resultado de `getGeneratedKeys()` también necesita avanzar con `next()`.

### 3c. Validaciones en el servicio

Abre `model/service/AlmacenService.java`, método `guardar(Producto p)`. Primero rechaza entradas inválidas; después abre la conexión con `try-with-resources`, normaliza el nombre y llama al repositorio. Conserva el tratamiento preparado de errores SQL. Así distingues el mecanismo CRUD del caso de uso que decide si se permite guardar. Los parámetros se enlazan mediante `PreparedStatement`; no concatenes nombres recibidos de la interfaz.

El nombre debe tener entre 1 y 80 caracteres tras eliminar espacios en los extremos. El precio debe estar entre 0,01 y 1.000.000 euros y las existencias no pueden ser negativas. Un nombre duplicado debe rechazarse conservando la causa técnica. La interfaz no sustituye estas reglas en el servicio.

**Ahora te toca:** combina esas reglas con el patrón de adquisición y cierre observado en `AlmacenService.listar`. El repositorio ya completado decide el SQL; tú decides si el producto puede guardarse. No necesitas cambiar el formulario ni abrir una conexión antes de rechazar un precio inválido.

Comprueba **Nuevo → Guardar**, **Editar → Cancelar**, edición guardada y eliminación cancelada o confirmada. Un nombre como `Cable d'oficina` debe guardarse sin romper SQL. Prueba también nombre vacío, precio cero y stock negativo. Después de guardar, verifica que el formulario se cierre y la tabla refleje los datos.

Ejecuta la fase `3` y revisa un nombre con apóstrofo, alta/edición/eliminación y rechazos del servicio. **Avanza cuando todos conservan los demás productos y no se insertan datos inválidos.**

## Etapa 4. Preparar las dos escrituras de una venta

Completa **TODO 4**: `JdbcProductoRepository.descontar` y `JdbcVentaRepository.registrar`. El descuento debe efectuarse únicamente si hay unidades suficientes y comprobar cuántas filas cambiaron. Registrar una venta no debe modificar existencias por segunda vez.

Ya has utilizado `executeUpdate()` en 3b: esa misma API sirve para el descuento condicionado. Para `registrar(Connection, long, int, long)`, adapta el `INSERT` anterior a los tres datos de la venta; la fecha la prepara la base. No necesitas recuperar su identificador porque este contrato devuelve `void`. Son operaciones separadas que todavía debes integrar en el servicio.

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

Para observarlo sin JUnit, detén Debug dentro del `try` de `AlmacenService.listar`, antes del `return`, con una base de prueba nueva. Evalúa por separado `c.setAutoCommit(false)`, `productos.descontar(c, 1, 2)` y `ventas.registrar(c, 1, 2, 5180)`. Consulta `productos.buscar(c, 1)` y `ventas.listar(c)` en esa conexión: verás 8 unidades y una venta. Evalúa `c.rollback()` y repite ambas lecturas: deben volver 10 y ninguna venta. Reanuda para cerrar la conexión. Estas llamadas son un **ensayo en Debug**, no código que debas insertar en `listar`.

## Etapa 5. Vender de forma atómica

Lee [transacciones y rollback](transacciones.md). En el método `vender`, completa por separado estos puntos: rechazo de cantidad; apertura y `setAutoCommit(false)`; búsqueda y cálculo; descuento condicionado y registro; `commit`; bloque de rollback que conserva el fallo original. Una operación debe pasar siempre por uno de los dos finales antes del cierre. Completa **TODO 5** en `AlmacenService.vender`. Valida una cantidad positiva y la existencia del producto; calcula el total y coordina descuento e historial con una misma conexión y una transacción.

??? tip "Pista — El fallo puede llegar después del descuento"

    Reutiliza lo observado en la etapa 4. Piensa qué ocurre si falla `registrar` y qué excepción debe llegar a la interfaz. Si también falla el rollback, conserva el error inicial y añade el secundario con `addSuppressed`; no lo reemplaces por un mensaje de éxito.

Sobre una base nueva, vender dos teclados debe dejar ocho unidades y registrar **51,80 €**. Vender once teclados cuando hay diez debe conservar las diez unidades y no añadir una venta. No compares ese resultado después de haber vendido dos: en ese caso el estado de partida ya es ocho.

Comprueba también cantidad cero, negativa, producto inexistente y eliminación de un producto con ventas. Esta última debe rechazarse para conservar el historial. Si una escritura falla después del descuento, el rollback debe restaurar el stock. El starter incluye la fase `5`, con `falloSegundaEscrituraRevierteDescuentoYConservaCausa`: ya está preparada la avería al registrar, después del descuento real. Espera stock 10, historial vacío y causa SQL conservada. **Termina cuando ese caso y la venta correcta pasan; rechazar antes de escribir no demuestra rollback.**

## Depurar y contrastar

Pon un breakpoint al entrar en `vender`. Inspecciona el producto, la cantidad y el total; sigue el flujo hasta `commit` o `rollback`. Usa el stack trace y el registro técnico para distinguir un incumplimiento de negocio de un fallo de base de datos. No captures cualquier excepción para fingir que se ha guardado.

Las pruebas preparadas del starter y la solución crean bases temporales: no alteran tu catálogo. Activa cada fase como se explica arriba; las demás se omiten hasta que las elijas. El README conserva una alternativa técnica de ejecución.

## Comprobación de conjunto

| Acción sobre una base de prueba nueva | Resultado esperado |
| --- | --- |
| Actualizar y buscar por ID | Tres productos; un ID ausente devuelve `null` en el repositorio. |
| Crear `Cable d'oficina`, editar y cancelar otra edición | Apóstrofo conservado; cancelar mantiene el último valor guardado. |
| Guardar precio cero, stock negativo o nombre repetido | Rechazo sin modificar las filas previas; causa técnica conservada en el duplicado. |
| Vender dos teclados | Stock 8 y una venta de 51,80 €. |
| Intentar vender 9 más después | Rechazo; siguen stock 8 y una sola venta. |
| Reiniciar con la misma ruta de base | Se conservan productos, stock e historial. |
| Eliminar un producto con ventas | Rechazo que conserva el historial. |

El fallo preparado de la fase `5` añade la comprobación de rollback después de una escritura real; una venta rechazada antes de escribir no lo demuestra.

**Explica con tus palabras:** ¿por qué una lista vacía y una `SQLException` significan cosas distintas? ¿Quién abre y cierra la conexión y quién cierra el `ResultSet`? ¿Por qué descuento y venta deben compartir conexión? ¿Qué pasaría si cada repositorio confirmara su operación?

## Solución completa

La solución contiene la misma interfaz y la implementación de todas las etapas. Su punto de entrada es también `app.Launcher`. Contrasta especialmente la propiedad de la conexión y el test que falla en la segunda escritura.

[Descargar solución completa](/fp-dam2-acda/descargas/codigo/almacen-guiado-solucion.zip){ .md-button download="almacen-guiado-solucion.zip" }

[Ver código y README de la solución](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/jdbc/guiado-solucion)
