# Proyecto guiado: servicios para el catálogo

La biblioteca mantiene libros y editoriales y necesita registrar precios y existencias desde una web. Partimos de la separación por capas de Hibernate y delegamos en Spring la construcción de componentes, los repositorios JPA y el límite transaccional. El frontend está preparado: completa exclusivamente los `TODO` del backend.

## Preparar y ejecutar

[Descargar proyecto inicial](/fp-dam2-acda/descargas/codigo/catalogo-guiado-inicial.zip){ .md-button .md-button--primary download="catalogo-guiado-inicial.zip" }

1. Descomprime el ZIP y abre en IntelliJ la carpeta con `pom.xml`. Espera la sincronización Maven y selecciona el JDK indicado en [proyectos Maven](../ayuda/proyectos-maven.md).
2. Inicia Docker Desktop. Ejecuta `es.mjsplayground.acda.spring.catalogo.config.MySqlLocal.main` con la carpeta de este proyecto como **Working directory**. Conserva el almacenamiento del servidor.
3. Ejecuta `es.mjsplayground.acda.spring.catalogo.CatalogoApplication.main`.
4. Abre `http://127.0.0.1:8080`. Para terminar, detén la aplicación desde IntelliJ.

MySQL escucha en el puerto 3309 y el guiado usa `spring_guiado`. Consulta [configuración](configuracion.md) y [contenedores](../ayuda/contenedores.md) si el servidor no está disponible. No ejecutes starter y solución simultáneamente en el mismo puerto. Cambiar de carpeta no crea una base distinta para cada variante del guiado: los datos permanecen en ese esquema.

La web inicial puede arrancar y mostrar avisos de funciones pendientes. Eso no significa que las búsquedas o los guardados estén implementados. Las editoriales y los componentes de infraestructura necesarios para mostrar las pantallas están preparados.

## Qué está preparado

| Zona | Responsabilidad y estado inicial |
| --- | --- |
| `model.entity` | `Libro` y `Editorial` ya mapeados, incluida la versión del libro. |
| `model.repository` | Contratos Spring Data; dos consultas de libros pendientes. |
| `model.service` | Servicios, solicitudes y resultados; reglas y operaciones señaladas con `TODO`. |
| `config` | Boot, propiedades, reloj, perfiles, seguridad y ayuda de arranque local. |
| `web` y `templates` | Controladores, formularios y frontend completos. |

`LibroSolicitud` transporta datos de entrada; `LibroDatos` prepara la salida para la vista. La entidad no se utiliza como un formulario que pueda reemplazar cualquier campo. `EditorialService` gestiona editoriales y colabora con `CatalogoService`. No cambies controladores, plantillas ni estilos.

## Tu avance

<div class="mjs-progress-panel" role="status" aria-live="polite"><span class="mjs-progress-label">Tu avance en el proyecto</span><strong class="mjs-progress-count">0 de 5 pasos</strong><div class="mjs-progress-track"><div class="mjs-progress-fill"></div></div></div>

<div class="mjs-task"><input type="checkbox" id="spring-etapa-1"><label for="spring-etapa-1">He localizado entidades, configuración y dependencias y puedo arrancar la web.</label></div>

<div class="mjs-task"><input type="checkbox" id="spring-etapa-2"><label for="spring-etapa-2">Las dos consultas derivadas permiten buscar por título y editorial.</label></div>

<div class="mjs-task"><input type="checkbox" id="spring-etapa-3"><label for="spring-etapa-3">El servicio guarda libros aplicando reglas, versiones y colaboración.</label></div>

<div class="mjs-task"><input type="checkbox" id="spring-etapa-4"><label for="spring-etapa-4">La demostración se crea conjuntamente y respeta los datos existentes.</label></div>

<div class="mjs-task"><input type="checkbox" id="spring-etapa-5"><label for="spring-etapa-5">He comprobado avisos tras commit y la selección por perfiles.</label></div>

## Etapa 1. Reconocer componentes preparados

**Aprendemos:** cómo Boot construye una aplicación y conecta sus dependencias. Lee [componentes](componentes.md) y [configuración](configuracion.md) antes de modificar código.

Examina `CatalogoApplication`, `CatalogoConfig`, `CatalogoProperties` y los constructores de `CatalogoService`, `EditorialService` y `ReglasCatalogo`. El reloj es un bean declarado con `@Bean`; los servicios y las reglas se detectan mediante sus anotaciones. Las entidades y los formularios ya están definidos: esta etapa es de exploración, **no tiene un TODO de mapeo**.

Arranca la web y localiza libros y editoriales. Comprueba qué aviso aparece cuando la consulta de libros sigue pendiente. Si el contexto no inicia, revisa primero la causa del error, la conexión MySQL y la disponibilidad de cada bean; no cambies aleatoriamente las anotaciones.

## Etapa 2. Consultar mediante repositorios

**Problema:** la pantalla necesita libros filtrados por título y, opcionalmente, por editorial. En [persistencia](persistencia.md) se explica cómo Spring interpreta los nombres de métodos.

Completa los dos `TODO` de `LibroRepository`. El starter utiliza métodos `default` que anuncian la tarea pendiente para poder arrancar. Sustitúyelos por las **declaraciones de consultas derivadas** indicadas, sin conservar sus cuerpos pendientes. El servicio de listado ya sabe elegir la consulta adecuada y convertir sus resultados.

