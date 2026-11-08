# Depurar errores y comprobar resultados

Cuando una operación falla, conserva el mensaje y el estado de partida. «No funciona» no permite distinguir una regla de negocio de una conexión que no se abrió. Anota qué proyecto ejecutas, qué datos introdujiste, qué esperabas y qué ocurrió realmente.

## Leer una excepción

La primera línea identifica el tipo y el mensaje. Después aparecen las llamadas que llevaron al fallo. Busca la primera línea de tu propio paquete y abre el archivo y la línea indicados. Una sección `Caused by` explica un error que otra capa ha envuelto; no te quedes únicamente con el mensaje exterior.

En JDBC, por ejemplo, un nombre repetido puede terminar como `PersistenciaException` causada por una `SQLException`. El servicio comunica el fallo a la interfaz y el registro conserva el detalle técnico. Una cantidad negativa, en cambio, es una entrada rechazada por la regla de negocio: no se arregla reiniciando la base de datos.

No sustituyas una excepción por una lista vacía o un mensaje de éxito. Si la consulta falló, no sabes si existen registros.

## Usar el depurador

1. Coloca un breakpoint en el método de servicio que ejecutará la acción.
2. Ejecuta **Debug** sobre la configuración de la aplicación y repite la entrada que falló.
3. Observa los parámetros y variables antes de avanzar. ¿El identificador corresponde a un registro? ¿La cantidad tiene el valor esperado? ¿Los céntimos se convirtieron correctamente?
4. Usa **Step Over** para seguir cada instrucción y **Step Into** si necesitas entrar en tu repositorio. Continúa con **Resume Program** cuando hayas entendido esa parte.
5. Repite con un caso correcto y compara el camino. Una corrección que arregla el error pero rompe el caso correcto no está terminada.

En una venta, comprueba que el descuento y el historial utilizan la misma conexión y que el fallo conduce al rollback. No modifiques datos de producción para experimentar: usa las bases temporales de los tests o una ruta nueva de prueba. La [ayuda del depurador de IntelliJ](https://www.jetbrains.com/help/idea/debugging-code.html) explica sus paneles y acciones.

## Leer los registros

Nuestros proyectos usan **SLF4J** como API de logging y **Log4j2** como implementación. En los proyectos JavaFX, la configuración preparada está en `src/main/resources/log4j2.xml`; en los proyectos Spring Boot se ajusta mediante `application.properties` y los perfiles indicados en su README. No necesitas reemplazar ese soporte para completar un TODO.

Los niveles separan detalle técnico (`DEBUG`), acontecimientos normales (`INFO`), situaciones que merecen atención (`WARN`) y fallos (`ERROR`). Por ejemplo, una venta confirmada puede registrarse en INFO; una consulta que falla debe conservar la excepción como causa en el registro técnico. No incluyas contraseñas, tokens ni claves privadas.

Un logger no sustituye la propagación del error: registrar un fallo y devolver éxito sigue siendo incorrecto. Usa parámetros de logging para insertar valores, y conserva el objeto de excepción al registrar un fallo técnico. Consulta la [configuración oficial de Log4j2](https://logging.apache.org/log4j/2.x/manual/configuration) y su [integración con SLF4J](https://logging.apache.org/log4j/2.x/manual/installation.html).

## Ejecutar e interpretar las pruebas

Abre `src/test/java`, localiza la clase de prueba indicada y pulsa Run en su margen. Los proyectos ya incluyen la dependencia JUnit: no añadas otra versión desde un asistente del IDE. También puedes ejecutar el ciclo **test** desde la ventana Maven para comprobar todo el proyecto.

Un test establece un estado inicial, ejecuta una acción y compara el resultado. En el almacén, importa comprobar tanto el total de venta como las existencias y el historial después de una operación rechazada. Una aserción fallida muestra el resultado esperado y el obtenido; una excepción no esperada puede indicar un error de preparación o de implementación.

Una prueba **omitida** no ha pasado: no se ejecutó. Las pruebas JavaFX de los proyectos JDBC se habilitan con la propiedad `qa.ui=true` en un entorno gráfico; sus README documentan cómo hacerlo. Los tests de persistencia trabajan con bases temporales, no con el catálogo que editas en la aplicación.

Los tests no demuestran por sí solos que una interfaz sea cómoda ni que la explicación sea suficiente. Complementa sus resultados con el recorrido manual de la práctica. Para entender la ejecución en el IDE, consulta la [guía de JUnit en IntelliJ](https://www.jetbrains.com/help/idea/junit.html).
