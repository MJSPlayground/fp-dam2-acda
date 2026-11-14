# Proyecto guiado: servicios para el catálogo

La biblioteca mantiene libros y editoriales y necesita registrar precios y existencias desde una web. Partimos de la separación por capas de Hibernate y delegamos en Spring la construcción de componentes, los repositorios JPA y el límite transaccional. El frontend está preparado: completa exclusivamente los `TODO` del backend.

Al terminar podrás buscar libros, crearlos y editarlos con sus reglas, mantener editoriales y preparar una demostración sin datos parciales. Necesitas conocer clases e interfaces Java, excepciones, colecciones, entidades JPA y el significado de una transacción. Si dudas sobre los dos últimos, vuelve a [entidades](../hibernate/entidades.md) y [persistencia](../hibernate/persistencia.md). Primero observarás editoriales ya resueltas; después adaptarás consultas y completarás los casos de libros, hasta coordinar una operación con varias escrituras.

## Preparar y ejecutar

[Descargar proyecto inicial](/fp-dam2-acda/descargas/codigo/catalogo-guiado-inicial.zip){ .md-button .md-button--primary download="catalogo-guiado-inicial.zip" }

1. Descomprime el ZIP y abre en IntelliJ la carpeta con `pom.xml`. Espera la sincronización Maven y selecciona el JDK indicado en [proyectos Maven](../ayuda/proyectos-maven.md).
2. Inicia Docker Desktop. Ejecuta `es.mjsplayground.acda.spring.catalogo.config.MySqlLocal.main` con la carpeta de este proyecto como **Working directory**. Conserva el almacenamiento del servidor.
3. Ejecuta `es.mjsplayground.acda.spring.catalogo.CatalogoApplication.main`.
4. Abre `http://127.0.0.1:8080`. Para terminar, detén la aplicación desde IntelliJ.

MySQL escucha en el puerto 3309 y el guiado usa `spring_guiado`. Consulta [configuración](configuracion.md) y [contenedores](../ayuda/contenedores.md) si el servidor no está disponible. No ejecutes starter y solución simultáneamente en el mismo puerto. Cambiar de carpeta no crea una base distinta para cada variante del guiado: los datos permanecen en ese esquema.

La web inicial puede arrancar y mostrar avisos de funciones pendientes. Eso no significa que las búsquedas o los guardados estén implementados. Las editoriales y los componentes de infraestructura necesarios para mostrar las pantallas están preparados.

**Primera comprobación, sin escribir código:** abre **Editoriales**, crea `Editorial de prueba Spring`, comprueba que aparece y abre su edición. Cambia el nombre y pulsa **Cancelar**: debe conservarse el anterior. Reinicia la aplicación y vuelve a consultarla. El circuito editorial ya funciona; el aviso de consulta pendiente en **Libros** corresponde a la etapa 2. Si ya existe ese nombre, usa otro para no confundir una restricción de unicidad con un fallo de arranque.

## Qué está preparado

| Zona | Responsabilidad y estado inicial |
| --- | --- |
| `model.entity` | `Libro` y `Editorial` ya mapeados, incluida la versión del libro. |
| `model.repository` | Contratos Spring Data; dos consultas de libros pendientes. |
| `model.service` | Servicios, solicitudes y resultados; reglas y operaciones señaladas con `TODO`. |
| `config` | Boot, propiedades, reloj, perfiles, seguridad y ayuda de arranque local. |
| `web` y `templates` | Controladores, formularios y frontend completos. |

`LibroSolicitud` transporta datos de entrada; `LibroDatos` prepara la salida para la vista. La entidad no se utiliza como un formulario que pueda reemplazar cualquier campo. `EditorialService` gestiona editoriales y colabora con `CatalogoService`. No cambies controladores, plantillas ni estilos.

## Primer recorrido: solo una consulta

No necesitas dominar todos los archivos para empezar. Abre primero estos tres: `web/CatalogoController.java` (preparado), `model/service/CatalogoService.java` (su método `listar` está preparado) y `model/repository/LibroRepository.java` (aquí completarás dos consultas). El botón de buscar llega al controlador, este llama a `listar` y el servicio utiliza el repositorio. Las entidades ya definen qué datos se recuperan.

