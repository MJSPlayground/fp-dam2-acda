# Ampliación

Estas explicaciones profundizan en formatos, procesamiento y conservación de datos. Las demostraciones se incluyen en el proyecto solución, dentro de `demo.AmpliacionDemo`; no añaden otra práctica ni exigen cambiar el frontend.

## XML y modelo de árbol DOM

XML representa información mediante elementos, atributos y texto. Por ejemplo:

```xml
<productos>
  <producto><nombre>Taza, blanca</nombre></producto>
  <producto><nombre>Caja &amp; soporte</nombre></producto>
</productos>
```

`productos` es la raíz. Cada `producto` contiene un elemento `nombre`. `&amp;` representa el carácter `&`; concatenar manualmente texto sin escapar puede producir XML inválido.

DOM representa el documento como un árbol en memoria. `Document` es el documento completo; `Element`, un elemento; `Node`, el tipo común de los nodos. Los espacios y saltos de formato también pueden producir nodos de texto, de modo que no se debe convertir cada hijo directamente a `Element`.

Un parser se obtiene a través de `DocumentBuilderFactory`. Después de configurarlo de forma segura, `parse` lee el archivo y `getDocumentElement` recupera su raíz. La [API de DocumentBuilderFactory](https://docs.oracle.com/en/java/javase/25/docs/api/java.xml/javax/xml/parsers/DocumentBuilderFactory.html) y el [modelo Document](https://docs.oracle.com/en/java/javase/25/docs/api/java.xml/org/w3c/dom/Document.html) describen esas responsabilidades.

La demostración recorre hijos directos y comprueba etiquetas:

```java
for (Node nodo = raiz.getFirstChild(); nodo != null; nodo = nodo.getNextSibling()) {
    if (nodo instanceof Element producto && producto.getTagName().equals("producto")) {
        // Examinar sus hijos y recuperar el elemento nombre.
    }
}
```

`getTextContent()` obtiene el texto del elemento. Buscar indiscriminadamente todos los descendientes con la misma etiqueta puede mezclar niveles que significan cosas distintas; el recorrido debe corresponder al contrato.

### Leer XML sin resolver contenido externo

Un XML puede declarar entidades que apuntan a archivos o direcciones externas. La demostración no necesita DTD, entidades externas ni XInclude; los desactiva y restringe el acceso externo antes de construir el parser.

No basta con capturar una excepción después de que el parser haya intentado acceder al recurso. La configuración debe rechazar ese comportamiento desde el principio. Si una protección requerida no está disponible, el ejemplo falla en lugar de continuar con una configuración insegura. La [guía JAXP de seguridad](https://docs.oracle.com/en/java/javase/25/security/java-api-xml-processing-jaxp-security-guide.html) explica XXE y las restricciones de recursos externos.

En `AmpliacionDemo`, `fabricaSegura` prepara esas opciones. `leerNombresXml` valida la raíz, recupera nombres y rechaza contenido incompatible. Una prueba utiliza un DOCTYPE con una entidad externa y comprueba el rechazo conservando la causa; no se presenta ese ensayo como garantía sobre cualquier XML o entorno.

### Construir, escribir y transformar

Para generar DOM se crean nodos, se incorpora su texto y se enlazan al padre:

```java
Element producto = documento.createElement("producto");
Element nombre = documento.createElement("nombre");
nombre.setTextContent("Caja & soporte");
producto.appendChild(nombre);
raiz.appendChild(producto);
```

`setTextContent` evita tener que escapar manualmente cada carácter al construir el árbol. Un `Transformer` escribe ese DOM como XML; su configuración también restringe accesos externos y utiliza UTF-8.

La demostración `nombresXmlACsv` transforma los nombres a un CSV de una columna mediante `CSVPrinter`, conservando comas y comillas. Es un ejemplo de transformación entre representaciones, **no un importador completo de productos**: el XML reducido no incluye unidades ni precios.

[Ver la demostración completa de XML y sus protecciones](https://github.com/MJSPlayground/fp-dam2-acda/blob/main/proyectos/ficheros/guiado-solucion/src/main/java/es/mjsplayground/acda/ficheros/inventario/demo/AmpliacionDemo.java)

## Procesamiento streaming de archivos grandes

Cargar una lista completa es cómodo cuando el catálogo está acotado. Para un archivo muy grande, puede interesar procesar un registro y escribir su resultado antes de pasar al siguiente, sin conservar todos en memoria.

La lectura con `BufferedReader` permite hacerlo con texto por líneas. Para CSV se mantiene el parser de registros lógicos: no se vuelve a `split` ni se supone que cada línea física sea un producto.

`AmpliacionDemo.filtrarCsv` recorre el parser, comprueba las unidades y escribe únicamente los registros que alcanzan un mínimo. Conserva el encabezado y los campos originales, incluidos nombres con comas o saltos de línea.

```java
long aceptados = AmpliacionDemo.filtrarCsv(origen, destinoNuevo, 5);
```

El destino debe ser nuevo y el método devuelve cuántos registros escribió. En caso de una fila inválida, comunica el error; una salida nueva parcialmente escrita no se considera una exportación completa. Para proteger un destino definitivo habría que generar primero un temporal y publicar el resultado únicamente al terminar.

Streaming describe el modo de procesamiento, no garantiza que cualquier biblioteca o algoritmo use memoria constante. Guardar todos los registros en una lista dentro del bucle anularía la ventaja. La demostración comprueba registros con coma y salto de línea; no afirma una medición de rendimiento sobre millones de productos.

## Observar cambios con WatchService

`WatchService` informa de cambios en directorios registrados. No lee automáticamente los productos ni garantiza que un archivo esté listo para importarse al recibir un evento.

El recorrido es: crear el servicio, registrar el directorio, esperar una clave, procesar sus eventos, reiniciar la clave y cerrar el servicio al terminar. Estos métodos de la demostración encapsulan ese ciclo:

```java
try (WatchService vigilancia = AmpliacionDemo.registrar(carpeta)) {
    List<AmpliacionDemo.Cambio> cambios = AmpliacionDemo.esperarEventos(vigilancia, 3000);
}
```

El tiempo está expresado en milisegundos. Sin una clave dentro del intervalo devuelve una lista vacía, en vez de esperar indefinidamente. Cada cambio identifica tipo y ruta relativa cuando existe.

Los eventos pueden agruparse, repetirse o perderse. `OVERFLOW` indica que hay que volver a examinar el estado; `reset()` informa si la clave sigue siendo válida. No se cuenta un evento como si representara exactamente una operación de usuario ni se presupone el mismo orden en todas las plataformas. Consulta la [referencia de WatchService](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/WatchService.html).

La prueba de la demostración registra primero el directorio y después crea un archivo temporal, comprobando un evento de creación. Ese resultado en el sistema probado no demuestra la misma secuencia exacta en todos los sistemas de archivos. La interfaz gráfica no debe bloquear su hilo de JavaFX esperando eventos; necesitaría coordinación de trabajo en segundo plano y actualización de la UI.

## Escrituras fiables con temporales

Abrir directamente un archivo definitivo para escribir puede truncar la versión anterior antes de terminar la nueva. El patrón de temporal separa generación y sustitución:

1. Crear un temporal en el mismo directorio del destino.
2. Generar y cerrar su contenido completo.
3. Intentar moverlo al destino con `ATOMIC_MOVE` y la política de sustitución elegida.
4. Limpiar el temporal si deja de ser necesario.

`AmpliacionDemo.escribirFiable` muestra ese patrón con texto UTF-8. Devuelve si utilizó movimiento atómico. Si el proveedor no lo admite, su alternativa realiza un reemplazo normal y **no promete la misma garantía de atomicidad**.

```java
boolean atomico = AmpliacionDemo.escribirFiable(destino, "Inventario revisado");
```

La prueba comprueba que se conserva el nuevo contenido y que no quedan temporales después del caso correcto. No fuerza todos los fallos de almacenamiento ni simula un corte eléctrico. Un movimiento atómico no sustituye copias de recuperación ni garantiza por sí solo durabilidad frente a cualquier fallo físico.

En el proyecto, `EscrituraProtegida` entrega esta infraestructura ya preparada. El alumno implementa el contenido CSV, JSON o binario sin tener que programar antes todo el protocolo de sustitución.

## Recuperación y decisiones de negocio

Una copia de recuperación debe representar un estado completo y validado. En la práctica final se guarda antes de aplicar una conciliación: si la copia falla, no se cambia el catálogo en memoria. Recuperarla también exige leer y validar todo antes de sustituir el estado actual.

No es lo mismo una copia deliberada de recuperación que un temporal abandonado por una ejecución interrumpida. La aplicación debe identificar cuál utilizar y pedir una acción expresa, sin importar automáticamente cualquier `.tmp` que encuentre. Tampoco se deserializan copias de procedencia desconocida.

## Ejecutar y contrastar los ejemplos

En el proyecto solución, ejecuta `demo.AmpliacionDemo.main` desde IntelliJ. Crea una carpeta temporal propia, genera y transforma XML, realiza una escritura y registra los eventos observados. El log muestra la ruta para inspeccionar sus archivos; no modifica tu catálogo.

La demostración breve de binarios se ejecuta con `demo.BinariosDemo.main`. Las pruebas `AmpliacionTest` utilizan carpetas temporales independientes para comprobar formatos, límites, rechazo de DOCTYPE, streaming, observación y escritura. Los comandos opcionales están en el README de la solución.
