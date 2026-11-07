# Almacén y ventas — proyecto guiado resuelto

Abre esta carpeta como proyecto Maven en IntelliJ, espera la sincronización y selecciona JDK 25. Ejecuta `app.Launcher.main` (clase normal de arranque que evita los problemas de lanzamiento directo de Application). No hay servidor ni credenciales.

La base SQLite se crea en `datos/almacen.db`, relativa al directorio de trabajo, con tres productos. Puedes configurar `-Dalmacen.db=C:/ruta/almacen.db` en VM options. Los datos persisten entre ejecuciones. Para otra prueba sin alterar tus datos, elige otra ruta nueva. No copies la base al classpath.

Recorrido: Nuevo → rellenar nombre, precio y existencias → Guardar; seleccionar → Editar/Cancelar; Vender → indicar cantidad; revisar pestaña Ventas. Eliminar pide confirmación y rechaza productos con historial. Precio en euros (coma o punto, máximo dos decimales), almacenado como céntimos enteros. Las fechas de ventas están en UTC.

Capas: vista → AlmacenService → repositorios JDBC → SQLite. El servicio comparte una conexión entre descuento y registro de venta. Si la segunda escritura falla, revierte ambas. Cada repositorio recibe la conexión; no la cierra ni confirma por su cuenta. El historial usa un JOIN preparado para la interfaz (se explica en Ampliación).

Validación técnica opcional: `.\mvnw.cmd clean test` y `.\mvnw.cmd javafx:run`. JavaFX 25.0.2, SQLite JDBC 3.53.4.0, SLF4J 2.0.17 y Log4j2 2.25.3 están fijados en el POM. Fuentes: https://github.com/xerial/sqlite-jdbc y https://logging.apache.org/log4j/2.x/manual/installation.html.

La interfaz se entrega preparada: el alumnado trabaja en configuración, repositorios y servicio. El esquema y datos iniciales son infraestructura. Las pruebas crean bases temporales independientes, sin modificar `datos/almacen.db`.

## Verificación de la interfaz

Con sesión gráfica disponible, ejecuta `.\mvnw.cmd -Dqa.ui=true test`. La prueba de JavaFX abre una ventana temporal, crea y edita un producto, comprueba cancelación y precio inválido, y redimensiona. Guarda una captura real en `target/qa/almacen.png` (no se distribuye en ZIP). Sin esta propiedad, la prueba gráfica se omite explícitamente para permitir CI sin pantalla; las ocho pruebas de servicio continúan ejecutándose. No sustituye la comprobación manual desde IntelliJ.
