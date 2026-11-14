# Proyecto guiado: equipos y jugadores

Una organización deportiva necesita mantener equipos y sus plantillas. La aplicación muestra equipos, jugadores y estadísticas con una interfaz web preparada. El trabajo se concentra en documentos MongoDB, repositorios, servicios y actualizaciones seguras.

Al terminar podrás mantener equipos y jugadores, buscar por categoría y posición, paginar y sumar estadísticas sin perder cambios de otra operación. Necesitas manejar records, listas, excepciones, inyección por constructor y consultas Spring Data del [tema de Spring](../spring/proyecto.md); el modelo documental y la atomicidad se explican durante este recorrido.

## Preparar el proyecto

[Descargar proyecto inicial](/fp-dam2-acda/descargas/codigo/jugadores-guiado-inicial.zip){ .md-button .md-button--primary download="jugadores-guiado-inicial.zip" }

Abre en IntelliJ la carpeta que contiene `pom.xml`, espera la sincronización y selecciona el JDK de [preparación de proyectos](../ayuda/proyectos-maven.md). No tienes que escribir HTML, CSS ni JavaScript. El frontend conoce el contrato de las operaciones que completarás.

## Arranque desde IntelliJ

1. Inicia Docker Desktop y espera a que el motor esté disponible.
2. Ejecuta `es.mjsplayground.acda.mongodb.liga.config.MongoLocal.main`. El lanzador utiliza el `compose.yaml` de la carpeta del proyecto: comprueba ese **Working directory** en su configuración Run.
3. Cuando MongoDB esté disponible, ejecuta `es.mjsplayground.acda.mongodb.liga.LigaApplication.main` y abre `http://localhost:8080`.

El servidor de datos publica `127.0.0.1:27018` y la base es `acda_liga`. Para Compass utiliza `mongodb://127.0.0.1:27018/acda_liga`. La [guía de contenedores](../ayuda/contenedores.md) explica el puerto y el volumen; la [configuración del tema](configuracion.md) indica qué observar en Compass.

Para cambiar de proyecto inicial a solución, detén primero la aplicación que ocupa 8080. El mismo MongoDB local puede seguir disponible. Los datos se conservan en su volumen; reiniciar Java no debe vaciar los equipos.

El catálogo preparado contiene las sedes `CENTRO` —Pabellón Centro— y `NORTE` —Campo Norte—, ambas en Bilbao. No hace falta desarrollar otro CRUD de sedes. Las funciones pendientes del starter comunican su estado y no simulan un guardado.

**Primera comprobación, sin programar:** abre **Nuevo equipo**, comprueba que puedes elegir las dos sedes y cancela. La lista de equipos no debe cambiar. También puedes abrir `http://localhost:8080/api/sedes`: debe responder con las dos sedes; esta lectura está preparada. Si todavía no has creado equipos, el listado sin filtros estará vacío. No selecciones filtros nuevos esperando que funcionen antes de completar las consultas.

## Cómo se reparte el trabajo

```text
Interfaz web preparada → LigaController → LigaService
                                             ↓
                     EquipoRepository / SedeRepository / EstadisticasRepository
                                             ↓
                          Spring Data / MongoTemplate → MongoDB
```

`Equipo` es el agregado persistido. Incluye jugadores y sus estadísticas; `sedeId` referencia otro documento. `EquipoRepository` ofrece operaciones generales y consultas; `EstadisticasRepository` contiene una actualización atómica avanzada **completamente preparada**. No tienes que implementar `MongoTemplate`, `Criteria` ni `$inc`: tu tarea es utilizar esa ayuda desde el servicio y comprender sus garantías. El servicio aplica las reglas y el controlador conserva el contrato HTTP aprendido en [Firestore](../firestore/rest.md).

Los modelos, DTO, catálogo de sedes, controladores y frontend ya existen. Modificarás tres consultas de `EquipoRepository` y los métodos señalados de `LigaService`; los validadores y el ayudante `conJugadores` se conservan. El alta de equipo, los cambios de plantilla y la suma de estadísticas son casos de uso distintos: los completarás y comprobarás por separado.

