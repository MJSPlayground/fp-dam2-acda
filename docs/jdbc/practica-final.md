# Operaciones de almacén

El almacén ya mantiene su catálogo y registra ventas. La encargada necesita localizar los productos que requieren atención, revisar sus precios y registrar las entregas de proveedores. Amplía **el mismo proyecto** en los tres niveles siguientes.

## Preparación

[Descargar proyecto inicial](../descargas/codigo/almacen-final-inicial.zip){ .md-button .md-button--primary download="almacen-final-inicial.zip" }

Descomprime el ZIP y abre su carpeta en IntelliJ como proyecto Maven. Espera a que termine la sincronización y selecciona el JDK indicado en [Ayuda](../ayuda/proyectos-maven.md). Ejecuta `es.mjsplayground.acda.jdbc.almacen.app.Launcher` con Run. La aplicación conserva las funciones resueltas del proyecto guiado y añade la pestaña **Operaciones**: no necesitas modificar la interfaz.

Usa una carpeta de trabajo nueva para esta práctica. La primera ejecución prepara Teclado (10 unidades, 25,90 €), Ratón (20 unidades, 12,95 €) y Monitor (5 unidades, 159,00 €). La base de datos se guarda fuera del código, en `datos/almacen.db` del directorio de trabajo. Cierra la aplicación antes de cambiar de base; puedes seleccionar otra ruta con la opción de VM `-Dalmacen.db=datos/practica.db`.

## Base: localizar y revisar precios

La encargada quiere buscar por una parte del nombre, sin distinguir mayúsculas, y limitar los resultados a los productos cuyas existencias no superen un máximo. Una búsqueda vacía muestra todos los nombres; el límite de existencias nunca puede ser negativo. Los símbolos `%` y `_` deben tratarse como parte del texto buscado, no como comodines.

También necesita revisar el precio de un producto mediante un porcentaje de subida o descuento. Admite porcentajes entre −50 y 100, ambos incluidos, con hasta dos decimales. El nuevo precio se redondea al céntimo más cercano; los medios céntimos se redondean hacia arriba. Nunca puede quedar en cero ni superar 1.000.000 €. No deben cambiar ni el nombre ni las existencias.

Comprueba estos resultados con los datos iniciales:

- Buscar `TE` con máximo 10 encuentra Teclado; buscar `monitor` con máximo 4 no devuelve productos.
- Subir Teclado un 10 % deja su precio en 28,49 € y conserva las 10 unidades.
- Un porcentaje fuera de los límites, un identificador inexistente o un máximo negativo producen un mensaje comprensible sin modificar datos.

## Aplicación: recibir una entrega

Al llegar mercancía se indica producto, unidades recibidas y coste de compra por unidad. Cada entrega debe aumentar las existencias y conservar un justificante consultable con fecha, producto, unidades y coste total. El precio de venta del catálogo permanece igual.

Solo se aceptan productos existentes, unidades enteras positivas y un coste entre 0,01 y 1.000.000 € por unidad. El total y las existencias resultantes deben poder representarse sin desbordamiento. La entrada de mercancía y el justificante se guardan juntos: si falla una escritura, no puede quedar la otra aplicada.

Con Teclado en 10 unidades, recibir 3 a 12,50 € deja 13 unidades y un justificante de 37,50 €. Recibir cero unidades o un producto inexistente no cambia las existencias ni el historial. Reinicia la aplicación y comprueba que ambos cambios se conservan.

## Reto avanzado: una entrega con varios productos

El proveedor puede entregar varios productos a la vez. La encargada prepara las líneas y fija el presupuesto máximo autorizado para el conjunto. Debe conocer el coste total confirmado al terminar.

- La entrega debe contener al menos una línea. Cada producto aparece una sola vez: las repeticiones se rechazan para evitar errores de introducción.
- Se aplican todas las condiciones del nivel anterior. Un presupuesto no positivo, una línea inválida o un coste total superior al presupuesto rechazan la entrega completa.
- El límite de presupuesto es inclusivo: una entrega que cuesta exactamente lo autorizado sí se admite.
- Todos los incrementos de existencias y sus justificantes se confirman juntos. Si falla la última línea o una escritura intermedia, se conserva íntegramente el estado previo.

Con los datos iniciales, recibir 2 teclados a 10,00 € y un monitor a 100,00 € cuesta 120,00 €. Con presupuesto de 120,00 € quedan 12 teclados y 6 monitores, con dos justificantes. Con 119,99 €, no cambia ningún producto ni aparece ningún justificante. Repite con una segunda línea que identifique un producto inexistente: tampoco debe persistir la primera.

## Pruebas preparadas por nivel

No necesitas crear desde cero una avería de disco ni un experimento de atomicidad. El starter incluye `AprendizajeFinalTest`: abre la clase en IntelliJ, crea su configuración JUnit y elige **VM options** `-Daprendizaje.nivel=base`, `aplicacion` o `reto` al completar cada nivel. Sin seleccionar nivel se omiten las pruebas. Cada caso prepara su propio archivo SQLite temporal.

- **Base:** filtros literales, nombre con tilde, redondeo, límites y conservación de campos.
- **Aplicación:** recepción individual y fallo preparado después de escribir el justificante; stock e historial deben volver al estado previo.
- **Reto avanzado:** presupuesto exacto, segunda línea inexistente y fallo técnico en la segunda escritura; comprueba todos los productos y justificantes, no solo la excepción.

El mecanismo de fallo instala un trigger únicamente en esa base temporal. Está incluido como apoyo de prueba: no es una API que debas aprender para resolver la práctica ni se añade al código del almacén. La solución ejecuta la misma suite con sus operaciones completas.

Para resolver los requisitos bastan consultas parametrizadas, resultados, repositorios, colecciones Java, cálculos exactos y transacciones del tema. [Servicios e importes](repositorios.md) explica sumas, redondeo y detección de repeticiones. No se exige `JOIN`, lotes ni un pool: el historial puede obtener sus datos con las lecturas que ya conoces, conservando las responsabilidades por capas.

## Comprobar los resultados

Prueba las funciones anteriores desde **Operaciones** y vuelve a **Catálogo y ventas** para comprobar las existencias actualizadas. Conserva las funciones del guiado: alta, edición, eliminación permitida y venta atómica. Ejecuta las pruebas de servicio preparadas, que distinguen una operación completa de una operación parcialmente guardada, incluyendo un fallo técnico después de la primera escritura. Los avisos de la interfaz no sustituyen las validaciones del servicio.

Los `TODO` de la práctica final están señalados en el backend. La interfaz y la infraestructura de arranque están preparadas. Mientras una función no se implementa, el starter muestra un aviso y no simula que se haya guardado nada.

## Solución completa

[Descargar solución de los tres niveles](../descargas/codigo/almacen-final-solucion.zip){ .md-button download="almacen-final-solucion.zip" }

[Ver código y pruebas en GitHub](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/jdbc/final-solucion)

La solución mantiene el recorrido por capas de [repositorios y servicios](repositorios.md) y aplica las [transacciones](transacciones.md) del tema a las entregas completas.
