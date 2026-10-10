# Práctica de bloque: fusionar y filtrar CSV

Dos sucursales entregan CSV UTF-8 con `id,nombre,ciudad,importe`. Debes generar un único archivo con los registros de una ciudad solicitada, sin duplicados por `id`. Cuando un identificador aparece en ambos archivos, prevalece el registro del primero y se registra la duplicación.

Un importe debe ser decimal positivo y una ciudad puede incluir coma si está entrecomillada. Descarta filas sin `id` o `nombre`, con importe inválido o con columnas incorrectas; conserva las demás. La salida usa el mismo encabezado y ordena por `id`. Además, genera un resumen con aceptados, rechazados, duplicados y suma exacta de importes filtrados. Un encabezado obligatorio ausente invalida ese archivo completo y debe informarse.

Los datos de prueba han de incluir dos lugares de la misma ciudad, una ciudad entrecomillada, un importe vacío, un duplicado y una fila con comillas. Comprueba que cambiar el orden de los archivos cambia únicamente el registro que prevalece en caso de duplicado.

[Solución pública](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/ficheros/solucion).
