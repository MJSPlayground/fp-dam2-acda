# Ampliación

Estas páginas profundizan en la API y la coordinación de documentos. Los fragmentos son **ejemplos conceptuales**, con sus dependencias y contexto indicados; no forman parte del recorrido mínimo ni afirman que la aplicación entregada ya tenga autenticación o actualización en tiempo real.

## Documentación OpenAPI y Swagger UI

Una colección Postman permite ejecutar peticiones. Una descripción [OpenAPI](https://spec.openapis.org/oas/v3.0.3.html) define de forma estructurada el contrato de una API: rutas, parámetros, cuerpos, respuestas y requisitos de seguridad. Swagger UI puede representar ese contrato como una interfaz de consulta y exploración.

Por ejemplo, esta descripción reducida documenta una lectura de nuestra API; omite otros endpoints y el esquema completo para centrarse en sus elementos:

```yaml
openapi: 3.0.3
info:
  title: Reservas de espacios
  version: 1.0.0
paths:
  /api/reservas/{id}:
    get:
      summary: Consultar una reserva
      parameters:
        - in: path
          name: id
          required: true
          schema:
            type: string
      responses:
        '200':
          description: Reserva encontrada
        '404':
          description: No existe esa reserva
```

`required: true` indica que la ruta no puede completarse sin su ID. Las respuestas describen los resultados previstos, pero escribir un `404` en el documento no hace que Java lo devuelva automáticamente. Hay que contrastar contrato, controlador, manejador de errores y pruebas.

En Spring Boot, [springdoc-openapi](https://springdoc.org/) puede generar una descripción a partir de los controladores y mostrar Swagger UI. Antes de añadirlo debe comprobarse su compatibilidad con la versión de Spring utilizada. La práctica no presupone que exista `/swagger-ui` si esa dependencia no está incorporada.

La documentación generada se revisa: campos internos, respuestas incompletas o ejemplos con datos sensibles no deben aparecer por defecto. Una operación ejecutada desde Swagger UI sigue estando sometida a autenticación, autorización y reglas del servicio.

## Escrituras agrupadas y transacciones

En [JDBC](../jdbc/transacciones.md) una conexión compartía varias escrituras con `commit` y `rollback`. Firestore ofrece otros mecanismos; no se copian llamadas JDBC sobre su SDK.

Una **escritura agrupada** (`WriteBatch`) confirma varias escrituras juntas cuando no es necesario leer datos para decidir sus valores. Este fragmento utiliza documentos demostrativos de otra colección, en un entorno de pruebas:

```java
WriteBatch lote = db.batch();
lote.set(db.collection("avisos").document("aviso-a"),
    Map.of("texto", "Revisión de sala", "pendiente", true));
lote.set(db.collection("avisos").document("aviso-b"),
    Map.of("texto", "Preparar equipo", "pendiente", true));
lote.commit().get();
```

La confirmación se espera antes de comunicar éxito. Agrupar esas escrituras no valida una condición que otro proceso pueda cambiar mientras tanto.

Una **transacción** lee documentos y decide escrituras de forma coordinada. En un contador demostrativo, leemos el valor anterior y guardamos el siguiente:

```java
DocumentReference contador = db.collection("contadores").document("visitas");
Long siguiente = db.runTransaction(transaccion -> {
    DocumentSnapshot actual = transaccion.get(contador).get();
    Long valor = actual.exists() ? actual.getLong("valor") : 0L;
    if (valor == null) throw new IllegalStateException("Contador sin valor numérico");
    long nuevo = Math.addExact(valor, 1L);
    transaccion.set(contador, Map.of("valor", nuevo));
    return nuevo;
}).get();
```

El ejemplo no sustituye el diseño de conflictos del proyecto. La función de transacción puede ejecutarse de nuevo ante contención: no se envían correos, no se cobra y no se modifica un estado externo dentro de ella. Las lecturas necesarias deben organizarse antes de las escrituras. La [guía de transacciones y lotes](https://firebase.google.com/docs/firestore/manage-data/transactions) detalla sus garantías y restricciones.

Para impedir reservas simultáneas entre múltiples servidores hay que definir qué documentos representan la ocupación y qué cambios se coordinan; no basta con envolver una consulta cualquiera en una función y dar por resuelto el problema. Esa evolución requiere pruebas de concurrencia y un modelo específico.

## Firebase Authentication

Firebase Authentication gestiona la identidad de las personas mediante proveedores de acceso. **Autenticar** contesta quién es el usuario; **autorizar** decide qué puede hacer. Una persona autenticada no tiene por ello permiso para cancelar todas las reservas.

En una arquitectura con API propia, el cliente obtiene un **ID token** y lo envía al backend. El servidor verifica firma, proyecto y validez; no confía en un campo JSON como `"administrador": true` ni en el nombre de responsable escrito en el formulario.

Este fragmento supone un `FirebaseApp app` real y un ID token recibido por un canal HTTP seguro:

```java
FirebaseToken identidad = FirebaseAuth.getInstance(app).verifyIdToken(idToken);
String uid = identidad.getUid();
```

El SDK debe tener una configuración válida y el código tratar los errores de verificación. La [documentación oficial de verificación de ID tokens](https://firebase.google.com/docs/auth/admin/verify-id-tokens) distingue estos tokens de otros tipos de credenciales. Decodificar un JWT para leer su JSON no verifica su autenticidad.

La reserva podría almacenar un `uid` controlado por el servidor como propietario. El responsable visible sería un dato de presentación, no el permiso. Este cambio afectaría al contrato y a las pruebas; no se añade silenciosamente al starter local.

El emulador Firestore y el de Authentication son servicios diferentes. Verificar CRUD local sin credenciales no demuestra que un flujo de login real funcione, y un token demo no debe aceptarse en producción.

## Firestore en tiempo real y listeners

Una consulta puntual devuelve los datos disponibles cuando se ejecuta. Un listener observa cambios mientras su registro siga activo. El cliente Java permite suscribir una consulta y recibir nuevas instantáneas:

```java
ListenerRegistration registro = db.collection("reservas")
    .whereEqualTo("espacio", "SALA-A")
    .addSnapshotListener((instantanea, error) -> {
        if (error != null) {
            log.error("No se pudo observar la agenda", error);
            return;
        }
        if (instantanea != null) {
            log.debug("Reservas observadas: {}", instantanea.size());
        }
    });
// Al terminar el componente que observa:
registro.remove();
```

Este fragmento supone un logger preparado. El callback puede ejecutarse en otro hilo: no es un lugar para modificar sin protección datos compartidos ni para reutilizar arbitrariamente el contexto de una petición web.

La [guía de actualizaciones en tiempo real](https://firebase.google.com/docs/firestore/query-data/listen) explica las instantáneas y el cierre de listeners. Escuchar continuamente puede implicar más actividad y costes que consultar cuando se necesita; también exige gestionar reconexión, fallos y ciclo de vida.

Un listener en el backend **no actualiza automáticamente el navegador**. Hace falta un canal adicional, como eventos enviados por el servidor, WebSocket o un diseño de cliente con SDK propio y sus reglas de acceso. No se debe añadir un listener por cada `GET` y abandonarlo después de responder: acumularía recursos sin un consumidor definido.

## Seguridad de la API

La aplicación de aprendizaje usa un backend local. Publicarla en una red accesible requiere añadir una política de seguridad, no solo cambiar el host del emulador por una cuenta cloud.

Un cliente que se identifica puede enviar una cabecera `Authorization: Bearer ...`. El backend debe verificar el token en un componente común antes de atender los endpoints protegidos. Un token válido identifica; el servicio o una política de autorización comprueba después si ese usuario es propietario, gestor o administrador para la operación solicitada.

| Situación | Respuesta coherente |
| --- | --- |
| No se presenta identidad válida para un recurso protegido | `401 Unauthorized` |
| Identidad válida sin permiso para esa operación | `403 Forbidden` |
| Datos inválidos dentro de una operación permitida | `400 Bad Request` |
| Reserva incompatible con el estado de negocio | `409 Conflict` |

También deben protegerse las rutas que consultan información, no solo las que escriben. La [integración de Spring Security para servidores de recursos](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html) aporta componentes para verificar tokens; elegir la estrategia concreta exige alinear emisor, claves y claims con el proveedor de identidad.

**CORS no autentica**: limita determinadas peticiones de navegadores, pero no impide que otro cliente HTTP llame a la API. El acceso al emulador no debe exponerse como si ya fuera un servicio protegido. El backend sigue siendo responsable de validar entradas y no filtrar causas técnicas a las respuestas.

Una revisión real de seguridad debe comprobar lectura de recursos ajenos, modificación de propietarios, cancelaciones indebidas y permisos por rol, además del login correcto. Nunca se utiliza una clave de cuenta de servicio como token del usuario final.
