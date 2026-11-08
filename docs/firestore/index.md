# Bases de datos documentales con Cloud Firestore

Cloud Firestore almacena documentos agrupados en colecciones. Construiremos una API de reservas de espacios con Spring Boot y un frontend ya preparado, manteniendo repositorios y servicios separados.

Para trabajar en local, inicia el **emulador oficial preparado**: no necesitas una cuenta cloud ni claves privadas. Sus datos pertenecen al proceso local y no a una base remota. Sigue [el arranque y la conservación de datos](configuracion.md) y mantén separados el emulador y la aplicación Java.

## Antes de empezar

Recupera los [servicios y la inyección de Spring](../spring/componentes.md). Descarga las [reservas iniciales y su colección Postman](proyecto.md) antes de estudiar el modelo documental. HTTP y los controladores REST se explican en este tema; no se presupone que ya sepas construir una API.

## Recorrido

1. [Modelo documental](documentos.md): campos, mapas, listas y decisiones sobre relaciones.
2. [Configuración y acceso seguro](configuracion.md): emulador local, Admin SDK, cloud e IAM.
3. [Persistencia y servicios](persistencia.md): CRUD, conversiones, validación y errores.
4. [Consultas e índices](consultas.md): filtros, ordenación, límites y cursores.
5. [HTTP y REST](rest.md): peticiones, DTO, controladores, estados y Postman.

El [proyecto guiado de reservas](proyecto.md) incorpora etapas a lo largo del tema. Avanza por conversión, CRUD del repositorio, filtros, servicio y HTTP; las comprobaciones optativas permiten aislar cada paso. Su descarga incluye interfaz web completa y colección Postman importable con peticiones de arranque y casos correctos e incorrectos. El modo inicial utiliza un emulador local sin claves privadas.

Al terminar, una sola [práctica final](practica-final.md) amplía el sistema con cancelaciones, disponibilidad y consultas por páginas en niveles Base, Aplicación y Reto avanzado. Encontrarás también [cinco fichas de repaso en preparación](repaso.md) y [Ampliación](ampliacion.md) sobre documentación API, transacciones, identidad, tiempo real y protección de endpoints.

Para preparar el IDE utiliza [proyectos Maven y Run](../ayuda/proyectos-maven.md). Para interpretar fallos y ejecutar comprobaciones consulta [diagnóstico y pruebas](../ayuda/diagnostico.md). El modelo de capas y la gestión de errores enlazan con lo aprendido en [JDBC](../jdbc/index.md).

Continúa después con [MongoDB](../mongodb/index.md).
