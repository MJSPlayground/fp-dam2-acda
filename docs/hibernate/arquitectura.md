# Arquitectura, ORM y Jakarta Persistence

En [Ficheros](../ficheros/arquitectura.md) separamos la lectura técnica de un archivo, las reglas del inventario y la interfaz. Mantendremos esas responsabilidades al trabajar con una base relacional: ahora los objetos Java se relacionan con tablas, claves y filas.

El tema parte de Java básico y de los conceptos de tablas, claves primarias y claves foráneas. No necesitas conocer JDBC ni Spring para seguir el proyecto.

## Objetos y tablas representan relaciones de forma distinta

En Java, un libro puede tener una propiedad `Editorial editorial` y una colección de autores. En la base de datos, una fila no contiene directamente una referencia de memoria a otro objeto. Conserva una clave que identifica la editorial, y una tabla de unión representa los autores de cada libro.

```text
Java                         Base relacional
Libro.editorial          →   libros.editorial_id → editoriales.id
Libro.autores            →   libro_autor(libro_id, autor_id)
Libro.detalle            →   libros.detalle_id → detalles.id
```

El **desfase objeto-relacional** aparece al traducir identidad, tipos, asociaciones y ciclos de vida entre ambos modelos. Un objeto recién creado no tiene por qué disponer aún de un identificador de base; dos referencias Java distintas pueden representar la misma fila en momentos diferentes.

## Qué hace un ORM

Un ORM —mapeo objeto-relacional— utiliza metadatos para relacionar clases y propiedades con tablas y columnas. A partir de las operaciones sobre objetos genera las instrucciones que necesita la base y reconstruye entidades al consultar.

Esto reduce código repetitivo de conversión, pero no elimina las restricciones SQL ni convierte cualquier navegación entre objetos en una operación gratuita. Una asociación puede necesitar otra consulta y una escritura puede fallar por una clave duplicada o una relación inválida.

La [introducción de Hibernate](https://docs.hibernate.org/orm/current/introduction/html_single/) explica su papel como herramienta de persistencia. Aprenderemos a observar las instrucciones generadas, no a tratar el ORM como un almacenamiento que siempre funciona sin comprender su efecto.

## JPA, Jakarta Persistence e Hibernate

**Jakarta Persistence** es la [especificación que define conceptos y contratos de persistencia para Java](https://jakarta.ee/specifications/persistence/3.2/). El nombre JPA sigue utilizándose para referirse a esta familia de API. Las anotaciones actuales pertenecen a `jakarta.persistence`, como `@Entity` y `@Id`.

**Hibernate ORM** es una implementación o proveedor. Implementa contratos de Jakarta Persistence y ofrece su propia API, como `Session` y `SessionFactory`, además de capacidades específicas.

| Elemento | Función |
| --- | --- |
| Jakarta Persistence | Especificación y API común |
| Hibernate ORM | Proveedor que realiza el mapeo y las operaciones |
| Driver MySQL | Biblioteca que permite al proveedor comunicarse con ese motor |
| MySQL | Servidor que conserva filas y aplica restricciones |

Añadir solo una API no equivale a tener un proveedor funcionando. El proyecto incluye las dependencias compatibles y la configuración preparada. No mezcles ejemplos antiguos con imports `javax.persistence` con los actuales `jakarta.persistence`.

## Capas desde el primer caso de uso

```text
Interfaz JavaFX preparada
          ↓
Servicio de catálogo: validación y transacción
          ↓
Repositorios: persistencia y consultas sobre entidades
          ↓
Session de Hibernate → MySQL
```

`model.entity` contiene las entidades persistidas. `model.repository` encapsula consultas y operaciones técnicas. `model.service` aplica las reglas y coordina varias modificaciones. `config` prepara el proveedor y las conexiones. El punto de entrada está en `app`.

La UI recibe valores, muestra catálogos y confirma acciones. No debe abrir una sesión ni eliminar autores directamente. Los repositorios tampoco invocan a la UI o al servicio para decidir qué operación de negocio ejecutar.

Por ejemplo, crear un libro requiere comprobar sus datos y recuperar editorial y autores existentes. El servicio coordina ese trabajo en una transacción; el repositorio ejecuta sus operaciones con la sesión de ese caso de uso.

## Identidad y estado no son lo mismo que presentación

Una entidad representa una fila con identidad persistente. Su ID no debe confundirse con la posición de una fila en una tabla JavaFX ni con el título visible del libro. Si ordenamos la pantalla, no cambian las claves de la base.

La pantalla puede utilizar una representación de lectura con nombres de editorial y autores ya preparados. Eso permite cerrar la sesión antes de devolver el resultado y evita que un control gráfico intente cargar una relación después. Las [sesiones y estados](persistencia.md) explican cuándo están disponibles esas asociaciones.

## Validaciones y errores

El servicio conserva reglas simples y comprensibles: nombres con contenido, precios válidos, relaciones existentes e identificadores únicos. La base añade restricciones, como unicidad de ISBN y claves foráneas.

Una validación previa no garantiza que otra operación no cambie la base después. Por eso también hay que tratar el fallo real de escritura y revertir el caso de uso cuando corresponda. Las excepciones mantienen sus causas y el logging registra el detalle, continuando lo aprendido en [Ficheros](../ficheros/arquitectura.md).

El objetivo no es diseñar reglas comerciales desmesuradas, sino entender cómo conservar un catálogo coherente y comprobarlo. Continúa con [entorno y configuración](configuracion.md).
