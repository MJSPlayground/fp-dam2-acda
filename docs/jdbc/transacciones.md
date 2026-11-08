# Transacciones y ventas atómicas

Una venta reduce las existencias y añade una entrada al historial. Si la primera escritura funciona y la segunda falla, el almacén no puede quedarse con menos unidades y sin justificante. Necesitamos tratar ambas como **una sola operación**.

## Qué aporta una transacción

Una transacción agrupa operaciones sobre la base. Podemos confirmar sus cambios con `commit` o deshacer los todavía no confirmados con `rollback`. Una operación de negocio no tiene por qué coincidir con una sola instrucción SQL.

Las propiedades ACID ayudan a razonar sobre el resultado:

| Propiedad | En una venta |
| --- | --- |
| Atomicidad | Descuento de stock e inserción del historial se confirman juntos |
| Consistencia | El estado final respeta restricciones e invariantes |
| Aislamiento | La interacción con otras operaciones se controla según el motor y nivel de aislamiento |
| Durabilidad | Tras confirmar, el motor conserva el resultado conforme a sus garantías de almacenamiento |

La consistencia requiere un diseño correcto: una transacción no detecta por sí sola que una cantidad de venta negativa carece de sentido. El servicio sigue siendo responsable de validarla.

## autoCommit y propiedad de la conexión

