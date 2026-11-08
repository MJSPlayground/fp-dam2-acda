# Proyecto guiado: reservas de espacios

El centro necesita una agenda para sus salas. El proyecto comienza con una interfaz web terminada y una API de apoyo; examinarás el modelo preparado y completarás, por separado, las conversiones, el acceso a documentos, los casos de uso y las operaciones REST. Trabajarás siempre sobre la misma aplicación.

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

Empieza por **00 · Primer contacto HTTP (antes de los TODO)**. Ejecuta «Ver espacios preparados»: obtendrás `200` y las capacidades 12 y 30. «Leer un error de parámetros» responde `400` al mínimo cero; «Leer un error de JSON recibido» responde `400` ante un cuerpo incompleto. Estas peticiones permiten reconocer petición y respuesta sin haber implementado persistencia.

Al terminar el repositorio ejecuta **01 · Lectura tras completar el repositorio**: el `GET` preparado devuelve un array, aunque esté vacío. Los `POST`, `PUT` y `DELETE` continúan pendientes hasta sus subpasos REST. En esa etapa ejecuta primero cada petición individual; utiliza el Runner de la carpeta CRUD completa cuando estén resueltas las tres escrituras. La creación captura el ID que utilizarán lectura, edición y limpieza; no inventes otro identificador.

## Cómo comprobar una fase sin esperar al proyecto completo

En `src/test/java/es/mjsplayground/acda/firestore/reservas/FasesAprendizajeTest.java` se entregan comprobaciones optativas. En IntelliJ abre la clase, crea su configuración JUnit con Run y añade **VM options** `-Daprendizaje.fase=conversion`. Selecciona después la misma configuración para repetirla. Cambia únicamente el valor de fase según la tabla:

| Fase | Qué puedes comprobar ya | Dependencia |
| --- | --- | --- |
| `conversion` | `datos` y `mapear`: campos, listas, mapas, ID y tipos incompatibles | Sin servidor ni futuros TODO |
| `alta-lectura` | Crear, recuperar y distinguir ausencia; limpieza preparada sin llamar al borrado pendiente | Emulador abierto; conversión resuelta |
| `repositorio` | Crear, recuperar, actualizar y borrar usando el SDK real | Emulador abierto; alta/lectura resueltas |
| `consultas` | Igualdad, rango y resultados sin coincidencias | Emulador y CRUD de repositorio |
| `servicio` | Validación antes de escribir y ausencia al actualizar | Sin emulador ni controlador implementado |
| `rest` | `201` + `Location`, `200`, `204` y error técnico seguro | Sin emulador; los tres métodos del controlador |

Las pruebas sin servidor utilizan dobles **ya preparados en el test** para aislar esa capa; no tienes que programarlos ni sustituir Firestore en la aplicación. Las de repositorio utilizan un proyecto `demo-fase-...` exclusivo y limpian sus documentos. No apuntan a cloud ni borran las reservas de la interfaz. Una prueba roja de la fase elegida indica un requisito de esa fase pendiente; las fases futuras se omiten. Sin la opción, estas comprobaciones quedan desactivadas y no impiden abrir el starter.

No actives `todas` hasta terminar el recorrido: ejecutaría también los pasos que todavía no has hecho. Un test aislado verde permite pasar al siguiente subpaso; la comprobación final combina emulador, API y frontend.

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

El archivo es `src/main/java/es/mjsplayground/acda/firestore/reservas/model/repository/FirestoreReservaRepository.java`. Trabaja por métodos; `esperar` y `conId` están preparados. Lee [persistencia](persistencia.md) antes de empezar.

### 2a. Traducir sin consultar la red

Completa `datos(Reserva)` y `mapear(DocumentSnapshot)`. El primero produce campos persistidos; el segundo recupera la identidad del snapshot y construye la reserva. Empieza por texto y números; añade después lista y mapa. Este fragmento solo inicia el mapa, no resuelve el método:

```java
Map<String, Object> campos = new HashMap<>();
campos.put("espacio", r.espacio());
// Añade los demás campos del contrato, sin duplicar el ID externo.
```

Al leer el inicio, separa presencia y conversión; este es un fragmento interior de `mapear`:

```java
Object valor = d.get("inicio");
if (!(valor instanceof Long numero)) {
    throw new IllegalArgumentException("inicio debe ser entero");
}
int inicio = Math.toIntExact(numero);
// Continúa con el resto de campos y construye la reserva con d.getId().
```

`get` permite comprobar el tipo original: `getLong` podría convertir un decimal y perder su fracción. `instanceof` rechaza ese caso; `toIntExact` comprueba el rango. El tratamiento del método conserva la causa de un documento incompatible. `mapear` debe conservar los datos y rechazar números fuera del rango `int`, elementos de lista que no sean texto o valores de mapa incompatibles. Conserva la causa del fallo en `PersistenciaException`. Ejecuta la fase `conversion`: compara una reserva completa y comprueba también los documentos incompatibles. No necesitas completar aún altas, servicios o HTTP.

??? tip "Si el mapa o la lista pierden datos"

    Compara primero nombres de campos y tipos. `List<?>` permite inspeccionar cada elemento antes de construir `List<String>`; un cast de toda la lista no comprueba sus elementos. El ID se obtiene con `getId()`, no buscando un campo `id` inventado.

### 2b. Crear y recuperar

