# Documentos, BSON y modelado

MongoDB es una base documental que podemos ejecutar como servidor local o utilizar como servicio gestionado. Recuperamos lo aprendido sobre [documentos, mapas y listas en Firestore](../firestore/documentos.md): la aplicación mantiene un modelo y sus reglas aunque la base permita documentos con formas diferentes.

En este tema usamos Spring Data MongoDB para una gestión de equipos y jugadores. El frontend y el contrato HTTP ya están preparados. La novedad se concentra en representar objetos, consultar documentos y proteger las actualizaciones.

## Qué cambia respecto a Firestore y SQL

| Aspecto | SQL con JDBC | Firestore del tema anterior | MongoDB de este tema |
| --- | --- | --- | --- |
| Unidad de almacenamiento | Fila de tabla | Documento en colección | Documento BSON en colección |
| Acceso desde Java | SQL y mapeo manual | SDK de servidor y mapeo explícito | Repositorios Spring Data y MongoTemplate |
| Organización de relaciones | Claves y JOIN | IDs, referencias, mapas, subcolecciones | IDs, documentos embebidos y consultas |
| Entorno inicial | SQLite local | Emulador | Servidor MongoDB en Docker |

Compartir un modelo documental no significa que las APIs, consultas, índices o garantías sean idénticos. El emulador Firestore no ejecuta MongoDB; Compass tampoco sirve para inspeccionar una base Firestore.

## JSON es una representación; BSON es el formato del motor

Los documentos MongoDB contienen pares campo–valor y pueden incluir otros documentos o listas. El motor utiliza **BSON**, un formato binario con tipos adicionales a los de JSON. Entre ellos hay enteros de distinto tamaño, fechas, identificadores `ObjectId` y decimales.

Esta representación de ejemplo permite observar un equipo, su lista de jugadores y sus estadísticas embebidas:

```json
{
  "nombre": "Atlético Norte",
  "categoria": "SENIOR",
  "jugadores": [{
    "nombre": "Alex",
    "posicion": "MEDIO",
    "disponible": true,
    "estadisticas": {"partidos": 4, "goles": 2, "asistencias": 1}
  }]
}
```

Es una ilustración del modelo documental, no el cuerpo exacto de alta del proyecto. `jugadores` es una lista embebida en el equipo, y cada elemento contiene sus estadísticas. Las [estructuras de documentos](https://www.mongodb.com/docs/manual/core/document/) y los [tipos BSON](https://www.mongodb.com/docs/manual/reference/bson-types/) explican qué representa realmente el motor.

Texto `"32"` y número `32` no son el mismo valor. JSON no muestra por sí solo todas las diferencias entre `int`, `long` o `Decimal128`. Al persistir desde Java hay que conocer el mapeo elegido, no interpretar todo como cadenas.

## Identidad: _id y ObjectId

Cada documento tiene un campo `_id` que identifica su posición dentro de la colección. MongoDB crea un índice único sobre él. Si no se proporciona otro identificador, los drivers pueden generar un `ObjectId`.

Un `ObjectId` suele representarse mediante 24 caracteres hexadecimales. Esa cadena visible y un valor BSON `ObjectId` son tipos distintos: una consulta sobre `_id` debe usar el tipo almacenado. Spring Data puede realizar conversiones según el tipo y las anotaciones de la entidad.

El proyecto debe utilizar un criterio consistente. No se adivina el equipo por su nombre ni se asume que todos los IDs sean números consecutivos. Un identificador también es un dato público de la API: conocerlo no concede autorización para modificar el documento.

## Embebidos: datos que se leen y cambian juntos

Las estadísticas simples de un jugador pueden vivir dentro del mismo documento. Esto permite recuperar nombre y cifras en una lectura y actualizar varios campos de ese documento con una sola operación atómica.

Los embebidos son adecuados cuando pertenecen al agregado y su crecimiento está acotado. Guardar todos los partidos de muchos años en una lista que crece sin límite no es la misma decisión que guardar tres contadores. Deben considerarse tamaño del documento, lecturas habituales y frecuencia de cambios.

El [modelado de datos de MongoDB](https://www.mongodb.com/docs/manual/data-modeling/) parte de cómo se usan los datos, no de convertir automáticamente cada tabla SQL en una colección ni de agruparlo todo en un único documento.

## Referencias: identidad sin clave foránea automática

En este proyecto la referencia explícita es `Equipo.sedeId`: `CENTRO` identifica un documento de la colección `sedes`. El servicio comprueba que exista antes de aceptar el equipo. Ese campo no crea por sí solo una clave foránea ni un borrado en cascada. Los jugadores, en cambio, están embebidos: no debes buscarlos en otra colección para completar este modelo.

Duplicar el nombre de la sede dentro del equipo facilitaría ciertas lecturas, pero obligaría a decidir si es una instantánea histórica o una copia que debe sincronizarse. No hace falta duplicar todo para evitar cualquier segunda consulta.

Spring Data ofrece mecanismos de referencia con comportamiento propio. No los añadimos por costumbre: una referencia explícita por ID resulta más visible en el primer proyecto. Una operación `$lookup`, explicada en [Ampliación](ampliacion.md), puede combinar colecciones al consultar sin convertirlas en un único documento persistido.

## Esquema flexible y validación

MongoDB permite que dos documentos de una colección no tengan los mismos campos. Eso ayuda a evolucionar una aplicación, pero también permite que existan datos que el código no sabe interpretar. La base puede incorporar validadores de colección; la aplicación sigue comprobando sus invariantes y sus contratos.

Antes de utilizar un campo nuevo, hay que decidir cómo tratar documentos anteriores: valor predeterminado, migración o rechazo explícito. «Flexible» no significa que un campo ausente y un cero sean siempre equivalentes.

En el proyecto diferenciaremos validación de entrada, reglas del servicio y restricciones del almacenamiento. Continúa con [Docker, Compass y configuración](configuracion.md).
