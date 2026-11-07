# Repositorios, servicios y errores

Ya sabemos [ejecutar SQL y convertir filas](consultas.md). Si colocáramos esas instrucciones dentro de cada botón, repetiríamos conexiones y consultas y mezclaríamos la persistencia con las reglas de venta. Separar responsabilidades permite probar cada decisión sin depender de la ventana.

## El recorrido de una operación

```text
Usuario pulsa «Vender»
        ↓
UI: recoge identificador y cantidad
        ↓
Servicio: valida cantidad, producto y disponibilidad
        ↓
Repositorio: ejecuta SQL en la conexión de la operación
        ↓
Motor: aplica restricciones y devuelve resultado
        ↓
Servicio devuelve resultado → UI actualiza listado o informa del error
```

Una entidad representa información del dominio, como un producto. El **repositorio** traduce operaciones de persistencia a SQL y transforma los resultados. El **servicio** representa casos de uso, valida invariantes y coordina operaciones. La **UI** presenta datos y recoge entradas: sus validaciones inmediatas mejoran la experiencia, pero nunca sustituyen a las del servicio.

DAO es otro nombre válido para organizar acceso a datos. En estos proyectos utilizamos la nomenclatura Repository de forma consistente; no significa que DAO esté obsoleto ni que un repositorio manual sea idéntico a todas las abstracciones de otros frameworks.

## Un contrato de persistencia pequeño

Un contrato debe expresar qué ofrece el repositorio y qué significa cada resultado. Esta interfaz ilustrativa permite listar, buscar e insertar productos:

```java
public interface ProductoRepository {
    List<Producto> listar();
    Optional<Producto> buscar(long id);
    long insertar(String nombre, long precioCentimos, int stock);
}
```

Una lista vacía significa que la consulta se realizó y no encontró filas. `Optional.empty()` significa que no existe la clave solicitada. `insertar` devuelve el identificador generado. Un fallo de acceso se comunica mediante una excepción, no como `-1`, `false` o una colección vacía que pueda confundirse con un resultado normal.

La implementación `JdbcProductoRepository` contiene el SQL. Una implementación de prueba podría trabajar en memoria, pero no validaría que el SQL real sea correcto: para eso necesitamos pruebas de integración con la base.

No es necesario crear una interfaz para cada clase por rutina. Una implementación concreta bien separada también puede ser suficiente en una aplicación pequeña. Lo esencial es que la UI no ejecute SQL ni el repositorio invoque al servicio.

## Validaciones en el servicio

El servicio recibe datos que podrían proceder de la UI, de una prueba o de otra entrada. Antes de persistir comprueba que el nombre tenga contenido, el precio sea positivo y el stock inicial no sea negativo. Esta demostración concentra esas reglas en una operación:

```java
public long alta(String nombre, long precioCentimos, int stock) {
    if (nombre == null || nombre.isBlank()) {
        throw new ReglaNegocioException("El nombre es obligatorio");
    }
    if (precioCentimos <= 0) {
        throw new ReglaNegocioException("El precio debe ser positivo");
    }
    if (stock < 0) {
        throw new ReglaNegocioException("Las existencias no pueden ser negativas");
    }
    return repository.insertar(nombre.strip(), precioCentimos, stock);
}
```

El repositorio no decide si ofrecer un descuento es válido ni calcula el importe de una venta. Tampoco debe llamar a otro repositorio para coordinarla: esa responsabilidad corresponde al servicio. Las restricciones SQL complementan las reglas de Java y protegen la integridad ante otras entradas.

Unidades y límites también son parte del contrato. El importe de una línea es precio por cantidad; para detectar desbordamiento de enteros podemos usar `Math.multiplyExact(precioCentimos, cantidad)`. Un valor que no cabe debe rechazarse con un mensaje comprensible; dejarlo desbordar puede convertir un importe positivo en negativo.

## Decimales, redondeo y céntimos

Guardamos dinero como `long` en céntimos, pero una entrada o un cálculo puede contener decimales. [`BigDecimal`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html) permite representarlos sin las aproximaciones binarias de `double`. Se construye desde texto, por ejemplo `new BigDecimal("3.275")`; convertir primero ese texto a `double` introduciría una aproximación innecesaria. Los objetos son inmutables: cada operación devuelve otro valor.

