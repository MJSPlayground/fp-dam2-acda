# Bases de datos documentales con Cloud Firestore

!!! info "Disponible para revisión docente"

    Incluye teoría, proyecto guiado, práctica final y cuatro descargas. La validación local utiliza el emulador oficial; las credenciales, IAM e índices del servicio cloud requieren una comprobación separada en el proyecto del aula.

Cloud Firestore almacena documentos agrupados en colecciones. Construiremos una API de reservas de espacios con Spring Boot y un frontend ya preparado, manteniendo repositorios y servicios separados.

## Recorrido

1. [Modelo documental](documentos.md): campos, mapas, listas y decisiones sobre relaciones.
2. [Configuración y acceso seguro](configuracion.md): emulador local, Admin SDK, cloud e IAM.
3. [Persistencia y servicios](persistencia.md): CRUD, conversiones, validación y errores.
4. [Consultas e índices](consultas.md): filtros, ordenación, límites y cursores.
5. [HTTP y REST](rest.md): peticiones, DTO, controladores, estados y Postman.

El [proyecto guiado de reservas](proyecto.md) incorpora etapas a lo largo del tema. Su descarga incluye interfaz web completa y colección Postman importable con peticiones correctas e incorrectas. El modo inicial utiliza un emulador local sin claves privadas.

Al terminar, una sola [práctica final](practica-final.md) amplía el sistema con cancelaciones, disponibilidad y consultas por páginas en niveles Base, Aplicación y Reto avanzado. Encontrarás también [cinco fichas de repaso en preparación](repaso.md) y [Ampliación](ampliacion.md) sobre documentación API, transacciones, identidad, tiempo real y protección de endpoints.

Para preparar el IDE utiliza [proyectos Maven y Run](../ayuda/proyectos-maven.md). Para interpretar fallos y ejecutar comprobaciones consulta [diagnóstico y pruebas](../ayuda/diagnostico.md). El modelo de capas y la gestión de errores enlazan con lo aprendido en [JDBC](../jdbc/index.md).

Continúa después con [MongoDB](../mongodb/index.md).
