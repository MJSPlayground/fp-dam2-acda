# Persistencia y reglas de reserva

El [modelo documental](documentos.md) explica qué guardamos. Ahora vamos a transformar esos datos entre Java y Firestore, manteniendo la separación por capas aprendida en [JDBC](../jdbc/repositorios.md).

Los ejemplos son fragmentos didácticos: suponen una variable `Firestore db` inicializada por la configuración y un método que trata las excepciones de las operaciones asíncronas. La configuración y el frontend están preparados en el proyecto; no hay que escribir conexiones nuevas en cada botón.

## Referencia, lectura y resultado

`db.collection("reservas")` representa la colección. `.document(id)` representa una ruta concreta: obtener esa referencia no consulta la red ni demuestra que exista un documento. La lectura real comienza con `get()`.

El SDK devuelve una operación asíncrona. En una aplicación MVC sencilla, esperar con `.get()` permite obtener su resultado antes de contestar a la petición HTTP. El cliente Java de Firestore es una dependencia de servidor, no una clase JDBC:

```java
DocumentReference referencia = db.collection("reservas").document(id);
DocumentSnapshot documento = referencia.get().get();
if (!documento.exists()) {
    throw new NoEncontradaException("La reserva no existe");
}
String espacio = documento.getString("espacio");
Long asistentes = documento.getLong("asistentes");
```

