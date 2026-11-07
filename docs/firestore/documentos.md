# Documentos, colecciones y relaciones

En [JDBC](../jdbc/index.md), cada producto ocupaba una fila con columnas definidas por un esquema. Una reserva también puede guardarse como un **documento**: un conjunto de campos con nombres y valores. Cloud Firestore organiza documentos en colecciones y permite consultar sus campos sin usar SQL.

NoSQL no significa «sin estructura» ni «sin reglas». Cambia dónde y cómo expresamos esa estructura. El servicio debe seguir validando fechas, horarios, capacidad e identidad de los espacios, aunque la base permita almacenar documentos con campos diferentes.

## Firebase y Firestore

Firebase reúne servicios para desarrollar aplicaciones: identidad, almacenamiento, mensajería y otros productos. **Cloud Firestore** es su base documental. Firebase Authentication identifica usuarios; no es la base de reservas. El Admin SDK permite que un backend Java utilice servicios Firebase desde un entorno de confianza.

En nuestro proyecto el navegador no accede directamente a Firestore:

```text
Navegador o Postman
        ↓ HTTP / JSON
Controlador REST de Spring Boot
        ↓ DTO de entrada
Servicio de reservas: validaciones y casos de uso
        ↓
Repositorio Firestore: documentos y consultas
        ↓ SDK de servidor
Firestore local emulado o servicio cloud
```

El frontend se entrega terminado. El alumnado implementa backend y parte de los endpoints REST. El servidor concentra las reglas para que funcionen igual desde la web y desde Postman.

## Un documento tiene una ruta y campos

El [modelo oficial de Firestore](https://firebase.google.com/docs/firestore/data-model) distingue colecciones y documentos. `reservas` puede ser una colección y `reserva-aula` el identificador de un documento. Su ruta completa dentro de la base sería `reservas/reserva-aula`.

Esta representación JSON sirve para comprender los campos de una reserva; no es una instrucción SQL ni una petición completa:

```json
{
  "espacio": "SALA-A",
  "fecha": "2027-02-15",
  "inicio": 10,
  "fin": 12,
  "responsable": "Coordinación",
  "asistentes": 8,
  "equipo": ["proyector", "pizarra"],
  "detalles": {"actividad": "Taller", "contacto": "Recepción"},
  "estado": "ACTIVA"
}
```

Los valores pueden ser texto, números, booleanos, timestamps, listas, mapas, referencias o nulos, entre otros tipos admitidos. JSON representa solo parte de esa riqueza: por ejemplo, una referencia Firestore y un timestamp no son necesariamente una cadena en el almacenamiento.

En este proyecto la fecha se guarda como texto ISO `AAAA-MM-DD` y las horas como enteros. Es una decisión de dominio sencilla para reservas por días y horas completas; no representa instantes universales ni resuelve cambios de zona horaria. Una aplicación con usuarios de distintas zonas necesitaría un contrato temporal más completo.

El identificador no necesita estar repetido dentro de los campos: `DocumentSnapshot.getId()` permite recuperarlo. Puede generarse automáticamente para una nueva reserva o elegirse explícitamente cuando exista una clave de negocio adecuada. No debe confundirse un ID de documento con la prueba de que el usuario tiene permiso para leerlo.

## Comparación con una tabla relacional

| Pregunta | Modelo relacional | Modelo documental |
| --- | --- | --- |
| ¿Cómo represento una reserva? | Una fila y sus columnas | Un documento y sus campos |
| ¿Dónde guardo una lista pequeña? | Habitualmente, tabla relacionada | Puede ser una lista dentro del documento |
| ¿Cómo vinculo otro objeto? | Clave foránea y consultas relacionadas | ID, referencia, documento embebido o subcolección |
| ¿Quién valida la estructura? | Esquema SQL y aplicación | Aplicación; reglas de acceso para clientes cuando correspondan |
| ¿Cómo recupero lo necesario? | Consultas SQL, incluidos JOIN | Consultas de documentos compatibles con los índices |

Firestore no es un sustituto universal de SQL. En un sistema con muchas relaciones y consultas analíticas cambiantes, el modelo relacional puede resultar más natural. Un agregado documental es útil cuando sus datos suelen leerse y modificarse juntos. La elección requiere considerar consultas, consistencia, coste y mantenimiento, no solo la forma de un JSON.

## Mapas y listas: datos que forman parte de la reserva

`detalles` es un mapa: cada clave tiene su valor, como `actividad → Taller`. `equipo` es una lista de textos. Al leer el documento, Java debe comprobar que los tipos esperados coinciden con los almacenados. Un número que antes se guardó como texto no se convierte automáticamente en una cantidad válida del dominio.

Un mapa pequeño y acotado evita abrir otra colección para información que siempre se consume con su reserva. No es apropiado acumular un historial ilimitado dentro de una lista: el documento tiene límites y cada lectura devuelve sus campos. La [estructura de datos](https://firebase.google.com/docs/firestore/manage-data/structure-data) debe decidirse según cómo crece y se consulta la información.

## Referencias, subcolecciones y duplicación

Imaginemos que los espacios pasan a tener su propio catálogo:

```text
espacios/SALA-A
    nombre: Sala de reuniones
    capacidad: 12

reservas/reserva-aula
    espacio: SALA-A
    fecha: 2027-02-15

reservas/reserva-aula/comentarios/comentario-1
    texto: Necesitamos acceso antes de la actividad
```

Guardar `SALA-A` como texto es una referencia lógica. Un campo de tipo referencia Firestore apunta a una ruta, pero **no crea una clave foránea ni garantiza que el documento apuntado exista**. El servicio valida la relación y define qué ocurre si se elimina el objeto relacionado.

Una subcolección permite que los comentarios crezcan independientemente y se consulten por separado. No se obtiene automáticamente al leer la reserva; tampoco debe suponerse que borrar el documento padre borra todos sus descendientes. Hay que planificar esa gestión expresamente.

Duplicar el nombre del espacio en la reserva puede facilitar una lectura o conservar cómo se llamaba cuando se reservó. Esa copia puede quedar desactualizada respecto al catálogo. Antes de duplicar hay que decidir si queremos una instantánea histórica o un dato sincronizado y quién mantendrá la coherencia.

## Etapa del proyecto: comprender el agregado

El proyecto utiliza espacios conocidos, reservas con listas y mapas, y una interfaz preparada. Antes de modificar el repositorio, localiza la entidad y los DTO: ¿qué campos recibe el cliente?, ¿cuál genera el servidor?, ¿dónde se decide el estado inicial?

No es necesario repartir cada campo en un documento distinto. El objetivo es recuperar una reserva comprensible y validar sus invariantes en un servicio. Continúa con [configuración y acceso seguro](configuracion.md).