Comprueba primero que la lista vacía se muestra sin un aviso de TODO. Tras crear libros en la etapa 3, retoma las búsquedas con coincidencia parcial, diferencias de mayúsculas y una combinación sin resultados. El orden debe seguir título e identificador, como expresan los métodos del proyecto. Para tener datos puedes crear editoriales y, tras la siguiente etapa, libros; el botón de demostración se completa en la etapa 4.

??? tip "La consulta no se crea al arrancar"

    Comprueba que el método sea una declaración sin cuerpo y que cada propiedad de su nombre exista en `Libro`. `EditorialId` navega por la relación hasta `editorial.id`; no hace referencia a una variable del controlador.

## Etapa 3. Guardar con reglas y colaboración

**Problema:** un guardado debe aplicar las mismas reglas desde cualquier entrada. Lee [servicios](servicios.md) y [transacciones](transacciones.md).

Completa `ReglasCatalogo.validar` y `CatalogoService.guardar`. El primero comprueba los datos del libro; el segundo coordina esas reglas, la editorial obtenida mediante `EditorialService`, el repositorio y la publicación de `CambioCatalogo` dentro de la operación transaccional. Utiliza el reloj inyectado para el instante del evento.

El ISBN exige 13 dígitos y unicidad; este catálogo no comprueba su dígito de control. El título admite entre 1 y 120 caracteres después de quitar espacios exteriores; el precio va de 0,01 a 10000 con hasta dos decimales; el stock admite de cero al máximo configurado. La editorial es obligatoria y debe existir. Para editar, compara la versión de la solicitud con la actual antes de modificar la entidad; conserva también `@Version` para detectar concurrencia durante la escritura.

Crea una editorial desde su pantalla preparada y guarda un libro. Después edita precio o stock y comprueba el listado. Ensaya precio cero, demasiados decimales, ISBN duplicado, stock fuera de rango y editorial inexistente. Abre un libro en dos pestañas, guarda desde una y vuelve a guardar el formulario anterior: debe rechazarse la versión antigua sin sobrescribir el cambio.

No devuelvas un identificador inventado ni ocultes una caída de MySQL como rechazo de un campo. El manejo preparado muestra los errores; el backend debe conservar sus causas técnicas.

## Etapa 4. Coordinar datos de demostración

**Problema:** necesitamos un conjunto inicial de editoriales y libros que se cree completo o no se cree. Completa `CatalogoService.demo` utilizando la infraestructura transaccional ya declarada y los repositorios de ambos tipos de entidad.

El botón de datos de demostración es una acción explícita y solo se admite cuando **ambos catálogos están vacíos**. No introduzcas una carga automática al arrancar. Si ya hay datos, el servicio debe rechazarse sin borrar ni mezclar registros. Publica el evento correspondiente a una creación confirmada.

Para preparar esta comprobación, elimina desde las pantallas únicamente el libro de prueba que creaste en la etapa 3 y, después, su editorial. No borres trabajo previo ajeno a estas comprobaciones: si ya hay otros datos, conserva el rechazo esperado de la demostración y comprueba el caso de base vacía en un entorno de pruebas aislado. No uses `DROP` ni `ddl-auto=create` para vaciar el catálogo.

Comprueba en una base de trabajo vacía que aparecen los libros y sus editoriales. Repite la acción: no deben duplicarse ni sustituirse los datos. Reinicia la aplicación y comprueba que el arranque conserva lo que había. Para un fallo intermedio, verifica desde otra operación que el rollback no deja solo las editoriales o solo parte de los libros. La [ayuda de diagnóstico](../ayuda/diagnostico.md) permite seguir el método y los errores sin cambiar el frontend.

## Etapa 5. Observar eventos y perfiles

**Aprendemos:** una notificación informa de un hecho confirmado y puede cambiar su implementación sin cambiar el servicio. Examina `AvisosCatalogo`, `Notificador`, `NotificadorLog` y `NotificadorSilencioso`; esta infraestructura ya está preparada y no tiene un TODO adicional.

Guarda un libro correctamente y observa el aviso después del commit. Intenta un guardado rechazado y comprueba que no anuncia éxito. Activa `silencioso` y repite un guardado válido: la persistencia debe seguir funcionando aunque no se escriba la notificación. Activa `practica` y prueba el límite de 50 unidades. Revisa en el arranque qué perfil se aplicó.

Un evento no reemplaza una operación esencial ni garantiza entrega duradera. Si el aviso falla después del commit, los datos ya se han confirmado. La explicación y sus límites están en [eventos y servicios](servicios.md#eventos-comunicar-un-hecho).

## Solución y comprobación conjunta

[Descargar solución del guiado](/fp-dam2-acda/descargas/codigo/catalogo-guiado-solucion.zip){ .md-button download="catalogo-guiado-solucion.zip" }

El README de cada variante identifica los requisitos, el punto de entrada y sus pruebas. Ejecuta desde IntelliJ las que correspondan a tu entorno; las pruebas de MySQL real requieren su configuración aislada y no se sustituyen por una compilación correcta.

Antes de continuar verifica búsquedas, CRUD de libros y editoriales, errores, versiones, datos de demostración y perfiles. No elimines una editorial con libros asignados: el catálogo debe comunicar el rechazo. La [práctica final](practica-final.md) parte de este guiado resuelto y añade reposición, presupuesto y confirmación transaccional de pedidos.
