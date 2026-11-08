# Ampliación del catálogo

La biblioteca mantiene editoriales, autores, libros y detalles. Ahora necesita registrar nuevas ediciones con menos trabajo, trasladar parte de un catálogo y reunir editoriales sin perder sus relaciones.

Amplía el mismo proyecto con los tres niveles siguientes. La interfaz y los controles están preparados; los algoritmos nuevos pertenecen al backend.

## Preparación

[Descargar proyecto inicial](/fp-dam2-acda/descargas/codigo/biblioteca-final-inicial.zip){ .md-button .md-button--primary download="biblioteca-final-inicial.zip" }

Abre la carpeta Maven y ejecuta `config.MySqlLocal.main` con Docker disponible; después, `app.Launcher.main`. Sigue [el arranque del guiado](proyecto.md#preparar-y-ejecutar) para seleccionar JDK y configuraciones Run.

Esta práctica utiliza `biblioteca_final`, separado de `biblioteca_guiado`. El backend del guiado se entrega resuelto. Los datos de demostración solo se incorporan de forma expresa cuando el catálogo está vacío; reiniciar no debe sobrescribir el trabajo.

## Base: nueva edición y filtros

La encargada quiere crear una ficha nueva tomando como punto de partida un libro existente. Introduce un ISBN de trece cifras que aún no esté usado y un título nuevo válido. La ficha nueva conserva precio, editorial y autores del original.

Si el original tiene detalle de idioma y páginas, la copia recibe los mismos valores en un detalle **independiente**. Modificar o retirar después el detalle de una ficha no debe alterar el de la otra. Si el original no tiene detalle, la copia tampoco lo inventa.

La duplicación crea un libro con identidad nueva y conserva el original completo. Un libro de origen inexistente, ISBN repetido o título inválido se rechaza sin dejar una ficha parcial.

La encargada también necesita filtrar por autor y por un intervalo de precios. Ambos extremos son opcionales e inclusivos; cuando se indican deben estar entre 0 y 10.000 euros, con hasta dos decimales, y el mínimo no puede superar al máximo. Un resultado sin coincidencias es una lista vacía.

Comprueba una copia de `Java y datos` con otro ISBN y título. Deben aparecer dos libros distintos con los mismos autores y editorial; sus detalles tienen identidades distintas. Prueba después un filtro por uno de esos autores y un intervalo que incluya exactamente su precio.

## Aplicación: trasladar una selección

Una editorial recibe entre uno y cien libros seleccionados de otras editoriales y aplica un descuento común. La encargada indica destino y porcentaje entre 0 y 30, con hasta dos decimales.

Cada libro debe existir, aparecer una sola vez en la selección y proceder de una editorial distinta del destino. Si alguno ya pertenece al destino, se rechaza el conjunto para evitar aplicar otra vez el descuento por un traslado accidentalmente repetido.

El nuevo precio se redondea al céntimo más cercano, con los empates hacia arriba para los importes positivos. Nunca puede quedar por debajo de 0,01 €. Los ISBN, títulos, autores y detalles se conservan.

Todos los cambios de editorial y precio se confirman juntos. Si falla una validación o escritura, ningún libro de la selección debe quedar trasladado o rebajado parcialmente. No se elimina la editorial anterior por haber dejado de tener libros.

Por ejemplo, trasladar un libro de 29,90 € con descuento del 10 % deja su precio en 26,91 € y cambia únicamente su editorial y precio. Repite con dos libros válidos y después con un segundo identificador inexistente o repetido: el intento rechazado conserva el estado previo de ambos.

## Reto avanzado: fusionar editoriales

La biblioteca quiere reunir una editorial de origen en otra de destino y dar un nombre final al catálogo receptor. Deben existir ambas editoriales y ser distintas. El nombre final tiene entre 1 y 80 caracteres y respeta la unicidad de nombres.

Antes de fusionar, todos los libros de origen deben tener su ficha de idioma y páginas completa. Es una condición de la revisión del catálogo que se incorpora: si falta alguna, la encargada la completa antes de confirmar la fusión.

La operación debe:

- Trasladar todos los libros de origen al destino.
- Conservar precios, ISBN, títulos, autores y detalles de cada libro.
- Eliminar la editorial de origen y aplicar el nombre final al destino.
- Confirmar el conjunto una sola vez o conservar íntegramente el estado anterior si algo falla.

Esta fusión no aplica descuentos adicionales. Los autores compartidos permanecen en el catálogo; retirar una editorial no autoriza a borrar sus libros ni sus autores.

El nombre original de la editorial que desaparece puede utilizarse como nombre final del destino. En cambio, no puede usarse el nombre de una tercera editorial que permanece. Un fallo de esa restricción debe revertir también traslados y eliminaciones previos de la misma operación.

Con los datos de demostración, Horizonte tiene un libro con detalle completo y Norte tiene el otro. Fusionar Horizonte en Norte con nombre final `Biblioteca Unida` deja los dos libros bajo el destino, elimina Horizonte y conserva a Alex Ríos y Luz Vega. Sus precios y detalles permanecen iguales.

Comprueba también origen igual a destino, editorial inexistente, un libro de origen sin detalle y nombre final ocupado por una tercera editorial. Después de cada rechazo consulta desde una operación nueva: los datos originales deben seguir presentes y relacionados.

## Verificación del conjunto

La UI está preparada para copiar fichas, filtrar, trasladar y fusionar. No debes escribir nuevos formularios para resolver las reglas. El starter conserva los flujos del guiado y señala las funciones nuevas como pendientes.

Comprueba creación y edición de libros, cambios de autores, retirada de detalle y borrados permitidos después de añadir las funciones. Una copia de detalle no se verifica solo comparando el texto: hay que observar también su identidad y que modificar una no afecta a la otra.

Las pruebas transaccionales deben provocar también un fallo técnico después de alguna escritura y comprobar que se conserva la causa y se revierte todo. Una excepción antes de modificar nada no demuestra por sí sola el rollback de una operación parcialmente ejecutada.

Los joins, estadísticas de rendimiento y SQL nativo de [Ampliación](ampliacion.md) pueden servir de consulta, pero no se exige implementar técnicas adicionales para completar estos niveles. Las reglas se resuelven con las entidades, repositorios, servicios y transacciones ya enseñados.

## Solución completa

### Pruebas disponibles desde el starter

En IntelliJ ejecuta la clase `FasesFinalTest`, situada en `src/test/java/es/mjsplayground/acda/hibernate/biblioteca`, con **VM options** `-Dqa.mysql=true -Dfase=base`. Cambia la fase por `aplicacion` o `reto`; `completo` activa las tres. El README explica las variables de conexión si tu MySQL no usa el puerto de desarrollo. `ApoyoFasesMysql` crea datos conocidos en un esquema UUID exclusivo y lo elimina al terminar; no vacía la biblioteca de trabajo. Los algoritmos nuevos siguen pendientes en el starter.

Base comprueba que la edición nueva tiene otra identidad de detalle y conserva autores y ficha original. Aplicación contrasta un traslado inválido con otro que transforma 29,90 € en 26,91 €. Reto prepara automáticamente una tercera editorial con el nombre solicitado: la restricción de unicidad fuerza un fallo real al final de la operación. La comprobación vuelve a consultar en otra sesión y exige editorial de origen y libro intactos; después contrasta una fusión válida. No tienes que provocar una caída del servidor ni inventar datos para reproducir el fallo.

Comando opcional: `./mvnw -Dtest=FasesFinalTest -Dqa.mysql=true -Dfase=reto test` (Windows: `mvnw.cmd`). Completa además los casos del enunciado: libro sin detalle, IDs repetidos, límite de descuento, filtros y cancelaciones. Que una prueba se omita por falta del flag o de la fase no significa que el nivel esté verificado.

[Descargar solución de los tres niveles](/fp-dam2-acda/descargas/codigo/biblioteca-final-solucion.zip){ .md-button download="biblioteca-final-solucion.zip" }

[Ver código, README y pruebas](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/hibernate/final-solucion)

La solución mantiene la propiedad de las asociaciones y sus ciclos de vida. Contrasta las pruebas de conservación y reversión junto al caso correcto.
