# Configuración y acceso seguro

El proyecto separa el backend de la base. Spring Boot atiende peticiones HTTP; el SDK de servidor comunica Java con Firestore. Antes de implementar CRUD conviene comprobar esa frontera sin introducir credenciales ni depender de una cuenta cloud.

## Dos modos con objetivos distintos

| Modo | Dónde están los datos | Identidad y permisos | Uso en el tema |
| --- | --- | --- | --- |
| Emulador | Proceso local de Firestore | Configuración demo sin claves privadas | Recorrido inicial y pruebas reproducibles |
| Cloud | Proyecto remoto de Google Cloud/Firebase | Credenciales de servidor e IAM | Explorar servicio remoto, permisos e índices con una cuenta autorizada |

El proyecto arranca en modo **emulador** con ID `demo-acda-reservas`. No hace falta crear un proyecto real para seguir el guiado. Un ID con prefijo `demo-` permite distinguir ese entorno de un proyecto remoto, pero no es una contraseña.

Para aprender, el emulador permite repetir reservas sin crear una cuenta ni gestionar una clave privada. Cloud permite estudiar un servicio remoto, su localización y sus permisos. Mantén ese entorno separado de las comprobaciones locales que crean y eliminan datos. Cambiar de modo no completa un TODO ni corrige sus errores.

## Arranque local desde IntelliJ

Abre el proyecto Maven y espera la sincronización, como en [preparación y Run](../ayuda/proyectos-maven.md). El recorrido utiliza dos clases ejecutables:

1. Ejecuta `es.mjsplayground.acda.firestore.reservas.config.Emulador.main`. Mantén ese proceso abierto. El lanzador utiliza el emulador oficial y verifica el archivo descargado según su configuración.
2. Ejecuta `es.mjsplayground.acda.firestore.reservas.ReservasApplication.main` en otra configuración Run.
3. Abre `http://localhost:8080` en el navegador. Esta dirección corresponde a la aplicación; el emulador escucha en `127.0.0.1:8085`.

Son dos procesos diferentes. Cerrar la ventana del navegador no detiene el servidor. Si el emulador se cierra, la portada puede seguir cargando porque los archivos de interfaz pertenecen a Spring; las operaciones de datos fallarán.

La primera descarga del emulador requiere conexión de red. Si el puerto ya está ocupado, comprueba si sigue abierta otra ejecución antes de lanzar un segundo proceso. No cambies un puerto en un único sitio dejando el backend conectado al anterior.

