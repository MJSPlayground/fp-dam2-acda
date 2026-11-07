# Proyecto guiado: catálogo de biblioteca

La biblioteca necesita mantener editoriales, autores, libros y sus detalles de publicación. El proyecto integra las tres cardinalidades en una sola aplicación JavaFX, con controles y formularios preparados. El alumnado completa mapeos, repositorios y servicio.

## Preparar y ejecutar

[Descargar proyecto inicial](/fp-dam2-acda/descargas/codigo/biblioteca-guiado-inicial.zip){ .md-button .md-button--primary download="biblioteca-guiado-inicial.zip" }

Descomprime y abre en IntelliJ la carpeta con `pom.xml`. Espera la sincronización Maven y selecciona el JDK siguiendo [preparación de proyectos](../ayuda/proyectos-maven.md).

1. Inicia Docker Desktop.
2. Ejecuta `es.mjsplayground.acda.hibernate.biblioteca.config.MySqlLocal.main`, con esta carpeta del proyecto como **Working directory**.
3. Ejecuta `es.mjsplayground.acda.hibernate.biblioteca.app.Launcher.main`.

El servidor local escucha en 3308 y el guiado utiliza `biblioteca_guiado`. La [configuración del tema](configuracion.md) explica conexión, Workbench y esquema. Para cambiar de starter a solución cierra la aplicación anterior; no es necesario borrar el servidor o su volumen.

La UI puede arrancar mientras existen TODO. Sus avisos no simulan persistencia. Completa las etapas en orden antes de registrar libros: una asociación marcada como transitoria todavía no representa la relación que exige el catálogo.

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

## Etapa 1. Completar asociaciones

Lee [arquitectura](arquitectura.md), [entidades](entidades.md) y [relaciones](relaciones.md). El **TODO 1** marca asociaciones que temporalmente tienen `@Transient` para permitir el arranque.

Sustituye esas marcas por:

- `Libro.editorial`: N:1 propietario, editorial obligatoria.
- `Editorial.libros`: 1:N inverso mediante `mappedBy`.
- `Libro.autores`: M:N propietario con tabla `libro_autor`.
- `Autor.libros`: M:N inverso.
- `Libro.detalle`: 1:1 opcional, cascada y eliminación de huérfano para su dependiente.

No añadas borrado en cascada a autores o editoriales compartidos. En Workbench observa las claves y la tabla de unión cuando el proveedor prepare el esquema; que existan campos en Java no demuestra que sus asociaciones estén mapeadas.

## Etapa 2. CRUD y consultas de repositorio

Lee [sesiones y transacciones](persistencia.md) y [HQL básico](consultas.md). Completa el **TODO 2** de `LibroRepository`: listar, buscar, insertar y eliminar, además de las consultas de editoriales y autores de `CatalogoRepository`.

Utiliza la sesión recibida y consultas tipadas con parámetros. El listado admite texto de título y editorial opcional, y ordena por título e ID. La consulta habla de propiedades Java; no uses `editorial_id` como si fuera una propiedad HQL.

Las operaciones preparadas del servicio permiten mantener editoriales y autores. Comprueba altas y consultas de estos catálogos antes de registrar libros. Un nombre repetido debe rechazarse con un mensaje comprensible; un fallo de conexión no debe mostrarse como una lista vacía.

## Etapa 3. Crear y editar libros

Completa el **TODO 3** de `BibliotecaService.guardarLibro` y `datosDemo`, usando validaciones y transacción de apoyo. El caso de uso recupera editorial y autores en esa sesión antes de relacionarlos con el libro.

Las reglas son:

- Editoriales y autores: nombre de 1–80 caracteres, único en su catálogo.
- Libro: ISBN de trece cifras y único, título de 1–120 y precio de 0,01–10.000 euros con hasta dos decimales.
- Editorial existente y entre uno y cinco autores existentes distintos.
- Detalle opcional: idioma de 2–30 caracteres y páginas enteras de 1–10.000, o ambos datos vacíos.

El ISBN se comprueba como identificador de trece cifras; este ejercicio no calcula su dígito de control. Se recortan espacios exteriores de los textos, conservando saltos interiores.

La demostración solo se prepara mediante el botón correspondiente cuando todos los catálogos están vacíos. Si ya creaste datos de trabajo, puedes seguir con ellos: no hace falta borrarlos para probar que las altas funcionan.