## Comprobar una parte sin terminar las siguientes

Abre `src/test/java/es/mjsplayground/acda/mongodb/liga/FasesAprendizajeTest.java` en IntelliJ. Crea su configuración JUnit y añade **VM options** `-Daprendizaje.fase=consultas`. Cambia el valor al alcanzar cada paso:

| Fase | Qué comprueba | Qué no exige todavía |
| --- | --- | --- |
| `consultas` | Categoría, posición, combinación, orden y página vacía con documentos preparados | Crear equipos o jugadores desde el servicio |
| `equipos` | Alta, sede válida y edición conservando la plantilla de una fixture | Operaciones de jugadores o estadísticas |
| `jugadores` | Añadir, editar y retirar jugadores sin repetir dorsal ni reiniciar cifras | El TODO de estadísticas |
| `estadisticas` | Coordinación con el helper, contadores, copia antigua y ocho incrementos concurrentes | Programar concurrencia o una operación MongoTemplate |

Mantén abierto MongoDB. Los tests preparan una base `acda_qa_fases_...` exclusiva y la eliminan al terminar; no limpian `acda_liga`. Las fixtures crean los datos del paso que quieres comprobar: no necesitas resolver operaciones futuras para cargarlos. Sin opción, estas pruebas se omiten. `todas` sirve al cerrar el guiado, no al empezar.

Comprueba cada etapa antes de utilizarla como base de la siguiente; puedes hacerlo manualmente desde la web. Si eliges las pruebas optativas, revisa sus fallos y no cuentes las omitidas como superadas. Una prueba de servicio no comprueba por sí sola formularios, selección ni cancelación.

## Etapa 1. Comprender el modelo

Lee [BSON y modelado](documentos.md). Localiza `Equipo`, `Jugador`, `Estadisticas` y `Sede`; identifica `@Document`, `@Id`, `@Version` y la lista embebida. Distingue un ID de equipo de 24 caracteres hexadecimales del identificador de un jugador de su plantilla.

Un equipo tiene nombre de 1 a 60 caracteres, categoría `JUNIOR` o `SENIOR` y sede existente. Cada jugador tiene nombre de 1 a 60 caracteres, dorsal entre 1 y 99 y posición `PORTERO`, `DEFENSA`, `MEDIO` o `DELANTERO`. La plantilla admite como máximo veinte jugadores y no repite dorsales.

Las estadísticas iniciales son cero y la disponibilidad inicial es verdadera. La identidad, la versión y las estadísticas no se inventan en el formulario de alta de jugador. La edición de sus datos personales debe conservar los contadores existentes.

Observa esta **declaración ya preparada** de `model/entity/Equipo.java` (se omiten las anotaciones de índices para centrarnos en los datos):

```java
@Document("equipos")
public record Equipo(
    @Id String id, String nombre, String categoria, String sedeId,
    List<Jugador> jugadores, @Version Long version) {}
```

`@Document` selecciona la colección y `@Id` corresponde a `_id`. `jugadores` se guarda dentro del mismo documento; `sedeId` es solo la identidad de otra sede, no una sede embebida ni una carga automática. `@Version` permite reconocer una copia antigua. No añadas esta declaración como otra entidad: localiza los mismos componentes en el archivo real y abre después `Jugador` y `Estadisticas` para seguir el anidamiento.

## Etapa 2. Repositorios y consultas

Lee [persistencia Spring Data](persistencia.md) y [consultas](consultas.md). En `src/main/java/es/mjsplayground/acda/mongodb/liga/model/repository/EquipoRepository.java`, completa el **TODO 2**: sustituye cada método `default` pendiente por la declaración de consulta que Spring Data implementará. Conserva sus firmas con `Page<Equipo>` y `Pageable`; no añadas un cuerpo Java que recorra todos los documentos para imitar la consulta. El método derivado por categoría y la consulta `@Query` por posición muestran dos maneras de expresar una búsqueda.

Primero observa **dos declaraciones resueltas que debes integrar** en lugar de sus apoyos `default`. La consulta derivada se expresa en el nombre; `@Query` introduce una condición sobre el jugador embebido. En `EquipoRepository`, conserva las firmas, elimina los cuerpos que lanzan la excepción de TODO y termina cada declaración con `;`:

