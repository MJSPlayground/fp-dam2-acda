# Reservas de espacios — final-inicial

## Ejecutar desde IntelliJ

Abre esta carpeta como Maven, espera la sincronización y selecciona JDK25. Ejecuta `config.Emulador.main` y déjalo activo; luego ejecuta `ReservasApplication.main` (paquete es.mjsplayground.acda.firestore.reservas). Abre http://127.0.0.1:8080.

Emulador.main reutiliza la caché Firebase o descarga el JAR oficial1.22.0 (~137MB) por HTTPS, con timeout y SHA-256 publicado en firebase-tools15.35.0 lib/emulator/downloadableEmulatorInfo.json. No requiere cuenta, claves ni npm. El JAR no se distribuye en Git/ZIP. Si8085 ya está ocupado por ese emulador, reutilízalo; no abras otra instancia.

Por defecto se usa emulador127.0.0.1:8085 y proyecto demo-acda-reservas. Los datos se pierden al detenerlo. Puedes configurar FIRESTORE_EMULATOR_HOST para otro host local; el launcher usa8085 fijo. Spring escucha solo loopback.

## Interfaz y modelo

La interfaz está terminada: Nuevo, Guardar, Editar, Cancelar y Eliminar confirmado. No se pide modificar HTML/CSS/JS. El formulario se cierra y limpia al guardar; ante errores conserva los datos. Tema según sistema y teclado con foco visible.

Reserva tiene id, espacio, fecha ISO AAAA-MM-DD, inicio/fin (horas enteras), responsable, asistentes, equipo (lista), detalles (mapa), estado y motivoCancelacion. SALA-A admite12 asistentes, SALA-B30. Horario08–20 e inicio anterior al fin. Responsable1–60 caracteres; equipo máximo10 cadenas1–40; detalles máximo10 entradas, clave1–30 y valor1–100. No se aceptan horas fraccionarias. Fecha texto ISO no es Timestamp.

## API y Postman

GET /api/espacios; GET/POST /api/reservas; GET/PUT/DELETE /api/reservas/{id}. POST201+Location, PUT200, DELETE204. GET lista admite espacio, fecha y minAsistentes1–30. Esos filtros se ejecutan en Firestore; el rango ordena por asistentes y luego documentId.

Importa postman/reservas.postman_collection.json. Cambia baseUrl si usas otro puerto. Collection Runner ejecuta carpetas en orden, captura IDs, comprueba estado/datos/errores y limpia sus reservas. sufijo y fecha se generan si están vacíos. Para otra sesión cambia fecha a un día libre o vacía ambas variables. En el inicial, los TODO devuelven501 hasta completarse. No ejecutes pruebas sobre datos reales.

Errores JSON: {estado,mensaje,ruta,instante}.400 entrada,404 ausencia,409 conflicto final,500 fallo técnico (causa solo en log),501 TODO. Para observar500 real, deja Spring activo y detén el emulador: Actualizar acabará con error tras espera (guiado8s, final45s por reintentos). Vuelve a arrancarlo después. Fallo de red del navegador no es una respuesta500.

## Configuración cloud e índices

Cloud requiere opt-in FIRESTORE_MODO=cloud, GOOGLE_CLOUD_PROJECT real y GOOGLE_APPLICATION_CREDENTIALS apuntando a archivo externo. No subas claves ni las copies al frontend. Admin usa IAM; las reglas de clientes no protegen esta API. La aplicación local no tiene autenticación: no exponer a Internet. Para utilizar cloud, prepara primero proyecto, cuenta autorizada e IAM; comprueba creación, lectura e índices con ese entorno. La conexión al emulador no configura los permisos remotos.

Se entrega firestore.indexes.json. El emulador no exige todos los índices de producción. Ante FAILED_PRECONDITION, revisa el enlace de creación del índice en el log y Firebase Console. No se crean índices cloud automáticamente. Una consulta local no garantiza los índices ni permisos de cloud; repite allí las operaciones con la configuración remota autorizada.

## Validación técnica opcional

