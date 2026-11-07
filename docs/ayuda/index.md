# Ayuda y herramientas

Esta sección reúne las referencias transversales necesarias para trabajar con los temas. Su contenido crecerá a medida que se publiquen las prácticas.

## Entorno Java y proyectos

Los proyectos Java tendrán **Java 25** como objetivo inicial. Instala el JDK, abre en IntelliJ IDEA la carpeta concreta que contiene `pom.xml`, espera la sincronización Maven y selecciona ese JDK. Ejecuta las clases `main` con el botón verde **Run**. La [guía de proyectos Maven e IntelliJ](proyectos-maven.md) explica la importación, la localización de resultados y el diagnóstico inicial. Maven Wrapper queda disponible para comprobaciones técnicas opcionales.

## Contenedores y bases de datos

Se usarán Docker y Docker Compose para los servicios que requieran servidor, además de herramientas como MySQL Workbench y MongoDB Compass. Comprueba el puerto configurado en cada proyecto antes de conectarte. Los ejemplos concretos de cada base se incorporarán junto a sus temas.

Consulta [contenedores y clientes de base de datos](contenedores.md) para distinguir imagen, servidor y cliente, entender los puertos y diagnosticar la conexión.

## Calidad, API y seguridad

Para investigar fallos, lee los mensajes de error, utiliza el depurador y consulta los registros. Las credenciales y cuentas de servicio deben mantenerse fuera del repositorio y configurarse mediante los mecanismos indicados en cada proyecto. La [configuración segura de Firestore](../firestore/configuracion.md) explica variables de entorno y cuentas de servicio. La [guía del proyecto de reservas](../firestore/proyecto.md#importar-la-coleccion-postman) enseña a importar y ejecutar Postman; su [Ampliación](../firestore/ampliacion.md) introduce OpenAPI y protección de endpoints.

La guía de [depuración, registros y pruebas](diagnostico.md) explica cómo seguir una excepción, observar variables y distinguir pruebas superadas de pruebas omitidas.

## Seguir el aprendizaje

Cada tema avanza con un único proyecto guiado. Descarga su inicial una vez, ejecuta la aplicación e identifica qué funciona ya y qué etapa sigue pendiente. Lee el concepto correspondiente antes de completar sus TODO; cambia el backend indicado y utiliza el resultado observable de la etapa para comprobarlo. La interfaz se entrega preparada.

Cuando una prueba falle, conserva el mensaje y compara entrada, estado inicial y resultado esperado. Vuelve al concepto o a la API enlazada y usa el depurador para localizar la primera diferencia. Si necesitas ayuda, comunica esos datos y el fragmento concreto que has investigado.

La solución pública permite contrastar decisiones y comprobar el recorrido completo. Abre su proyecto en otra carpeta para conservar tus cambios. La práctica final parte del guiado resuelto y añade requisitos nuevos en tres niveles; sus resultados deben comprobarse también con entradas incorrectas y casos límite.

Vuelve a la [portada de Acceso a Datos](../index.md).
