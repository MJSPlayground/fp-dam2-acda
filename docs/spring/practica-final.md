# Práctica final: reglas y servicios del catálogo

El catálogo necesita atender solicitudes de compra sin perder el control de existencias. El cliente quiere conocer el importe antes de confirmar y evitar que un fallo deje algunos libros descontados sin registrar el pedido. La web incorpora las pantallas de estas funciones; desarrolla únicamente su comportamiento de backend.

## Punto de partida

El starter conserva la **solución completa del guiado**: editoriales, libros, consultas, reglas, perfiles, eventos y transacciones. Añade modelos, formularios y controladores preparados para las nuevas funciones. Sus operaciones pendientes deben comunicar que aún no están implementadas; no representan pedidos guardados.

[Descargar práctica inicial](/fp-dam2-acda/descargas/codigo/catalogo-final-inicial.zip){ .md-button .md-button--primary download="catalogo-final-inicial.zip" }

Ábrela como proyecto Maven en IntelliJ. Utiliza las instrucciones de [arranque del guiado](proyecto.md#preparar-y-ejecutar), pero con la carpeta de esta práctica. La base `spring_final` es distinta de `spring_guiado`, para trabajar sin modificar los datos del guiado. No hace falta editar Thymeleaf, HTML, CSS, JavaScript ni los controladores de UI.

## Base: reposición de existencias

> «Cuando llega material, queremos añadir entre 1 y 100 unidades a un libro. Debemos ver sus existencias actuales y conservar los demás datos. Si alguien modificó el libro desde que abrimos el formulario, necesitamos actualizar la información antes de continuar. Nunca se puede superar el máximo de stock configurado».

El nuevo caso de uso debe funcionar con cualquier libro existente. Una cantidad negativa, cero, superior a 100 o incompatible con el límite de stock debe rechazarse sin cambios. Un libro eliminado y una versión antigua también deben producir un mensaje comprensible.

Comprueba un caso permitido, uno que alcanza exactamente el máximo y otro que lo supera en una unidad. Cambia el perfil a `practica` para comprobar que el límite procede de la configuración y no de un número repetido en el código.

## Aplicación: presupuesto sin reservar stock

> «Queremos seleccionar libros y cantidades y obtener un presupuesto detallado antes de comprar. El presupuesto debe usar precios actuales y comprobar las existencias. A partir de 100 euros de subtotal, inclusive, ofrecemos un 5 % de descuento. Consultar un presupuesto no debe reservar material ni alterar el catálogo».

La solicitud admite entre 1 y 20 líneas, con libros distintos y cantidades entre 1 y 100. Cada línea identifica también la versión consultada; una versión obsoleta debe rechazarse. No se admiten libros inexistentes ni cantidades superiores al stock disponible.

La respuesta debe incluir las líneas, subtotal, descuento e importe final, con importes monetarios de dos decimales y redondeo al céntimo más cercano, con los empates hacia arriba. Es un resultado inmutable: consultar o mostrar la respuesta no debe permitir modificar posteriormente la operación calculada.

Comprueba subtotal inferior a 100, exactamente 100 y superior a 100; incluye precios que obliguen a redondear el descuento. Verifica que ni el presupuesto correcto ni uno rechazado cambian existencias o guardan pedidos. El presupuesto describe el estado consultado: no garantiza que el stock siga disponible minutos después.

## Reto avanzado: confirmar un pedido completo

> «Al confirmar, queremos descontar las existencias y conservar un pedido con sus líneas e importes. Si falta un libro, cambia una versión, no queda material suficiente o falla el guardado, no debe quedar ninguna parte aplicada. Cada confirmación llevará una clave única para impedir registrar dos veces la misma operación».

La clave contiene entre 1 y 60 letras ASCII, dígitos, guiones o guiones bajos (`[A-Za-z0-9_-]{1,60}`), sin espacios. Distingue mayúsculas de minúsculas. Se mantienen las reglas de la solicitud del presupuesto. La confirmación verifica de nuevo los datos: no puede confiar en que un presupuesto anterior siga vigente.

El pedido conserva una instantánea de sus líneas e importes, para que una modificación posterior del título o del precio del catálogo no cambie lo vendido. Todas las disminuciones de stock y el registro completo deben confirmarse juntos. Si una segunda confirmación utiliza una clave ya registrada, debe comunicar el conflicto y no volver a descontar existencias.

Comprueba un pedido con varios libros y una repetición de clave. Provoca también un conflicto o fallo que aparezca cuando ya se han iniciado cambios y vuelve a consultar desde una operación independiente: no basta con que se muestre un error, deben conservarse todos los stocks anteriores y no existir un pedido parcial. Los avisos de éxito se emitirán únicamente tras la confirmación efectiva.

## Cómo comprobar cada nivel con datos aislados

Utiliza las operaciones y el CRUD ya resuelto desde la web. Para los casos que necesitan un fallo intermedio o dos ejecuciones simultáneas, el starter incluye `AprendizajeFinalTest`: no tienes que programar el mecanismo de fallo ni modificar el frontend. En IntelliJ crea una configuración JUnit de esa clase y añade en **VM options** `-Daprendizaje.nivel=base`, `aplicacion` o `reto`. MySQL debe estar disponible; la prueba crea su propio esquema temporal, sin usar tu catálogo.

| Nivel | Qué observa la prueba preparada |
| --- | --- |
| Base | Reposición que alcanza 50 bajo el perfil de práctica y rechazo de una versión antigua o del exceso posterior. |
| Aplicación | Presupuesto, descuento y ausencia de cambios persistidos. |
| Reto avanzado | Repetición exacta de clave tras escribir stock: rollback completo; dos confirmaciones concurrentes: una sola operación efectiva; historial consultable. |

Los datos se reconstruyen para cada caso: no encadenes expectativas que partan de estados distintos. Las suites están desactivadas por defecto y se activan cuando has completado el nivel. Una prueba omitida o una compilación correcta no demuestran el requisito. Sus aserciones expresan resultados del cliente y no contienen la implementación de las operaciones.

Los modelos, la consulta de bloqueo, el reloj y el observador de eventos ya están preparados. Los conceptos que necesitas están en [repositorios](persistencia.md), [servicios e importes](servicios.md) y [transacciones/versiones](transacciones.md). El reto combina esos conocimientos; no exige configurar mensajería, seguridad ni APIs de Ampliación.

## Entrega funcional y comprobación

Recorre las pantallas con datos correctos e incorrectos, utiliza los perfiles preparados y ejecuta las pruebas desde IntelliJ según el README. Conserva el CRUD y las búsquedas del guiado. No sustituyas una excepción por una lista vacía o un mensaje de éxito: el cliente necesita distinguir un rechazo de negocio de un fallo técnico.

La [teoría de servicios](servicios.md) explica la colaboración entre componentes; [transacciones y conflictos](transacciones.md) desarrolla las garantías necesarias. Los requisitos describen el resultado esperado: decide cómo distribuir las nuevas reglas entre los componentes existentes y los nuevos servicios sin crear dependencias circulares.

[Descargar solución completa](/fp-dam2-acda/descargas/codigo/catalogo-final-solucion.zip){ .md-button download="catalogo-final-solucion.zip" }

La solución comprende los tres niveles y su README identifica el arranque y las pruebas.
