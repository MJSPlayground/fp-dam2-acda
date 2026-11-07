# Binarios, acceso aleatorio y serialización

Un archivo de texto codifica caracteres. Un binario utiliza bytes según un contrato que conocen quien escribe y quien lee. La extensión `.bin` no explica ese contrato ni hace que cualquier contenido pueda recuperarse como un objeto Java.

El proyecto utilizará **serialización de objetos** como mecanismo binario principal para guardar su catálogo propio. Primero comprenderemos los flujos y una demostración pequeña de formato explícito.

## Flujos de bytes

`InputStream` permite leer bytes; `OutputStream`, escribirlos. `Files.newInputStream` y `Files.newOutputStream` conectan esos flujos a una ruta. No son lectores o escritores de caracteres: para texto necesitamos además una codificación.

Un bloque de lectura puede devolver menos bytes que la capacidad del búfer. Este fragmento copia exactamente los bytes leídos a un destino nuevo:

```java
try (InputStream entrada = Files.newInputStream(origen);
     OutputStream salida = Files.newOutputStream(destino, StandardOpenOption.CREATE_NEW)) {
    byte[] bloque = new byte[4096];
    int leidos;
    while ((leidos = entrada.read(bloque)) != -1) {
        salida.write(bloque, 0, leidos);
    }
}
```

`-1` señala el fin. Escribir siempre los 4096 bytes añadiría datos sobrantes en el último bloque. El fragmento supone rutas externas y tratamiento de `IOException`; no interpreta productos ni valida su formato. Consulta las referencias de [InputStream](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/io/InputStream.html) y [OutputStream](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/io/OutputStream.html).

## DataInputStream y DataOutputStream

Estos flujos añaden métodos para tipos primitivos. Un programa puede acordar un formato con versión, identificador y precio en céntimos:

```java
try (DataOutputStream salida = new DataOutputStream(
        Files.newOutputStream(archivo, StandardOpenOption.CREATE_NEW))) {
    salida.writeInt(1);      // Versión del formato: 4 bytes.
    salida.writeInt(42);     // Identificador: 4 bytes.
    salida.writeLong(1250);  // Céntimos: 8 bytes.
}
```

El archivo contiene 16 bytes. Para leerlo hay que llamar a `readInt`, `readInt` y `readLong` en ese orden, comprobando la versión. Leer todo como texto no recupera esa estructura. La [API de DataInputStream](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/io/DataInputStream.html) define los métodos de lectura compatibles.

`EOFException` antes de completar el registro indica que faltan bytes. No debe confundirse un archivo truncado con un catálogo vacío. `writeUTF` también tiene un contrato específico y longitud variable; no representa un campo fijo de texto de tamaño arbitrario.

## RandomAccessFile: una demostración de posición

Cuando los registros tienen tamaño fijo, se puede calcular dónde empieza uno sin leer todos los anteriores. Imaginemos una cabecera de cuatro bytes y registros de doce bytes: un `int` y un `long`.

```java
try (RandomAccessFile acceso = new RandomAccessFile(archivo.toFile(), "r")) {
    int indice = 0;
    long posicion = 4L + 12L * indice;
    if (indice < 0 || posicion + 12L > acceso.length()) {
        throw new IOException("No existe ese registro completo");
    }
    acceso.seek(posicion);
    int id = acceso.readInt();
    long centimos = acceso.readLong();
}
```

Si se utiliza el archivo de la demostración anterior, el índice cero permite leer `42` y `1250`. `seek` desplaza el puntero; las lecturas lo avanzan. El modo `r` no permite escribir; `rw` permite modificar y debe utilizarse solo cuando esa sea la intención. La [referencia de RandomAccessFile](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/io/RandomAccessFile.html) detalla sus operaciones.

Una cadena de longitud variable rompe ese cálculo de posiciones. Por eso la demostración usa campos de tamaño fijo. **No vas a construir un subsistema de registros aleatorios en el inventario**: su persistencia binaria se resuelve con serialización.

## Serializable: conservar un grafo de objetos propio

`Serializable` es una interfaz marcadora: no exige escribir métodos. Permite que `ObjectOutputStream` represente el estado serializable de un objeto y sus componentes. Las clases implicadas deben ser compatibles con esa operación.

En el proyecto, `Producto` y `Instantanea` son serializables. `Instantanea` agrupa un array de productos; así la lectura espera un único objeto raíz con un tipo definido. Esta forma reducida muestra la idea:

```java
public record Instantanea(Producto[] productos) implements Serializable {
    private static final long serialVersionUID = 1L;
}
```

`Producto` también implementa la interfaz, y sus campos utilizan tipos serializables. `serialVersionUID` identifica compatibilidad de la clase; no migra automáticamente archivos antiguos cuando cambia el modelo. Cambios incompatibles pueden impedir recuperar un archivo. Consulta la [referencia de Serializable](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/io/Serializable.html).

## Escribir y leer la instantánea

Una vez validado el catálogo, se crea la instantánea y se escribe con `writeObject`. Este fragmento didáctico crea un archivo nuevo:

```java
Instantanea estado = new Instantanea(productos.toArray(Producto[]::new));
try (ObjectOutputStream salida = new ObjectOutputStream(
        Files.newOutputStream(archivo, StandardOpenOption.CREATE_NEW))) {
    salida.writeObject(estado);
}
```

El resultado no es un JSON comprimido ni un formato recomendado para intercambiar datos con cualquier aplicación. Es un archivo propio, acoplado a clases Java. Para intercambio con terceros utilizamos CSV o JSON y sus validaciones.

Para leer, el proyecto entrega un ayudante que instala el filtro antes de reconstruir objetos:

```java
try (ObjectInputStream entrada = FiltroSerializacion.abrirSeguro(archivo)) {
    Object recibido = entrada.readObject();
    if (!(recibido instanceof Instantanea estado)) {
        throw new IOException("El archivo no contiene una instantánea del inventario");
    }
    Producto[] candidatos = estado.productos();
    // El servicio validará todos los productos antes de sustituir el catálogo.
}
```

Este fragmento muestra el núcleo de la lectura. El repositorio completo comprueba además que el array exista y que no haya un segundo objeto o contenido sobrante. `readObject` puede lanzar `ClassNotFoundException` además de errores de entrada; al convertirlos en un error del proyecto se conserva su causa.

## Filtro y procedencia son controles distintos

No se deben deserializar archivos de procedencia desconocida. Reconstruir objetos puede activar comportamiento de clases; una lista de clases permitidas reduce riesgos, pero no convierte cualquier archivo recibido en confiable.

`FiltroSerializacion` está preparado y restringe clases, profundidad, referencias, bytes y tamaños de arrays. Solo se usa para copias generadas y controladas por esta aplicación. La [documentación de ObjectInputFilter](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/io/ObjectInputFilter.html) explica esos límites.

El filtro comprueba la estructura de objetos, no las reglas del almacén: un producto con stock negativo sigue siendo inválido aunque pertenezca a una clase permitida. Por eso la recuperación solo sustituye el catálogo después de validar todo el contenido.

## Etapa y comprobaciones

Completa el **TODO 3** de `BinProductoRepository` utilizando el ayudante seguro. Guarda un catálogo propio, modifica el estado en memoria y vuelve a cargarlo; deben recuperarse todos los campos originales.

Comprueba archivo truncado, tipo raíz incorrecto, clases no permitidas y catálogo semánticamente inválido. En todos esos fallos el estado actual debe mantenerse. La [arquitectura y gestión de errores](arquitectura.md) une estas operaciones con CSV, JSON y la interfaz.