| Lee para orientarte | Modifica en el guiado | Conserva preparado |
| --- | --- | --- |
| `Libro`, `Editorial`, `LibroSolicitud`, `LibroDatos` | `LibroRepository`: consultas de etapa 2 | Mapeos JPA y campos de formulario |
| Constructores de servicios | `ReglasCatalogo.validar`: etapa 3a | Inyección por constructor |
| `CatalogoService` | `guardar`: etapas 3b y 3c; `demo`: etapa 4 | `listar`, `obtener`, eliminación y gestión de errores web |
| `CatalogoConfig`, perfiles y avisos | Ningún TODO adicional de infraestructura | Reloj, propiedades, listener, notificadores, seguridad y frontend |

No cambies la configuración del servidor para resolver un TODO de negocio. El mapa de [servicios](servicios.md) muestra las colaboraciones; los colores y la leyenda distinguen los componentes ya preparados de los que completarás.

## Comprobar una etapa desde IntelliJ

El starter incluye `src/test/java/.../AprendizajeGuiadoTest.java`, con datos propios y fallos preparados. Son pruebas **optativas**: se omiten mientras no eliges una fase y no indican que lo pendiente está resuelto. En IntelliJ abre esa clase, crea su configuración de JUnit con Run y añade en **VM options** `-Daprendizaje.fase=2` (después usa `3`, `4` o `5`). Ejecuta la clase; no escribas SQL de limpieza ni cambies las pruebas para hacerlas pasar.

Necesitan el MySQL local del proyecto y crean una base temporal `qa_spring_UUID`; no usan `spring_guiado`. El README explica las variables de conexión si trabajas con otro servidor. La fase 2 prepara un libro directamente como dato de prueba: permite comprobar la consulta antes de implementar un guardado. Una prueba roja señala el TODO de esa fase o una regresión; una prueba omitida no cuenta como superada.

## Tu avance

<div class="mjs-progress-panel" role="status" aria-live="polite"><span class="mjs-progress-label">Tu avance en el proyecto</span><strong class="mjs-progress-count">0 de 5 pasos</strong><div class="mjs-progress-track"><div class="mjs-progress-fill"></div></div></div>

<div class="mjs-task"><input type="checkbox" id="spring-etapa-1"><label for="spring-etapa-1">He localizado entidades, configuración y dependencias y puedo arrancar la web.</label></div>

<div class="mjs-task"><input type="checkbox" id="spring-etapa-2"><label for="spring-etapa-2">Las dos consultas derivadas permiten buscar por título y editorial.</label></div>

<div class="mjs-task"><input type="checkbox" id="spring-etapa-3"><label for="spring-etapa-3">El servicio guarda libros aplicando reglas, versiones y colaboración.</label></div>

<div class="mjs-task"><input type="checkbox" id="spring-etapa-4"><label for="spring-etapa-4">La demostración se crea conjuntamente y respeta los datos existentes.</label></div>

<div class="mjs-task"><input type="checkbox" id="spring-etapa-5"><label for="spring-etapa-5">He comprobado avisos tras commit y la selección por perfiles.</label></div>

## Etapa 1. Reconocer componentes preparados

**Aprendemos:** cómo Boot construye una aplicación y conecta sus dependencias. Lee [componentes](componentes.md) y [configuración](configuracion.md) antes de modificar código.

Empieza por `CatalogoApplication.main`: una sola llamada inicia el contenedor y el servidor. Después sigue los tres archivos del primer recorrido. Localiza únicamente los campos y el constructor de `CatalogoService`: Spring entrega esos colaboradores; no tienes que fabricarlos. Deja la lectura detallada de `CatalogoConfig`, `CatalogoProperties` y los avisos para la etapa 5. El reloj es un bean declarado con `@Bean`; los servicios y las reglas se detectan mediante sus anotaciones. Las entidades y los formularios ya están definidos: esta etapa es de exploración, **no tiene un TODO de mapeo**.

Observa en `model/service/EditorialService.java` este **fragmento ya incluido en el starter**:

