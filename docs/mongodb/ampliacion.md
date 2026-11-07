# Ampliación

El proyecto ordinario calcula sus reglas y clasificaciones en Java. Estas explicaciones muestran cómo trasladar algunas transformaciones al motor y analizar el coste de las consultas. **No necesitas Aggregation Pipeline para resolver la práctica final.**

## Pensar en etapas

Un pipeline recibe documentos y los transforma mediante etapas. La salida de una alimenta a la siguiente: filtrar, proyectar, agrupar u ordenar no son intercambiables si cambia qué datos permanecen.

Por ejemplo, podemos calcular los goles acumulados de plantillas por categoría. El siguiente pipeline utiliza la colección `equipos` y los campos del proyecto; es una consulta, no una actualización:

```javascript
db.equipos.aggregate([
  { $match: { categoria: "SENIOR" } },
  { $project: {
      categoria: 1,
      golesEquipo: { $sum: "$jugadores.estadisticas.goles" }
  } },
  { $group: {
      _id: "$categoria",
      equipos: { $sum: 1 },
      goles: { $sum: "$golesEquipo" }
  } },
  { $sort: { goles: -1, _id: 1 } }
]);
```

Se puede ejecutar desde una consola `mongosh` conectada a la base del proyecto o construir esas etapas en el panel **Aggregations** de Compass. Sin equipos de esa categoría, el resultado será vacío. La [introducción oficial a agregación](https://www.mongodb.com/docs/manual/aggregation/) explica el flujo de etapas.

## $match: reducir el conjunto de entrada

`$match` utiliza condiciones de consulta para conservar documentos coincidentes. En el ejemplo permite pasar solo equipos sénior. Si filtramos antes de transformar, evitamos trabajo sobre documentos que no necesitamos.

Un filtro situado después de `$group` ya no ve documentos de equipo: ve grupos con campos como `equipos` y `goles`. Colocar allí una condición sobre `jugadores` no recupera los documentos originales.

## $project y cálculos

`$project` elige qué campos salen y puede calcular otros. El `1` conserva una propiedad; una expresión produce su valor nuevo. `_id` permanece salvo que se excluya expresamente.

En nuestro pipeline, `$sum` aplicado al array de goles calcula el total de un equipo. Después, en `$group`, `$sum` acumula esos totales entre equipos. El mismo nombre de operador participa en contextos distintos.

Otro ejemplo podría calcular una medida explícita de participaciones directas sumando goles y asistencias:

```javascript
{ $project: {
    nombre: 1,
    intervenciones: {
      $add: [
        { $sum: "$jugadores.estadisticas.goles" },
        { $sum: "$jugadores.estadisticas.asistencias" }
      ]
    }
} }
```

Es una métrica de demostración, no los puntos de competición. Las expresiones deben recibir valores numéricos compatibles: guardar estadísticas como texto impide confiar en esos cálculos. La [etapa de proyección](https://www.mongodb.com/docs/manual/reference/operator/aggregation/project/) permite contrastar inclusión, exclusión y campos calculados.

## $group: una salida por clave

`$group` reúne documentos según la expresión de `_id`. `"$categoria"` toma el valor del campo; `"categoria"` sería un texto constante y reuniría todos bajo esa misma clave.

`equipos: { $sum: 1 }` cuenta documentos del grupo. `goles: { $sum: "$golesEquipo" }` suma los valores de cada documento. La [referencia de agrupación](https://www.mongodb.com/docs/manual/reference/operator/aggregation/group/) explica estos acumuladores y otros como mínimo, máximo o media.

El resultado agrupado no es una entidad `Equipo`. Desde Java convendría mapearlo a un DTO de lectura específico. Forzarlo dentro del modelo persistido mezclaría dos contratos distintos.

## $sort: ordenar el resultado que existe ahora

`$sort` ordena por las propiedades presentes en ese punto del pipeline. `1` indica ascendente y `-1`, descendente. Añadir una clave de desempate permite un orden determinista.

Ordenar antes de agrupar no garantiza el orden final de los grupos. Si lo que presentamos son categorías por goles totales, ordenamos el resultado agrupado. Un índice puede ayudar en determinados recorridos, pero un campo calculado durante el pipeline no tiene necesariamente un índice persistido aprovechable.

## $lookup: resolver una referencia en una consulta

La sede de cada equipo vive en otra colección. Podemos combinarla para preparar una lectura sin duplicar su nombre dentro de todos los equipos:

```javascript
db.equipos.aggregate([
  { $lookup: {
      from: "sedes",
      localField: "sedeId",
      foreignField: "_id",
      as: "sede"
  } },
  { $project: { nombre: 1, categoria: 1, sede: 1 } },
  { $sort: { nombre: 1, _id: 1 } }
]);
```

`from` es el nombre de la colección, no el de una clase Java. `sede` será una lista de coincidencias; si no existe la referencia, puede estar vacía. El tipo de `sedeId` debe coincidir con el de `_id` de las sedes: una cadena no coincide automáticamente con un `ObjectId` de aspecto parecido.

La [etapa `$lookup`](https://www.mongodb.com/docs/manual/reference/operator/aggregation/lookup/) combina datos para la respuesta; no crea claves foráneas ni impide borrar una sede utilizada. Tampoco significa que debamos resolver todas las lecturas mediante combinaciones complejas: un catálogo pequeño y preparado puede ser suficiente en la aplicación inicial.

## Índices compuestos

Un índice compuesto ordena varias propiedades en una estructura. Para una consulta que selecciona categoría y ordena por nombre e ID, podríamos estudiar este índice:

```javascript
db.equipos.createIndex(
  { categoria: 1, nombre: 1, _id: 1 },
  { name: "demo_categoria_nombre_id" }
);
```

Este comando crea un índice y debe ejecutarse solo en un entorno de pruebas autorizado. No se añade ni elimina infraestructura en una base compartida para seguir una demostración.

El orden de las claves importa: el índice no ofrece las mismas posibilidades para cualquier combinación de filtros y ordenaciones. Un índice sobre un array puede ser multikey y tiene restricciones adicionales. La [documentación de índices compuestos](https://www.mongodb.com/docs/manual/core/indexes/index-types/index-compound/) ayuda a evaluar prefijos y orden de las claves.

Más índices no equivalen a más calidad. También ocupan espacio y deben mantenerse en escrituras. Es preferible estudiar consultas utilizadas, volumen de datos y frecuencia de acceso.

## explain y rendimiento observado

`explain` permite inspeccionar el plan y, con `executionStats`, observar trabajo de una ejecución. Esta consulta mantiene el mismo filtro y orden que queremos analizar:

```javascript
db.equipos.find({ categoria: "SENIOR" })
  .sort({ nombre: 1, _id: 1 })
  .explain("executionStats");
```

Revisa el plan elegido, si hay recorrido de colección o índice, cuántas claves y documentos se examinan y cuántos resultados se devuelven. Los nombres y la forma del árbol pueden variar entre versiones y motores; no debe fijarse un único árbol como contrato de la aplicación. La [referencia de resultados explain](https://www.mongodb.com/docs/manual/reference/explain-results/) describe sus campos.

Compara antes y después de un índice con datos representativos y la misma consulta. Una colección con dos equipos y una sola medición no permite afirmar una mejora de rendimiento. El optimizador puede preferir recorrer una colección pequeña aunque exista el índice.

La optimización también incluye evitar recuperar campos innecesarios, consultas repetidas por cada fila y desplazamientos de páginas enormes. No se sustituye una regla de negocio correcta por un cálculo más rápido que devuelve un resultado distinto.
