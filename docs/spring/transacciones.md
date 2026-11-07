# Transacciones y conflictos

Guardar dos cambios relacionados requiere decidir qué debe ocurrir si el segundo falla. Si un caso de uso actualiza existencias y registra una operación, no puede dejar solo la mitad confirmada. Una **transacción** agrupa los cambios que deben confirmarse o deshacerse juntos. La base de datos aporta esa garantía; Spring coordina su inicio y su final alrededor del servicio.

## El límite es el caso de uso

`@Transactional` pertenece normalmente al método de servicio que realiza la operación completa. En este ejemplo reducido, el repositorio participa en la operación del servicio:

```java
@Service
class OperacionCatalogo {
    private final LibroRepository libros;

    OperacionCatalogo(LibroRepository libros) {
        this.libros = libros;
    }

    @Transactional
    public void cambiarTitulo(Long id, String nuevoTitulo) {
        Libro libro = libros.findById(id)
            .orElseThrow(() -> new ReglaNegocioException("Libro inexistente"));
        if (nuevoTitulo == null || nuevoTitulo.isBlank()) {
            throw new ReglaNegocioException("El título es obligatorio");
        }
        libro.setTitulo(nuevoTitulo.strip());
    }
}
```

El ejemplo supone una excepción propia no comprobada y una entidad con `setTitulo`. El libro cargado queda gestionado dentro del contexto de persistencia. Hibernate detecta el cambio y lo sincroniza al terminar; no necesitamos llamar a `save` después de cada setter de una entidad gestionada. El cambio solo queda confirmado si la transacción termina correctamente.

No copies este método como sustitución del servicio real: el proyecto añade límites, versiones y otras validaciones. La finalidad del fragmento es identificar dónde empieza y termina una operación. Consulta [`@Transactional`](https://docs.spring.io/spring-framework/reference/6.2/data-access/transaction/declarative/annotations.html) y [transacciones de Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/3.5/jpa/transactions.html).

## Qué hace el proxy

Spring coloca un intermediario o **proxy** delante del bean transaccional. Cuando otro componente llama a un método a través de ese proxy, este abre o se incorpora a una transacción, ejecuta el método y confirma o revierte al salir.

Esto explica dos errores frecuentes:

- Si construyes el servicio con `new`, no recibes automáticamente el proxy transaccional de Spring.
- Si un método de la clase llama a otro método de la misma instancia, esa llamada interna no pasa por el proxy. Poner `@Transactional` únicamente en el segundo método no crea por sí solo un nuevo límite.

Para evitar sorpresas, sitúa la transacción en el caso de uso público que invoca otro bean. No fragmentes una operación en varias transacciones independientes para «hacer funcionar» las anotaciones. Las llamadas a otros servicios transaccionales con la propagación predeterminada `REQUIRED` participan normalmente en la transacción ya existente.

## Cuándo se hace rollback

De forma predeterminada, Spring revierte al salir una `RuntimeException` o un `Error`; una excepción comprobada no implica rollback por defecto. Una excepción de dominio que hereda de `RuntimeException` puede expresar un rechazo que debe deshacer el caso de uso.

Si una operación necesita revertir ante una excepción comprobada, se puede declarar explícitamente `rollbackFor`. No copies esa opción sin comprobar qué excepción puede salir y por qué. Tampoco captures un fallo para devolver un resultado de éxito: ocultarlo cambia lo que el coordinador transaccional puede observar.

Una transacción puede quedar marcada para rollback aunque se capture posteriormente el error de un colaborador. El commit puede terminar con `UnexpectedRollbackException`. Capturar no equivale a reparar la operación.

`flush` envía los cambios pendientes a MySQL y puede descubrir una restricción incumplida. **No es un commit.** Si después se produce un fallo, una transacción correcta todavía revierte lo enviado. Algunas restricciones pueden descubrirse al finalizar el método, por lo que el controlador preparado también debe manejar fallos que salen del proxy.

## Lecturas y relaciones perezosas

`@Transactional(readOnly = true)` comunica la intención de lectura y puede permitir optimizaciones. No es un mecanismo de permisos ni una barrera universal que prohíba cualquier SQL de escritura. Los permisos se definen en otra capa.

Con `open-in-view=false`, el servicio prepara los datos que mostrará la página mientras la persistencia está disponible. Acceder luego a `libro.getEditorial().getNombre()` sobre una relación no cargada puede producir un fallo de carga perezosa. La solución es diseñar la consulta y la salida del servicio; no abrir indiscriminadamente la sesión durante toda la interfaz.

## Dos personas modifican el mismo libro

`@Version` permite control optimista: Hibernate comprueba la versión al actualizar. Si otra transacción cambió la fila desde la lectura, la actualización encuentra un conflicto. No se deben ignorar esas excepciones ni repetir automáticamente una operación de negocio como si no hubiera ocurrido nada.

Además, un formulario puede haberse abierto hace varios minutos. Para detectar ese caso, el servicio necesita comparar la versión recibida del formulario con la actual; la anotación por sí sola solo cubre la ventana entre la lectura y la escritura del servidor. En el proyecto, conserva el dato de versión preparado por la interfaz y respeta las comprobaciones del servicio.

Una comprobación de disponibilidad seguida de varias escrituras sigue expuesta a concurrencia si las filas cambiaron después de comprobar. El control de versiones y la transacción se complementan: la versión detecta cambios incompatibles; el rollback evita que quede una parte del caso de uso confirmada.

En las operaciones nuevas de la práctica final, el repositorio también entrega preparada una consulta de **bloqueo de escritura**. A diferencia del control optimista, esta reserva el acceso de escritura a la fila mientras dura la transacción y puede hacer esperar a otra operación. El servicio utiliza esa consulta y comprueba la versión recibida; el alumnado no tiene que diseñar una nueva infraestructura de bloqueo. Véase [bloqueos en Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/3.5/jpa/locking.html). Mantener operaciones cortas y un orden estable de acceso reduce conflictos; una espera agotada o un interbloqueo sigue siendo un fallo que debe revertir el caso de uso, no un éxito parcial.

## Verificarlo sin confundir las pruebas

Comprueba un caso correcto y otro que falle **después de iniciar cambios**. Tras el fallo, consulta de nuevo desde otra operación y verifica que las filas mantienen sus valores anteriores. Comprobar solo que apareció un mensaje no demuestra un rollback.

Una prueba unitaria sin contexto no prueba el proxy. Una prueba que ejecuta todo dentro de una transacción propia y luego la revierte puede ocultar diferencias con la ejecución real. Las pruebas del proyecto deben entrar por los servicios administrados por Spring y utilizar su base aislada.

La [práctica final](practica-final.md) utiliza estas garantías para confirmar conjuntamente un pedido y sus cambios de stock. La notificación posterior al commit es un efecto informativo, explicado en [servicios](servicios.md#eventos-comunicar-un-hecho).