La configuración `FIRESTORE_EMULATOR_HOST` utiliza formato `host:puerto`, sin `http://`. Los valores entregados permiten trabajar localmente sin cuentas de servicio. Consulta el [uso del SDK de servidor con el emulador](https://firebase.google.com/docs/emulator-suite/connect_firestore) para contrastar este mecanismo y sus diferencias respecto al servicio remoto.

## Qué prepara FirestoreConfig

`FirestoreConfig` es configuración de Spring: produce los objetos compartidos que se inyectan en las capas de acceso. No debemos inicializar una aplicación Firebase distinta por cada petición.

El [Firebase Admin SDK para Java](https://firebase.google.com/docs/admin/setup) utiliza `FirebaseOptions` para indicar proyecto y credenciales, y `FirebaseApp` para representar la aplicación inicializada. El cliente `Firestore` se obtiene mediante `FirestoreClient` y se cierra al terminar el backend.

Este esquema muestra la inicialización conceptual de **cloud**, separada del modo demo preparado. No es el código que debes copiar para arrancar localmente:

```java
FirebaseOptions opciones = FirebaseOptions.builder()
    .setProjectId("identificador-del-proyecto-real")
    .setCredentials(GoogleCredentials.getApplicationDefault())
    .build();
FirebaseApp app = FirebaseApp.initializeApp(opciones);
Firestore db = FirestoreClient.getFirestore(app);
```

`getApplicationDefault` utiliza el mecanismo de credenciales predeterminadas del entorno. En un servicio desplegado puede usar una identidad asociada al entorno; en desarrollo se configura una identidad autorizada. No requiere incrustar una clave privada en una clase Java.

El modo emulador del proyecto configura explícitamente su host y una identidad local sin autoridad en cloud. El modo `cloud` solo se utiliza cuando se selecciona expresamente y se proporciona un proyecto real; no se activa como fallback cuando el emulador no responde.

## Crear un entorno cloud de forma consciente

Para explorar el servicio remoto con una cuenta autorizada, entra en Firebase Console y selecciona o crea el proyecto apropiado. Dentro de Firestore Database, revisa la creación de la base y sus opciones vigentes. Elige la localización teniendo en cuenta dónde estarán usuarios y backend, los requisitos de datos y las condiciones del servicio.

La [localización de Firestore](https://firebase.google.com/docs/firestore/locations) es una decisión de infraestructura que no se cambia como un nombre de colección. No elijas una región al azar pensando que el código corregirá después la latencia o la residencia de los datos.

Antes de una prueba remota, comprueba ID de proyecto, base seleccionada, credenciales e IAM, límites de uso y política de limpieza de sus datos. El emulador no genera una cuenta real ni demuestra que esos permisos cloud funcionen. Comprueba el acceso desde tu backend autorizado y observa el documento en la consola; una sesión de emulador no prepara esos permisos remotos.

## IAM y reglas de clientes no son la misma protección

El acceso desde los SDK móviles o web de cliente se controla mediante Firebase Authentication y reglas de seguridad Firestore, según la arquitectura. El **SDK Admin de servidor no aplica esas reglas de cliente**: utiliza credenciales de servidor y permisos IAM. Una regla que prohíba todas las escrituras desde un cliente no convierte automáticamente en segura una API Java con privilegios.

La [descripción de seguridad de Firestore](https://firebase.google.com/docs/firestore/security/overview) distingue ambos caminos. En nuestra arquitectura el navegador llama a la API y esta llama a Firestore. Por tanto, la API debe validar las entradas, identificar a sus usuarios cuando se exponga y autorizar cada operación.

```text
Cliente directo → reglas Firestore + identidad de cliente

Navegador → API propia → SDK Admin → permisos IAM de servidor
            ↑
       autenticación y autorización de la API
```

No se entrega una clave privada al frontend para que este «se conecte mejor». El identificador de proyecto y una clave privada tienen funciones y riesgos distintos. La práctica local no incorpora acceso público autenticado: ese diseño se estudia en [Ampliación](ampliacion.md#seguridad-de-la-api).

## Credenciales fuera del repositorio

En un entorno que utilice un archivo de cuenta de servicio, `GOOGLE_APPLICATION_CREDENTIALS` puede apuntar a su ruta externa. El archivo no se copia a `src/main/resources`, al ZIP de descarga ni a una carpeta estática. Tampoco se publica su contenido en capturas, logs o ejemplos Postman.

IAM debe conceder a la identidad del servidor solo los permisos necesarios. Darle un rol administrador para evitar un error oculta un problema de autorización y aumenta el impacto de un fallo de la API. Si una clave se expone, retirarla de un commit posterior no revoca la credencial: hace falta invalidarla y revisar su uso.

## Diagnosticar el entorno

| Situación | Qué comprobar |
| --- | --- |
| La web no abre | Proceso Spring, puerto 8080 y URL de la aplicación |
| La web abre pero los datos fallan | Proceso emulador, host 8085 y logging del backend |
| `PERMISSION_DENIED` en cloud | Identidad efectiva, proyecto e IAM |
| Parece no haber documentos | Proyecto, modo, colección y ciclo de vida de los datos locales |
| Una consulta falla solo en cloud | Índices y diferencias documentadas del emulador |
| El starter devuelve `501` | Operación pendiente de implementar; no indica falta de credenciales |

Para interpretar stack traces y depurar sin ocultar las causas, utiliza [la guía de diagnóstico](../ayuda/diagnostico.md). Continúa con [repositorio y servicio](persistencia.md).
