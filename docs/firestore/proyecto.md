# Proyecto guiado: reservas de espacios

El centro necesita una agenda para sus salas. El proyecto comienza con una interfaz web terminada y una API de apoyo; completarás el modelo, el acceso a documentos, las reglas y las operaciones REST. Trabajarás siempre sobre la misma aplicación.

## Preparar el proyecto

[Descargar proyecto inicial](/fp-dam2-acda/descargas/codigo/reservas-guiado-inicial.zip){ .md-button .md-button--primary download="reservas-guiado-inicial.zip" }

Abre la carpeta que contiene `pom.xml` como proyecto Maven en IntelliJ y espera su sincronización. Selecciona el JDK siguiendo [la preparación del entorno](../ayuda/proyectos-maven.md). La descarga contiene frontend, configuración, Maven Wrapper y colección Postman; no debes construir manualmente las peticiones ni editar HTML, CSS o JavaScript.

## Arranque desde IntelliJ

1. Ejecuta `es.mjsplayground.acda.firestore.reservas.config.Emulador.main`. La primera vez puede descargar el emulador oficial y verificarlo. Espera a que esté disponible en `127.0.0.1:8085` y mantén abierto ese proceso.
2. Ejecuta `es.mjsplayground.acda.firestore.reservas.ReservasApplication.main` en otra configuración Run.
3. Abre `http://localhost:8080`. Esa es la aplicación, no la dirección del emulador.

Al cambiar entre proyecto inicial y solución, detén la aplicación anterior para liberar el puerto 8080. Puedes reutilizar el mismo emulador: no necesitas abrir uno por proyecto. Los campos de equipo y detalles de la interfaz utilizan JSON: `[]` y `{}` representan valores vacíos; `["Proyector"]` y `{"actividad":"Taller"}` son ejemplos con contenido. La interfaz avisa si el JSON no tiene el formato requerido y conserva el formulario para corregirlo.

No hacen falta cuentas de servicio ni un proyecto cloud para el modo local. Se utiliza el proyecto demo `demo-acda-reservas`. No actives otro modo para intentar corregir un TODO o una falta de arranque: consulta [configuración y diagnóstico](configuracion.md).

El catálogo preparado contiene `SALA-A` con capacidad de 12 personas y `SALA-B` con capacidad de 30. Los datos de reservas se crean durante las comprobaciones; un catálogo de salas visible no demuestra que la persistencia de reservas ya esté resuelta. El emulador local mantiene datos mientras sigue en marcha; no se presupone que reiniciarlo los conserve sin un mecanismo de exportación.

Al principio algunas peticiones responden `501` con un mensaje de etapa pendiente. Es una señal del starter, no un guardado simulado ni una avería de red. La solución debe sustituir esos estados por comportamiento real.

## Importar la colección Postman

En Postman selecciona **Import** y abre `postman/reservas.postman_collection.json`, incluido en la descarga. Comprueba `baseUrl = http://localhost:8080`. Las peticiones ya contienen métodos, rutas, cabeceras, cuerpos y comprobaciones; modifica únicamente los valores previstos para los escenarios.

Ejecuta la carpeta del guiado en el orden indicado en el README: las peticiones de creación recuperan IDs para las siguientes consultas, ediciones y borrados. Antes de repetir pruebas sobre una reserva, observa si la petición de limpieza ya la eliminó. No escribas un ID arbitrario en lugar del generado esperando encontrar el mismo recurso.

La colección diferencia éxitos, entradas inválidas y recursos inexistentes. Si ejecutas todos los casos antes de completar los TODO, los fallos previstos del starter no significan que los requisitos finales estén implementados.

## Responsabilidades y datos

```text
Navegador / Postman
        ↓
ReservaController: HTTP, DTO y respuestas
        ↓
ReservaService: catálogo, validaciones y casos de uso
        ↓
FirestoreReservaRepository: consultas y mapeo
        ↓
FirestoreConfig → cliente Firestore → emulador
```

`ReservaRequest` es la entrada pública. `Reserva` incluye identidad, estado y motivo de cancelación. El guiado crea reservas activas; el cliente no decide el ID ni fuerza un estado interno a través de campos adicionales. La configuración rechaza propiedades JSON desconocidas.

La colección `reservas` guarda una reserva por documento. `equipo` es una lista de textos y `detalles` un mapa de textos. No se necesita una colección independiente para cada elemento pequeño y acotado que pertenece a la reserva.

## Etapa 1. Modelo y validaciones de entrada

Lee [documentos y relaciones](documentos.md). Localiza `Reserva`, `Espacio` y `ReservaRequest`: el modelo y el DTO se entregan preparados para que la interfaz pueda arrancar. Esta primera etapa es de comprensión, sin un TODO de modelo. Distingue campos recibidos de los que controla el servidor.

