# Proyecto guiado: equipos y jugadores

Una organización deportiva necesita mantener equipos y sus plantillas. La aplicación muestra equipos, jugadores y estadísticas con una interfaz web preparada. El trabajo se concentra en documentos MongoDB, repositorios, servicios y actualizaciones seguras.

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

## Cómo se reparte el trabajo

```text
Interfaz web preparada → LigaController → LigaService
                                             ↓
                     EquipoRepository / SedeRepository / EstadisticasRepository
                                             ↓
                          Spring Data / MongoTemplate → MongoDB
```

`Equipo` es el agregado persistido. Incluye jugadores y sus estadísticas; `sedeId` referencia otro documento. `EquipoRepository` ofrece operaciones generales y consultas; `EstadisticasRepository` expresa una actualización específica con `MongoTemplate`. El servicio aplica las reglas y el controlador conserva el contrato HTTP aprendido en [Firestore](../firestore/rest.md).

## Etapa 1. Comprender el modelo

Lee [BSON y modelado](documentos.md). Localiza `Equipo`, `Jugador`, `Estadisticas` y `Sede`; identifica `@Document`, `@Id`, `@Version` y la lista embebida. Distingue un ID de equipo de 24 caracteres hexadecimales del identificador de un jugador de su plantilla.

Un equipo tiene nombre de 1 a 60 caracteres, categoría `JUNIOR` o `SENIOR` y sede existente. Cada jugador tiene nombre de 1 a 60 caracteres, dorsal entre 1 y 99 y posición `PORTERO`, `DEFENSA`, `MEDIO` o `DELANTERO`. La plantilla admite como máximo veinte jugadores y no repite dorsales.

Las estadísticas iniciales son cero y la disponibilidad inicial es verdadera. La identidad, la versión y las estadísticas no se inventan en el formulario de alta de jugador. La edición de sus datos personales debe conservar los contadores existentes.

## Etapa 2. Repositorios y consultas

Lee [persistencia Spring Data](persistencia.md) y [consultas](consultas.md). Completa el **TODO 2** de `EquipoRepository` para aplicar consultas. El método derivado por categoría y la consulta `@Query` por posición muestran dos maneras de expresar una búsqueda.

La API utiliza `GET /api/equipos?categoria=SENIOR&pagina=0&tamanio=10`, y permite añadir `posicion`. `pagina` empieza en cero; se admiten páginas hasta 10.000 y tamaños de 1 a 20. La respuesta contiene `elementos`, `totalElementos`, `totalPaginas` y `pagina`.

Comprueba primero sin filtros, luego una categoría y después una posición con jugador existente. La consulta selecciona equipos: sus plantillas no se recortan automáticamente a los jugadores que produjeron la coincidencia.

## Etapa 3. Mantener equipos y plantilla

Completa el **TODO 3** de `LigaService`: creación y actualización de equipos y mantenimiento de jugadores, siguiendo las firmas del proyecto. Usa repositorios para persistir y conserva la versión de la entidad recuperada cuando propongas un nuevo estado. No conviertas un `PUT` sobre un equipo inexistente en un alta accidental. Los controladores están preparados, porque el contrato REST se aprendió en el tema anterior.

Crea un equipo sénior en CENTRO y añade un jugador llamado Alex, dorsal 7 y posición MEDIO. Después añade otra persona con el mismo dorsal: debe rechazarse con `409`, sin sustituir a Alex. Prueba también dorsal cero, posición inexistente y sede desconocida: deben rechazarse como entrada inválida.

Cambia el nombre del equipo y vuelve a abrir su plantilla: los jugadores deben conservarse. Cambia el dorsal del jugador a otro libre y cancela una edición desde la interfaz: cancelar no guarda modificaciones. Comprueba que el alta, la edición y la eliminación corresponden al elemento seleccionado.

El nombre de equipo se protege mediante un índice único. Un intento de duplicarlo debe producir un conflicto comprensible, conservando el dato existente y la causa técnica disponible en el registro.

## Etapa 4. Sumar estadísticas de forma segura

Lee [atomicidad y versiones](atomicidad.md) y completa el **TODO 4** de `EstadisticasRepository.sumar`. La operación debe sumar un partido y los incrementos recibidos de goles y asistencias dentro del mismo documento.

`PATCH /api/equipos/{id}/jugadores/{jugadorId}/estadisticas` recibe un cuerpo como `{"goles":2,"asistencias":1}`. Ambos incrementos están entre 0 y 20. La versión también debe avanzar para impedir que otra edición basada en un estado anterior borre esos cambios.

En un jugador nuevo, aplicar ese ejemplo debe dejar un partido, dos goles y una asistencia. Aplicarlo otra vez suma otra participación: no es un reemplazo idempotente de sus estadísticas. Un incremento negativo se rechaza con `400`, sin alterar ninguno de los contadores.

Los límites acumulados son 10.000 partidos y 100.000 goles o asistencias. Si una operación supera cualquiera, se rechaza por conflicto y no queda aplicada parcialmente. La solución incluye pruebas de estado y concurrencia; no basta con comprobar que la llamada compila.

## Comprobación del recorrido

Comprueba estos escenarios desde la interfaz y las pruebas del proyecto:

- Alta, consulta, edición y eliminación de un equipo de trabajo.
- Jugadores embebidos con identidad propia y referencias de sede válidas.
- Filtros, página vacía y límites de paginación inválidos.
- Dorsal repetido y plantilla llena sin pérdida de jugadores anteriores.
- Incrementos conservados tras una edición y conflicto con una versión antigua.
- Fallo del servidor distinguible de un resultado sin coincidencias.

Utiliza [diagnóstico y logging](../ayuda/diagnostico.md) para seguir un error desde el controlador al driver. En Compass observa el documento completo antes y después; no lo edites manualmente para hacer que un test pase.

## Solución completa

[Descargar solución del guiado](/fp-dam2-acda/descargas/codigo/jugadores-guiado-solucion.zip){ .md-button download="jugadores-guiado-solucion.zip" }

[Ver código, README y pruebas](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/mongodb/guiado-solucion)

La solución conserva la interfaz y los puntos de entrada del inicial. Contrasta especialmente la versión, el incremento atómico y la distinción entre entidad persistida y respuesta de consulta antes de comenzar [la práctica final](practica-final.md).
