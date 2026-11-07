# Práctica de bloque: conversor multiformato

Una cooperativa mantiene una lista de clientes (`id`, `nombre`, `ciudad`, `correo` opcional) y recibe archivos CSV, XML o JSON. Necesita convertir cualquiera de esos formatos a cualquiera de los otros dos. La salida debe contener los mismos clientes válidos, ordenados por identificador, sin añadir ni perder caracteres especiales.

La aplicación recibe formato y ruta de origen, formato y ruta de destino. Debe comunicar formato desconocido, archivo ilegible, estructura incorrecta y errores de registros por separado. Un registro con `id` o `nombre` vacío se rechaza y se informa; los registros válidos se conservan. Los `id` duplicados conservan la primera aparición. Si la estructura del archivo impide reconocer registros, no reemplaces un destino previo.

Comprueba una conversión circular CSV → XML → JSON → CSV, nombres con comas, comillas y `&`, un campo opcional vacío, duplicados y una entrada mal formada. Describe en el README la representación de campos opcionales en cada formato y compara los datos normalizados, no el espaciado del archivo.

[Solución pública](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/ficheros/solucion).
