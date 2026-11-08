# Repositorios con Spring Data JPA

En [Hibernate](../hibernate/persistencia.md) escribimos repositorios que recibían una sesión y realizaban operaciones ORM. Spring Data JPA permite declarar gran parte de ese contrato mediante una interfaz. JPA sigue definiendo el modelo de persistencia y Hibernate sigue siendo el proveedor utilizado por el proyecto. **Spring Data no sustituye la base de datos ni elimina las transacciones.**

## Entidad y repositorio

El catálogo utiliza `Libro` y `Editorial`: cada libro pertenece a una editorial. Los identificadores son `Long`; el ISBN identifica un libro para el negocio y tiene una restricción de unicidad. `@Entity`, `@Id`, `@ManyToOne` y `@Version` conservan el significado explicado en [el mapeo de entidades](../hibernate/entidades.md).

Este fragmento reducido muestra un repositorio. Su ubicación es el paquete `model.repository`; consulta los nombres completos en el código descargable:

```java
public interface LibroRepository extends JpaRepository<Libro, Long> {
    List<Libro> findByTituloContainingIgnoreCaseOrderByTituloAscIdAsc(String texto);
}
```

`JpaRepository<Libro, Long>` indica la entidad y el tipo de su identificador. Spring crea un objeto que implementa la interfaz y lo registra en el contenedor. Por eso el servicio puede recibir `LibroRepository` por constructor sin escribir `new` ni una clase `LibroRepositoryImpl` para el CRUD básico. Véase [Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/3.5/jpa.html).

## CRUD y resultados

| Operación | Resultado y precaución |
| --- | --- |
| `findById(id)` | `Optional<Libro>`: puede no existir. |
| `findAll()` | Lista de entidades; no garantiza por sí sola un orden concreto. |
| `save(libro)` | Persiste o combina el estado según si la entidad se considera nueva; utiliza su resultado cuando lo necesites. |
| `delete(libro)` | Solicita la eliminación; restricciones pueden fallar al sincronizar o confirmar. |
| `existsById(id)` | Comprueba existencia en ese instante; no bloquea futuras modificaciones. |

`Optional` representa presencia o ausencia. Un servicio convierte la ausencia en una decisión de negocio, sin llamar a `get()` a ciegas. Este ejemplo ilustrativo usa una excepción propia que hereda de `RuntimeException`:

```java
Libro libro = libros.findById(id)
    .orElseThrow(() -> new ReglaCatalogoException("El libro ya no existe"));
```

Si encuentra el libro, continúa. Si otro usuario lo eliminó, la operación termina con un mensaje comprensible. La excepción técnica de una caída de MySQL es distinta: no debemos transformarla en «libro inexistente» ni devolver una lista vacía.

`save` tampoco equivale a «el servidor ya confirmó todo». El SQL puede ejecutarse al hacer *flush* y el commit todavía puede fallar. El límite del caso de uso pertenece al [servicio transaccional](transacciones.md).

## Consultas derivadas

Spring interpreta los nombres de métodos utilizando **propiedades Java**, no nombres de columnas escritos en SQL. En el método anterior:

- `findBy` inicia una búsqueda.
- `Titulo` señala la propiedad `titulo`.
- `Containing` busca el texto dentro del título.
- `IgnoreCase` ignora diferencias de mayúsculas.
- `OrderByTituloAscIdAsc` ordena por título y después por identificador, para resolver empates de forma estable.

Con «program» se obtienen títulos que contienen ese texto, según la consulta generada. El parámetro es un valor, no una parte de SQL que concatenamos. Las consultas derivadas `Containing` escapan los comodines reconocidos por `LIKE`; escribir `%` en el formulario no debe convertirse en una instrucción libre.

Otro ejemplo reducido filtra una relación por su identificador:

```java
List<Libro> findByEditorialIdOrderByTituloAsc(Long editorialId);
```

Spring recorre `editorial.id`. Un error como `findByTitlo` impide crear la consulta porque `titlo` no es una propiedad válida. Lee el error de arranque y compara el nombre con la entidad. La [referencia de consultas derivadas](https://docs.spring.io/spring-data/jpa/reference/3.5/jpa/query-methods.html) recoge los operadores disponibles.

Si el nombre deja de ser legible, una consulta `@Query` puede ser más clara. No es necesario convertir todas las consultas HQL previas en nombres gigantes: el criterio sigue siendo expresar correctamente la búsqueda y comprobar sus resultados.

## Repositorio y servicio no son intercambiables

El repositorio sabe guardar y buscar. El servicio decide si se permite crear un ISBN duplicado, qué rango admite el stock o si una editorial puede eliminarse. Una restricción única en MySQL sigue siendo necesaria aunque se compruebe antes la existencia: dos operaciones concurrentes pueden superar esa comprobación inicial.

La interfaz web llama al servicio. Si el controlador accede directamente al repositorio, puede omitir reglas y fragmentar la transacción. Además, una entidad tiene identidad y estado de persistencia; un formulario no debe copiar sobre ella campos que la persona no puede modificar.

## Contratos e implementaciones sustituibles

La inyección por interfaz separa el uso de una capacidad de su implementación. Spring Data ya lo hace con sus repositorios. No hace falta añadir una segunda interfaz idéntica únicamente para repetir sus métodos.

Para una capacidad que sí tiene alternativas, el proyecto utiliza un contrato de notificación. Una implementación registra un mensaje y otra permite ejecutar sin esa notificación. El consumidor recibe el contrato; los perfiles eligen la implementación. Esa sustitución cambia **cómo se notifica**, no finge haber guardado un catálogo que no se ha persistido.

Una implementación en memoria para una prueba puede devolver datos controlados, pero no demuestra que MySQL respete restricciones, consultas y rollback. Las pruebas de integración deben comprobar también el repositorio real.

Continúa en [la etapa de repositorios del proyecto](proyecto.md), donde encontrarás las consultas y los `TODO` concretos del starter.
