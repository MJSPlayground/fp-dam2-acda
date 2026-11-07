# Consultas, índices y paginación

Leer una reserva por su ID resuelve una pregunta concreta. El listado necesita responder otras: qué reservas tiene una sala, cuáles corresponden a un día o cuáles reúnen un mínimo de asistentes. En Firestore construimos un objeto consulta y después lo ejecutamos; no enviamos una cadena SQL.

Los fragmentos utilizan el cliente `Firestore db` de la [configuración](configuracion.md) y suponen tratamiento de las excepciones explicado en [persistencia](persistencia.md).

## Igualdad y condiciones combinadas

`whereEqualTo` expresa igualdad sobre un campo. Añadir otra condición encadena un filtro: queremos documentos que cumplan ambos. Por ejemplo, las reservas de SALA-A para una fecha concreta:

```java
Query consulta = db.collection("reservas")
    .whereEqualTo("espacio", "SALA-A")
    .whereEqualTo("fecha", "2027-02-15");
QuerySnapshot resultado = consulta.get().get();
for (QueryDocumentSnapshot documento : resultado.getDocuments()) {
    // Transformar cada documento usando el mapeo del repositorio.
}
```

Construir `consulta` no recupera los documentos. `get()` inicia su lectura y la segunda espera obtiene el resultado de esa operación. El listado vacío es un resultado correcto cuando no hay coincidencias.

La [guía de consultas de Firestore](https://firebase.google.com/docs/firestore/query-data/queries) describe filtros de igualdad, rangos y combinaciones. No todas las combinaciones arbitrarias son válidas ni todas corresponden al mismo índice. Primero hay que decidir qué preguntas permitirá la aplicación.

Si un parámetro opcional no existe, no añadimos esa condición. Una cadena vacía no debe convertirse en un filtro inútil por accidente. El servicio valida espacios conocidos, fechas y límites antes de construir el acceso.

## Rangos y ordenación

Un mínimo de asistentes se representa con `whereGreaterThanOrEqualTo`. Al ordenar por asistentes y después por ID, obtenemos un resultado determinista también cuando varias reservas tienen la misma cantidad:

```java
Query consulta = db.collection("reservas")
    .whereGreaterThanOrEqualTo("asistentes", 5)
    .orderBy("asistentes")
    .orderBy(FieldPath.documentId());
```

El campo se almacena como número, por lo que el límite debe enviarse con un tipo numérico compatible. Guardar algunos valores como texto produciría otro comportamiento y rompería nuestro contrato.

`orderBy` también influye en qué documentos pueden aparecer: un documento sin el campo ordenado no participa como si tuviera un cero implícito. La aplicación debe mantener un modelo consistente, aunque la base permita documentos de diferentes formas.

Con el filtro de igualdad de una sala, la fecha y el rango de asistentes podemos necesitar un índice compuesto. No se arregla la consulta quitando condiciones silenciosamente ni ordenando de una manera diferente sin informar al cliente.

## Leer el aviso de índice

Un índice relaciona valores de campos con documentos para responder consultas sin recorrer toda la colección. Firestore mantiene índices de campo individuales y permite definir otros compuestos para determinadas combinaciones.

En el servicio cloud, una consulta que necesita un índice ausente suele fallar con información para crearlo. Revisa proyecto, campos y orden, define el índice, espera a que esté listo y repite la consulta. Si el repositorio incluye `firestore.indexes.json`, conserva ahí la definición para reproducir el entorno. La [guía oficial de índices](https://firebase.google.com/docs/firestore/query-data/indexing) explica este flujo.

El emulador facilita pruebas locales, pero no reproduce todas las restricciones de índices del servicio real. **Que una consulta funcione localmente no demuestra que tenga los índices necesarios en cloud.** La comprobación del proyecto remoto, cuando exista, debe registrarse de forma separada.

Un índice acelera una pregunta concreta; no valida asistentes, evita horarios incompatibles ni reemplaza la capa de servicio.

## Limitar resultados

Si una colección crece, devolver todos sus documentos hace cada respuesta más costosa y difícil de utilizar. `limit` permite pedir solo un número acotado:

```java
Query consulta = db.collection("reservas")
    .orderBy(FieldPath.documentId())
    .limit(5);
```

El límite pertenece también al contrato de la API: se rechazan números negativos, cero o valores excesivos en lugar de ignorarlos. La práctica final utilizará tamaños entre 1 y 20.

Un límite por sí solo devuelve siempre la primera parte del mismo orden. Para continuar necesitamos recordar dónde terminó la página anterior.

## Cursores y orden estable

Un cursor señala una posición en el orden de la consulta. `startAfter` comienza después de esa posición; `startAt` la incluye. En un listado ordenado únicamente por ID, un ejemplo de continuación sería:

```java
Query siguiente = db.collection("reservas")
    .orderBy(FieldPath.documentId())
    .startAfter("reserva-aula")
    .limit(5);
```

El texto es un valor de posición, no una orden para buscar y devolver ese documento. En este diseño no hace falta que siga existiendo el documento cuyo ID representa la posición. La [guía oficial de cursores](https://firebase.google.com/docs/firestore/query-data/query-cursors) explica otras opciones, como utilizar una instantánea de documento.

Si ordenásemos por asistentes e ID, el cursor necesitaría valores compatibles con ese orden. Un cursor que solo guarda asistentes sería ambiguo cuando varias reservas empatan. No se puede reutilizar un cursor de una consulta con filtros y orden distintos sin definir qué significa.

Paginación no garantiza una instantánea global: entre dos peticiones otros clientes podrían crear, modificar o eliminar reservas. El contrato del listado debe evitar prometer que todas las páginas representan exactamente el mismo instante.

## Consultar no equivale a garantizar disponibilidad

Para detectar una incompatibilidad horaria hay que considerar reservas activas de la misma sala y fecha, y comparar sus intervalos. Una consulta devuelve información para tomar esa decisión; no bloquea automáticamente nuevas escrituras de otras aplicaciones.

La práctica final entrega preparada la infraestructura que coordina una escritura con la comprobación de sus condiciones. El alumnado completa las reglas de agenda; no tiene que implementar el protocolo transaccional del SDK. Una lectura seguida de una escritura independientes no ofrece esa garantía, y un bloqueo Java local tampoco coordina varios procesos. Las [transacciones Firestore](ampliacion.md#escrituras-agrupadas-y-transacciones) que utiliza esa infraestructura se explican en Ampliación. La protección se aplica a las operaciones que siguen el protocolo de la API; una escritura manual que lo eluda puede violar las reglas.

## Comprobaciones en el proyecto

El **TODO 2** de `FirestoreReservaRepository.listar` utiliza los filtros de `espacio`, `fecha` y `minAsistentes`. Comprueba el listado sin filtros y con cada condición por separado; después combínalas. El mínimo de asistentes admitido está entre 1 y 30; cero o un valor superior a 30 se rechazan con `400`. Una consulta válida sin coincidencias devuelve una lista vacía con `200`.

La práctica final añadirá consultas de estado y responsable y un endpoint de páginas independiente. La interfaz y la colección Postman ya contienen sus controles y peticiones; implementarlas no requiere construir un nuevo frontend.

Continúa con [HTTP y REST](rest.md) para entender cómo recibe estos filtros el controlador y qué devuelve al cliente.
