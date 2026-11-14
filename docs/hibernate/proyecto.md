# Proyecto guiado: catálogo de biblioteca

La biblioteca necesita mantener editoriales, autores, libros y sus detalles de publicación. Al terminar podrás crear y editar fichas, buscar por título y editorial y eliminar libros conservando sus autores compartidos. Completarás mapeos, repositorios y casos de uso en una sola aplicación JavaFX; sus controles y formularios están preparados.

Necesitas manejar clases, colecciones, excepciones y lambdas sencillas de Java, y distinguir repositorio y servicio como en [Ficheros](../ficheros/arquitectura.md). Sigue aquí el recorrido **entidades y relaciones → sesión y repositorios → consultas → servicio → comprobación conjunta**, apoyándote en las páginas de teoría enlazadas.

## Preparar y ejecutar

[Descargar proyecto inicial](/fp-dam2-acda/descargas/codigo/biblioteca-guiado-inicial.zip){ .md-button .md-button--primary download="biblioteca-guiado-inicial.zip" }

Descomprime y abre en IntelliJ la carpeta con `pom.xml`. Espera la sincronización Maven y selecciona el JDK siguiendo [preparación de proyectos](../ayuda/proyectos-maven.md).

1. Inicia Docker Desktop.
2. Ejecuta `es.mjsplayground.acda.hibernate.biblioteca.config.MySqlLocal.main`, con esta carpeta del proyecto como **Working directory**.
3. Ejecuta `es.mjsplayground.acda.hibernate.biblioteca.app.Launcher.main`.

El servidor local escucha en 3308 y el guiado utiliza `biblioteca_guiado`. La [configuración del tema](configuracion.md) explica conexión, Workbench y esquema. Para cambiar de starter a solución cierra la aplicación anterior; no es necesario borrar el servidor o su volumen.

**Primera comprobación, sin escribir código:** abre las pestañas **Libros**, **Editoriales** y **Autores**, pulsa **Actualizar** y localiza el aviso de etapa pendiente. La ventana debe responder; ese aviso indica que aún falta consultar los catálogos, no que MySQL esté averiado. Un error de conexión requiere revisar Docker, el puerto y la causa del registro. No prepares todavía la demostración ni registres libros: las asociaciones marcadas como transitorias aún no representan las relaciones del catálogo.

## Paquetes y apoyos preparados

```text
app.Launcher → Interfaz JavaFX
                    ↓
             BibliotecaService
                    ↓
       LibroRepository / CatalogoRepository
                    ↓
            Session → MySQL
```

`HibernateFactory` prepara el proveedor. Las entidades incluyen campos, constructores, accesores y mapeos simples. El servicio entrega `LibroDatos` a la interfaz con la información necesaria ya preparada dentro de la sesión. La UI no navega por proxies cerrados ni abre sesiones.

El helper `transaccion` y las convenciones de excepción y logging están resueltos. Los repositorios reciben una sesión: no cierran la que pertenece al caso de uso ni confirman cada operación por separado.

## Etapa 1. Entidades y asociaciones

