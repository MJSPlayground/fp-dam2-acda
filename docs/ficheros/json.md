# JSON y objetos Java con Jackson

CSV representa filas con columnas. JSON permite expresar objetos, listas y valores con estructura explícita. Lo utilizaremos para intercambiar el mismo catálogo de productos, manteniendo sus reglas de negocio.

## Objetos, arrays y tipos

Un objeto agrupa campos con nombre; un array conserva una secuencia de elementos. En JSON las claves y cadenas utilizan comillas dobles. Los números no llevan comillas y el separador decimal es el punto.

```json
[
  {"codigo":"P01","nombre":"Taza, blanca","unidades":3,"precio":4.50},
  {"codigo":"P02","nombre":"Carpeta Azul","unidades":10,"precio":2.75}
]
```

La raíz es un array con dos objetos. Cada objeto representa un producto. La coma dentro del nombre no necesita el mecanismo de comillas de CSV porque ya está dentro de una cadena JSON.

La [RFC 8259](https://www.rfc-editor.org/rfc/rfc8259) define su sintaxis: no admite comentarios, cadenas con comillas simples ni una coma sobrante antes del cierre. `null`, cadena vacía y campo ausente son situaciones distintas; ninguna debe convertirse silenciosamente en un producto válido si faltan datos obligatorios.

## Del objeto JSON al objeto Java

El modelo Java expresa los campos que esperamos. Este record reducido coincide con la información intercambiada:

```java
public record Producto(String codigo, String nombre, int unidades, BigDecimal precio) {}
```

Jackson conoce los componentes del record y puede construirlo a partir de nombres de campo. El proyecto utiliza Jackson 2 y el paquete `com.fasterxml.jackson.databind`; las versiones e imports se conservan según su POM, sin mezclarlos con ejemplos de otra versión mayor. Consulta el [proyecto oficial Jackson Databind](https://github.com/FasterXML/jackson-databind).

`ObjectMapper` es el objeto encargado de convertir entre JSON y Java. No contiene las reglas del almacén: construir un record con `unidades = -1` sigue siendo posible si no se valida después.

## Leer un catálogo completo

Si el contrato es conocido, podemos leer directamente un array de productos. Este fragmento supone un mapper configurado para el contrato y un `Path origen`:

```java
Producto[] productos;
try (Reader lector = Files.newBufferedReader(origen, StandardCharsets.UTF_8)) {
    productos = mapper.readValue(lector, Producto[].class);
}
```

`Producto[].class` indica que la raíz debe convertirse en un array de ese tipo. Para leer un solo objeto se utilizaría `Producto.class`, que es otro contrato. Los objetos resultantes todavía deben pasar por la validación del servicio.

El catálogo no se sustituye mientras se está leyendo. Primero se construye el conjunto candidato, después se comprueba entero y solo entonces se aplica. Si un objeto del final es inválido, el usuario conserva su catálogo anterior.

## Configurar para no ocultar errores

Las conversiones permisivas pueden parecer cómodas y, a la vez, ocultar datos defectuosos. Para este catálogo no aceptamos un decimal que se trunque como unidades ni un campo obligatorio omitido que termine en cero.

En Jackson 2 pueden configurarse estas opciones, según el modelo utilizado:

| Opción | Intención |
| --- | --- |
| Desactivar `ACCEPT_FLOAT_AS_INT` | Rechazar unidades como `1.5` |
| Activar `FAIL_ON_MISSING_CREATOR_PROPERTIES` | Detectar componentes obligatorios ausentes del record |
| Activar `FAIL_ON_NULL_FOR_PRIMITIVES` | Evitar que `null` se convierta en cero para un `int` |
| Activar `FAIL_ON_TRAILING_TOKENS` | Rechazar contenido adicional después del catálogo |

La configuración preparada y las pruebas del proyecto concretan el contrato. No basta con que el JSON sea sintácticamente válido: tiene que poder interpretarse como productos completos y válidos. Las propiedades desconocidas tampoco deben utilizarse para introducir datos ajenos al formato anunciado.

Un número decimal se maneja como `BigDecimal`, construido a partir de su representación decimal. Eso evita los errores binarios de `double` en importes. El servicio comprueba intervalo y precisión; no redondea una entrada inválida sin avisar.

## Escribir los objetos como JSON

`writeValue` realiza la conversión contraria. Un escritor con formato legible añade sangrías para facilitar la inspección; no cambia el significado de los datos:

```java
try (Writer escritor = Files.newBufferedWriter(destino,
        StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
    mapper.writerWithDefaultPrettyPrinter().writeValue(escritor, productos);
}
```

El ejemplo crea un archivo nuevo; si existe, falla en lugar de truncarlo. La exportación del proyecto utiliza su ayudante de escritura segura y las confirmaciones de la interfaz. La validación debe realizarse antes de generar el archivo, no después de reemplazar un destino válido.

Guardar → abrir de nuevo permite comparar códigos, nombres, cantidades e importes. Los espacios de formato del JSON no tienen por qué coincidir byte a byte para representar el mismo catálogo.

## Anidamiento y árbol: solo cuando se necesita

JSON también puede anidar un objeto dentro de otro. Por ejemplo, un informe distinto del catálogo podría utilizar esta forma:

```json
{"origen":{"nombre":"proveedor"},"aceptados":2,"incidencias":[]}
```

Si siempre conociéramos ese contrato, podríamos crear records anidados y seguir mapeándolos como objetos Java. Un árbol `JsonNode` resulta útil cuando necesitamos inspeccionar una estructura variable o distinguir explícitamente tipos antes de decidir la conversión.

```java
JsonNode raiz = mapper.readTree(lector);
JsonNode aceptados = raiz.path("aceptados");
if (!aceptados.isIntegralNumber() || !aceptados.canConvertToInt()) {
    throw new IOException("El informe no contiene un número entero de aceptados válido");
}
int cantidad = aceptados.intValue();
```

Es un fragmento ilustrativo de otro formato, no una segunda manera obligatoria de implementar el catálogo. Primero comprobamos tipo y rango; convertir con un valor predeterminado podría disimular un campo inexistente. No se necesita construir todo el proyecto con árboles si sus productos tienen un modelo fijo.

## Errores y comprobaciones

Distingue tres problemas: no se puede abrir el archivo, su JSON está mal formado o sus productos incumplen el contrato. La respuesta al usuario debe indicar qué ocurrió y conservar la causa técnica para el registro.

Completa el **TODO 2** de `JsonProductoRepository` y compara el recorrido con CSV. Prueba raíz de tipo incorrecto, unidades fraccionarias, un campo obligatorio ausente, código repetido e importe fuera de rango. En el guiado cualquier carga inválida debe conservar el catálogo anterior.

Continúa con [binarios y serialización](binarios.md).

## Comprobar una operación cada vez

En `model/repository/JsonProductoRepository.java`, comienza por `leer(Path)`: abre el lector, utiliza el mapper preparado para obtener `Producto[]` y devuelve su lista. Después completa `guardar` escribiendo la lista mediante el mismo mapper y la infraestructura de escritura entregada. No necesitas `JsonNode` para este catálogo.

Antes del recorrido gráfico, ejecuta la fase `json` de `FasesGuiadoTest` según [la guía](proyecto.md#comprobaciones-optativas-por-fase). El test contrasta el producto completo, incluido un nombre con salto, y rechaza unidades fuera del rango de `int`. Después usa el servicio y verifica que el mismo error mantiene los datos anteriores: leer bien y aceptar un catálogo son responsabilidades distintas.
