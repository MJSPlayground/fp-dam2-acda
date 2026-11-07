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

## Etapa 1. Entender la conexión

Lee [conectores y conexiones](conexiones.md). Abre `config.Database` e identifica la URL, `DriverManager.getConnection` y el cierre de recursos. Esta parte está preparada para que el starter pueda arrancar: no tienes que sustituirla.

Comprueba dónde se crea la base y localiza las tablas `producto` y `venta`. Explica qué cambia respecto a MySQL: en SQLite la base es un archivo local; en MySQL hay un servidor, puerto y credenciales. El CRUD del proyecto se desarrollará una sola vez sobre SQLite.

Si la apertura falla, comprueba el directorio, permisos y mensaje de `SQLException`. No reemplaces un fallo de apertura por una lista vacía: significaría algo distinto.

## Etapa 2. Leer el catálogo

Tras [SQL y resultados](consultas.md), completa **TODO 2** en `JdbcProductoRepository.listar`. Prepara la consulta, recorre el `ResultSet` con `next()` y construye un `Producto` por fila. Cierra sentencia y resultado con `try-with-resources`.

Pulsa **Actualizar**. En una base nueva aparecerán Teclado con 10 unidades, Ratón con 20 y Monitor con 5. Observa que una lista vacía sin error es válida cuando no quedan productos; un fallo técnico debe comunicar un error.

## Etapa 3. Mantener productos

Lee [repositorios y servicios](repositorios.md). Completa los **TODO 3** de `buscar`, `guardar` y `eliminar`, y las validaciones de `AlmacenService.guardar`. Los parámetros se enlazan mediante `PreparedStatement`; no concatenes nombres recibidos de la interfaz.

El nombre debe tener entre 1 y 80 caracteres tras eliminar espacios en los extremos. El precio debe estar entre 0,01 y 1.000.000 euros y las existencias no pueden ser negativas. Un nombre duplicado debe rechazarse conservando la causa técnica. La interfaz no sustituye estas reglas en el servicio.

Comprueba **Nuevo → Guardar**, **Editar → Cancelar**, edición guardada y eliminación cancelada o confirmada. Un nombre como `Cable d'oficina` debe guardarse sin romper SQL. Prueba también nombre vacío, precio cero y stock negativo. Después de guardar, verifica que el formulario se cierre y la tabla refleje los datos.

## Etapa 4. Preparar las dos escrituras de una venta

Completa **TODO 4**: `JdbcProductoRepository.descontar` y `JdbcVentaRepository.registrar`. El descuento debe efectuarse únicamente si hay unidades suficientes y comprobar cuántas filas cambiaron. Registrar una venta no debe modificar existencias por segunda vez.

Ambos métodos reciben una conexión. Todavía no llames a `commit` en ellos: la venta solo tiene sentido cuando las dos operaciones terminan correctamente. No añadas una llamada desde un repositorio al otro.

## Etapa 5. Vender de forma atómica

Lee [transacciones y rollback](transacciones.md) y completa **TODO 5** en `AlmacenService.vender`. Valida una cantidad positiva y la existencia del producto; calcula el total y coordina descuento e historial con una misma conexión y una transacción.

Sobre una base nueva, vender dos teclados debe dejar ocho unidades y registrar **51,80 €**. Vender once teclados cuando hay diez debe conservar las diez unidades y no añadir una venta. No compares ese resultado después de haber vendido dos: en ese caso el estado de partida ya es ocho.

Comprueba también cantidad cero, negativa, producto inexistente y eliminación de un producto con ventas. Esta última debe rechazarse para conservar el historial. Si una escritura falla después del descuento, el rollback debe restaurar el stock. La solución incluye una prueba que provoca ese fallo de manera controlada sobre una base temporal.

## Depurar y contrastar

Pon un breakpoint al entrar en `vender`. Inspecciona el producto, la cantidad y el total; sigue el flujo hasta `commit` o `rollback`. Usa el stack trace y el registro técnico para distinguir un incumplimiento de negocio de un fallo de base de datos. No captures cualquier excepción para fingir que se ha guardado.

Los tests de la solución crean bases temporales: no alteran tu catálogo. Desde la ventana Maven puedes ejecutar el ciclo **test** y consultar los resultados. El README conserva los comandos opcionales de QA.

## Solución completa

La solución contiene la misma interfaz y la implementación de todas las etapas. Su punto de entrada es también `app.Launcher`. Contrasta especialmente la propiedad de la conexión y el test que falla en la segunda escritura.

[Descargar solución completa](/fp-dam2-acda/descargas/codigo/almacen-guiado-solucion.zip){ .md-button download="almacen-guiado-solucion.zip" }

[Ver código y README de la solución](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/jdbc/guiado-solucion)
