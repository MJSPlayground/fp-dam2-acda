# Nuevas funciones del inventario

La tienda ya puede mantener su catálogo y cargarlo o guardarlo en CSV, JSON y binario. Ahora necesita entregar selecciones a otras personas, conciliar novedades de proveedores y conservar una recuperación antes de aplicar cambios.

Amplía el mismo proyecto en los tres niveles siguientes. La interfaz y los controles nuevos están preparados; el trabajo corresponde al backend.

## Preparación

[Descargar proyecto inicial](/fp-dam2-acda/descargas/codigo/inventario-final-inicial.zip){ .md-button .md-button--primary download="inventario-final-inicial.zip" }

Abre la carpeta Maven en IntelliJ, espera la sincronización y ejecuta `es.mjsplayground.acda.ficheros.inventario.app.Launcher.main`. El proyecto parte de la solución del guiado y conserva su catálogo inicial. La [guía de preparación](proyecto.md#preparar-y-ejecutar) explica importación y Run.

Copia los ejemplos desde la interfaz a una carpeta nueva y trabaja con archivos externos. Los recursos originales del classpath no se modifican. La recuperación binaria solo utiliza archivos propios generados por la aplicación, nunca archivos de procedencia desconocida.

## Base: exportar una selección

La encargada necesita enviar únicamente los productos que cumplen un texto de búsqueda y un mínimo de existencias. La búsqueda afecta al código y al nombre y no distingue mayúsculas. El umbral es inclusivo: un mínimo de diez incluye también los productos que tienen exactamente diez unidades.

El mínimo admitido está entre 0 y 1.000.000. El texto vacío no limita por nombre o código. La selección puede exportarse a CSV, JSON o binario, manteniendo los mismos campos y validaciones del catálogo. Exportar no altera los productos en memoria.

Con los datos iniciales, un mínimo de diez y búsqueda vacía debe exportar Teclado y Ratón; Monitor queda fuera. Una búsqueda `rat` selecciona Ratón. Una selección sin coincidencias produce un archivo válido de catálogo vacío, no una simulación de error técnico.

Comprueba los archivos volviéndolos a leer en otro escenario de trabajo, sin confundir la exportación de una selección con la sustitución de tu catálogo actual. Un fallo de escritura conserva el catálogo y no se anuncia como éxito.

## Aplicación: conciliar un lote de novedades

Un proveedor envía un lote CSV o JSON. La encargada quiere incorporarlo al catálogo existente, conservando los datos que no se puedan conciliar y recibiendo un informe de incidencias.

Las reglas son:

- Cada producto entrante debe cumplir las condiciones de código, nombre, unidades y precio del guiado.
- Un código nuevo y válido se añade, siempre que el catálogo no supere 5.000 productos.
- Un código existente solo se concilia si su nombre coincide exactamente tras quitar espacios exteriores. Las mayúsculas y los acentos siguen siendo significativos.
- Cuando coincide, se suman las existencias y se utiliza el precio válido del lote. Si la suma supera 1.000.000, se rechaza esa novedad y se conserva el producto anterior completo.
- Si el mismo código aparece varias veces dentro del lote, se rechazan **todas** sus apariciones. No se elige la primera ni la última para resolver una ambigüedad.
- Una fila inválida o incompatible genera una incidencia y no impide estudiar otras filas bien formadas. Si el archivo no puede interpretarse estructuralmente, se rechaza el análisis completo sin modificar el catálogo.

Las incidencias indican fila, código cuando pueda identificarse y motivo. La numeración empieza en uno para los datos; la fila de encabezados CSV no cuenta como producto. Un nombre con comas, comillas o salto de línea debe conservarse, sin deformar sus columnas.

Con el lote preparado, partiendo del catálogo inicial:

- P01, Teclado: pasa de 10 a 12 unidades y de 25,90 € a 24,90 €.
- P04, Cable: se incorpora con 3 unidades y precio de 5,90 €.
- P02 conserva su nombre y datos anteriores porque el lote propone otro nombre para el mismo código.
- Las dos apariciones de DUP y el producto de stock negativo se descartan con sus incidencias.

El resultado previsto tiene cuatro productos y cuatro incidencias. Las filas rechazadas no borran ni sustituyen productos anteriores.

## Reto avanzado: previsualizar, aplicar y recuperar

Antes de confirmar, la encargada quiere ver el catálogo resultante y sus incidencias **sin modificar** el actual. Puede revisar esa propuesta y decidir si la aplica.

Si el catálogo cambia después de previsualizar —por ejemplo, se edita un producto, se elimina o se carga otro archivo—, la propuesta queda desactualizada. Debe rechazarse su aplicación y pedir otra previsualización. No se reemplaza el estado nuevo con una propuesta calculada sobre el anterior.

Cuando la propuesta siga vigente, antes de aplicarla se guarda una copia binaria completa del catálogo anterior en un archivo nuevo de recuperación. Si falla esa copia, no se aplica ningún cambio. Un archivo de recuperación ya existente no se sobrescribe ni se elimina como solución al problema.

La aplicación permite recuperar después esa copia propia. Se lee y valida todo su contenido antes de sustituir el catálogo: archivo truncado, clases no permitidas o productos inválidos deben dejar intacto el estado actual.

Las incidencias de la previsualización pueden exportarse a CSV con encabezados `fila,codigo,motivo`, UTF-8 y comillas correctas. Un informe con comas en sus motivos debe seguir siendo importable como tres columnas.

Comprueba este recorrido:

1. Previsualiza el lote y compara la propuesta con los cuatro productos y cuatro incidencias esperados; la tabla actual mantiene los tres productos originales.
2. Edita un producto e intenta aplicar esa propuesta anterior: debe rechazarse sin guardar cambios parciales.
3. Vuelve a partir del catálogo inicial, previsualiza de nuevo y aplica con una ruta nueva de recuperación.
4. Recupera esa copia: vuelven los tres productos y valores anteriores a la conciliación.
5. Prueba un destino no escribible o una recuperación ya existente: el intento fallido no aplica la propuesta.
6. Exporta las incidencias y comprueba su estructura y contenido.

La versión de la propuesta coordina el catálogo en memoria de esa ejecución. No es un bloqueo de archivos entre varias aplicaciones ni un sistema de sincronización con proveedores.

## Comprobación del conjunto

Conserva los flujos del guiado: altas, edición, filtrado, eliminación, carga y exportación de catálogo completo. Cancelar una confirmación no debe cambiar datos ni crear una copia de recuperación.

Comprueba también la causa de los errores técnicos, las actualizaciones de la tabla y la limpieza del formulario. Las pruebas deben comparar el estado anterior y posterior a un fallo; una excepción por sí sola no demuestra que se preservaron los productos.

El starter contiene TODO de las funciones nuevas, sin sus algoritmos resueltos. El protocolo de escritura protegida y el filtro de deserialización son infraestructura entregada; no hay que desarrollar un nuevo frontend ni un sistema de acceso aleatorio para resolver la práctica.

## Solución completa

[Descargar solución de los tres niveles](/fp-dam2-acda/descargas/codigo/inventario-final-solucion.zip){ .md-button download="inventario-final-solucion.zip" }

[Ver código, README y pruebas](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/ficheros/final-solucion)

La solución separa lectura técnica, conciliación, validación y presentación. Contrasta los casos rechazados y la recuperación además del ejemplo correcto.
