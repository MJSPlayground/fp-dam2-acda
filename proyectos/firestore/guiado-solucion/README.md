# Reservas de espacios — guiado-solucion

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

## Solución guiada

CRUD, DTO y reglas del servicio, mapeo comprobado con conservación de causas y errores REST. Aún no excluye reservas solapadas ni permite cancelación: son requisitos nuevos de la práctica final.

## Comprobaciones del recorrido por fases

Ruta: `src/test/java/es/mjsplayground/acda/firestore/reservas/FasesAprendizajeTest.java`. En IntelliJ crea la configuración JUnit de la clase y añade VM options `-Daprendizaje.fase=conversion`. Reutiliza esa configuración cambiando únicamente el valor al completar cada fase:

| Valor | Métodos de producción que comprueba | Dependencia |
| --- | --- | --- |
| conversion | datos/mapear, listas/mapas, ID y errores de tipo | Sin emulador ni otros TODO |
| alta-lectura | crear/obtener y ausencia; limpieza independiente del TODO de borrado | Emulador; conversión |
| repositorio | crear/obtener/actualizar/eliminar | Emulador; alta/lectura |
| consultas | listar con filtros y resultados vacíos | Emulador; CRUD de repositorio |
| servicio | crear/actualizar, validar antes de persistir | Dobles preparados; sin HTTP ni emulador |
| rest | POST/PUT/DELETE, estados, Location y error técnico seguro | Servicio de prueba preparado; sin emulador |

Los métodos se sitúan en model/repository/FirestoreReservaRepository.java (TODO2a–2d), model/service/ReservaService.java (TODO3a–3b) y api/ReservaController.java (TODO4a–4c). `esperar`, `conId`, validaciones auxiliares y frontend están preparados. No sustituyas clases completas para avanzar. Los resultados y pistas están en la página del proyecto.

Sin propiedad, estas pruebas quedan omitidas; no exigen completar fases futuras para que el starter compile. `todas` se utiliza al terminar. Las pruebas reales generan un proyecto demo-fase-UUID exclusivo, usan siempre el emulador y limpian sus documentos; no vacían los datos de la web.

Postman por pasos: carpeta00 Primer contacto HTTP antes de los TODO; carpeta01 Lectura tras completar repositorio; CRUD petición por petición después de implementar POST, PUT y DELETE. El Runner CRUD completo se ejecuta al terminar las tres escrituras, porque su limpieza utiliza DELETE.

Alternativa técnica opcional: `.\mvnw.cmd -Daprendizaje.fase=conversion test`; para todas las fases, con emulador abierto, `.\mvnw.cmd -Daprendizaje.fase=todas test`. El caso técnico de la fase rest provoca una excepción con causa y verifica500 sin stack: no requiere apagar servicios ni programar otra infraestructura.
