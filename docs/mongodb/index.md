# Bases de datos NoSQL con MongoDB

MongoDB permite persistir documentos BSON y consultarlos desde Java. Reutilizaremos Spring y REST para concentrarnos en el modelo de equipos, jugadores embebidos, repositorios y actualizaciones seguras.

## Antes de empezar

Necesitas la [arquitectura Spring](../spring/index.md) y las [peticiones y respuestas REST](../firestore/rest.md). Descarga el [proyecto inicial de equipos](proyecto.md) y utiliza el servidor Docker preparado. Empieza por el modelo BSON y comprueba después la conexión; no necesitas Aggregation Pipeline para resolver el guiado ni la práctica final.

## Recorrido

1. [Documentos y modelado](documentos.md): BSON, identidad, embebidos y referencias.
2. [Docker y Compass](configuracion.md): servidor local, conexión y observación de documentos.
3. [Spring Data MongoDB](persistencia.md): entidades, repositorios, CRUD y servicios.
4. [Consultas y paginación](consultas.md): métodos derivados, `@Query`, orden e índices.
5. [Atomicidad y errores](atomicidad.md): versiones, incrementos y límites de consistencia.

El [proyecto guiado de equipos y jugadores](proyecto.md) evoluciona sobre una aplicación con frontend preparado. Trabajarás por separado consultas, equipos, jugadores y coordinación de estadísticas. La actualización atómica avanzada se entrega como ayuda preparada; el trabajo se centra en Spring Data, servicios y reglas, sin modificar las vistas.

Al finalizar, una sola [práctica de gestión de competición](practica-final.md) incorpora consultas, disponibilidad, jornadas y clasificación con niveles Base, Aplicación y Reto avanzado. Encontrarás también [cinco fichas de repaso en preparación](repaso.md) y [Ampliación](ampliacion.md) sobre pipelines e interpretación de planes.

Reutiliza [HTTP y REST](../firestore/rest.md), [preparación Maven](../ayuda/proyectos-maven.md), [contenedores](../ayuda/contenedores.md) y [diagnóstico](../ayuda/diagnostico.md) como referencias. No es necesario reconstruir esos conocimientos desde cero.

Vuelve a la [portada de Acceso a Datos](../index.md).
