# Final fácil: conversor de datos de clientes

> Nuestra asociación recibe una lista de clientes en CSV y debe enviarla a un colaborador que solo acepta JSON. Queremos poder elegir ambos archivos al ejecutar la herramienta. Necesitamos saber cuántos clientes se han convertido y cuáles se han descartado.

El CSV de entrada contiene `id,nombre,ciudad,correo`. Cada identificador debe ser único; el primero gana si se repite. Un nombre no puede estar vacío, la ciudad puede estar vacía y el correo debe contener un `@` con texto a ambos lados. El JSON de salida es un array de clientes aceptados, ordenado por identificador. Un campo entrecomillado con coma debe conservarse íntegro. La aplicación informa número de aceptados, rechazados y duplicados, con la fila y el motivo de cada incidencia.

Se entrega una muestra con un nombre con coma, un correo inválido, un duplicado y un registro válido posterior al error. La salida debe conservar ese último registro. Una entrada inexistente o un CSV estructuralmente ilegible no deben reemplazar un JSON anterior. El programa escribe en una carpeta externa elegida por el usuario.

**Comprobación de entrega:** el JSON puede volver a leerse como array, los clientes válidos mantienen sus valores y un segundo uso con otra carpeta no depende del directorio del profesor.

[Solución pública](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/ficheros/solucion)
