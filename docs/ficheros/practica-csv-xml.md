# Práctica de bloque: CSV a XML

Una empresa recibe `id,nombre,ciudad` y quiere un documento `<clientes>` con un elemento `<cliente id="…">` por fila aceptada. `nombre` y `ciudad` son elementos hijos. El resultado debe ser XML UTF-8 que otro programa pueda analizar, incluso si un nombre contiene `&` o `<`.

Rechaza identificadores o nombres vacíos, encabezados ausentes y filas con columnas incompletas. No exportes registros rechazados; entrega un resumen con número de fila y motivo. Dos filas con el mismo `id` son duplicadas: conserva la primera. Para una entrada completamente inválida, genera un documento con raíz válida y cero clientes solo si la estructura CSV pudo leerse; si no pudo, informa de que la conversión no se completó.

Prueba al menos un campo entrecomillado con coma, una ciudad vacía, un nombre con `&`, un duplicado y un encabezado erróneo. Vuelve a analizar el XML producido para comprobar que es válido.

[Solución pública](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/ficheros/solucion).
