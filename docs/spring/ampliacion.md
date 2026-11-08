# Ampliación

Aquí observamos cómo se conecta el backend con la web, cómo se comprueba su comportamiento y cómo se ejecuta fuera de IntelliJ. Las plantillas y controladores siguen entregados: estudiar su funcionamiento no convierte el frontend en una tarea de la práctica.

## Spring MVC y Thymeleaf

**HTTP** es el intercambio de peticiones y respuestas entre navegador y servidor. Una petición incluye un método y una ruta. `GET` solicita una representación, como un listado; `POST` envía una acción, como guardar un formulario. Los cambios de negocio no deben ejecutarse al seguir un enlace `GET`.

En Spring MVC, un `@Controller` recibe la petición, llama al servicio y añade datos a un **modelo de la vista**. Devuelve el nombre de una plantilla. Thymeleaf combina ese HTML con los datos y el servidor envía el resultado al navegador. El modelo de la vista no es el mismo concepto que la entidad persistente.

Este ejemplo reducido representa un listado; se estudia dentro de la aplicación preparada, no como nueva clase que deba añadirse al proyecto:

```java
@GetMapping("/libros")
String listado(@RequestParam(defaultValue = "") String texto, Model modelo) {
    modelo.addAttribute("libros", catalogo.listar(texto, null));
    return "libros";
}
```