```java
private final EditorialRepository editoriales;
private final LibroRepository libros;
private final ReglasCatalogo reglas;

public EditorialService(
    EditorialRepository editoriales, LibroRepository libros, ReglasCatalogo reglas) {
    this.editoriales = editoriales;
    this.libros = libros;
    this.reglas = reglas;
}
```

El constructor recibe los colaboradores; `this.editoriales` conserva el que Spring entrega. `@Service`, situada sobre la clase, permite detectarla como componente. No hay que añadir `new EditorialService(...)` al controlador. En [inyección de dependencias](componentes.md) puedes seguir cómo se elige y entrega cada objeto.

Arranca la web y localiza libros y editoriales. Comprueba qué aviso aparece cuando la consulta de libros sigue pendiente. Si el contexto no inicia, revisa primero la causa del error, la conexión MySQL y la disponibilidad de cada bean; no cambies aleatoriamente las anotaciones.

## Etapa 2. Consultar mediante repositorios

**Problema:** la pantalla necesita libros filtrados por título y, opcionalmente, por editorial. En [persistencia](persistencia.md) se explica cómo Spring interpreta los nombres de métodos.

Antes de modificarlo, abre `model/repository/EditorialRepository.java`: su declaración preparada `findAllByOrderByNombreAscIdAsc()` devuelve todas las editoriales, ordenadas por nombre y luego por identificador. No tiene cuerpo porque Spring Data lo genera. Ahora integra esta **primera consulta de referencia** en `LibroRepository.java`, sustituyendo únicamente el método `default` del mismo nombre:

```java
List<Libro> findByTituloContainingIgnoreCaseOrderByTituloAscIdAsc(String texto);
```

`texto` es la entrada; el resultado es una lista de libros, posiblemente vacía. `Containing` busca una parte del título, `IgnoreCase` ignora mayúsculas y el tramo `OrderBy` fija el orden. Conserva un solo método con ese nombre: elimina `default` y su cuerpo con `throw`, manteniendo la declaración terminada en `;`.

**Ahora te toca:** completa el segundo `TODO` del mismo archivo, el método que combina editorial y título. Su nombre y parámetros ya están definidos: deduce qué condición añade y qué relación atraviesa. Convierte ese apoyo en una declaración Spring Data, sin escribir SQL ni un bucle de filtrado. El servicio `CatalogoService.listar` ya elige entre ambas consultas.

Ejecuta opcionalmente la fase `2`: sus datos preparados permiten comprobar `JAVA` con una coincidencia y un texto ausente sin ellas, antes de completar guardar. Desde la web, **Libros → Buscar**, con filtro vacío, debe dejar de mostrar un aviso de TODO y mostrar los datos existentes o una lista vacía real. El guardado y la demostración continúan pendientes.

Comprueba primero que la lista vacía se muestra sin un aviso de TODO. Tras crear libros en la etapa 3, retoma las búsquedas con coincidencia parcial, diferencias de mayúsculas y una combinación sin resultados. El orden debe seguir título e identificador, como expresan los métodos del proyecto. Para tener datos puedes crear editoriales y, tras la siguiente etapa, libros; el botón de demostración se completa en la etapa 4.

??? tip "La consulta no se crea al arrancar"

    Comprueba que el método sea una declaración sin cuerpo y que cada propiedad de su nombre exista en `Libro`. `EditorialId` navega por la relación hasta `editorial.id`; no hace referencia a una variable del controlador.

## Etapa 3. Guardar con reglas y colaboración

**Problema:** un guardado debe aplicar las mismas reglas desde cualquier entrada. Lee [servicios](servicios.md) y [transacciones](transacciones.md).

### 3a. Validar una solicitud sin escribir

Observa primero `ReglasCatalogo.stock`, **ya implementado**: compara el valor con cero y con `propiedades.maxStock()`, y lanza `ReglaCatalogoException` si no cumple. Así una regla depende de configuración sin que el controlador decida el máximo.

**Ahora te toca:** en `model/service/ReglasCatalogo.java`, completa solo `validar(LibroSolicitud r)`. Comprueba primero que existe la solicitud; reutiliza los métodos preparados `nombre` y `stock`, y añade las reglas de ISBN, precio y editorial indicadas a continuación. Este **fragmento orientativo**, parte de esa validación, muestra una comprobación de ausencia; no es el método completo:

