# Operaciones de almacén — práctica final inicial

Abre la carpeta en IntelliJ como proyecto Maven y selecciona JDK 25. Ejecuta `es.mjsplayground.acda.jdbc.almacen.app.Launcher`. El catálogo y las ventas son la solución completa del guiado. La pestaña **Operaciones** añade búsqueda y revisión de precios, recepción individual y entrega conjunta con presupuesto. La interfaz está resuelta; el trabajo se concentra en el backend.

La base SQLite se guarda en `datos/almacen.db` relativa al directorio de trabajo. Para repetir con datos nuevos sin borrar nada, usa otra ruta en VM options: `-Dalmacen.db=datos/otra-prueba.db`. La semilla solo se inserta al crear la tabla por primera vez. No escribas dentro del classpath. Las fechas del historial están en UTC.

## Recorrido

1. En Operaciones → Localizar productos, escribe parte del nombre y máximo de existencias, y pulsa Buscar. Selecciona una fila para trasladar su identificador al panel de operaciones.
2. En Precios y recepciones, introduce el identificador y porcentaje para revisar su precio. Los porcentajes admiten hasta dos decimales.
3. Para recibir un producto indica unidades y coste unitario, y pulsa Recibir una línea.
4. Para una entrega conjunta prepara las líneas mediante Añadir a entrega; puedes seleccionar y quitar una línea o Vaciar sin guardar. Indica el presupuesto y pulsa Confirmar entrega. Al fallar, conserva las líneas para corregirlas; al confirmar, se vacían y se actualizan los justificantes.
5. Vuelve a Catálogo y ventas: las existencias se recargan. Los precios de venta no cambian al recibir mercancía.

Las cantidades monetarias se guardan en céntimos enteros. Búsquedas de nombre literales sin distinguir mayúsculas; los símbolos `%` y `_` no son comodines. La revisión de precios redondea HALF_UP. Entregas sin productos repetidos, unidades positivas, coste positivo y presupuesto inclusivo. Las transacciones conservan existencias e historial como una unidad.

Los TODO de `OperacionesService` y `JdbcRecepcionRepository` están pendientes. Las nuevas acciones muestran un aviso y no modifican datos. Las pruebas básicas comprueban el arranque y el guiado conservado. Las nuevas pruebas funcionales optativas están preparadas por nivel; no es necesario sustituirlas al completar los requisitos.

Validación técnica secundaria: `.\mvnw.cmd clean test`. Con sesión gráfica, `.\mvnw.cmd -Dqa.ui=true -Dtest=OperacionesUiTest test` abre y prueba la interfaz de operaciones y genera `target/qa/operaciones.png`. Las pruebas de servicio usan bases temporales; la prueba UI requiere pantalla y se omite por defecto en CI. No se ha comprobado el arranque dentro de IntelliJ en esta sesión.

Versiones fijadas en POM: JavaFX 25.0.2, SQLite JDBC 3.53.4.0, SLF4J 2.0.17, Log4j2 2.25.3. Se conserva Maven Wrapper del guiado.

## Comprobaciones optativas por nivel

Abre `AprendizajeFinalTest` en IntelliJ y crea una configuración JUnit. En VM options indica `-Daprendizaje.nivel=base`, `aplicacion` o `reto` tras resolver ese nivel. `todos` ejecuta el conjunto completo. Por defecto se omiten; una prueba omitida no demuestra que un requisito funcione. Los datos y mecanismos de fallo están preparados y no resuelven las operaciones del alumno.

Alternativa técnica: `.\mvnw.cmd -Daprendizaje.nivel=reto -Dtest=AprendizajeFinalTest test`. La solución contiene la misma suite para contrastar la cobertura.
