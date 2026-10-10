# Transformación de formatos y aplicación

Una conversión fiable necesita más que un lector y un escritor. La **lectura** interpreta la sintaxis del origen; la **validación** decide qué registros son admisibles; la **transformación** calcula o reorganiza información; la **escritura** genera el destino. Si una clase mezcla todo, un error de una fila puede destruir la salida completa o dejar un informe ambiguo.

## Un modelo común

Un importador CSV, XML o JSON puede producir la misma colección de `Cliente` u otro modelo de dominio. Un servicio valida identificadores, cantidades y duplicados; un exportador CSV, XML o JSON recibe solo registros válidos. Así se puede cambiar el formato de entrada sin duplicar reglas. El DAO o adaptador de formato conoce la estructura del archivo; el servicio conoce las reglas comerciales; la interfaz conoce la interacción con el usuario.

```text
Archivo → importador → registros candidatos → servicio de validación
                                            ├→ válidos → cálculo → exportador → destino
                                            └→ incidencias con origen y número de registro
```

Si un registro tiene `precio="abc"`, conserva los anteriores que sean válidos y registra una incidencia que señale archivo, registro y campo. No sustituyas ese precio por cero de forma silenciosa. En cambio, un CSV con comillas sin cerrar puede impedir que el parser identifique el siguiente registro; informa de que la lectura estructural falló y evita prometer una recuperación imposible.

## Codificación y errores parciales

La codificación pertenece al contrato de entrada, no a una regla de negocio. Si el productor suministra ISO-8859-1, configura explícitamente ese charset; no uses la codificación por defecto del sistema. Para archivos recibidos, verifica cabeceras y tamaño antes de procesar y registra cuántos registros se aceptaron, rechazaron y exportaron. El destino debe escribirse en una ruta externa distinta del origen, salvo que exista una política de reemplazo fiable.

Un fallo durante la escritura puede dejar un archivo parcial. Para resultados importantes escribe primero en un temporal del mismo directorio y reemplaza al final; la [ampliación de escrituras fiables](ampliacion-escrituras-fiables.md) explica el soporte de `ATOMIC_MOVE`, limpieza y recuperación. Un archivo de incidencias también puede fallar: informa al usuario y no declares éxito total.

## Diseñar la operación

El flujo comienza identificando formatos y rutas, después importa, valida y transforma en memoria si el volumen lo permite. La exportación se hace solo con una colección coherente. Para entradas grandes, usa [procesamiento streaming](ampliacion-streaming.md) y una estrategia que no obligue a guardar todos los registros.

La [práctica del bloque](practica-conversor.md) pide un conversor multiformato. Las [prácticas finales](final-clientes.md) plantean encargos con requisitos funcionales más abiertos.