Lee [arquitectura](arquitectura.md), [entidades](entidades.md) y [relaciones](relaciones.md). Empieza observando `model/entity/Detalle.java`, ya mapeado en el starter. Este fragmento **ya existe**, no tienes que añadirlo:

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(nullable = false, length = 30)
private String idioma;
```

`@Id` identifica la entidad; `IDENTITY` deja que MySQL genere su identificador. `@Column` describe una columna obligatoria y su longitud. Localiza también `@Entity`, el constructor sin argumentos y `paginas`. Las anotaciones describen persistencia; no sustituyen la validación del idioma y las páginas en el servicio.

El **TODO 1** marca asociaciones que temporalmente tienen `@Transient` para permitir el arranque. Los campos simples ya están resueltos. Sustituirás esas marcas, sin cambiar constructores ni accesores, para representar estas relaciones:

- `Libro.editorial`: N:1 propietario, editorial obligatoria.
- `Editorial.libros`: 1:N inverso mediante `mappedBy`.
- `Libro.autores`: M:N propietario con tabla `libro_autor`.
- `Autor.libros`: M:N inverso.
- `Libro.detalle`: 1:1 opcional, cascada y eliminación de huérfano para su dependiente.

No añadas borrado en cascada a autores o editoriales compartidos. En Workbench observa las claves y la tabla de unión cuando el proveedor prepare el esquema; que existan campos en Java no demuestra que sus asociaciones estén mapeadas.

La primera asociación sirve de referencia resuelta: **añade este mapeo** al atributo ya existente `Libro.editorial`, sustituyendo su `@Transient`. Es solo ese atributo, no un archivo completo:

```java
@ManyToOne(optional = false, fetch = FetchType.LAZY)
@JoinColumn(name = "editorial_id", nullable = false)
private Editorial editorial;
```

Cada libro referencia una editorial existente y su clave queda en `libro.editorial_id`. `optional = false` expresa la relación obligatoria; `LAZY` solicita carga diferida. **Ahora te toca** completar `Editorial.libros`: ¿qué nombre de atributo debe indicar `mappedBy`? Consulta [1:N](relaciones.md) antes de seguir con M:N y 1:1. No sustituyas el atributo por otro ID Java independiente.

Hazlo en tres pasos en `src/main/java/es/mjsplayground/acda/hibernate/biblioteca/model/entity`: primero `Libro.editorial` y `Editorial.libros`; después `Libro.autores` y `Autor.libros`; por último `Libro.detalle`. En cada pareja identifica el atributo que escribe la clave o la tabla de unión antes de añadir anotaciones. `mappedBy` contiene el **nombre del atributo Java propietario**, no el de una columna SQL. Compara tus anotaciones con [las relaciones](relaciones.md), sin añadir cascadas por ensayo y error.

??? tip "Pista — Propiedad y ciclo de vida"

    En M:N, compara los dos `Set`: solo uno declara `@JoinTable`; el otro apunta al atributo propietario con `mappedBy`. En 1:1, pregunta si el detalle puede compartirse y qué debe ocurrir al retirarlo. La [teoría de relaciones](relaciones.md) muestra la sintaxis; decide qué opciones corresponden a cada asociación antes de aplicarlas.

**Comprueba:** identifica en el código las cinco asociaciones y explica dónde se guarda cada vínculo. Las fases `uno-muchos`, `muchos-muchos` y `uno-uno` comprueban cada paso sin necesitar MySQL; `mapeos` reúne las cinco asociaciones. Todavía no certifican el esquema ni su ciclo de vida real. La interfaz seguirá anunciando consultas pendientes; no esperes todavía un catálogo funcional.

## Etapa 2. Sesiones, repositorios y consultas

Lee [sesiones y transacciones](persistencia.md) y [HQL básico](consultas.md). Antes de programar, observa `CatalogoRepository.editorial`, **ya resuelto en el starter**:

```java
public Editorial editorial(Session s, long id) {
    return s.find(Editorial.class, id);
}
```

Recibe una sesión abierta por el servicio y busca una identidad. Devuelve una entidad gestionada o `null` si no existe; no inventa una editorial vacía. Localiza también `insertar` y `eliminar` de esa clase: `persist` incorpora un objeto nuevo y `remove` marca uno gestionado para borrado. Ninguno abre o confirma una transacción.

### 2A. Recuperar y mantener entidades

**Ahora te toca:** en `model/repository/LibroRepository.java`, completa `buscar(Session,long)`, `insertar(Session,Libro)` y `eliminar(Session,Libro)` adaptando esos métodos de referencia. Conserva el `flush` preparado. Una búsqueda inexistente debe devolver `null`; el servicio preparado decide cuándo eso constituye un error de negocio.

**Comprueba:** puedes ejecutar la fase optativa `repositorio`, que prepara sus propios libros sin utilizar `guardarLibro`, `datosDemo` ni `listar` pendientes. Inserta y elimina un tercer libro y conserva los dos anteriores y sus autores. Sin JUnit, observa con el depurador el patrón preparado de mantenimiento de autor/editorial; comprobarás tus operaciones de libros desde la interfaz al completar la etapa 3. Todavía no funcionará **Guardar libro**.

### 2B. Integrar una consulta y adaptar otra

En HQL se consultan entidades y atributos Java. Este método es una **referencia completa para añadir** en lugar del TODO de `CatalogoRepository.editoriales`; las consultas más complejas se construirán después:

```java
public List<Editorial> editoriales(Session s) {
    return s.createSelectionQuery(
        "from Editorial e order by e.nombre,e.id", Editorial.class)
        .getResultList();
}
```

`Editorial.class` fija el tipo del resultado. `getResultList()` devuelve una lista, vacía si no hay datos; ordenar también por ID hace estable el orden entre nombres iguales. **Ahora te toca:** adapta el patrón a `CatalogoRepository.autores` con su entidad y el mismo criterio de orden.

**Comprueba:** en la interfaz, crea una editorial **Horizonte** y dos autores **Alex Ríos** y **Luz Vega** desde sus pestañas; pulsa **Actualizar** y verifica esos catálogos. El listado de libros puede seguir anunciando su TODO: se completa a continuación. Si hay datos previos, utiliza nombres nuevos y consérvalos. La fase optativa `catalogos` contrasta dos editoriales y dos autores con datos aislados.

### 2C. Filtrar libros con HQL

**Ahora te toca:** completa `LibroRepository.listar(Session,String,Long)`. Debe aceptar texto de título y editorial opcional y devolver libros ordenados por título e ID. No cambies el contrato ni abras otra sesión.

Utiliza consultas tipadas con parámetros. La consulta habla de propiedades Java; no uses `editorial_id` como si fuera una propiedad HQL.

Este fragmento **ilustrativo dentro de `listar`** muestra una consulta sin filtros. No es la implementación final del método:

```java
List<Libro> encontrados = s.createSelectionQuery(
    "from Libro l order by l.titulo, l.id", Libro.class).getResultList();