Completa `crear` y `obtener`. En el alta distingue referencia, escritura, espera e identidad generada; al leer distingue un documento ausente de una operación fallida. La secuencia del SDK está explicada en [referencia y resultado](persistencia.md#referencia-lectura-y-resultado).

En el interior de `obtener`, `id` viene del parámetro y `reservas` es la colección inyectada. La llamada devuelve primero una operación y después su resultado:

```java
ApiFuture<DocumentSnapshot> lectura = reservas.document(id).get();
DocumentSnapshot documento = esperar(lectura);
// Falta distinguir exists() y convertir el documento con mapear.
```

Ejecuta `alta-lectura` y coloca un breakpoint tras `esperar`: ya puedes inspeccionar el resultado aunque no hayas implementado edición, borrado, servicio o HTTP. La limpieza del test utiliza el SDK preparado y no depende del TODO de `eliminar`. Continúa cuando recupere la misma reserva y distinga un ID ausente. El controlador de alta todavía responderá `501`: aún no es la herramienta para comprobar este método.

### 2c. Editar y eliminar

Completa `actualizar` y `eliminar`. La edición mantiene el ID; el borrado espera el resultado del SDK. La decisión «este ID no existe» pertenece al servicio, que ya dispone de `obtener`.

Ejecuta `repositorio`: guarda una reserva, compara todos sus campos después de leerla, cambia responsable/asistentes y verifica la ausencia tras borrar. Este test llama directamente al repositorio; no depende de los TODO de servicio ni de REST. Si falla al guardar, inspecciona primero `datos`; si guarda pero recupera mal, inspecciona `mapear`.

### 2d. Añadir filtros de uno en uno

Completa `listar` siguiendo [consultas](consultas.md): listado sin filtros, igualdad de sala, igualdad de fecha, mínimo de asistentes y orden estable. Construye primero la consulta; ejecutarla es una operación diferente:

```java
Query consulta = reservas;
if (espacio != null && !espacio.isBlank()) {
    consulta = consulta.whereEqualTo("espacio", espacio);
}
QuerySnapshot resultado = esperar(consulta.get());
// Añade los demás filtros y convierte cada documento de resultado.getDocuments().
```

El ejemplo solo implementa la condición de sala. El parámetro ausente no debe filtrar por el texto literal `null`. El mínimo admite 1–30. Ejecuta `consultas` y después la petición Postman de la carpeta **01**. Debe aparecer un array, no un éxito vacío utilizado para ocultar una excepción.

Continúa cuando el CRUD del repositorio y sus consultas funcionen. Todavía no tiene sentido ejecutar el Runner CRUD de la API: faltan las capas siguientes.

## Etapa 3. Reglas del caso de uso

Localiza `model/service/ReservaService.java`. En **3a**, completa `crear`: utiliza `validar` antes de llamar a `repository.crear`. En **3b**, completa `actualizar`: comprueba existencia y conserva estado y motivo. El ayudante `validar` ya contiene las comprobaciones de campos; léelo y recórrelo con el depurador, no lo dupliques en el controlador. Las validaciones del DTO ayudan a detectar formato; el servicio debe volver a comprobar las invariantes para llamadas que no procedan de HTTP.

En SALA-A, una reserva de ocho asistentes puede aceptarse; una de trece debe rechazarse. Prueba una fecha imposible, un intervalo invertido y un responsable vacío. Ninguno de esos intentos debe producir una reserva válida por accidente.

Ejecuta la fase `servicio`: una entrada de trece asistentes para SALA-A debe rechazarse **sin invocar persistencia**, y actualizar un ID ausente produce `404`. Se puede comprobar esta capa aunque el controlador aún responda `501`.

El guiado no resuelve todavía la ocupación entre reservas ni las cancelaciones: esas necesidades nuevas forman la práctica final. La solución del CRUD no se presenta como un sistema completo de disponibilidad.

## Etapa 4. Operaciones REST

Lee [HTTP y respuestas REST](rest.md) y abre `api/ReservaController.java`. Completa **4a**, el `POST`, y ejecuta solo la petición de creación de Postman. Usa el ID devuelto en el `GET` preparado. Después completa **4b**, el `PUT`, compara el resultado con otro `GET` y, finalmente, **4c**, el `DELETE`. Ahora puedes ejecutar `rest` y el Runner CRUD completo. El controlador recibe DTO, invoca al servicio y devuelve el estado correcto; no contiene reglas sobre capacidad ni llamadas al SDK. Si el alta persiste pero el cliente no recibe `201` o `Location`, revisa esta capa, no reescribas el repositorio.

Cuando ya tengas `Reserva creada` devuelta por el servicio, esta expresión construye estado, ubicación y representación:

```java
ResponseEntity.created(URI.create("/api/reservas/" + creada.id()))
    .body(creada);
```

El método debe devolver esa respuesta; no vuelvas a escribir en Firestore desde el controlador. Para entender cada parte utiliza [la construcción de respuestas](rest.md#construir-la-respuesta-de-una-escritura).

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

Utiliza [la guía de diagnóstico, logging y pruebas](../ayuda/diagnostico.md) para seguir una petición desde el controlador al repositorio. El test de fase `rest` incluye un fallo técnico preparado y comprueba `500` sin exponer su causa en JSON; no tienes que romper el emulador ni programar otra infraestructura para reproducirlo. Las pruebas deben distinguir error de negocio, entrada mal formada, ausencia y fallo técnico. Un test de compilación no comprueba la persistencia real.

## Solución completa

[Descargar solución del guiado](/fp-dam2-acda/descargas/codigo/reservas-guiado-solucion.zip){ .md-button download="reservas-guiado-solucion.zip" }

[Ver código, README y pruebas](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/firestore/guiado-solucion)

La solución utiliza el mismo arranque, frontend y colección Postman. Contrasta el flujo de una escritura, el mapeo de documentos y la conservación de las causas técnicas antes de comenzar [la práctica final](practica-final.md).
