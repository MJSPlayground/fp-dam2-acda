# Guiada: guardar y modificar registros binarios

Una aplicación guarda existencias por identificador numérico. El archivo tendrá una cabecera `int` con versión `1` y registros de **12 bytes**: `int id` y `long unidades`. Este contrato permite calcular la posición exacta de un registro.

1. Escribe la cabecera y tres registros con `DataOutputStream`. Registra por escrito el orden de campos y no uses `writeUTF`, porque sus cadenas tienen tamaño variable.
2. Lee con `DataInputStream`. Comprueba la versión y, mientras haya registros completos, muestra `id` y `unidades`. Un archivo con 2 bytes finales debe notificarse como truncado, no aceptarse silenciosamente.
3. Abre `RandomAccessFile` en modo `rw`. Para actualizar el registro de índice `1`, calcula `4 + 12L * 1`; valida que el desplazamiento y los 12 bytes caben en `length()`. Lee el `id`, cambia `unidades` y vuelve a leer el registro.

```java
long posicion = Integer.BYTES + 12L * indice;
if (indice < 0 || posicion + 12L > raf.length()) {
    throw new IllegalArgumentException("Registro fuera del archivo");
}
raf.seek(posicion + Integer.BYTES);
raf.writeLong(nuevasUnidades);
```

El fragmento modifica solo el campo `unidades`; presupone que `raf` está abierto y que se comprobó que el identificador de ese índice es el esperado. Repite la lectura tras cerrar y reabrir el archivo: el cambio debe persistir. Si `raf.length() - 4` no es múltiplo de 12, rechaza el formato antes de permitir modificaciones.

??? tip "Comprobación del desplazamiento"
    El primer registro empieza en el byte 4, el segundo en el 16 y el tercero en el 28. El campo `unidades` comienza cuatro bytes después del inicio de cada registro.

La [solución pública](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/ficheros/solucion) incluye el contrato y pruebas. Continúa con el [informe autónomo](practica-informe-binario.md).
