# Práctica de bloque: transformar datos anidados

Un proveedor ofrece productos en JSON, con `codigo`, `nombre`, `categoria` y un array `precios` cuyos elementos contienen `tarifa` e `importe`. Otro proveedor envía un XML equivalente. Se necesita un CSV plano con una fila por combinación válida de producto y tarifa, filtrable por categoría. Debe poder reconstruirse un JSON con productos y precios agrupados a partir del CSV exportado.

Un producto sin código o categoría se rechaza. Un precio ausente, negativo o sin tarifa se rechaza sin perder los otros precios del mismo producto. Para un mismo código y tarifa, conserva la primera aparición y registra el duplicado. El informe indica productos leídos, precios válidos, precios rechazados y filas exportadas. Usa decimales exactos; no redondees valores de origen sin una regla explícita.

Prueba un producto con dos tarifas, categorías repetidas, texto con coma y comillas, y un precio inválido entre dos válidos. Compara JSON y XML de entrada equivalentes: deben producir el mismo CSV normalizado.

[Solución pública](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/ficheros/solucion).