Las condiciones del proyecto son:

- Espacio conocido: SALA-A o SALA-B.
- Fecha válida con formato ISO `AAAA-MM-DD`, sin días imposibles.
- Inicio y fin en horas enteras entre 08:00 y 20:00, con inicio anterior al fin.
- Responsable de 1 a 60 caracteres, sin contar espacios exteriores.
- Asistentes positivos y no superiores a la capacidad de la sala.
- Equipo: lista no nula, como máximo diez textos de 1 a 40 caracteres.
- Detalles: mapa no nulo, como máximo diez entradas; claves de 1 a 30 caracteres y valores de 1 a 100.

Listas y mapas vacíos son válidos cuando no se necesita equipo o información adicional. Un texto obligatorio compuesto solo por espacios no cuenta como contenido.

## Etapa 2. Persistir y recuperar documentos

Lee [persistencia](persistencia.md) y completa el **TODO 2** de `FirestoreReservaRepository`: creación, lectura individual, listado, actualización y borrado, junto con los ayudantes `datos` y `mapear`. El ayudante `conId` ya está preparado. Mantén los nombres de campos y transforma números, listas y mapas de forma coherente con el modelo; un documento incompatible debe producir un error técnico con su causa.

No anuncies un guardado antes de conocer el resultado de la escritura. Una ausencia debe poder convertirse en `404`; un fallo del emulador debe conservar su causa técnica y no convertirse en un documento vacío.

Aplica después [los filtros de consulta](consultas.md) a `listar`: sala, fecha y mínimo de asistentes. Este último admite entre 1 y 30. Compara un listado sin filtros con uno combinado; el resultado sin coincidencias debe ser una lista vacía.

## Etapa 3. Reglas del caso de uso

Completa el **TODO 3** de `ReservaService` para crear y actualizar. Las validaciones del DTO ayudan a detectar formato; el servicio debe volver a comprobar las invariantes para llamadas que no procedan de HTTP.

En SALA-A, una reserva de ocho asistentes puede aceptarse; una de trece debe rechazarse. Prueba una fecha imposible, un intervalo invertido y un responsable vacío. Ninguno de esos intentos debe producir una reserva válida por accidente.

El guiado no resuelve todavía la ocupación entre reservas ni las cancelaciones: esas necesidades nuevas forman la práctica final. La solución del CRUD no se presenta como un sistema completo de disponibilidad.

## Etapa 4. Operaciones REST

Lee [HTTP y respuestas REST](rest.md) y completa el **TODO 4** de `ReservaController`: `POST /api/reservas`, `PUT /api/reservas/{id}` y `DELETE /api/reservas/{id}`. El controlador recibe DTO, invoca al servicio y devuelve el estado correcto; no contiene reglas sobre capacidad ni SQL.

Una creación correcta responde `201`, devuelve la reserva con ID y señala su dirección con `Location`. Una edición válida responde `200`. El borrado responde `204` sin cuerpo. Un ID inexistente devuelve `404`; una entrada inválida, `400`.

El manejador común `ApiErrors` devuelve `estado`, `mensaje`, `ruta` e `instante`. Comprueba tanto el estado HTTP como el contenido. El JSON del error no debe contener stack traces ni credenciales.

## Comprobación completa

Crea una reserva de SALA-A para `2027-02-15`, de 10 a 12, con ocho asistentes, un equipo y un detalle. Conserva el ID de respuesta y realiza esta secuencia:

1. Recuperarla por ID y comparar todos los campos, incluida lista y mapa.
2. Encontrarla con los filtros de espacio, fecha y mínimo de asistentes.
3. Cambiar responsable u horario válido y volver a consultar.
4. Intentar una edición inválida y comprobar que los datos anteriores permanecen.
5. Borrarla y verificar el `404` posterior.

Repite los flujos desde la interfaz preparada. Reiniciar solo Spring, manteniendo el emulador activo, no debe borrar los documentos guardados. Detener el emulador es otro cambio de entorno y debe interpretarse según su ciclo de vida.

Utiliza [la guía de diagnóstico, logging y pruebas](../ayuda/diagnostico.md) para seguir una petición desde el controlador al repositorio. Las pruebas deben distinguir error de negocio, entrada mal formada, ausencia y fallo técnico. Un test de compilación no comprueba la persistencia real.

## Solución completa

[Descargar solución del guiado](/fp-dam2-acda/descargas/codigo/reservas-guiado-solucion.zip){ .md-button download="reservas-guiado-solucion.zip" }

[Ver código, README y pruebas](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/firestore/guiado-solucion)

La solución utiliza el mismo arranque, frontend y colección Postman. Contrasta el flujo de una escritura, el mapeo de documentos y la conservación de las causas técnicas antes de comenzar [la práctica final](practica-final.md).
