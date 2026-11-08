# Acceso a datos mediante componentes Spring

Ya sabemos separar persistencia y reglas y hemos utilizado [Hibernate](../hibernate/index.md) para relacionar objetos con tablas. Ahora aprenderemos a construir y conectar esos componentes con Spring Boot, a declarar repositorios Spring Data JPA y a coordinar casos de uso mediante servicios transaccionales.

El proyecto **Servicios para el catálogo** conserva las ideas de libros y editoriales y añade stock. La aplicación dispone de interfaz web terminada: el trabajo del alumnado se centra en el backend. No es necesario conocer JDBC ni construir páginas HTML para seguir el recorrido.

## Antes de empezar

Parte del [catálogo Hibernate](../hibernate/index.md): entidades, repositorios y transacciones. Descarga el [proyecto inicial](proyecto.md#preparar-y-ejecutar) y localiza en la guía qué clases están preparadas antes de modificar código. Empieza por componentes e inyección; las vistas web, seguridad y soporte de arranque se entregan preparados.

## Recorrido

1. [Componentes e inyección de dependencias](componentes.md): contratos, composición, beans y contenedor.
2. [Spring Boot y configuración](configuracion.md): arranque, dependencias, propiedades y perfiles.
3. [Repositorios con Spring Data JPA](persistencia.md): CRUD y consultas derivadas.
4. [Servicios, reglas y colaboración](servicios.md): validaciones, excepciones y eventos.
5. [Transacciones y conflictos](transacciones.md): límites del caso de uso y concurrencia.

Aplica cada parte en [el proyecto guiado](proyecto.md). Después desarrolla [Reglas y servicios del catálogo](practica-final.md), con niveles Base, Aplicación y Reto avanzado. Las cinco [prácticas de repaso](repaso.md) están en preparación. [Ampliación](ampliacion.md) profundiza en MVC, formularios, pruebas, diagnóstico, JAR, seguridad y Docker.

Para preparar IntelliJ consulta [proyectos Maven](../ayuda/proyectos-maven.md); para problemas de ejecución, utiliza [diagnóstico](../ayuda/diagnostico.md).
