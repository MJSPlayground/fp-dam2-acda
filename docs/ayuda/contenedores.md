# Contenedores y herramientas de base de datos

Una aplicación puede necesitar dos procesos distintos: el programa Java y un servidor de base de datos. Abrir IntelliJ no arranca necesariamente ese servidor. Esta guía ayuda a identificar qué falta cuando el proyecto no consigue conectarse.

## Imagen, contenedor y volumen

Una **imagen** describe el entorno del servicio. Un **contenedor** es una instancia en ejecución de esa imagen. Un **volumen** conserva archivos fuera de la vida del contenedor, por ejemplo los datos de una base. Una imagen descargada no demuestra que haya un servidor escuchando.

Docker Desktop permite iniciar el motor y observar contenedores, puertos y registros. Espera a que indique que el motor está disponible antes de arrancar los servicios del proyecto. Si una herramienta indica que no puede conectar con el motor, todavía no está intentando autenticarte en MongoDB o MySQL. Referencia: [conceptos de contenedor](https://docs.docker.com/get-started/docker-concepts/the-basics/what-is-a-container/).

## Leer el Compose preparado

Cada proyecto que necesita Docker incluye su configuración y explica cómo arrancarla. **Compose** reúne la imagen, puertos, variables y almacenamiento de sus servicios; no tienes que reconstruirlos manualmente. Trabaja desde la carpeta indicada por su README para seleccionar el archivo correcto.

La publicación `127.0.0.1:27019:27017`, por ejemplo, significa: una aplicación en tu ordenador conecta a **27019** y Docker dirige esa conexión al **27017** del contenedor. No son dos servidores distintos. Desde otro contenedor de la misma red se utiliza el nombre del servicio y su puerto interno; `localhost` dentro de ese contenedor se refiere a él mismo.

Comprueba el estado y los registros del servicio antes de abrir la aplicación Java. Que el proceso haya empezado no siempre significa que la base esté lista; un chequeo de salud ayuda a distinguir ambos momentos. Detener y volver a iniciar un servicio con volumen conserva sus datos. Eliminar ese volumen puede perderlos: no lo uses como arreglo automático de un fallo de conexión. Consulta la [introducción oficial a Compose](https://docs.docker.com/compose/gettingstarted/).

## Conectar MongoDB Compass

Compass es un cliente gráfico: no sustituye al servidor MongoDB. Copia la URI indicada en el README del proyecto. Si el puerto local fuera 27019, una URI local podría ser `mongodb://localhost:27019/club`; utiliza el puerto y la base reales de tu configuración.

Conecta y abre la base y colección esperadas. Compara un documento antes y después de una operación desde la aplicación: identidad, campos, tipos y documentos embebidos. Modificar directamente documentos en Compass puede saltarse las validaciones del servicio Java; úsalo para inspeccionar y para los experimentos que indique el tema, conservando tus datos de trabajo. La [guía de conexión de Compass](https://www.mongodb.com/docs/compass/connect/) explica URI y opciones avanzadas.

## Conectar MySQL Workbench

Workbench también es un cliente. Con el servidor disponible, crea una conexión desde **MySQL Connections → +**. Indica nombre descriptivo, host, puerto publicado y usuario del proyecto. Utiliza **Test Connection** antes de abrir el editor SQL y selecciona el esquema adecuado.

Un error de autenticación exige revisar usuario y credenciales; una conexión rechazada exige comprobar servidor y puerto. No son el mismo fallo. Las credenciales de desarrollo del proyecto no deben reutilizarse en servicios reales. Referencia: [crear una conexión en Workbench](https://dev.mysql.com/doc/workbench/en/wb-getting-started-tutorial-create-connection.html).

## SQLite y el emulador Firestore

SQLite guarda la base en un archivo y no requiere contenedor ni servidor separado. Comprueba la ruta y el directorio de trabajo antes de concluir que se han perdido datos: abrir otro archivo crea o consulta otra base.

El proyecto Firestore utiliza un **emulador oficial**, con su propio proceso local. No es un contenedor MongoDB ni una consola cloud. Su [guía de configuración](../firestore/configuracion.md) distingue aplicación, emulador y servicio remoto.

## Diagnóstico inicial

Identifica primero qué proceso falla: Docker, base de datos o aplicación. Después comprueba host, puerto publicado, base, usuario y configuración efectiva. Si el puerto está ocupado, identifica el servicio que ya lo utiliza antes de cambiar configuraciones o detener procesos. Conserva el mensaje y sigue la [guía de depuración y registros](diagnostico.md).
