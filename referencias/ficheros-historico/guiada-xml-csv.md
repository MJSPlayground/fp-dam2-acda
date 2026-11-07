# Guiada: convertir XML en CSV

Un proveedor entrega `<clientes><cliente id="…"><nombre>…</nombre><ciudad>…</ciudad></cliente></clientes>`. El resultado pedido es un CSV UTF-8 con encabezados `id,nombre,ciudad`. Esta guía une el [DOM seguro](xml.md) y [CSVPrinter](csv.md).

1. Abre el XML con la configuración segura contra DTD y entidades externas explicada en [XML](xml.md). Comprueba que la raíz se llama `clientes`. Intenta un archivo con `DOCTYPE`: debe rechazarse antes de recorrer nodos.
2. Selecciona los elementos `cliente`. Para cada uno, lee `id` y los hijos directos `nombre` y `ciudad`; registra una incidencia si falta un campo obligatorio. Un `NodeList` puede contener nodos de texto con espacios, así que comprueba `getNodeType() == Node.ELEMENT_NODE` al recorrer hijos.
3. Abre `CSVPrinter` en una ruta externa y escribe solo registros válidos. Usa `setHeader("id", "nombre", "ciudad")`; no concatenes comas manualmente. Introduce una ciudad con coma y comprueba que sale entrecomillada.

??? tip "Comprobación intermedia"
    Muestra el número de elementos `cliente` antes de exportar y compara después `válidos + rechazados` con ese número. Así podrás localizar si el error ocurrió al analizar el XML o al validar un registro.

La salida debe poder reimportarse con el dialecto CSV acordado sin cambiar nombres o ciudades. La [solución pública](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/ficheros/solucion) incluye un XML válido y uno con estructura incorrecta. En la [práctica autónoma](practica-csv-xml.md) construirás el documento inverso.