```java
if (r.precio() == null) {
    throw new ReglaCatalogoException("El precio es obligatorio");
}
```

Después de descartar `null`, puedes llamar a `compareTo` y comprobar la escala; el orden evita un `NullPointerException`. Todavía no abras repositorios desde esta clase.

Para observar las reglas desde la web, añade al principio de `CatalogoService.guardar` la llamada `reglas.validar(r)`, conservando provisionalmente el `throw` de etapa pendiente que hay después. Pon un breakpoint en `validar` y envía desde **Libros → Nuevo** ISBN `9780000000099`, título `Prueba Spring`, precio `12,50`, stock `2` y una editorial existente. Sigue las reglas con el depurador: al superarlas llegarás al aviso pendiente, todavía sin persistencia. El formulario puede detectar algunos errores antes de entrar en servicio; la fase `3` permitirá contrastar también las reglas del backend después de completar el guardado.

??? tip "Pista — Comparar importes"

    `BigDecimal.compareTo` compara valores; `scale()` permite comprobar los decimales recibidos. No redondees una entrada inválida para aceptarla. Para ISBN necesitas comprobar su forma, mientras que la unicidad también está protegida por la base.

### 3b. Crear un libro

Antes de escribir el guardado de libros, estudia este **método completo ya resuelto en `EditorialService`**:

```java
@Transactional
public Long guardar(Long id, String nombre) {
    reglas.nombre(nombre, 80);
    Editorial e = id == null ? new Editorial(nombre.strip()) : obtener(id);
    e.setNombre(nombre.strip());
    return editoriales.saveAndFlush(e).getId();
}
```

La primera línea del cuerpo aplica una regla, la siguiente distingue alta de edición y el repositorio escribe. `saveAndFlush` envía los cambios a la base; **no equivale al commit**: la transacción termina al regresar correctamente la llamada al servicio. El identificador permite a la web localizar lo guardado. Este patrón explica el caso que has probado en la primera comprobación.

**Ahora te toca:** en `model/service/CatalogoService.java`, método `guardar`, empieza por una solicitud con `r.id() == null`. Sustituye el aviso pendiente por este primer caso de uso y conserva la validación de 3a. Reutiliza el patrón de editoriales, incorporando la editorial obtenida mediante `EditorialService`. El constructor de `Libro` y la anotación transaccional ya existen. Debes devolver el identificador real, no uno calculado en memoria; mientras no completes 3c, conserva un rechazo explícito para las solicitudes de edición.

Dentro de `CatalogoService.guardar`, la colaboración comienza con objetos ya inyectados. Este fragmento no guarda ni construye el libro; muestra qué componente aplica cada decisión:

```java
reglas.validar(r);
Editorial editorial = editorialService.obtener(r.editorialId());
// Continúa aquí con alta o edición y la escritura del libro.
```

`r` es la solicitud recibida. Si no supera las reglas o su editorial no existe, la operación termina con la excepción correspondiente y no llega a guardar. Si continúa, `editorial` es la entidad que necesita el constructor o la actualización del libro. No instancies otra copia de `EditorialService` ni abras una sesión manual de Hibernate.

Comprueba el alta antes de resolver la edición: en **Libros → Nuevo**, utiliza ISBN `9780000000099`, título `Prueba Spring`, precio `12,50`, stock `2` y tu editorial de prueba. Debe aparecer una sola fila y recuperarse al volver a abrir el listado. Otro intento con ese ISBN debe rechazarse y conservar la fila original. Utiliza otro ISBN si ya lo habías creado. La fase `3` completa todavía necesita la edición de 3c.

### 3c. Editar conservando identidad y versión

