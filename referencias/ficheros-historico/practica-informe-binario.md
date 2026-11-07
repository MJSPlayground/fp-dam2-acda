# Práctica de bloque: informe de registros binarios

Recibes el formato de la [guiada de registros](guiada-binarios.md): versión `int` y registros de 12 bytes con `id` y `unidades`. Genera un informe CSV o JSON con identificador, existencias y clasificación (`sin stock`, `bajo` para 1–9, `normal` para 10 o más). Añade total de unidades, número de registros por categoría e identificador con mayor existencia. En empate, elige el menor identificador.

Rechaza una versión desconocida, una longitud no múltiplo de 12 y existencias negativas. Detecta identificadores duplicados; no los sumes como si fueran productos distintos. Ninguna salida anterior debe sobrescribirse si la entrada es inválida. Comprueba archivo vacío, cabecera incompleta, registro truncado, valores límite 0/1/9/10 y dos máximos empatados.

[Solución pública](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/ficheros/solucion).