Desde esta carpeta: `.\mvnw.cmd clean test`; con emulador activo `.\mvnw.cmd -Dqa.emulador=true test`. Los tests de integración limpian sus propios datos. La persistencia usa Admin SDK real, sin mapa alternativo; las pruebas unitarias aíslan errores y reglas con dobles. `.\mvnw.cmd package` produce un JAR alternativo a IntelliJ.

Spring Boot3.5.7, Firebase Admin9.7.0, Java25 y SLF4J/Log4j2 están fijados en POM. Referencias: https://firebase.google.com/docs/admin/setup ; https://firebase.google.com/docs/emulator-suite/connect_firestore ; https://firebase.google.com/docs/firestore/query-data/queries ; https://docs.spring.io/spring-boot/3.5/system-requirements.html .

## Nuevos requisitos pendientes

El guiado sigue resuelto: listado sin filtros nuevos y CRUD. Interfaz nueva ya preparada. TODO de servicio: cancelar, disponibilidad, filtros nuevos, paginación y reglas de agenda. TODO de repositorio: persistir cancelación y consulta paginada. Las nuevas funciones permanecen pendientes; sus contratos están en el enunciado.

## Contrato final e infraestructura preparada

PATCH /api/reservas/{id}/cancelacion {motivo}: motivo1–120, conserva historial. Repetir devuelve200 sin cambiar el primer motivo. PUT cancelada409. GET lista añade estado y responsable (igualdad exacta).

GET /api/disponibilidad?espacio&fecha&inicio&fin devuelve {disponible:boolean}. Intervalos [inicio,fin): adyacentes compatibles; canceladas no ocupan. Crear y modificar comprueban conflictos. Máximo dos activas por responsable/día, ignorando mayúsculas (entrada strip).

GET /api/reservas/pagina?limite&cursor: limite1–20, cursor id validado; devuelve {elementos,siguienteCursor}, ordena documentId y startAfter. La vista paginada no aplica filtros del listado. El resultado informa si existe continuación, sin repetir el último elemento.

coordinar(candidata,id,regla) es infraestructura transaccional PREPARADA, no TODO: lee/actualiza marcador coordinacion/agenda y consulta día antes de escribir. Serializa escritores incluso cuando la agenda está vacía. El callback del servicio es puro: puede repetirse sin efectos externos. Las reglas permanecen en servicio. La API transaccional pertenece a Ampliación. Marcador global sencillo limita rendimiento: prioriza una coordinación sencilla; no está diseñado para alta carga. No mezcles app guiada y final contra el mismo proyecto: el guiado no usa el coordinador.

## Comprobaciones optativas por nivel

En IntelliJ ejecuta `NivelesAprendizajeTest`, en el paquete de tests es.mjsplayground.acda.firestore.reservas. Añade VM options `-Daprendizaje.nivel=base`; cambia a aplicacion o reto cuando completes ese nivel. `todos` corresponde al cierre. Sin opción las nuevas pruebas se omiten.

Base prepara una reserva y comprueba cancelación repetida, motivo, filtros y edición rechazada. Aplicacion prepara horarios adyacentes y dos altas simultáneas: solo una ocupación incompatible se confirma. Reto comprueba cupo normalizado, cancelación y páginas cuyo cursor se elimina. El test prepara los datos y los hilos; no tienes que escribir esa infraestructura. Siempre utiliza un proyecto demo-niveles-UUID propio en emulador y limpia sus reservas.

El starter conserva el CRUD del guiado. También hay puntos de cambio en crear/actualizar: incorporar reglas nuevas no consiste solo en escribir los métodos añadidos. Mantén preparada la función coordinar del repositorio; el algoritmo de disponibilidad/cupo pertenece al servicio. El callback puede repetirse y no debe tener efectos externos.

Alternativa técnica opcional: `.\mvnw.cmd -Daprendizaje.nivel=base test`; todos los niveles con emulador activo: `.\mvnw.cmd -Daprendizaje.nivel=todos test`. Compara el documento tras cada rechazo, no solo el estado HTTP.