**Ahora te toca:** en el mismo método completa la rama de una solicitud con identificador: recupera el libro existente, rechaza una versión antigua y modifica sus campos permitidos. La interfaz ya transporta esa versión. No construyas otro libro con identidad nueva para editar. El evento tiene su contrato preparado; incorpora la llamada mostrada en [servicios](servicios.md#eventos-comunicar-un-hecho), sin implementar un listener ni una cola. Para comprender dónde encaja, observa `CatalogoService.eliminar`, ya resuelto: publica un `CambioCatalogo` después de solicitar la escritura y dentro de la transacción.

??? tip "Pista — Una edición no empieza desde cero"

    Compara con `EditorialService.obtener` y con `CatalogoService.obtener`: ambos transforman la ausencia en un error claro mediante `orElseThrow`. Para la versión, consulta el valor actual antes de tocar campos. Piensa qué dato conservarías si el formulario fuese antiguo.

Completa `ReglasCatalogo.validar` y `CatalogoService.guardar`. El primero comprueba los datos del libro; el segundo coordina esas reglas, la editorial obtenida mediante `EditorialService`, el repositorio y la publicación de `CambioCatalogo` dentro de la operación transaccional. Utiliza el reloj inyectado para el instante del evento.

El ISBN exige 13 dígitos y unicidad; este catálogo no comprueba su dígito de control. El título admite entre 1 y 120 caracteres después de quitar espacios exteriores; el precio va de 0,01 a 10000 con hasta dos decimales; el stock admite de cero al máximo configurado. La editorial es obligatoria y debe existir. Para editar, compara la versión de la solicitud con la actual antes de modificar la entidad; conserva también `@Version` para detectar concurrencia durante la escritura.

Crea una editorial desde su pantalla preparada y guarda un libro. Después edita precio o stock y comprueba el listado. Ensaya precio cero, demasiados decimales, ISBN duplicado, stock fuera de rango y editorial inexistente. Abre un libro en dos pestañas, guarda desde una y vuelve a guardar el formulario anterior: debe rechazarse la versión antigua sin sobrescribir el cambio.

Ejecuta la fase `3`: comprueba el recorrido MVC y la versión obsoleta en datos aislados. **Continúa cuando crear, editar y rechazar sin cambiar datos sean comprobables.**

No devuelvas un identificador inventado ni ocultes una caída de MySQL como rechazo de un campo. El manejo preparado muestra los errores; el backend debe conservar sus causas técnicas.

## Etapa 4. Coordinar datos de demostración

**Ahora te toca integrar:** necesitamos un conjunto inicial de editoriales y libros que se cree completo o no se cree. En `model/service/CatalogoService.java`, completa `demo` utilizando la infraestructura transaccional ya declarada y los repositorios de ambos tipos de entidad. Reutiliza lo aprendido al guardar; aquí decides cómo coordinar varias escrituras.

El conjunto esperado es: editoriales `Horizonte` y `Norte`; libro `9780000000001`, `Java y datos`, precio `29,90`, stock `10`, de Horizonte; y libro `9780000000002`, `Historias locales`, precio `18,50`, stock `5`, de Norte. No hay autores ni detalles que añadir en este proyecto.

El botón de datos de demostración es una acción explícita y solo se admite cuando **ambos catálogos están vacíos**. No introduzcas una carga automática al arrancar. Si ya hay datos, el servicio debe rechazarse sin borrar ni mezclar registros. Publica el evento correspondiente a una creación confirmada.

??? tip "Pista — Unidad de trabajo"

    Los repositorios participan en la transacción de este servicio. Una escritura enviada con `flush` todavía puede deshacerse si falla la operación. No captures una excepción para continuar como si todo hubiese terminado correctamente.

Para preparar esta comprobación, elimina desde las pantallas únicamente el libro de prueba que creaste en la etapa 3 y, después, su editorial. No borres trabajo previo ajeno a estas comprobaciones: si ya hay otros datos, conserva el rechazo esperado de la demostración y comprueba el caso de base vacía en un entorno de pruebas aislado. No uses `DROP` ni `ddl-auto=create` para vaciar el catálogo.

En `CatalogoService.demo`, separa recuentos previos, creación de editoriales, libros relacionados y el aviso preparado. La fase `4` contiene `falloPreparadoTrasEscribirEditorialesRevierteDemo`: un mecanismo ya escrito rechaza el libro después de persistir editoriales, invoca tu servicio y comprueba cero libros, cero editoriales y ningún aviso. El mecanismo se retira al terminar. **Pasa cuando funcionan la demostración completa y esa reversión.**

Comprueba en una base de trabajo vacía que aparecen los libros y sus editoriales. Repite la acción: no deben duplicarse ni sustituirse los datos. Reinicia la aplicación y comprueba que el arranque conserva lo que había. Para un fallo intermedio, verifica desde otra operación que el rollback no deja solo las editoriales o solo parte de los libros. La [ayuda de diagnóstico](../ayuda/diagnostico.md) permite seguir el método y los errores sin cambiar el frontend.

## Etapa 5. Observar eventos y perfiles

**Aprendemos:** una notificación informa de un hecho confirmado y puede cambiar su implementación sin cambiar el servicio. Examina `AvisosCatalogo`, `Notificador`, `NotificadorLog` y `NotificadorSilencioso`; esta infraestructura ya está preparada y no tiene un TODO adicional.

Guarda un libro correctamente y observa el aviso después del commit. Intenta un guardado rechazado y comprueba que no anuncia éxito. Activa `silencioso` y repite un guardado válido: la persistencia debe seguir funcionando aunque no se escriba la notificación. Activa `practica` y prueba el límite de 50 unidades. Revisa en el arranque qué perfil se aplicó.

Para activarlos, detén la aplicación, abre **Run → Edit Configurations**, selecciona `CatalogoApplication` y añade en **Environment variables** `SPRING_PROFILES_ACTIVE=practica,silencioso`. Reinicia: el stock `50` es válido y `51` debe rechazarse. Quita `silencioso` para observar de nuevo los avisos del notificador; no edites su implementación. Usa después el perfil habitual para continuar con tus datos.

Ejecuta la fase `5` para contrastar versiones y avisos tras commit. Aquí observas componentes preparados: no implementas administración de perfiles, seguridad ni infraestructura de eventos.

Un evento no reemplaza una operación esencial ni garantiza entrega duradera. Si el aviso falla después del commit, los datos ya se han confirmado. La explicación y sus límites están en [eventos y servicios](servicios.md#eventos-comunicar-un-hecho).

## Solución y comprobación conjunta

Recorre estas operaciones desde la web; conserva los datos de una comprobación antes de probar su error asociado:

| Operación | Resultado observable |
| --- | --- |
| Buscar `PRUEBA`, elegir su editorial y después un texto ausente | Coincidencias sin distinguir mayúsculas; el texto ausente devuelve lista vacía. |
| Crear y editar un libro; cancelar otra edición | Se mantiene su identidad; Cancelar conserva el último dato guardado. |
| Precio cero, ISBN repetido o stock por encima del máximo | Mensaje comprensible y catálogo previo intacto. |
| Guardar formularios del mismo libro desde dos pestañas | La versión antigua se rechaza sin deshacer la primera edición. |
| Eliminar una editorial con libros | Rechazo sin perder la editorial ni sus libros. |
| Preparar demostración sobre catálogo ocupado | Rechazo sin borrar, duplicar ni mezclar datos. |
| Reiniciar después de guardar; cambiar al perfil `silencioso` | Persisten los datos; solo cambia la notificación. |

La fase `4` permite comprobar además una demostración desde vacío y un fallo posterior a las primeras escrituras sin vaciar tu catálogo. Ejecuta `todas` únicamente cuando estén resueltos los TODO; las pruebas omitidas no son resultados satisfactorios.

**Explica con tus palabras:** ¿quién crea un `EditorialService` y quién valida un libro? ¿Por qué un repositorio puede ser una interfaz sin implementación escrita? ¿Qué diferencia hay entre `flush` y commit? ¿Qué debe ocurrir con las editoriales y el aviso si falla la creación del segundo libro de una demostración?

[Descargar solución del guiado](/fp-dam2-acda/descargas/codigo/catalogo-guiado-solucion.zip){ .md-button download="catalogo-guiado-solucion.zip" }

El README de cada variante identifica los requisitos, el punto de entrada y sus pruebas. Ejecuta desde IntelliJ las que correspondan a tu entorno; las pruebas de MySQL real requieren su configuración aislada y no se sustituyen por una compilación correcta.

Antes de continuar verifica búsquedas, CRUD de libros y editoriales, errores, versiones, datos de demostración y perfiles. No elimines una editorial con libros asignados: el catálogo debe comunicar el rechazo. La [práctica final](practica-final.md) parte de este guiado resuelto y añade reposición, presupuesto y confirmación transaccional de pedidos.
