# Final intermedia: importador y exportador de inventario

> Tenemos dos almacenes que entregan ficheros de existencias. Queremos consultar un inventario combinado, conocer el valor de nuestras unidades y exportar los productos que necesitan reposición. Cada responsable debe poder entregar sus datos en CSV o JSON, y la aplicación debe guardar el informe solicitado en una carpeta de trabajo.

Cada registro contiene código, nombre, almacén, unidades y precio unitario. Si el mismo código aparece en ambos almacenes, sus existencias se suman solo si nombre y precio coinciden; en otro caso debe notificarse un conflicto sin alterar el producto ya aceptado. Las unidades no pueden ser negativas y el precio debe ser positivo. Se considera reposición cuando el total de unidades de un código es inferior a un umbral elegido por el usuario.

La consulta muestra códigos ordenados, unidades combinadas, precio y valor (`unidades × precio`) con precisión decimal. El informe de reposición puede exportarse a CSV o JSON y contiene también el valor total de los productos seleccionados. Deben aparecer por separado registros rechazados, conflictos y archivos no legibles. Si un archivo falla, el otro puede seguir aportando registros válidos; el resumen debe indicar qué fuentes se procesaron.

Prueba un código presente en ambos almacenes, un conflicto de precio, dos productos bajo el umbral, un campo CSV con coma, un archivo inexistente y un JSON con un registro inválido entre dos válidos. Si la exportación falla, no presentes el resultado como guardado.

[Solución pública](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/ficheros/solucion)