Una conexión JDBC nueva está normalmente en modo `autoCommit`: cada instrucción se confirma como operación independiente al completarse. Para agrupar escrituras utilizamos [`Connection.setAutoCommit(false)`](https://docs.oracle.com/en/java/javase/25/docs/api/java.sql/java/sql/Connection.html#setAutoCommit(boolean)).

La conexión debe ser la misma en todos los repositorios que participan. El servicio la abre, inicia la transacción, entrega la conexión a los métodos de persistencia y decide cuándo confirmar. Los repositorios cierran sus sentencias y resultados; **no cierran una conexión que les ha prestado el servicio**.

```text
Servicio abre conexión
   ├─ inicia transacción
   ├─ consulta el producto              ← misma conexión
   ├─ descuenta stock                  ← misma conexión
   ├─ inserta venta                    ← misma conexión
   └─ commit si todo termina bien
      rollback si falla algo antes de confirmar
Servicio cierra conexión
```

Abrir una conexión interna diferente en `registrarVenta` haría que esa inserción quedara fuera de la transacción anterior. Es un defecto que la compilación no detecta.

## Descontar sin perder una condición de negocio

Leer stock, comprobarlo en Java y escribir un valor absoluto puede producir una carrera: otra operación podría modificarlo entre lectura y escritura. Una actualización condicionada mantiene el límite en la propia escritura:

```sql
UPDATE producto
SET stock = stock - ?
WHERE id = ? AND stock >= ?
```

Los parámetros primero y tercero reciben la misma cantidad positiva. Una fila afectada indica que se realizó el descuento. Cero indica que la condición no se cumplió: el producto puede no existir o no disponer de stock suficiente. La consulta previa sirve para ofrecer contexto y calcular el importe; la actualización condicionada evita confiar exclusivamente en una lectura anterior.

Una transacción no elimina todas las decisiones de concurrencia. Pueden aparecer bloqueos o conflictos; la aplicación debe comunicarlos y nunca anunciar éxito antes del `commit`.

## Confirmar y revertir conservando las causas

Este esquema ilustra una operación cuyo método declara `throws SQLException`; `ejecutarVenta` representa las llamadas a repositorios y las reglas que ya conocemos. El bloque interior protege la operación completa:

```java
try (Connection conexion = database.abrir()) {
    conexion.setAutoCommit(false);
    try {
        ejecutarVenta(conexion, productoId, cantidad);
        conexion.commit();
    } catch (SQLException | RuntimeException fallo) {
        try {
            conexion.rollback();
        } catch (SQLException falloRollback) {
            fallo.addSuppressed(falloRollback);
        }
        throw fallo;
    }
}
```

Si una consulta o regla falla, se intenta el rollback y se propaga el fallo original. Si además falla la reversión, `addSuppressed` conserva ese segundo problema sin sustituir la causa principal. En la implementación final, el límite de servicio traduce los fallos SQL a una excepción de persistencia con causa, como se explicó en [errores](repositorios.md).

No se necesita restaurar `autoCommit` en este ejemplo porque la conexión pertenece a la operación y se cierra al salir. Si se reutiliza una conexión prestada o un pool, hay que conocer su contrato de limpieza. Activar `autoCommit` mientras una transacción sigue abierta puede confirmar trabajo pendiente; no debe usarse como sustituto de `commit` o `rollback`.

Un rollback solo deshace cambios **aún no confirmados** de esa conexión. No revierte un correo enviado, un archivo escrito o una venta confirmada anteriormente. Una cancelación posterior es otro caso de uso, con reglas y operaciones compensatorias.

## La venta del proyecto

En `AlmacenService.vender`, la cantidad debe ser positiva. Se recupera el producto, se calcula el importe en céntimos, se descuenta stock de forma condicionada y se registra la venta. Únicamente después se confirma y se informa a la UI.

`JdbcProductoRepository.descontar` se ocupa del SQL del stock; `JdbcVentaRepository.registrar` escribe el historial. Ambos reciben la conexión del caso de uso. El servicio coordina, sin hacer que un repositorio invoque al otro.

La interfaz ya está preparada: el alumno trabaja en estas capas de backend. Una vez terminada una etapa, debe poder comprobar el resultado desde los formularios y el listado, además de ejecutar las pruebas.

## Probar realmente el rollback

Comprobar solo que se lanzó una excepción no demuestra atomicidad. Es necesario consultar el estado posterior usando una conexión nueva.

1. Anotar stock e historial de un producto conocido.
2. Ejecutar una venta válida. El stock debe disminuir y el historial crecer en una entrada con el importe correcto.
3. Intentar vender más unidades de las disponibles. Ambos estados deben permanecer iguales.
4. Ejecutar la fase `5` de `AprendizajeGuiadoTest`, ya incluida en el starter. `falloSegundaEscrituraRevierteDescuentoYConservaCausa` sustituye únicamente el registro de la venta por una avería preparada; tu descuento real ya se habrá ejecutado. No tienes que construir esa sustitución ni cambiar tu base habitual.
5. Consultar de nuevo con otra conexión. El descuento de la operación fallida debe haberse revertido.

La cuarta comprobación distingue una transacción efectiva de una simple validación previa. La prueba debe provocar el fallo técnico en un entorno temporal, sin alterar la base que usa la aplicación ni dejar mecanismos de fallo en el frontend.

También conviene vender exactamente el stock disponible: el resultado válido es cero existencias. Intentar otra venta debe rechazarse. Una cantidad cero, negativa o un identificador inexistente tampoco debe crear entradas de historial.

## Particularidades de SQLite

SQLite limita las escrituras concurrentes y puede devolver un error de base ocupada. Mantener transacciones cortas ayuda: no hay que esperar a que el usuario confirme un diálogo con una transacción abierta. Sus [garantías de aislamiento](https://sqlite.org/isolation.html) dependen también de cómo se configura el diario y del uso de conexiones.

Las claves foráneas deben habilitarse por conexión con `PRAGMA foreign_keys = ON` antes de iniciar la transacción. Puede verificarse su estado mediante `PRAGMA foreign_keys`. Esta configuración es específica de SQLite; no se copia sin cambios a MySQL. Consulta la [referencia oficial de la opción](https://www.sqlite.org/pragma.html#pragma_foreign_keys).

En [Ampliación](ampliacion.md) profundizaremos en aislamiento, lotes y rendimiento sin añadirlos como requisitos ocultos del proyecto ordinario.

## Etapas del proyecto: completar una venta

El **TODO 4** está en `JdbcProductoRepository.descontar` y `JdbcVentaRepository.registrar`. El primero devuelve si consiguió descontar la cantidad y el segundo inserta el historial; ninguno confirma ni cierra la conexión prestada.

El **TODO 5** está en `AlmacenService.vender`: valida la cantidad, abre una conexión, desactiva `autoCommit`, consulta el producto, calcula el total, llama a ambos repositorios y resuelve la transacción. Los fallos de negocio también deben recorrer la reversión cuando ya hay una transacción iniciada.

Con una base de prueba recién inicializada, vender dos teclados debe dejar ocho unidades y registrar un total de 51,80 €. En otra comprobación independiente que comience con diez teclados, intentar vender once debe dejar diez y ningún registro nuevo. No encadenes ambas expectativas sobre el mismo estado sin tener en cuenta la primera venta. El starter y la solución incluyen esa prueba optativa: consulta su activación por fase en [el guiado](proyecto.md#pruebas-preparadas-por-fase).
