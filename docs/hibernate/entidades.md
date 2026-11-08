# Entidades y mapeo

Una entidad relaciona un objeto Java con una fila que tiene identidad persistente. No todas las clases del programa son entidades: un formulario o un resultado de consulta puede utilizar otros objetos sin crear una tabla para cada uno.

## @Entity, tabla y clave

Esta entidad reducida muestra el mapeo de una editorial. Las anotaciones proceden de `jakarta.persistence`:

```java
@Entity
@Table(name = "editorial")
public class Editorial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    protected Editorial() {}

    public Editorial(String nombre) { this.nombre = nombre; }
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
```

`@Entity` identifica una clase persistente. `@Table` indica su tabla. `@Id` identifica la clave y `@GeneratedValue` permite que la infraestructura la obtenga; con `IDENTITY` se utiliza el mecanismo de identidad de la base. Un objeto nuevo empieza con ID nulo y, después de persistirlo, recibe su identidad.

La [API de Entity](https://jakarta.ee/specifications/persistence/3.2/apidocs/jakarta.persistence/jakarta/persistence/Entity.html) describe los requisitos de las entidades. En este proyecto se utilizan clases no finales con constructor público o protegido sin parámetros. Los records se reservan a otras representaciones de datos.

## Constructor y acceso a propiedades

El constructor sin parámetros permite que el proveedor construya una instancia al recuperar una fila. Puede ser `protected` para expresar que no es la vía habitual de creación desde la aplicación. El otro constructor facilita crear una editorial con sus datos.

Las anotaciones están sobre campos: ese es el criterio de acceso al estado persistente del ejemplo. Los métodos `getNombre` y `setNombre` permiten que el resto de Java consulte o modifique el objeto. No hay que mezclar sin criterio anotaciones en campos y getters suponiendo que son dos mapeos independientes.

Una clase sin constructor adecuado puede compilar y fallar al persistir o recuperar. Una anotación en un import de otra versión tampoco se arregla cambiando la UI. Revisa modelo y metadatos cuando el mensaje de arranque señale una entidad.

## Columnas y tipos

`nullable = false` prohíbe nulos en la columna; `length` limita la longitud cuando se genera su definición; `unique = true` pide unicidad. Estas restricciones no sustituyen los mensajes de servicio: una cadena de espacios puede no ser nula y seguir siendo un nombre inválido.

Los tipos Java se relacionan con tipos SQL compatibles: `String` con texto, enteros con cantidades y `BigDecimal` con importes decimales. Para un precio se puede especificar precisión y escala:

```java
@Column(nullable = false, precision = 7, scale = 2)
private BigDecimal precio;
```

La precisión limita el total de dígitos de esa columna decimal y la escala reserva dos para la parte decimal. El servicio aplica además el intervalo admitido por el catálogo. Reutiliza la explicación de [BigDecimal en Ficheros](../ficheros/arquitectura.md#comprobar-importes-con-bigdecimal): comparar o redondear importes requiere una decisión expresa.

Un identificador técnico y un ISBN no son intercambiables. El ID distingue la fila; el ISBN del proyecto es un dato de catálogo de trece cifras protegido por unicidad. Cambiar un título no genera por ello una identidad nueva.

## @Version y edición concurrente

Una propiedad `@Version` permite detectar que una entidad leída ha cambiado antes de guardar su nueva versión. Hibernate incorpora la versión esperada a la actualización y rechaza una escritura obsoleta.

Esa protección se refiere al estado que conoce la sesión. El catálogo entrega además la versión dentro de `LibroDatos` y la recibe al guardar una edición: si el formulario utiliza una versión anterior, el servicio lo rechaza y pide actualizar. El contrato explícito permite detectar también esa edición antigua del cliente; no se obtiene por añadir una anotación y olvidarse de la versión recibida.

## Relaciones forman parte del mapeo

Un libro contiene editorial, autores y un detalle de publicación opcional. No se guardan esas referencias como direcciones de memoria: se relacionan con claves y tablas de unión. La página de [relaciones y ciclo de vida](relaciones.md) desarrolla las tres cardinalidades y sus propietarios.

El detalle tiene idioma y número de páginas. Es una entidad dependiente de su libro; una editorial o un autor tienen vida independiente y pueden relacionarse con varios libros. Esa diferencia determinará qué cascadas son razonables.

## Mapeo XML como alternativa breve

Las anotaciones no son la única forma de describir entidades. Un archivo de mapeo XML puede relacionar una clase Java ordinaria con su tabla sin anotar sus campos. Este fragmento representa una entidad de demostración independiente del catálogo:

```xml
<entity-mappings xmlns="https://jakarta.ee/xml/ns/persistence/orm" version="3.2">
  <entity class="es.mjsplayground.acda.hibernate.biblioteca.demo.NotaXml" access="FIELD">
    <table name="demo_notas_xml"/>
    <attributes>
      <id name="id"><generated-value strategy="IDENTITY"/></id>
      <basic name="texto"><column nullable="false" length="100"/></basic>
    </attributes>
  </entity>
</entity-mappings>
```

La clase conserva sus campos, constructor sin parámetros y accesores. El proveedor debe recibir el recurso de mapeo al construir sus metadatos: escribir el XML sin registrarlo no activa una entidad. La [especificación Jakarta Persistence](https://jakarta.ee/specifications/persistence/3.2/) incluye los contratos de mapeo y sus esquemas.

El proyecto guiado usa anotaciones; esta demostración no obliga a implementar el mismo catálogo dos veces. Su entrada y prueba se enlazan desde [Ampliación](ampliacion.md), donde se concentran los ejemplos de consulta adicionales.

## Etapa del proyecto

Examina las entidades: campos, constructores, columnas, identidad y versión están preparados. El **TODO 1** marca asociaciones temporalmente excluidas con `@Transient`; sustituye esa anotación por los mapeos 1:N, M:N y 1:1 explicados en la página de relaciones. La interfaz ya conoce los campos y no debe reemplazarse para adaptar un mapeo incorrecto.

Comprueba que crear dos filas con el mismo ISBN se rechaza, que un nombre vacío no llega al almacenamiento y que el constructor permite recuperar una entidad. Las [sesiones y transacciones](persistencia.md) mostrarán cuándo se realizan esas operaciones.

### Comprobación antes de programar el CRUD

Identifica en `model/entity/Libro.java` el constructor sin argumentos que necesita el proveedor, el constructor que usa tu servicio y el atributo `id` que asigna la base. No sustituyas una entidad existente por un objeto nuevo del mismo título. Conserva `@Version`, sus accesores y el apoyo `BibliotecaService.comprobarVersion`: están preparados para que puedas editar sin diseñar concurrencia propia.

Completa las asociaciones por parejas siguiendo [las tres comprobaciones de relaciones](relaciones.md#seguir-identidades-concretas). Ejecuta primero la fase `mapeos`, que inspecciona las anotaciones sin servidor, y después `catalogos` con MySQL. Una clase que compila con `@Transient` todavía no demuestra que sus asociaciones se guarden.
