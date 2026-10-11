# Ayuda y herramientas

Esta sección reúne las referencias transversales necesarias para trabajar con los temas. Su contenido crecerá a medida que se publiquen las prácticas.

## Entorno Java y proyectos

Los proyectos Java tendrán **Java 25** como objetivo inicial. Instala el JDK, abre en IntelliJ IDEA la carpeta concreta que contiene `pom.xml`, espera la sincronización Maven y selecciona ese JDK. Ejecuta las clases `main` con el botón verde **Run**. La [guía de proyectos Maven e IntelliJ](proyectos-maven.md) explica la importación, la localización de resultados y el diagnóstico inicial. Maven Wrapper queda disponible para comprobaciones técnicas opcionales.

## Contenedores y bases de datos

Se usarán Docker y Docker Compose para los servicios que requieran servidor, además de herramientas como MySQL Workbench y MongoDB Compass. Comprueba el puerto configurado en cada proyecto antes de conectarte. Los ejemplos concretos de cada base se incorporarán junto a sus temas.

## Calidad, API y seguridad

Para investigar fallos, lee los mensajes de error, utiliza el depurador y consulta los registros. Las credenciales y cuentas de servicio deben mantenerse fuera del repositorio y configurarse mediante los mecanismos indicados en cada proyecto. Las guías detalladas de pruebas, Postman y OpenAPI se publicarán con los contenidos que las utilizan.

Vuelve a la [portada de Acceso a Datos](../index.md).
