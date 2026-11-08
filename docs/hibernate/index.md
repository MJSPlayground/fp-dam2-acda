# Herramientas ORM y JPA con Hibernate

Aprenderás a relacionar objetos Java con tablas y asociaciones, persistir entidades y mantener operaciones coherentes mediante sesiones y transacciones. El proyecto es un catálogo de biblioteca con interfaz JavaFX preparada.

## Antes de empezar

Reutiliza las [capas y los errores de Ficheros](../ficheros/arquitectura.md) y repasa tablas, claves primarias y foráneas. Descarga el [catálogo inicial](proyecto.md#preparar-y-ejecutar); la configuración de MySQL está preparada. Empieza por arquitectura y continúa con el arranque. No necesitas Spring ni JDBC.

## Recorrido

1. [Arquitectura y fundamentos ORM](arquitectura.md): responsabilidades, desfase objeto-relacional y especificación frente a proveedor.
2. [Configuración](configuracion.md): MySQL, Maven, Workbench y arranque.
3. [Entidades](entidades.md): identidad, columnas, constructores, versión y alternativa XML.
4. [Sesiones y transacciones](persistencia.md): estados, CRUD, cierre y errores.
5. [Relaciones](relaciones.md): 1:1, 1:N, M:N y ciclo de vida.
6. [HQL básico](consultas.md): filtros, parámetros y ordenación.

El [proyecto guiado](proyecto.md) evoluciona con estas explicaciones sobre una sola aplicación. Completarás asociaciones, repositorios y casos de uso sin editar el frontend.

Después encontrarás una sola [práctica final de ampliación del catálogo](practica-final.md), con niveles Base, Aplicación y Reto avanzado; [cinco fichas de repaso en preparación](repaso.md); y [Ampliación](ampliacion.md) sobre consultas y rendimiento.

Reutiliza las capas y el logging introducidos en [Ficheros](../ficheros/arquitectura.md), así como [preparación Maven](../ayuda/proyectos-maven.md), [contenedores](../ayuda/contenedores.md) y [diagnóstico](../ayuda/diagnostico.md). No se necesita conocer JDBC o Spring para seguir este tema.

Las [comprobaciones por etapas](proyecto.md#pruebas-preparadas-para-avanzar) del starter permiten contrastar cada avance sin completar todo el proyecto primero. Activa solo la fase que estés trabajando y conserva los casos pendientes para las siguientes etapas.
