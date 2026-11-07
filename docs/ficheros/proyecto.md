# Proyecto guiado: inventario local de productos

La tienda necesita mantener un catálogo y recuperarlo desde archivos. Trabajarás sobre una sola aplicación JavaFX: la interfaz está preparada y el backend avanza con la teoría. No debes modificar vistas, estilos ni controles.

## Preparar y ejecutar

[Descargar proyecto inicial](/fp-dam2-acda/descargas/codigo/inventario-guiado-inicial.zip){ .md-button .md-button--primary download="inventario-guiado-inicial.zip" }

Descomprime el ZIP y abre en IntelliJ la carpeta que contiene `pom.xml`. Espera la sincronización Maven y selecciona el JDK indicado en [preparación de proyectos](../ayuda/proyectos-maven.md). Ejecuta `es.mjsplayground.acda.ficheros.inventario.app.Launcher.main` con Run. No necesitas un servidor ni Docker.

La aplicación muestra Teclado, Ratón y Monitor como **datos en memoria**. Todavía no están guardados por el hecho de aparecer en la tabla. Los TODO pendientes comunican un mensaje explícito, sin simular que se haya persistido una operación.

La carpeta inicial es `datos/inventario`, relativa al directorio de trabajo de Run. Su ruta absoluta aparece en **Ubicación**. Puedes escoger otra carpeta desde la interfaz; los archivos modificables quedan fuera de `src/main/resources`.

**Copiar ejemplos** extrae los recursos `/data/productos.csv`, `/data/productos.json`, `/data/lote.csv` y `/data/lote.json` a la carpeta seleccionada. Usa una carpeta nueva: no se sobrescriben ejemplos que ya existan. Los lotes se utilizarán después en la práctica final, no son un catálogo guiado válido para sustituir el actual.

## Modelo y responsabilidades

```text
Launcher → InventarioApp → InventarioView
                              ↓
                       InventarioService
                              ↓
  ArchivosRepository / CsvProductoRepository / JsonProductoRepository / BinProductoRepository
                              ↓
                         Archivos externos
```

`Producto` contiene código, nombre, unidades y precio. El modelo, la configuración, los validadores de apoyo, el filtro binario y la infraestructura de escritura protegida están preparados. El servicio mantiene el catálogo y sus reglas; los repositorios convierten cada formato. La UI nunca debe llamar directamente a un repositorio para saltarse la validación.

## Etapa 1. Elegir carpeta y listar archivos

Lee [rutas y operaciones de ficheros](rutas.md). Completa el **TODO 1** de `ArchivosRepository.listar`: solo archivos regulares del directorio elegido, filtro por extensión y orden de nombre. No recorras subdirectorios.

La implementación puede utilizar `Files.list`, cuyo flujo se cierra con `try-with-resources`, o el bucle con `DirectoryStream` explicado en teoría. `filter` selecciona elementos de un flujo y `toList` los reúne; no es un `InputStream` de bytes. Conserva el contrato del método y sus errores.

Copia los ejemplos en una carpeta de trabajo y pulsa **Usar carpeta**. Al filtrar CSV deben aparecer los dos archivos CSV de esa carpeta. Una carpeta inaccesible debe producir un error comprensible, no una lista vacía que aparente éxito.

## Etapa 2. Primer recorrido completo con CSV

Lee [texto y CSV](texto-csv.md) y la introducción a [repositorios y servicios](arquitectura.md). Completa el **TODO 2** de `CsvProductoRepository.leer/guardar`.

Para comprobar ya el recorrido desde la interfaz, integra en esta etapa las dos operaciones del **TODO 4** de `InventarioService`: `guardarProducto` y `cargar`. Los validadores de apoyo están preparados. Usa una lista candidata y no sustituyas la actual hasta que todos los datos sean válidos. La misma integración servirá después para JSON y binario.

El catálogo admite como máximo 5.000 productos. Cada uno cumple:

- Código único con 1–20 caracteres entre A–Z, dígitos, guion y guion bajo.
- Nombre con contenido de hasta 100 caracteres, quitando espacios exteriores y conservando comas, comillas y saltos interiores.
- Unidades enteras de 0 a 1.000.000.
- Precio de 0,01 a 1.000.000 euros, con hasta dos decimales, sin redondear entradas inválidas.

El CSV utiliza UTF-8, coma y encabezados exactos `codigo,nombre,unidades,precio` en ese orden. Lee registros con Commons CSV y escribe con `CSVPrinter`. No utilices `split` ni concatenación manual de columnas.

Comprueba **Nuevo → Guardar producto**, **Editar → Cancelar**, edición guardada y eliminación cancelada o confirmada. Al editar, el código conserva la identidad. Después guarda el catálogo como CSV y vuelve a cargar el archivo, confirmando la sustitución. Deben recuperarse todos los valores.