```java
Page<Equipo> findByCategoriaOrderByNombreAsc(String categoria, Pageable pageable);

@Query("{ 'jugadores.posicion': ?0 }")
Page<Equipo> porPosicion(String posicion, Pageable pageable);
```

`findByCategoria...` devuelve equipos de esa categoría. `?0` recibe el primer argumento de `porPosicion`: por ejemplo, `MEDIO` selecciona un equipo que tenga algún jugador en esa posición. El punto navega por el documento embebido. `Pageable` recibe página, tamaño y orden construidos por el servicio; una `Page` contiene resultados y recuentos, no solo una lista. Añade el import de `org.springframework.data.mongodb.repository.Query` a la interfaz si IntelliJ lo solicita.

**Ahora te toca:** completa `porCategoriaYPosicion(String categoria, String posicion, Pageable pageable)`, el tercer método del **TODO 2**. Ya conoces los campos y las dos condiciones por separado; combínalas en una `@Query` conservando el retorno y la paginación. No resuelvas el filtro leyendo toda la colección en Java.

??? tip "Pista — Dos parámetros, dos condiciones"

    En una consulta `@Query`, `?0` corresponde al primer argumento y `?1` al segundo. Revisa en [consultas](consultas.md) cómo un mismo documento debe satisfacer varias condiciones. `Pageable` no es un valor que debas comparar con un campo.

La API utiliza `GET /api/equipos?categoria=SENIOR&pagina=0&tamanio=10`, y permite añadir `posicion`. `pagina` empieza en cero; se admiten páginas hasta 10.000 y tamaños de 1 a 20. La respuesta contiene `elementos`, `totalElementos`, `totalPaginas` y `pagina`.

Ejecuta la fase `consultas`, que ya prepara dos equipos de categorías distintas y un jugador MEDIO sin utilizar el servicio pendiente. Comprueba primero sin filtros, luego una categoría y después una posición con jugador existente. La consulta selecciona equipos: sus plantillas no se recortan automáticamente a los jugadores que produjeron la coincidencia.

Desde la web puedes comprobar ya que un filtro no provoca el aviso de TODO y que una página vacía sigue siendo una respuesta correcta. Para comprobar manualmente coincidencias reales, repite los filtros cuando hayas creado los equipos y jugadores en 3a/3b; no necesitas crearlos ahora para ejecutar la fase optativa, que aporta sus propios datos.

## Etapa 3a. Crear y editar equipos

Abre `model/service/LigaService.java` y completa `crear` y `actualizar`, señalados **TODO 3a**. Ya tienes `validar`, `obtener`, el catálogo de sedes y el repositorio inyectado. Un alta empieza sin ID ni versión y con plantilla vacía; la edición recupera el equipo y conserva sus jugadores y su versión.

Antes de escribir, lee este **método completo ya implementado** en ese servicio:

```java
public Equipo obtener(String id) {
    id(id);
    return equipos.findById(id)
        .orElseThrow(() -> new LigaException(404, "Equipo no encontrado"));
}
```

La primera llamada comprueba el formato del ID. Después el repositorio busca el documento y devuelve un `Optional`; el servicio decide que una ausencia significa `404`. Un fallo del servidor es otro error y no se convierte en «no encontrado». Reutiliza este caso de uso al editar, en lugar de suponer que una identidad recibida demuestra que existe un equipo.

**Ahora te toca:** completa primero `crear`: valida la solicitud y persiste un equipo nuevo con los datos normalizados. Después completa `actualizar`: cambia solo nombre, categoría y sede sobre la identidad recuperada. Revisa `insert` y `save` en [persistencia](persistencia.md); no tienen la misma finalidad cuando el documento ya existe.

`Equipo` es un record: no cambia sus componentes mediante setters. Construye el estado propuesto a partir del documento recuperado. Este fragmento solo muestra la conservación, sin resolver el caso de uso:

```java
// e es el equipo recuperado. Los datos editables proceden de la solicitud.
List<Jugador> plantillaAnterior = e.jugadores();
Long versionLeida = e.version();
// Utiliza ambos al construir el nuevo estado; guarda mediante el repositorio.
```

Ejecuta `equipos`: debe poder crear y editar sin necesitar `agregarJugador`, que todavía está pendiente. Una sede desconocida se rechaza; editar un nombre no elimina la plantilla preparada por la prueba. `PUT` sobre un equipo inexistente devuelve `404`, nunca un alta accidental.

El índice único protege el nombre de equipo. Un duplicado se comunica como conflicto y no reemplaza el documento existente. No captures esa excepción para devolver un éxito sin guardar.

**Comprueba desde la web:** crea «Equipo prueba» como SENIOR/CENTRO; edita el nombre y cambia la sede a NORTE. Debe seguir siendo un solo equipo. Intenta otro con el mismo nombre y cancela una edición: ambas acciones conservan los datos previos. Añadir jugadores y sumar estadísticas siguen pendientes.

## Etapa 3b. Mantener jugadores embebidos

Completa `agregarJugador`, `editarJugador` y `eliminarJugador` (**TODO 3b**) en el mismo servicio. Reutiliza `validarJugador`, `jugador` y `conJugadores`. La lista propuesta conserva los jugadores no seleccionados; al editar conserva también ID, disponibilidad y estadísticas del seleccionado.

Observa **`conJugadores`, ya preparado**: recibe el equipo y una lista propuesta, construye otro `Equipo` con la misma identidad, sede y versión, y hace una copia de la lista. No añade, elimina ni valida jugadores por ti. Este es el patrón de cambio de un record: propones otro estado y el repositorio lo guarda comprobando la versión.

**Ahora te toca:** empieza por `agregarJugador`, con identidad propia, disponibilidad inicial y cifras a cero. Comprueba el alta antes de continuar con `editarJugador`; después adapta la selección de un jugador para retirarlo con `eliminarJugador`. Decide qué debe conservar cada operación y utiliza `conJugadores` para conservar el resto del equipo. No escribas un segundo documento en una colección de jugadores.

Empieza añadiendo Alex, dorsal 7 y posición MEDIO. Añade otra persona con el mismo dorsal: debe responder `409` sin sustituirlo. Prueba dorsal cero, posición inexistente y el jugador de otro equipo. Después retira únicamente al seleccionado. La plantilla admite veinte personas como máximo.

??? tip "Si cambian las estadísticas al editar un nombre"

    Compara el constructor del jugador editado con el del alta. El alta comienza en cero; la edición utiliza las estadísticas del jugador encontrado. `conJugadores` ayuda a conservar la versión del equipo, pero no decide qué elementos debe contener tu lista.

??? tip "El dorsal de la propia persona no es un duplicado"

    Al editar, identifica al jugador por su ID. Mantener su dorsal es válido; el conflicto aparece si otro jugador del mismo equipo lo utiliza. Comprueba el caso «cambiar solo el nombre» antes de probar un dorsal ocupado por otra persona.

Ejecuta `jugadores`: su fixture ya tiene contadores distintos de cero; así puedes detectar pérdidas aunque todavía no hayas programado la coordinación de estadísticas. Vuelve a abrir la plantilla desde la web, modifica datos y cancela otra edición: cancelar no guarda.

## Etapa 4. Sumar estadísticas de forma segura

Lee [atomicidad y versiones](atomicidad.md) y completa el **TODO 4 de `LigaService.sumarEstadisticas`**. Comprueba equipo, jugador e incrementos; llama a `estadisticas.sumar` y traduce su resultado nulo a un conflicto. El helper de repositorio ya está terminado en el inicial: suma partido, goles, asistencias y versión dentro de una única escritura. No lo reemplaces por leer–modificar–guardar sin protección.

Tras validar las entradas en el servicio, esta llamada utiliza la operación preparada:

```java
Equipo actualizado = estadisticas.sumar(
    id, jugadorId, r.goles(), r.asistencias());
// Si es null, ninguna entidad cumplió jugador y límites: comunica conflicto.
```