Antes de convertir a céntimos hay que decidir si se permite redondear. Como demostración independiente, una tarifa calculada de 3,275 € debe cobrarse con dos decimales. `setScale(2, ...)` fija esa escala; [`RoundingMode.HALF_UP`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/RoundingMode.html#HALF_UP) elige el vecino más cercano y, en un empate, se aleja de cero:

```java
BigDecimal calculado = new BigDecimal("3.275");
BigDecimal cobrable = calculado.setScale(2, RoundingMode.HALF_UP);
long centimos = cobrable.movePointRight(2).longValueExact();
BigDecimal euros = BigDecimal.valueOf(centimos, 2);
```

`cobrable` vale `3.28`, `centimos` vale `328` y `euros` vuelve a representar `3.28`. `movePointRight(2)` cambia euros a céntimos. `longValueExact()` rechaza fracciones pendientes o valores que no caben; `longValue()` podría truncarlos. Para comparar valores numéricos utiliza `compareTo`: `new BigDecimal("3.0").compareTo(new BigDecimal("3.00"))` devuelve cero aunque sus escalas sean distintas.

La política de redondeo pertenece al caso de uso. Para un importe escrito con demasiados decimales, puede corresponder rechazar la entrada en lugar de redondearla; para un resultado calculado puede exigirse un redondeo concreto. `HALF_UP` no significa redondear siempre hacia el valor positivo: `-3.275` con dos decimales da `-3.28`. En el almacén los precios se validan como positivos.

## Error de negocio y fallo técnico

«No hay existencias suficientes» es un resultado previsto del dominio: el usuario puede reducir la cantidad. «No se puede abrir el archivo de base de datos» es un fallo técnico: repetir datos distintos probablemente no lo arreglará. Debemos conservar esa diferencia.

[`SQLException`](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/SQLException.html) contiene el mensaje del driver, el estado SQL y un código del proveedor. Esos datos ayudan a diagnosticar; la UI no necesita mostrar un stack trace completo. Se puede encapsular el fallo en una excepción propia que conserve su causa:

```java
public final class PersistenciaException extends RuntimeException {
    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
```

Dentro del repositorio, el bloque de acceso puede terminar así:

```java
catch (SQLException error) {
    throw new PersistenciaException("No se han podido consultar los productos", error);
}
```

`error` queda accesible mediante `getCause()`. Crear una excepción nueva solo con `error.getMessage()` perdería el tipo original, el stack trace y las causas encadenadas. El mensaje de nuestra excepción aporta el contexto de la operación; la causa conserva el detalle técnico.

Una excepción de negocio sencilla puede heredar de `RuntimeException` y recibir un mensaje. Crear un tipo diferente tiene valor cuando el código necesita tratarlo de manera distinta, no para multiplicar nombres que todos se gestionan igual.

## Logging útil con SLF4J y Log4j2

SLF4J proporciona una API de logging; Log4j2 escribe los eventos según su configuración. El proyecto incluye su configuración para que no tengas que construirla antes de trabajar con JDBC. La [API de SLF4J](https://www.slf4j.org/manual.html) utiliza mensajes parametrizados:

```java
private static final Logger log = LoggerFactory.getLogger(AlmacenService.class);

// Tras confirmar correctamente la operación:
log.info("Venta registrada: producto={}, cantidad={}", productoId, cantidad);
```

El evento se registra después de confirmar, para no anunciar una venta que terminará revertida. Los niveles permiten distinguir seguimiento técnico (`DEBUG`), hechos relevantes (`INFO`), situaciones recuperables (`WARN`) y fallos (`ERROR`). No registres contraseñas ni copies cuerpos completos sin necesidad.

Conviene registrar un fallo una sola vez en el límite responsable de informar de él. Registrar el mismo stack trace en repositorio, servicio y UI produce ruido. Para un fallo inesperado, la UI puede mostrar un mensaje breve y registrar el detalle:

```java
catch (PersistenciaException error) {
    log.error("Fallo al cargar el almacén", error);
    mostrarError("No se ha podido cargar el almacén. Consulta el registro.");
}
```

`mostrarError` representa la acción de presentación ya preparada, no una operación del repositorio. Un `System.out.println` disperso no sustituye niveles, destino y contexto de logging.

## Probar el caso de uso y la persistencia

Una comprobación sólida observa el estado, además de comprobar la excepción. Tras intentar vender cero unidades, el stock y el historial deben seguir iguales. Tras una consulta sin coincidencias, la lista debe estar vacía sin registrar un fallo.

| Caso | Qué observar |
| --- | --- |
| Alta válida | Producto recuperable con los mismos valores |
| Nombre vacío o precio no positivo | Rechazo del servicio y ninguna fila nueva |
| Stock insuficiente | Mensaje de negocio y ninguna venta parcial |
| Identificador inexistente | Ausencia reconocida, sin confundirla con desconexión |
| Base inaccesible | Excepción técnica cuya causa original se conserva |
| Reinicio | Datos confirmados disponibles de nuevo |

Para aislar pruebas SQLite se puede utilizar un archivo temporal nuevo para cada caso. `jdbc:sqlite::memory:` tiene una particularidad importante: cada conexión independiente suele abrir su propia base en memoria. Si el código abre una conexión para crear tablas y otra para consultarlas, no verá necesariamente el mismo esquema. Una base temporal en archivo hace explícito el ciclo utilizado por la aplicación.

La operación de venta modifica varios datos relacionados. A continuación aprenderemos a mantenerlos unidos con [transacciones](transacciones.md).

## Etapa del proyecto: aplicar las reglas al guardar

El **TODO 3** de `AlmacenService.guardar` debe validar el objeto recibido antes de llamar al repositorio. En el almacén, un producto nuevo tiene identificador cero; no se admiten identificadores negativos, nombres vacíos ni nombres con más de 80 caracteres tras eliminar espacios exteriores. El precio debe estar entre 1 y 100.000.000 céntimos y las existencias no pueden ser negativas.

Comprueba que una llamada al servicio con datos inválidos también se rechaza aunque no proceda del formulario. Intenta un nombre que solo contenga espacios, precio cero y stock negativo. Después consulta los productos: ninguno de esos intentos debe haber añadido una fila. Un nombre duplicado es distinto: el esquema lo rechaza mediante su restricción `UNIQUE` y el error técnico debe conservarse como causa.