`NoEncontradaException` es aquí un nombre ilustrativo para una excepción del dominio. El proyecto utiliza su propio tipo de error. La [lectura de documentos](https://firebase.google.com/docs/firestore/query-data/get-data) distingue la ausencia de un documento de una lectura fallida. Una respuesta vacía no significa que la red haya fallado; un fallo de acceso no debe transformarse en una reserva inventada.

[`DocumentSnapshot`](https://cloud.google.com/java/docs/reference/google-cloud-firestore/latest/com.google.cloud.firestore.DocumentSnapshot) contiene los datos leídos y su ID. `getLong` devuelve un objeto que puede ser nulo: antes de convertirlo en `int`, hay que comprobar presencia y rango. `Math.toIntExact` rechaza un entero demasiado grande; un simple cast podría truncarlo.

## Convertir campos a una entidad

La conversión debe tener un único lugar reconocible en el repositorio. Un documento con campo `inicio` de texto en vez de número no cumple el contrato de nuestra reserva. No arreglamos ese problema devolviendo cero como si fuese un horario válido.

```java
Long inicioGuardado = documento.getLong("inicio");
if (inicioGuardado == null) {
    throw new IllegalStateException("Falta el campo inicio de la reserva " + documento.getId());
}
int inicio = Math.toIntExact(inicioGuardado);
```

Después se construye la entidad con sus campos, incluida la identidad externa `documento.getId()`. Para listas y mapas hay que comprobar que los elementos tengan los tipos previstos antes de construir `List<String>` o `Map<String,String>`. Un cast genérico no demuestra que los datos almacenados sean correctos.

Otra opción del SDK es mapear documentos a objetos mediante `toObject`. Requiere un modelo compatible con las reglas de su serialización. No asumas que cualquier constructor, record o propiedad Java funcionará del mismo modo: en este proyecto el mapeo explícito hace visibles nombres, tipos y valores ausentes.

## Crear un documento

Para un alta, el servidor elige un ID y construye los campos permitidos. Un mapa asocia nombres de campo a valores Java compatibles. Este ejemplo se centra en la escritura y supone datos ya validados:

```java
DocumentReference nueva = db.collection("reservas").document();
Map<String, Object> campos = new HashMap<>();
campos.put("espacio", "SALA-A");
campos.put("fecha", "2027-02-15");
campos.put("inicio", 10);
campos.put("fin", 12);
campos.put("responsable", "Coordinación");
campos.put("asistentes", 8);
campos.put("equipo", List.of("proyector"));
campos.put("detalles", Map.of("actividad", "Taller"));
campos.put("estado", "ACTIVA");
nueva.create(campos).get();
String idCreado = nueva.getId();
```

`create` expresa que el documento no debe existir antes. `set` puede crear o reemplazar según sus opciones; `update` modifica campos de uno existente. La [referencia Java de DocumentReference](https://cloud.google.com/java/docs/reference/google-cloud-firestore/latest/com.google.cloud.firestore.DocumentReference) especifica sus contratos. Una API debe elegir una operación que corresponda a la intención del caso de uso, en lugar de convertir una actualización inexistente en alta accidental.

El estado inicial lo decide el servicio. Copiar todos los campos recibidos por HTTP directamente a Firestore permitiría que un cliente introdujera propiedades internas no previstas. El DTO limita la entrada y el mapeo controla lo persistido.

## Actualizar y borrar

Actualizar una reserva exige comprobar primero el recurso y validar sus nuevos datos. Un `PUT` de la API representa sustituir los campos editables; no concede permiso para sobrescribir identidad, estado o metadatos internos por cualquier JSON.

En el SDK, actualizar un campo individual puede escribirse así:

```java
db.collection("reservas").document(id)
    .update("responsable", "Recepción")
    .get();
```

Este fragmento cambia solo `responsable`. Un mapa con `set` sin opciones de combinación podría reemplazar otros campos. La diferencia importa si queremos conservar listas, detalles o el estado de la reserva.

Borrar utiliza `document(id).delete().get()`. Si el contrato HTTP exige `404` para un ID inexistente, el servicio debe imponer ese contrato: la operación de borrado del almacenamiento no necesariamente considera la ausencia un error. La [guía oficial de borrado](https://firebase.google.com/docs/firestore/manage-data/delete-data) también advierte que hay que gestionar las subcolecciones de forma expresa.

## Reglas de negocio en el servicio

Una reserva debe referirse a un espacio conocido, tener una fecha válida y utilizar un intervalo de horas completas dentro del horario permitido. El número de asistentes debe ser positivo y no superar la capacidad del espacio.

`LocalDate.parse` interpreta una fecha ISO y rechaza días imposibles; comprobar solo que el texto tenga diez caracteres no detecta un 30 de febrero. `inicio` debe ser menor que `fin`: ambos extremos iguales no representan una actividad. El contrato del proyecto indica sus límites de horario y el catálogo de capacidades.

El responsable necesita contenido real, no solo espacios. Las colecciones de equipo y detalles deben respetar los tipos y límites definidos en el modelo, para evitar documentos arbitrariamente grandes. La UI puede advertir de un campo vacío, pero una petición directa de Postman también tiene que pasar por las mismas reglas.

El guiado cubre validación básica y CRUD. La detección de reservas incompatibles pertenece a la práctica final: no se presenta una simple alta válida como prueba de disponibilidad.

## Asincronía, excepciones y logging

Una llamada que devuelve un futuro todavía no ha terminado de guardar. Devolver `201` antes de esperar a su resultado podría anunciar éxito aunque la escritura falle después. El repositorio debe esperar o componer la operación de manera que el servidor conozca su resultado antes de responder.

`.get()` puede lanzar `InterruptedException` y `ExecutionException`. Si se interrumpe la espera, restaura la marca del hilo y conserva la causa:

```java
catch (InterruptedException error) {
    Thread.currentThread().interrupt();
    throw new PersistenciaException("Se interrumpió el acceso a reservas", error);
} catch (ExecutionException error) {
    throw new PersistenciaException("No se pudo completar el acceso a reservas", error.getCause());
}
```

El ejemplo supone una excepción propia que recibe mensaje y causa, como en [JDBC](../jdbc/repositorios.md). Se registra el detalle técnico con SLF4J/Log4j2; la respuesta REST muestra un mensaje comprensible y un código estable, sin exponer credenciales o stack traces.

La espera síncrona mantiene sencillo el recorrido MVC, pero ocupa un hilo durante la operación. No hay que confundir una API asíncrona con una aplicación que nunca bloquea. En un servidor de mayor carga se estudian tiempos máximos, cancelación y composición sin añadir esa complejidad al primer CRUD.

## Etapas del proyecto

La primera etapa consiste en examinar el modelo y el DTO preparados. El **TODO 2** está en `FirestoreReservaRepository`: crear, obtener, listar, actualizar y eliminar, además de los ayudantes `datos` y `mapear` para convertir los campos. El **TODO 3** completa las operaciones de creación y actualización en `ReservaService`, utilizando las validaciones de apoyo y respetando sus reglas.

Comprueba alta con mapa y lista, lectura de la misma reserva, edición y borrado. Un ID inexistente debe distinguirse de un emulador apagado. Intenta asistentes por encima de capacidad, fecha imposible e intervalo invertido: no deben quedar documentos inválidos. Continúa con [consultas, índices y paginación](consultas.md).