El resultado no nulo ya incluye los contadores actualizados. No lo vuelvas a guardar con una versión anterior.

Compara las dos formas de modificación: `editarJugador` propone un documento completo con su versión; el helper de estadísticas incrementa campos de ese documento directamente y también hace avanzar la versión. Si una edición recuperó el documento antes del incremento, su `save` debe fallar en vez de borrar las cifras nuevas. La protección no consiste en volver a guardar a ciegas tras cualquier conflicto.

**Ahora te toca:** completa únicamente `LigaService.sumarEstadisticas`, con comprobaciones de entrada, existencia y resultado del helper. El algoritmo avanzado de `EstadisticasRepository` continúa preparado: léelo junto a [atomicidad](atomicidad.md), sin reconstruirlo ni duplicar el incremento en el servicio.

`PATCH /api/equipos/{id}/jugadores/{jugadorId}/estadisticas` recibe un cuerpo como `{"goles":2,"asistencias":1}`. Ambos incrementos están entre 0 y 20. La versión también debe avanzar para impedir que otra edición basada en un estado anterior borre esos cambios.

En un jugador nuevo, aplicar ese ejemplo debe dejar un partido, dos goles y una asistencia. Aplicarlo otra vez suma otra participación: no es un reemplazo idempotente de sus estadísticas. Un incremento negativo se rechaza con `400`, sin alterar ninguno de los contadores.

Los límites acumulados son 10.000 partidos y 100.000 goles o asistencias. Si una operación supera cualquiera, se rechaza por conflicto y no queda aplicada parcialmente. Ejecuta `estadisticas`. La prueba ya crea hilos y sincroniza sus llamadas: debes obtener los contadores esperados y comprobar que un `save` de la copia antigua es rechazado. No tienes que inventar un lanzador concurrente, modificar el motor ni implementar bloqueos. Estos resultados comprueban el helper preparado y tu coordinación, no solo que la llamada compila.

## Comprobación del recorrido

Comprueba estos escenarios desde la interfaz y las pruebas del proyecto:

- Alta, consulta, edición y eliminación de un equipo de trabajo.
- Jugadores embebidos con identidad propia y referencias de sede válidas.
- Filtros, página vacía y límites de paginación inválidos.
- Dorsal repetido y plantilla llena sin pérdida de jugadores anteriores.
- Incrementos conservados tras una edición y conflicto con una versión antigua.
- Fallo del servidor distinguible de un resultado sin coincidencias.

Utiliza [diagnóstico y logging](../ayuda/diagnostico.md) para seguir un error desde el controlador al driver. En Compass observa el documento completo antes y después; no lo edites manualmente para hacer que un test pase.

Para un recorrido continuo, crea un equipo SENIOR/CENTRO, añade Alex con dorsal 7/MEDIO y Luz con dorsal 8/DEFENSA. Filtra por SENIOR y MEDIO: el equipo debe aparecer con sus dos jugadores. Suma a Alex dos goles y una asistencia: debe quedar con un partido. Edita únicamente su nombre, vuelve a abrir la plantilla y confirma las mismas cifras. Un dorsal repetido o un incremento negativo deben conservar toda la plantilla. Reinicia solo Spring y vuelve a consultar: equipos, jugadores y estadísticas siguen guardados.

**Explica con tus palabras:** ¿qué datos se guardan embebidos y cuáles se referencian? ¿Por qué filtrar por MEDIO no elimina a Luz del resultado? ¿Qué información conserva `conJugadores`? ¿Por qué no se llama a `save` después de `estadisticas.sumar`? ¿Qué debe hacer el usuario al recibir un conflicto de versión?

## Solución completa

[Descargar solución del guiado](/fp-dam2-acda/descargas/codigo/jugadores-guiado-solucion.zip){ .md-button download="jugadores-guiado-solucion.zip" }

[Ver código, README y pruebas](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/mongodb/guiado-solucion)

La solución conserva la interfaz y los puntos de entrada del inicial. Contrasta especialmente la versión, el incremento atómico y la distinción entre entidad persistida y respuesta de consulta antes de comenzar [la práctica final](practica-final.md).