En una base vacía, la demostración crea Horizonte, Norte, Alex Ríos y Luz Vega. `Java y datos` cuesta 29,90 €, pertenece a Horizonte, tiene ambos autores y detalle Español/320 páginas. `Historias locales` cuesta 18,50 €, pertenece a Norte, tiene a Alex y no tiene detalle.

Prueba **Nuevo → Guardar**, **Editar → Cancelar** y edición confirmada. Cambiar el precio no debe borrar autores ni detalle. La UI devuelve la versión que abrió y el servicio rechaza una edición si el libro ya cambió.

??? tip "Pista: primero recuperar las identidades relacionadas"

    Elegir una editorial o autor existente no equivale a crear otro objeto con su nombre. Recupera esas entidades en la sesión del caso de uso y asigna las relaciones desde su lado propietario. El detalle nuevo tiene otro ciclo de vida y puede persistirse con la cascada del libro.

## Etapa 4. Comprobar las tres relaciones

Crea un libro con detalle, retira ese detalle y guarda: el libro permanece y su fila de detalle desaparece. Añade otro detalle y comprueba su recuperación.

Quita un autor de un libro manteniendo al menos otro: debe desaparecer el vínculo, no la fila del autor. Si un autor sigue asociado a otro libro, continúa disponible allí. No se permite eliminar directamente un autor o editorial que están siendo utilizados.

Elimina un libro de prueba con detalle y comprueba que desaparecen su dependiente y sus vínculos, conservándose autores y editorial. Revisa el resultado con una operación nueva y en Workbench; no basta observar la lista que había en memoria.

## Etapa 5. Errores, rollback y consulta

Busca por título, por editorial y con filtros combinados. Un apóstrofo no debe romper la consulta. Los comodines `%` y `_` conservan su significado de `LIKE`, como explica la teoría.

Intenta ISBN repetido, precio inválido, autor inexistente y detalle incompleto. Ningún rechazo debe dejar cambios parciales. El log conserva la causa técnica y la UI mantiene los datos necesarios para corregir.

La solución contiene pruebas de relaciones, versión y rollback. Se ejecutan sobre un esquema QA aislado, sin vaciar tu catálogo. Si una prueba no conecta con MySQL porque no activaste su entorno, esa omisión no demuestra que la persistencia haya pasado. Consulta [diagnóstico y pruebas](../ayuda/diagnostico.md) y el README para las opciones técnicas.

## Registro del recorrido

<div class="mjs-progress-panel" role="status" aria-live="polite"><span class="mjs-progress-label">Tu avance en el proyecto</span><strong class="mjs-progress-count">0 de 5 pasos</strong><div class="mjs-progress-track"><div class="mjs-progress-fill"></div></div></div>

<div class="mjs-task"><input type="checkbox" id="biblioteca-etapa-1"><label for="biblioteca-etapa-1">He mapeado las tres cardinalidades y puedo identificar sus propietarios.</label></div>

<div class="mjs-task"><input type="checkbox" id="biblioteca-etapa-2"><label for="biblioteca-etapa-2">Los repositorios utilizan la sesión recibida y las consultas parametrizadas.</label></div>

<div class="mjs-task"><input type="checkbox" id="biblioteca-etapa-3"><label for="biblioteca-etapa-3">Puedo crear y editar libros sin perder sus relaciones y compruebo las validaciones.</label></div>

<div class="mjs-task"><input type="checkbox" id="biblioteca-etapa-4"><label for="biblioteca-etapa-4">He verificado cascadas, retirada de detalle y conservación de autores compartidos.</label></div>

<div class="mjs-task"><input type="checkbox" id="biblioteca-etapa-5"><label for="biblioteca-etapa-5">He contrastado consultas, errores, versión y reversión de cambios.</label></div>

## Solución completa

[Descargar solución del guiado](/fp-dam2-acda/descargas/codigo/biblioteca-guiado-solucion.zip){ .md-button download="biblioteca-guiado-solucion.zip" }

[Ver código, README y pruebas](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/hibernate/guiado-solucion)

La solución mantiene la misma interfaz y entrada `app.Launcher`. Contrasta propiedad de la sesión, preparación de `LibroDatos` y pruebas de conservación antes de comenzar [la práctica final](practica-final.md).
