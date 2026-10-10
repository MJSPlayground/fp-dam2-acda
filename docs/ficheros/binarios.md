# Ficheros binarios

En un fichero binario los bytes siguen un **contrato de formato**. El programa que lee debe conocer el orden y el tamaño de cada campo. [`InputStream`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/io/InputStream.html) y [`OutputStream`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/io/OutputStream.html) mueven bytes sin interpretar. `Files.newInputStream` y `Files.newOutputStream` los conectan a rutas externas. Para copiar un archivo grande se usa un búfer y se respeta el número real de bytes devuelto por `read`, que puede ser menor que el tamaño del array; `-1` indica fin.

[`DataOutputStream`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/io/DataOutputStream.html) añade métodos como `writeInt`, `writeLong`, `writeDouble` y `writeUTF`; [`DataInputStream`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/io/DataInputStream.html) debe llamarlos en el mismo orden. `writeUTF` utiliza una variante propia de UTF-8 con longitud limitada: úsalo solo cuando ambos extremos acepten exactamente este contrato. Un `int` ocupa cuatro bytes y un `long`, ocho; una cadena con `writeUTF` tiene longitud variable. `EOFException` durante un registro indica archivo truncado o lectura fuera del formato.

```java
try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(destino))) {
    out.writeInt(1);       // versión del formato
    out.writeInt(42);      // identificador
    out.writeLong(1250);   // céntimos, sin redondeos de coma flotante
}
```

El archivo anterior ocupa 16 bytes. Al leerlo, comprueba primero la versión, luego llama a `readInt`, `readInt` y `readLong`; intercambiar el orden produce valores incorrectos sin que Java pueda adivinarlo. Documenta versión, orden, tamaños y codificación antes de distribuir un formato propio.

## Acceso aleatorio

[`RandomAccessFile`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/io/RandomAccessFile.html) mantiene un puntero de posición. `seek(desplazamiento)` mueve el puntero y `getFilePointer()` permite inspeccionarlo. Es útil para modificar un registro sin reescribir todo el archivo **si todos los registros tienen longitud fija**. Por ejemplo, una cabecera de 4 bytes y registros de 12 bytes colocan el registro `i` en `4 + 12L * i`. `writeUTF` no sirve para esos registros porque su longitud depende del texto. Antes de `seek`, valida `i`, calcula con `long` para evitar desbordamientos y comprueba que la posición cabe dentro de `length()`.

```java
try (RandomAccessFile raf = new RandomAccessFile(archivo.toFile(), "rw")) {
    long posicion = 4L + 12L * indice;
    if (indice < 0 || posicion + 12L > raf.length()) throw new IllegalArgumentException("Índice fuera de rango");
    raf.seek(posicion);
    int id = raf.readInt();
    long centimos = raf.readLong();
}
```

Este fragmento lee un registro; la [guiada](guiada-binarios.md) crea y actualiza uno. No uses acceso aleatorio cuando el formato contiene campos variables sin un índice adicional.

## Serialización de objetos Java

[`Serializable`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/io/Serializable.html) marca objetos que `ObjectOutputStream` puede escribir y `ObjectInputStream` reconstruir. Declara `private static final long serialVersionUID = 1L` para identificar la versión compatible de la clase. La serialización Java acopla los datos a la forma de las clases; suele ser peor contrato de intercambio que CSV, XML o JSON.

**No deserialices archivos no confiables.** La reconstrucción puede activar comportamiento de clases presentes en el proceso. Si un caso interno exige leer datos serializados, controla su procedencia y aplica [`ObjectInputFilter`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/io/ObjectInputFilter.html) con límites de clases, profundidad y tamaño. Un filtro reduce superficie de ataque, pero no convierte una fuente arbitraria en segura. Para intercambio con terceros, usa un formato explícito y validación de dominio.

La demostración [`SerializacionDemo`](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/ficheros/solucion) escribe una `Ficha` propia y solo vuelve a leer el archivo que acaba de crear. Antes de `readObject()` instala un filtro que limita profundidad, referencias, bytes y clases permitidas; después comprueba el tipo devuelto. Si cambias la forma de `Ficha` sin mantener compatibilidad, la lectura puede fallar con `InvalidClassException`. No uses ese ejemplo para aceptar archivos enviados por terceros.
