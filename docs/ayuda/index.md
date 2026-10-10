# Ayuda y herramientas

Esta sección reúne las referencias transversales necesarias para trabajar con los temas. Su contenido crecerá a medida que se publiquen las prácticas.

## Entorno Java y proyectos

Los proyectos Java tendrán **Java 25** como objetivo inicial y utilizarán **Maven Wrapper**. Para preparar el equipo, instala un JDK 25, selecciona ese JDK en el IDE y comprueba `java -version`. Ejecuta el wrapper incluido en cada proyecto (`.\mvnw.cmd` en Windows o `./mvnw` en otros sistemas).

## Contenedores y bases de datos

Se usarán Docker y Docker Compose para los servicios que requieran servidor, además de herramientas como MySQL Workbench y MongoDB Compass. Comprueba el puerto configurado en cada proyecto antes de conectarte. Los ejemplos concretos de cada base se incorporarán junto a sus temas.

## Calidad, API y seguridad

Para investigar fallos, lee los mensajes de error, utiliza el depurador y consulta los registros. Las credenciales y cuentas de servicio deben mantenerse fuera del repositorio y configurarse mediante los mecanismos indicados en cada proyecto. Las guías detalladas de pruebas, Postman y OpenAPI se publicarán con los contenidos que las utilizan.

Vuelve a la [portada de Acceso a Datos](../index.md).
