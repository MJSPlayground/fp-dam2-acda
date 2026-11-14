# Inventario local — guiado-inicial

## Abrir y ejecutar desde IntelliJ

Abre esta carpeta Maven, espera la descarga de dependencias y selecciona JDK 25. Ejecuta `app.Launcher.main` del paquete `es.mjsplayground.acda.ficheros.inventario`. La interfaz se entrega terminada; no debes editar vistas ni CSS.

La aplicación abre con tres productos de demostración EN MEMORIA, sin fingir que ya están guardados. La carpeta predeterminada es `datos/inventario`, relativa al Working directory de IntelliJ. Se muestra su ruta absoluta en Ubicación. Puedes escribir otra ruta existente y pulsar Usar carpeta, o usar Examinar. VM option opcional: `-Dinventario.dir=C:/ruta/de/trabajo`.

Copiar ejemplos extrae los archivos readonly `/data/productos.csv`, `/data/productos.json`, `/data/lote.csv` y `/data/lote.json` del classpath a la carpeta elegida. No sobrescribe ejemplos que ya existan. Los datos modificables se guardan SIEMPRE fuera del classpath. La lista solo recorre el directorio elegido: no desciende a sus subdirectorios.

## Recorrido de uso

Nuevo abre un formulario vacío; Editar mantiene fijo el código; Guardar producto actualiza la tabla en memoria y cierra/limpia el formulario. Cancelar no cambia el catálogo. Eliminar pide confirmación segura por defecto. El campo Nombre admite varias líneas sin perder los saltos al editar solo precio o stock.

Buscar filtra código/nombre sin distinguir mayúsculas. Para persistir, elige CSV/JSON/BIN y el nombre de archivo de destino, y pulsa Guardar catálogo. Si existe, solicita confirmación antes de reemplazarlo. Cargar seleccionado sustituye TODO el catálogo, incluidos cambios no guardados, tras confirmación. Una carga inválida no altera el catálogo anterior. Escapar de una confirmación o cancelar evita la operación.

La interfaz usa repositorio a través del servicio: UI → InventarioService → InventarioRepository. El servicio mantiene invariantes y estado; cada repositorio convierte un formato; ArchivosRepository lista y copia recursos preparados. Errores propios conservan la causa técnica cuando existe. SLF4J/Log4j2 registra diagnósticos, sin usar sout como log habitual.

## Contrato de datos

Producto(codigo,nombre,unidades,precio): código único de 1–20 caracteres A–Z/dígitos/guion/guionbajo; nombre no vacío de hasta 100 caracteres (se recortan extremos, se conservan comas/comillas/saltos internos); unidades enteras de 0 a 1000000; precio BigDecimal de 0,01 a 1000000, hasta dos decimales sin redondear entrada. Máximo 5000 productos por catálogo. Se normaliza precio válido a escala 2.

CSV: UTF-8, coma, reglas RFC4180, encabezados exactos `codigo,nombre,unidades,precio`, en ese orden. Se usa Apache Commons CSV, nunca split. Las comillas, comas y saltos dentro de un campo forman parte del dato. El precio de intercambio usa punto decimal. JSON: array de objetos con esos cuatro campos; Jackson rechaza campos ausentes/nulos, desconocidos, enteros fraccionarios/fuera de rango y contenido después del array. No se truncan unidades grandes.

BIN: Instantanea(Producto[]) mediante Serializable y ObjectOutputStream/ObjectInputStream. Es el formato binario principal. FiltroSerializacion es infraestructura preparada: lista blanca de clases exactas y límites de profundidad, referencias, bytes y longitud de array. Solo debes abrir binarios generados y controlados por la aplicación. El filtro no convierte archivos externos en confiables; la serialización Java no es formato recomendado de intercambio. serialVersionUID ayuda a identificar versiones, sin garantizar compatibilidad ilimitada de cambios.

Una carga guiada es estricta: si hay un formato incorrecto, producto inválido o código duplicado, se rechaza el archivo completo y se conserva la memoria anterior. La escritura usa EscrituraProtegida, ya preparada: primero escribe aparte y solo reemplaza tras éxito. La explicación de temporales/movimiento atómico pertenece a Ampliación; no es un TODO adicional ni una garantía contra cualquier corte físico.

## Comprobar y ejecutar


Desde esta carpeta, comprobaciones opcionales: `.\mvnw.cmd clean test`; interfaz real con sesión gráfica: `.\mvnw.cmd -Dqa.ui=true test`; ejecución alternativa `.\mvnw.cmd javafx:run`. Las pruebas usan carpetas temporales, no datos del usuario. Sin qa.ui se omiten explícitamente los recorridos gráficos; no confundir esa omisión con haber comprobado la UI. La vía normal del alumnado es Run/Debug en IntelliJ.

POM: Java 25, JavaFX 25.0.2, Commons CSV 1.14.1, Jackson 2.20.0, SLF4J 2.0.17/Log4j2 2.25.3. Documentación oficial: https://commons.apache.org/proper/commons-csv/apidocs/ ; https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/io/ObjectInputFilter.html ; https://fasterxml.github.io/jackson-databind/javadoc/2.20/ .

## Alcance de esta variante

Sigue las cinco etapas de la guía web: 1, listado en ArchivosRepository.listar; 2, CSV en CsvProductoRepository.leer/guardar e integración de InventarioService.guardarProducto/cargar; 3, JSON en JsonProductoRepository.leer/guardar; 4, BIN en BinProductoRepository.leer/guardar; 5, errores y conservación del estado. Los números TODO del código identifican grupos de trabajo y no coinciden uno a uno con las etapas de la guía. Usa EscrituraProtegida y FiltroSerializacion, ya preparados. Los modelos, validadores de apoyo, interfaz y datos de demostración se entregan resueltos. Los TODO producen un mensaje explícito y no simulan persistencia. Las pruebas por defecto comprueban los apoyos preparados; las fases optativas permiten contrastar los TODO conforme los completes.

## Pruebas optativas de aprendizaje por fases

El proyecto entrega `FasesGuiadoTest` en `src/test/java`. Abre la clase en IntelliJ, crea una configuración JUnit y selecciona en **VM options** `-Dfase=rutas`. Valores disponibles: rutas, csv-lectura, csv-escritura, crud, carga, json, bin. El valor `completo` activa todas las fases de esta clase. Todos los archivos de estas pruebas se crean en directorios temporales propios.

Alternativa con Wrapper: `.\mvnw.cmd '-Dtest=FasesGuiadoTest' '-Dfase=completo' test`. En Unix utiliza `./mvnw`. Sin `fase`, estos casos se omiten; esa omisión no verifica los TODO. En el starter se espera que fallen las fases cuyos métodos siguen pendientes. Los mismos oráculos están en la solución para contrastar los resultados, sin incluir sus algoritmos en el starter.
