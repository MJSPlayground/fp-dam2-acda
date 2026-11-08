# Sesiones, estados, CRUD y transacciones

El mapeo describe cómo relacionar entidades y tablas. Para realizar una operación necesitamos un contexto que gestione esas entidades y una transacción que delimite sus cambios.

## SessionFactory y Session

`SessionFactory` representa la configuración y los metadatos del proveedor. Construirla tiene un coste: la aplicación prepara una instancia y la reutiliza hasta cerrar. A partir de ella abre sesiones para los casos de uso.

Una [`Session`](https://docs.hibernate.org/orm/7.4/javadocs/org/hibernate/Session.html) gestiona entidades durante una operación. No es una ventana de JavaFX ni una tabla de la base. Se abre al comenzar el trabajo de persistencia y se cierra después; no se comparte arbitrariamente entre hilos ni se mantiene abierta mientras el usuario piensa en un formulario.

La [API de SessionFactory](https://docs.hibernate.org/orm/7.4/javadocs/org/hibernate/SessionFactory.html) distingue ambos ciclos. Cerrar una sesión no apaga MySQL ni impide abrir la siguiente.

## Estados de una entidad

| Estado | Ejemplo |
| --- | --- |
| Transitoria | `new Editorial("Norte")`, todavía sin persistir |
| Gestionada o persistente | Entidad asociada a una sesión abierta |
| Separada o detached | Entidad que estuvo gestionada y conserva datos fuera de aquella sesión |
| Marcada para eliminación | Entidad para la que se solicitó `remove` dentro de la operación |

Una entidad recuperada con `find` está gestionada por esa sesión. Al cerrarla, su información ya cargada sigue existiendo en Java, pero algunas asociaciones pendientes no podrán obtenerse desde ese contexto cerrado.

Una sesión identifica cada entidad por su tipo y clave. Recuperar la misma identidad varias veces en ese contexto no debería producir varias copias gestionadas independientes de la misma fila.

## Crear y consultar

Este fragmento ilustrativo supone `factory` configurada y una transacción resuelta por el caso de uso. `persist` registra una entidad nueva; `find` busca por su identidad:

```java
Editorial nueva = new Editorial("Editorial Norte");
sesion.persist(nueva);
Editorial encontrada = sesion.find(Editorial.class, nueva.getId());
```

La identidad generada pertenece a la base y no tiene por qué formar una numeración sin huecos. Un intento revertido puede haber consumido un valor. La ausencia de una fila se expresa mediante `null` al utilizar `find`; el servicio decide cómo comunicar que no existe.

El repositorio no inventa un objeto vacío cuando falta una clave ni trata una excepción de conexión como ausencia. La configuración del catálogo y los datos preparados permiten observar estas diferencias sin escribir instrucciones de conexión manualmente.

## Modificar y borrar

Hibernate detecta cambios de una entidad gestionada. Dentro de una transacción, podemos recuperar una editorial y cambiar su nombre:

```java
Editorial editorial = sesion.find(Editorial.class, id);
if (editorial == null) throw new ReglaCatalogoException("La editorial no existe");
editorial.setNombre("Nombre revisado");
```

`ReglaCatalogoException` es un nombre ilustrativo para una excepción de negocio. El proyecto utiliza su tipo propio. No hay que llamar siempre a una operación de guardado adicional sobre una entidad ya gestionada: al sincronizar, Hibernate determina qué cambió.

`remove(entidad)` solicita eliminar una entidad gestionada. Borrar una editorial utilizada por libros debe rechazarse según las reglas del catálogo, en lugar de activar cascadas que destruyan todos esos libros. [Relaciones](relaciones.md) explica qué objetos tienen vida independiente.

## merge y entidades separadas

Fuera de la sesión, cambiar una entidad separada no implica que ese cambio ya esté guardado. `merge` copia su estado a una instancia gestionada y devuelve esta última:

```java
Editorial gestionada = sesion.merge(separada);
```

La referencia `separada` no se convierte mágicamente en la misma referencia gestionada. En este proyecto suele ser más claro recuperar la entidad por ID dentro del servicio y aplicar solo los campos editables recibidos de la UI.

No copies a ciegas todos los campos de una entidad antigua: podrías reemplazar relaciones o información que ya cambió. La presencia de `@Version` y el contrato de edición deben entenderse antes de prometer detección de cualquier formulario obsoleto.

## Una transacción delimita el caso de uso

Una transacción coordina cambios que deben confirmarse juntos. Su recorrido es comenzar, realizar lecturas y modificaciones, confirmar con `commit` y revertir con `rollback` si falla el trabajo previo a la confirmación.

Este patrón muestra la propiedad de sesión y transacción. El servicio es quien decide el límite:

```java
try (Session sesion = factory.openSession()) {
    Transaction transaccion = sesion.beginTransaction();
    try {
        realizarOperacion(sesion);
        transaccion.commit();
    } catch (RuntimeException fallo) {
        try {
            if (transaccion.isActive()) transaccion.rollback();
        } catch (RuntimeException falloRollback) {
            fallo.addSuppressed(falloRollback);
        }
        throw fallo;
    }
}
```

`realizarOperacion` representa las llamadas del caso de uso; no es una nueva clase exigida al alumno. Si la reversión también falla, se conserva ese segundo problema sin sustituir la excepción principal. Después de un fallo no se continúa utilizando la sesión como si mantuviera un estado fiable.

Los repositorios reciben la sesión del caso de uso: no abren otra transacción independiente para cada modificación coordinada. La UI confirma antes de llamar al servicio; no se mantiene una transacción abierta esperando un diálogo.

## Flush y commit no son lo mismo

`flush` sincroniza cambios pendientes con la base. Puede hacer aparecer en ese momento una restricción o error SQL, pero no equivale a confirmar la transacción. Si el caso de uso falla después, las escrituras aún no confirmadas deben revertirse.

El `commit` puede provocar sincronización y también fallar. Por eso el log de éxito se emite después de confirmar, no después de llamar a `persist` ni de modificar un setter.

Una transacción de base no revierte automáticamente un archivo exportado ni un mensaje enviado por otra herramienta. En este tema coordinamos operaciones relacionales del mismo catálogo; no añadimos un sistema de transacciones entre servicios externos.

## Responsabilidades y errores

El servicio valida datos, recupera relaciones y coordina el trabajo. El repositorio expresa persistencia y consultas. La excepción propia añade contexto y conserva la causa de un fallo técnico; el logging SLF4J/Log4j2 mantiene la información de diagnóstico, como en [Ficheros](../ficheros/arquitectura.md).

Una edición rechazada debe conservar los datos anteriores. Un ISBN duplicado se distingue de una base inaccesible. La interfaz presenta el mensaje y mantiene los campos para corregirlos, sin mostrar éxito por el mero hecho de que se abrió una sesión.

## Etapa y comprobaciones

Completa los TODO del repositorio y del servicio previstos en [la guía](proyecto.md). Comprueba altas, consultas, modificaciones y borrados permitidos. Intenta también modificar una clave inexistente y violar una restricción.

Para comprobar rollback, observa el estado de la base con una sesión nueva después del fallo. Que una excepción se haya lanzado no demuestra por sí solo que se revirtieran todas las modificaciones. Continúa con [relaciones y ciclo de vida](relaciones.md).

## Aplicar la transacción preparada sin escribir otra infraestructura

En `model/repository/LibroRepository.java`, `buscar` recibe la sesión y puede utilizar `s.find(Libro.class, id)`; `insertar` recibe una entidad nueva y utiliza `s.persist(libro)`; `eliminar` recibe una entidad recuperada en esa misma sesión y utiliza `s.remove(libro)`. Completa y contrasta esas operaciones antes de combinar alta y relaciones en `BibliotecaService.guardarLibro`.

El método preparado `transaccion(Function<Session, T>)` recibe una tarea: la lambda `s -> { ...; return resultado; }` contiene el trabajo del caso de uso y recibe la sesión abierta. El tipo `T` es el resultado que devuelve esa tarea, por ejemplo el ID creado; no debes añadir un `commit` a cada repositorio. El helper confirma si termina y revierte si falla, preservando causas.

La fase `rollback` de [las pruebas del guiado](proyecto.md#pruebas-preparadas-para-avanzar) ya inyecta un error después de una inserción sincronizada con `flush`. Observa el conteo dentro de la transacción y compáralo con el de una sesión nueva posterior al rechazo. Este contraste explica por qué `flush` no equivale a `commit`.