// Añade los filtros parametrizados explicados en consultas.md.
```

No concatenes el texto introducido por la persona en ese HQL. `find` busca una identidad; esta consulta devuelve una lista. Ninguna operación abre por sí misma una nueva sesión.

??? tip "Pista — Parámetros de una consulta"

    Observa `CatalogoRepository.editorialUsada`, preparado: declara `:id`, enlaza su valor con `setParameter` y obtiene un resultado. Para el título necesitas `LIKE`; el filtro de editorial solo se añade cuando hay una identidad. No confundas un parámetro con concatenar su valor al texto HQL.

**Comprueba:** la fase `consultas` prepara sus propios datos: sin filtro devuelve dos libros; `Java` con Horizonte devuelve uno; `Java` con Norte devuelve cero. La interfaz ya debe mostrar un listado vacío sin aviso de TODO si tu base no tiene libros. Tras la etapa 3 podrás repetir manualmente los filtros con fichas reales. Si sale cero siempre, inspecciona parámetros y propiedades antes de cambiar el servicio.

## Etapa 3. Crear y editar libros

Antes de escribir el **TODO 3**, observa `BibliotecaService.guardarAutor`, preparado: valida el nombre, delega una tarea al helper `transaccion`, recupera o construye la entidad y devuelve su ID. Pon un breakpoint en `transaccion`: el helper abre/cierra la sesión, confirma si la tarea termina y revierte si falla. La entidad nueva es transitoria antes de `persist`; una recuperada en esa sesión está gestionada. Al cerrarse la sesión queda separada y la UI recibe valores preparados, no una sesión abierta.

**Ahora te toca:** completa `model/service/BibliotecaService.guardarLibro(Long,Long,LibroSolicitud)` por dos avances: primero alta; después edición de la misma identidad. `validar`, `editorial`, `autores`, `requerido`, `comprobarVersion` y `transaccion` ya están preparados. Decide cómo coordinarlos y dónde termina la operación atómica; no reconstruyas su infraestructura ni cambies `@Version`.

Las reglas son:

- Editoriales y autores: nombre de 1–80 caracteres, único en su catálogo.
- Libro: ISBN de trece cifras y único, título de 1–120 y precio de 0,01–10.000 euros con hasta dos decimales.
- Editorial existente y entre uno y cinco autores existentes distintos.
- Detalle opcional: idioma de 2–30 caracteres y páginas enteras de 1–10.000, o ambos datos vacíos.

El ISBN se comprueba como identificador de trece cifras; este ejercicio no calcula su dígito de control. Se recortan espacios exteriores de los textos, conservando saltos interiores.

La demostración solo se prepara mediante el botón correspondiente cuando todos los catálogos están vacíos. Si ya creaste datos de trabajo, puedes seguir con ellos: no hace falta borrarlos para probar que las altas funcionan.

En una base vacía, la demostración crea Horizonte, Norte, Alex Ríos y Luz Vega. `Java y datos` cuesta 29,90 €, pertenece a Horizonte, tiene ambos autores y detalle Español/320 páginas. `Historias locales` cuesta 18,50 €, pertenece a Norte, tiene a Alex y no tiene detalle.

Prueba **Nuevo → Guardar**, **Editar → Cancelar** y edición confirmada. Cambiar el precio no debe borrar autores ni detalle. La UI devuelve la versión que abrió y el servicio rechaza una edición si el libro ya cambió.

**Comprueba el alta:** crea un libro de ISBN `9780000000003`, título `Ensayo`, precio `12,50`, con la editorial y autores creados en 2B, inicialmente sin detalle. Debe aparecer una sola ficha y recuperarse tras **Actualizar**. Prueba después un precio cero: no debe aparecer otro libro. Todavía puedes tener pendiente la edición y la demostración.

**Comprueba la edición:** cambia el título a `Ensayo revisado` y el precio a `13,50`; conserva ISBN, editorial y autores. Cancela otra edición: el listado mantiene el último dato guardado. La fase `servicio` comprueba edición y versión preparada, sin necesitar `datosDemo`.

En la rama de edición, estas llamadas pertenecen al **interior de la misma tarea transaccional**:

```java
Libro actual = requerido(s, id);
comprobarVersion(actual, version); // Apoyo entregado para el formulario abierto.
actual.setTitulo(r.titulo().strip());
// Completa los demás valores y asociaciones sin crear otra identidad de libro.
```

`r` es la solicitud ya validada y `s` la sesión recibida por la lambda. El setter cambia una entidad gestionada; Hibernate sincroniza sus cambios dentro de la transacción. El detalle y los autores requieren sus decisiones de ciclo de vida, explicadas en [relaciones](relaciones.md); no basta copiar todos los campos indiscriminadamente.

??? tip "Pista: primero recuperar las identidades relacionadas"

    Elegir una editorial o autor existente no equivale a crear otro objeto con su nombre. Recupera esas entidades en la sesión del caso de uso y asigna las relaciones desde su lado propietario. El detalle nuevo tiene otro ciclo de vida y puede persistirse con la cascada del libro.

**Integra con mayor autonomía:** completa ahora `BibliotecaService.datosDemo`. Crea el conjunto descrito arriba en una única operación y recházalo si cualquiera de los catálogos ya contiene datos. No utilices varias transacciones independientes ni llames a un método que confirme cada registro por separado. En tu base de trabajo, pulsar **Preparar demostración** debe rechazar la acción sin borrar las fichas existentes. Para probar la creación completa necesitas un esquema local vacío distinto del anterior: créalo desde Workbench y configura `MYSQL_URL`, `MYSQL_USER` y `MYSQL_PASSWORD` en **Environment variables** de Run con una cuenta que tenga permisos sobre ese esquema. La aplicación crea las tablas, pero no el esquema MySQL. No vacíes tu volumen para repetir el ensayo.

## Etapa 4. Comprobar las tres relaciones

Crea un libro con detalle, retira ese detalle y guarda: el libro permanece y su fila de detalle desaparece. Añade otro detalle y comprueba su recuperación.

Quita un autor de un libro manteniendo al menos otro: debe desaparecer el vínculo, no la fila del autor. Si un autor sigue asociado a otro libro, continúa disponible allí. No se permite eliminar directamente un autor o editorial que están siendo utilizados.

Elimina un libro de prueba con detalle y comprueba que desaparecen su dependiente y sus vínculos, conservándose autores y editorial. Revisa el resultado con una operación nueva y en Workbench; no basta observar la lista que había en memoria.

## Etapa 5. Errores, rollback y consulta

Busca por título, por editorial y con filtros combinados. Un apóstrofo no debe romper la consulta. Los comodines `%` y `_` conservan su significado de `LIKE`, como explica la teoría.

Intenta ISBN repetido, precio inválido, autor inexistente y detalle incompleto. Ningún rechazo debe dejar cambios parciales. El log conserva la causa técnica y la UI mantiene los datos necesarios para corregir.

La solución contiene pruebas de relaciones, versión y rollback. Se ejecutan sobre un esquema de pruebas aislado, sin vaciar tu catálogo. Si una prueba no conecta con MySQL porque no activaste su entorno, esa omisión no demuestra que la persistencia haya pasado. Consulta [diagnóstico y pruebas](../ayuda/diagnostico.md) y el README para las opciones técnicas.

### Pruebas preparadas para avanzar

El starter entrega `src/test/java/es/mjsplayground/acda/hibernate/biblioteca/FasesGuiadoTest.java` y `ApoyoFasesMysql.java`. En una configuración JUnit de IntelliJ selecciona la primera clase y añade **VM options** `-Dfase=mapeos`. Para `catalogos`, `repositorio`, `consultas`, `servicio` o `rollback`, añade también `-Dqa.mysql=true` y utiliza el servidor local de desarrollo. El README explica las variables si usas otro puerto. `-Dfase=completo` selecciona todas. Los tests crean y eliminan solo su esquema UUID, nunca el catálogo de la aplicación.

La fase `rollback` **ya incorpora el fallo**: un repositorio de prueba ejecuta `flush`, comprueba que se ha insertado el tercer libro dentro de la transacción y lanza una excepción. El test espera la causa original y abre una operación nueva para comprobar que libros y detalles recuperan sus cantidades previas. No necesitas cortar MySQL, alterar credenciales ni escribir infraestructura de errores.

Comando opcional: `./mvnw -Dtest=FasesGuiadoTest -Dfase=rollback -Dqa.mysql=true test` (Windows: `mvnw.cmd`). Un test omitido no acredita una fase; un fallo antes de completar su TODO te indica qué comportamiento falta.

## Registro del recorrido

<div class="mjs-progress-panel" role="status" aria-live="polite"><span class="mjs-progress-label">Tu avance en el proyecto</span><strong class="mjs-progress-count">0 de 5 pasos</strong><div class="mjs-progress-track"><div class="mjs-progress-fill"></div></div></div>

<div class="mjs-task"><input type="checkbox" id="biblioteca-etapa-1"><label for="biblioteca-etapa-1">He mapeado las tres cardinalidades y puedo identificar sus propietarios.</label></div>

<div class="mjs-task"><input type="checkbox" id="biblioteca-etapa-2"><label for="biblioteca-etapa-2">Los repositorios utilizan la sesión recibida y las consultas parametrizadas.</label></div>

<div class="mjs-task"><input type="checkbox" id="biblioteca-etapa-3"><label for="biblioteca-etapa-3">Puedo crear y editar libros sin perder sus relaciones y compruebo las validaciones.</label></div>

<div class="mjs-task"><input type="checkbox" id="biblioteca-etapa-4"><label for="biblioteca-etapa-4">He verificado cascadas, retirada de detalle y conservación de autores compartidos.</label></div>

<div class="mjs-task"><input type="checkbox" id="biblioteca-etapa-5"><label for="biblioteca-etapa-5">He contrastado consultas, errores, versión y reversión de cambios.</label></div>

## Solución completa

Antes de descargarla, comprueba el conjunto:

| Acción | Resultado esperado |
| --- | --- |
| Crear, editar y buscar un libro | Una identidad; sus relaciones y valores se recuperan en una consulta nueva. |
| ISBN repetido, precio inválido o detalle incompleto | Error comprensible; ninguna escritura parcial. |
| Retirar detalle o vínculo de autor | Desaparece solo el dependiente o vínculo correspondiente. |
| Borrar libro con detalle | Libro y detalle desaparecen; autores y editorial permanecen. |
| Reiniciar Launcher y pulsar Actualizar | MySQL conserva las fichas guardadas, sin volver a sembrarlas. |
| Repetir demostración en una base con datos | Rechazo; el catálogo anterior permanece. |

**Explica con tus palabras:** ¿por qué `mappedBy` usa un atributo Java? ¿Por qué no se borra en cascada un autor compartido? ¿Quién abre y confirma la transacción? ¿Por qué una entidad gestionada puede editarse sin llamar a `merge`? ¿Qué problema evita preparar `LibroDatos` antes de cerrar la sesión?

[Descargar solución del guiado](/fp-dam2-acda/descargas/codigo/biblioteca-guiado-solucion.zip){ .md-button download="biblioteca-guiado-solucion.zip" }

[Ver código, README y pruebas](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/hibernate/guiado-solucion)

La solución mantiene la misma interfaz y entrada `app.Launcher`. Contrasta propiedad de la sesión, preparación de `LibroDatos` y pruebas de conservación antes de comenzar [la práctica final](practica-final.md).