??? tip "Pista: un nombre con coma ocupa una sola columna"

    El parser entrega registros lógicos según las reglas de comillas del formato. Un nombre como `Taza, blanca` no debe dividirse manualmente. Antes de convertir cantidades, comprueba encabezados y consistencia de columnas; utiliza los nombres de encabezado para recuperar los campos.

## Etapa 3. Intercambio JSON

Lee [JSON y Jackson](json.md) y completa el **TODO 2** de `JsonProductoRepository`. La raíz es un array de productos y el mapeo a objetos Java va antes que cualquier necesidad de árbol.

Guarda el mismo catálogo en JSON y cárgalo después. Compara códigos, nombres, cantidades y precios con CSV. Prueba unidades fraccionarias, campo obligatorio ausente y contenido adicional tras el array: no deben interpretarse como un catálogo válido.

La carga guiada es estricta: un error de estructura, un producto inválido o un código repetido rechaza el archivo completo. El estado anterior se conserva, aunque el problema aparezca al final.

## Etapa 4. Recuperación binaria propia

Lee [binarios y serialización](binarios.md) y completa el **TODO 3** de `BinProductoRepository`. Utiliza `ObjectOutputStream` para escribir una `Instantanea` y el ayudante `FiltroSerializacion.abrirSeguro` antes de `readObject`.

Guarda una copia propia en BIN, cambia un producto y vuelve a cargar esa copia. Deben recuperarse todos los productos guardados, tras validar el conjunto. No utilices archivos binarios recibidos de terceros.

Comprueba archivo truncado, objeto raíz distinto y productos inválidos. Un rechazo mantiene el catálogo actual y conserva la causa técnica. El acceso aleatorio se queda en la demostración pequeña `demo.BinariosDemo`; no es un requisito del inventario.

## Etapa 5. Casos límite, errores y estado de la interfaz

Revisa el **TODO 4** ya integrado y sus comprobaciones de negocio. Prueba duplicados, nombre vacío, stock negativo y precio con demasiados decimales. Comprueba también que una carga inválida no sustituye parcialmente la memoria.

Guardar un archivo existente requiere confirmación. Cancelarla no modifica el archivo ni el catálogo. Al guardar un producto, el formulario se cierra y se limpia; al cancelar una edición se conserva el dato previo. Filtrar la tabla no elimina los productos que quedan fuera de la vista.

Las excepciones técnicas conservan su causa y se registran con SLF4J/Log4j2; la interfaz muestra un mensaje breve. Utiliza [diagnóstico y pruebas](../ayuda/diagnostico.md) para inspeccionar el problema y [arquitectura](arquitectura.md) para comprobar la responsabilidad de cada capa.

## Registro personal del recorrido

<div class="mjs-progress-panel" role="status" aria-live="polite"><span class="mjs-progress-label">Tu avance en el proyecto</span><strong class="mjs-progress-count">0 de 5 pasos</strong><div class="mjs-progress-track"><div class="mjs-progress-fill"></div></div></div>

<div class="mjs-task"><input type="checkbox" id="inventario-etapa-1"><label for="inventario-etapa-1">Puedo listar los archivos directos de una carpeta y distinguir una ruta de su contenido.</label></div>

<div class="mjs-task"><input type="checkbox" id="inventario-etapa-2"><label for="inventario-etapa-2">El mantenimiento de productos y el recorrido CSV funcionan sin perder comas, comillas ni saltos.</label></div>

<div class="mjs-task"><input type="checkbox" id="inventario-etapa-3"><label for="inventario-etapa-3">JSON conserva los mismos productos y rechaza entradas incompatibles.</label></div>

<div class="mjs-task"><input type="checkbox" id="inventario-etapa-4"><label for="inventario-etapa-4">Recupero una copia binaria propia mediante el filtro preparado.</label></div>

<div class="mjs-task"><input type="checkbox" id="inventario-etapa-5"><label for="inventario-etapa-5">Los errores y las cancelaciones conservan el estado anterior; he contrastado las pruebas y la interfaz.</label></div>

Puedes desmarcar una etapa para volver a comprobarla. El avance se conserva en este navegador.

## Solución completa

[Descargar solución del guiado](/fp-dam2-acda/descargas/codigo/inventario-guiado-solucion.zip){ .md-button download="inventario-guiado-solucion.zip" }

[Ver código, README y pruebas](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/ficheros/guiado-solucion)

La solución utiliza la misma interfaz y entrada `app.Launcher`. Las pruebas de requisitos completos permiten contrastar escenarios; las del starter comprueban los apoyos preparados. Antes de comenzar [la práctica final](practica-final.md), compara especialmente la validación completa antes de sustituir y la conservación de causas.