Una petición `/libros?texto=java` aporta el filtro. El servicio devuelve datos preparados para la interfaz y el nombre `libros` selecciona `templates/libros.html`. El método de un `@Controller` que devuelve `String` no devuelve automáticamente JSON. `@RestController` tiene otro propósito y no es necesario para esta interfaz MVC. Véase [MVC en Spring Boot](https://docs.spring.io/spring-boot/3.5/reference/web/servlet.html).

En una plantilla, `th:each` repite un elemento y `th:text` escribe texto escapado. El ejemplo muestra una propiedad del listado:

```html
<tr th:each="libro : ${libros}">
  <td th:text="${libro.titulo}">Título de ejemplo</td>
</tr>
```

Un título introducido por el usuario se muestra como texto, no como HTML ejecutable. No reemplaces `th:text` por `th:utext` para mostrar datos del catálogo: este último permite contenido sin escapar. La [guía oficial de integración Thymeleaf y Spring](https://www.thymeleaf.org/doc/tutorials/3.1/thymeleafspring.html) desarrolla modelos, formularios y expresiones.

## Formularios, listados y mensajes

Un formulario reúne valores con nombres de campo. Al enviarlo, Spring intenta convertirlos a los tipos de su objeto de entrada: un texto no numérico no puede convertirse a `Integer`. Ese error de entrada se detecta antes de comprobar si el stock supera el máximo del negocio.

En el controlador preparado identifica tres pasos: recibir el objeto del formulario, comprobar los errores de conversión o validación y llamar al servicio cuando la entrada sea válida. Con Jakarta Validation, anotaciones como `@NotBlank` o `@Min` expresan condiciones de entrada. `@Valid` solicita aplicarlas y `BindingResult`, situado después del objeto validado, permite consultar los errores. Ninguna de estas anotaciones verifica por sí sola la disponibilidad actual en MySQL.

Esta estructura ilustrativa muestra cómo volver al formulario sin intentar guardar:

```java
@PostMapping("/ejemplo")
String recibir(@Valid @ModelAttribute("formulario") Formulario formulario,
               BindingResult errores) {
    if (errores.hasErrors()) {
        return "formulario";
    }
    servicio.guardar(formulario);
    return "redirect:/";
}
```

`Formulario` y `servicio` representan los objetos de esa explicación. Revisa los nombres reales en el controlador preparado. Al volver al formulario también deben recuperarse las opciones del desplegable, como las editoriales. Conservar los datos válidos y mostrar el campo que falló evita que la persona tenga que empezar de nuevo.

Tras una escritura correcta se utiliza una redirección: el navegador solicita un listado con `GET`. Este patrón evita reenviar accidentalmente el mismo formulario al recargar. No sustituye la clave única de un pedido, porque dos peticiones concurrentes todavía pueden llegar al servidor. Los mensajes temporales de redirección sirven para confirmar el resultado al usuario sin convertirlo en una entidad persistente. Consulta [validación de métodos MVC](https://docs.spring.io/spring-framework/reference/6.2/web/webmvc/mvc-controller/ann-validation.html).

Prueba desde la interfaz un número inválido, un campo vacío y una regla de negocio rechazada. Comprueba que muestran errores comprensibles, conservan la navegación y no presentan trazas técnicas ni valores sensibles.

## JUnit y Spring Boot Test

Una prueba debe expresar el resultado que nos interesa, no repetir línea por línea la implementación. Una prueba unitaria crea un componente con colaboradores controlados y comprueba una regla. Una prueba con `@SpringBootTest` levanta el contexto Boot y permite comprobar inyección, configuración y servicios gestionados.

JUnit utiliza `@Test` para identificar un caso y aserciones como `assertEquals` o `assertThrows` para comprobar resultados. Este ejemplo Java ordinario ilustra una comprobación monetaria independiente del contenedor:

```java
@Test
void compararImportes() {
    BigDecimal esperado = new BigDecimal("10.00");
    BigDecimal actual = new BigDecimal("10.0");
    assertEquals(0, esperado.compareTo(actual));
}
```

La prueba pasa porque compara valores; `BigDecimal.equals` también considera la escala. En una prueba de servicio comprueba entradas válidas, límites exactos y rechazos sin cambios. En el código descargable, abre `src/test/java` y ejecuta sus pruebas con el triángulo de IntelliJ. El README separa pruebas ordinarias de las que requieren MySQL real; no omitas ese requisito para interpretar un resultado.

`@SpringBootTest` no inicia un puerto real por defecto. `webEnvironment = RANDOM_PORT` sí levanta un servidor en un puerto disponible. `MockMvc` permite enviar peticiones al soporte MVC sin abrir ese servidor; por tanto, un test con MockMvc no demuestra por sí solo la ejecución del JAR o del contenedor. Véase [pruebas de aplicaciones Boot](https://docs.spring.io/spring-boot/3.5/reference/testing/spring-boot-applications.html).

Para probar `AFTER_COMMIT`, la operación debe confirmar realmente. Una prueba transaccional que siempre termina en rollback puede no activar el observador. Para probar rollback de un pedido, entra por el servicio real, provoca el fallo y consulta después desde otra operación. Mantén una base de pruebas separada; nunca ejecutes limpieza de fixtures sobre los datos de tu aplicación.

## Diagnóstico con logging, excepciones y perfiles

El proyecto utiliza SLF4J para expresar mensajes y Log4j2 como implementación. Un mensaje parametrizado evita concatenaciones y una excepción registrada conserva su traza. Este fragmento muestra la estructura:

```java
log.info("Operación {} sobre libro {}", operacion, libroId);
log.error("No se pudo completar la operación sobre {}", libroId, causa);
```

La última variable del segundo mensaje es la excepción. No registres únicamente `causa.getMessage()`: perderías la pila y la cadena de causas. Tampoco registres contraseñas, cookies de sesión ni tokens CSRF.

El perfil `diagnostico` permite aumentar el detalle sin modificar clases. Actívalo desde IntelliJ con `--spring.profiles.active=diagnostico` y compara el nivel efectivo con el arranque habitual. Si además quieres comprobar el máximo reducido, una lista de perfiles puede activarlos conjuntamente: `--spring.profiles.active=diagnostico,practica`. No combines perfiles sin entender qué componentes seleccionan.

Distingue un rechazo esperado de una caída técnica: ISBN duplicado, stock insuficiente y formulario antiguo necesitan mensajes comprensibles; una conexión interrumpida requiere conservar el error técnico para investigar. La [configuración de logging Boot](https://docs.spring.io/spring-boot/3.5/reference/features/logging.html) y la [ayuda de diagnóstico](../ayuda/diagnostico.md) completan la investigación con breakpoints y trazas.

## Empaquetado como JAR

El JAR ejecutable de Boot incluye las clases de la aplicación, sus recursos y las bibliotecas necesarias. No hace falta instalar un servidor Tomcat externo: el servidor está integrado. Sí necesitas una JVM compatible y acceso a MySQL.

En IntelliJ abre Maven y ejecuta el objetivo `package`. El plugin Boot del proyecto prepara el archivo ejecutable en `target`. Para ejecutarlo fuera del IDE, usa `java -jar` con **el nombre exacto del JAR generado**, indicado en su README:

```text
java -jar target/catalogo-guiado-solucion-1.0.0.jar
```

El ejemplo corresponde a `guiado-solucion`; las otras variantes llevan su nombre correspondiente en el JAR. Conserva las variables `MYSQL_URL`, `MYSQL_USER`, `MYSQL_PASSWORD` y `PORT` si necesitas cambiar el entorno. Un JAR no incluye el contenido de tu servidor MySQL ni hereda automáticamente las variables de una configuración Run de IntelliJ.

Comprueba la página principal y una consulta después de arrancarlo; luego detén ese proceso antes de volver a ejecutar la aplicación en el mismo puerto. Consulta [empaquetado y ejecución](https://docs.spring.io/spring-boot/3.5/reference/packaging/index.html).

## Spring Security: identidad, permisos y CSRF

**Autenticación** responde quién es la persona; **autorización**, qué puede hacer. El perfil `seguro` del proyecto utiliza dos cuentas locales de demostración: `lector` / `lector_local` y `gestor` / `gestor_local`. El lector consulta; el gestor también realiza las acciones de escritura mediante `POST`. Estas cuentas no son un sistema de usuarios de producción.

La configuración utiliza un `SecurityFilterChain`: los filtros de seguridad actúan antes del controlador. El acceso al formulario de login y la sesión los gestiona Spring Security. Cuando una persona no autenticada pide una página protegida, el navegador se dirige al login; no hay que esperar el mismo comportamiento que una API con respuestas JSON. Una persona autenticada sin permiso recibe un rechazo de acceso.

En `SeguridadConfig`, observa cómo los permisos se vinculan al método de la petición y cómo los usuarios reciben sus roles. Las reglas del servicio siguen siendo necesarias: tener rol de gestor no permite un stock negativo ni un ISBN duplicado. Véase [autorización de peticiones](https://docs.spring.io/spring-security/reference/6.5/servlet/authorization/authorize-http-requests.html).

**CSRF** es un ataque en el que otro sitio intenta provocar una acción aprovechando una sesión del navegador. La protección exige un token válido en las peticiones de cambio. Thymeleaf y la integración de seguridad preparan ese dato en los formularios; las peticiones programáticas de prueba deben conservar también la sesión y enviar el token correspondiente.

El proyecto mantiene CSRF también en el modo local. Un `403` al enviar un `POST` manual puede deberse al token y no a una regla de stock. No desactives CSRF para ocultar ese fallo; inspecciona el formulario, la sesión y los permisos. La [referencia de CSRF](https://docs.spring.io/spring-security/reference/6.5/servlet/exploits/csrf.html) explica esta protección.

Prueba lectura con lector, una escritura rechazada con lector y la misma escritura permitida con gestor, además de un envío sin token rechazado. En un despliegue público faltaría resolver cuentas persistentes, secretos, HTTPS y políticas de sesión; el perfil es una demostración local de los conceptos.

## Ejecución con Docker

Un contenedor empaqueta la aplicación con su entorno de ejecución. No sustituye su base de datos ni hace persistentes por sí solos los datos. El proyecto incluye `Dockerfile` y `compose.yaml`. Tras generar el JAR con Maven, abre una terminal en la carpeta de la variante y ejecuta el despliegue local preparado. Esta es una alternativa de Ampliación al arranque desde IntelliJ:

```powershell
.\mvnw.cmd package
docker compose --profile app up --build -d
```

Abre `http://127.0.0.1:8088` para el guiado o `http://127.0.0.1:8089` para la final. Las variantes comparten el proyecto Compose `acda-spring`: ejecuta una variante cada vez. Para detener solo la aplicación, utiliza `docker compose stop app`, conservando MySQL y su volumen. El perfil Compose `app` decide qué contenedores arrancan; es distinto de los perfiles Spring como `seguro`.

El `Dockerfile` utiliza `eclipse-temurin:25-jre`, copia el JAR ya generado y lo ejecuta con un usuario sin privilegios. La configuración externa proporciona la URL y credenciales de MySQL. El servidor de la aplicación debe escuchar en una dirección accesible desde el contenedor, mientras la publicación de puertos del equipo puede limitarse a `127.0.0.1` para la demostración local.

Dentro de un contenedor, `127.0.0.1` se refiere a ese contenedor. Para acceder al servicio MySQL de Compose se utiliza su nombre de servicio y su puerto interno; no el puerto publicado para Workbench en el equipo. Un chequeo de salud de MySQL ayuda a esperar a que esté listo antes de iniciar la aplicación.

Antes de arrancar, distingue el volumen de MySQL, la imagen de la aplicación y los contenedores. Reconstruir la imagen no exige borrar el volumen. Usa [la ayuda de contenedores](../ayuda/contenedores.md) para puertos y diagnóstico y [Dockerfiles en Boot](https://docs.spring.io/spring-boot/3.5/reference/packaging/container-images/dockerfiles.html) para el empaquetado. Verifica mediante el navegador que se muestran los datos esperados y que se conservan tras reiniciar la aplicación sin eliminar el almacenamiento.
