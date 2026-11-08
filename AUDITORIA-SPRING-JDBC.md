# Auditoría y corrección de Spring y JDBC

Fecha: 2026-11-08. Documento interno; no forma parte de la web del alumnado.

## Alcance, fuentes y criterio

Revisión completa de las diez páginas Spring y las nueve JDBC (incluidas portadas, Ampliación y repaso), los ocho proyectos vigentes, sus TODO, soluciones, README y pruebas. Fuentes: CONTENIDOS_ACDA.md, AGENTS.md, PLAN-DESARROLLO.md, TEACHING-STANDARD.md y skills fp-revision-didactica, fp-contenidos y fp-practicas. Estándares compartidos únicamente leídos. Las portadas fueron limpiadas por root y se conservaron.

Esta es una autocorrección del material producido anteriormente por el mismo agente; necesita la revisión independiente de root y la revisión docente humana. No se declara aprobado pedagógicamente. Se contrastan las nueve preguntas del recorrido: propósito, conocimientos previos, página actual, TODO, ubicación, resultado, errores, pistas y condición para avanzar.

Se conservaron arquitectura, dependencias, dominio, datos, frontend y dificultad de los tres niveles. No se editaron scripts globales, navegación, estándares, guía manual ni registro de progreso. No se construyó site, ZIP o PDF ni se hicieron commits/push.

## Hallazgos materiales y correcciones

1. Las guías nombraban varios métodos juntos, sin separar el primer resultado de las siguientes responsabilidades. Spring presenta ahora un recorrido de tres archivos y una tabla de lectura/modificación/infraestructura; distingue validar, crear y editar. JDBC separa CRUD de validación del servicio y añade pasos de sentencia, cursor, mapeo y devolución.
2. Se pedían rollback, versiones y concurrencia sin dar al starter los mecanismos preparados para comprobarlos. Se incorporan suites optativas por fase/nivel, idénticas entre inicial y solución, con fixtures aisladas, averías y coordinación ya escritas. No contienen implementaciones de los TODO.
3. Las pruebas básicas del inicial JDBC exigían que las funciones continuasen pendientes; completar el ejercicio hacía fallar esas pruebas. Ahora comprueban infraestructura y conservación del guiado, sin exigir mantener TODO. Los requisitos nuevos se verifican con las suites optativas.
4. El primer ejemplo completo JDBC combinaba parámetros, cursor y record. Se añade antes una lectura fija de una columna con Statement y ResultSet, con valor observado e interpretación de next.
5. Los ejemplos Spring usaban un nombre de excepción ajeno al proyecto y una consulta con orden reducido. Se alinean con ReglaCatalogoException y el orden título/id real. El driver MySQL se denomina driver, evitando confundirlo con el controlador MVC.
6. Faltaba enseñar algunas herramientas de Java exigidas por las finales: sumas exactas, pertenencia/repeticiones, operaciones BigDecimal. Ahora se explican antes de la final con ejemplos independientes y fuentes oficiales. No se revela el algoritmo de presupuesto, pedido o recepción.
7. La figura Spring no distinguía el trabajo del alumno. Mantiene la arquitectura real, pero identifica preparado/completar mediante texto y color; conserva tamaño intrínseco420x650 y fuente mínima18. La verificación visual móvil/PDF queda para root.
8. Se eliminó lenguaje de planificación futura de las fichas de repaso y la referencia pública a comandos QA. Se conservan cinco fichas En preparación por tema.

## Revisión por página: Spring

| Página | Diagnóstico y actuación | Evidencia/criterio de cierre |
| --- | --- | --- |
| index.md | Portada limpiada por root; prerrequisitos Hibernate y entrada al starter correctos. Conservada. | No presupone JDBC ni frontend. |
| componentes.md | Fundamentos de contrato/encapsulación/composición, IoC, singleton, anotaciones, constructor, Bean, ciclo/ciclos revisados. Se añade primer recorrido pequeño hacia los tres archivos reales. | Conceptos antes del código; no obliga a construir toda la infraestructura. |
| configuracion.md | Boot/Framework/starters/main/propiedades/perfiles/errores revisados. Se aclara driver y qué configuración viene resuelta y puede leerse después del listado. | PORT/MYSQL_* y perfiles coinciden con proyectos. |
| persistencia.md | JPA/proveedor/CRUD/Optional/save/flush/consultas y sustitución revisados. Se alinean excepción y orden con código real. | Dos métodos default pendientes se convierten en firmas; sin repositorio extra redundante. |
| servicios.md | Reglas/capas/excepciones/logging/eventos revisados. Mapa coloreado y textual; se añaden operaciones monetarias y Set junto al record/copia defensiva ya explicados. | Notificación, propiedades, reloj y listener siguen preparados. |
| transacciones.md | Proxy, llamada interna, REQUIRED, rollback/checked, flush, readOnly, LAZY, versión formulario y bloqueo preparado revisados. Se alinea excepción. | No exige programar el proxy, listener o bloqueo; distingue conflicto y fallo posterior a escribir. |
| proyecto.md | Reestructurada guía con mapa de archivos, primera consulta, activación JUnit y subpasos3a/3b/3c. Añadidos casos y criterios explícitos de avance por fase. | Archivo/clase/método, pista, dato, resultado y error observables; frontend intacto. |
| practica-final.md | Revisado cada requisito de los tres niveles; se añade activación optativa y matriz de observaciones. Requisitos del cliente conservados. | No nuevos algoritmos resueltos en starter ni APIs de Ampliación exigidas. |
| ampliacion.md | Revisados los siete puntos: MVC/Thymeleaf, formularios, JUnit/BootTest, diagnóstico, JAR, Security y Docker. Conservados ejemplos y límites. | Se distingue MockMvc de servidor real, authn/authz/CSRF y perfiles Compose/Spring. No se afirma aquí nueva ejecución visual o Docker. |
| repaso.md | Suprimido mantenimiento futuro; cinco fichas intactas. | Cinco En preparación, sin recursos ficticios. |

## Revisión por página: JDBC

| Página | Diagnóstico y actuación | Evidencia/criterio de cierre |
| --- | --- | --- |
| index.md | Portada limpiada por root; SQL previo y SQLite principal/MySQL demostración correctos. Conservada. | Ruta normal no requiere servidor ni frontend. |
| conexiones.md | API/driver, URLs, apertura/cierre, recursos, SQLite/MySQL y errores revisados. Añadida condición conceptual de cierre antes del CRUD. | Conexión preparada; propietario explícito; no simula tablas por abrir URL. |
| consultas.md | DDL/Statement, parámetros, tipos/NULL, CRUD, filas afectadas, claves y mapeo revisados. Se añade lectura gradual fija de una columna antes del PreparedStatement completo. | next antes de getters; objeto por fila; SQL separado de entrada. |
| repositorios.md | Capas/contratos/reglas/errores/logging/dinero revisados. Se añaden Math.addExact, colecciones y operaciones BigDecimal necesarias para la final. | Fuente oficial junto a API; ejemplos distintos del algoritmo final. |
| transacciones.md | ACID/autoCommit/conexión prestada/update condicionado/rollback/causas/SQLite revisados. Se sustituye avería abstracta por prueba ya preparada del starter. | Nueva consulta después de fallar, no solo excepción. |
| proyecto.md | Separados subpasos del listado y CRUD frente a validaciones. Activación JUnit y condiciones de avance para fases2/3/4/5. | Conexión no se abre/cierra/confirma en repositorios; venta con dos finales. |
| practica-final.md | Cada requisito revisado; matriz de pruebas preparadas por nivel y límites de APIs añadida. Se evita llamar Ampliación a la práctica final. | No exige JOIN/pool/lotes para el historial; dificultad funcional intacta. |
| ampliacion.md | Revisados siete puntos: metadatos, JOIN, CallableStatement, HikariCP, lotes, índices/planes y aislamiento. Conservada. | Diferencia MySQL/SQLite, pool/tx y batch/atomicidad; no se afirma nueva ejecución de fragmentos en esta ronda. |
| repaso.md | Suprimido mantenimiento futuro; cinco fichas intactas. | Cinco En preparación, sin proyectos nuevos. |

## Cada requisito final: Spring

| Requisito | Conocimiento previo enseñado / soporte | Verificación |
| --- | --- | --- |
| Reponer1..100, libro existente, mantener otros datos | JpaRepository, servicio/reglas, DTO preparado | Suite Base; límite configurado50 y stock anterior49. |
| Máximo configurado inclusivo | Propiedades y comparación de reglas | Alcanza50 y rechaza51. |
| Rechazar formulario antiguo | Comparación de versión y Version explicados | Base y guiado fase3: segundo uso de versión antigua no modifica. |
| Presupuesto1..20 líneas distintas, cantidades1..100 | Colecciones/Set, reglas y consultas | Aplicación: duplicadas, vacía,0/-1/101, inexistente y stock insuficiente. Aceptadas20 líneas existentes y rechazadas21 en la suite, sin persistir. |
| Precios actuales y versión de cada línea | Consulta del servicio/DTO, control optimista | Aplicación: versión obsoleta y datos recuperados del catálogo. |
| Umbral100 inclusivo y5%, redondeo | BigDecimal/multiply/compareTo/setScale | Subtotal99,90 sin descuento;100 con5;100,10 con5,01 y total95,09. |
| Resultado inmutable, sin persistir | Record/copia defensiva/elementos inmutables | Mutar lista original no cambia resultado; clear rechazado; stock/pedidos sin cambios. |
| Clave1..60 ASCII, case-sensitive, única | Contrato preparado y restricciones; no exige diseñar collation | Claves vacía/con espacio/61 rechazadas; Pedido_A y pedido_a diferentes; repetición exacta revierte. |
| Confirmación vuelve a comprobar estado | Colaboración de servicios/tx/versiones | Caso concurrente con dos solicitudes y una sola confirmación efectiva. |
| Stock de todas las líneas y pedido juntos | Transacción del caso de uso/propagación preparada | Clave duplicada tras flush de stock: sin descuentos ni pedido parcial, sin evento. |
| Instantánea estable del historial | Modelos y persistencia preparados | Cambio de título/precio posterior no cambia línea ni total del pedido guardado. |
| Causa técnica y aviso solo tras commit | Excepciones/eventos | Causa no nula y lista de eventos vacía tras rollback. |

## Cada requisito final: JDBC

| Requisito | Conocimiento previo enseñado | Verificación |
| --- | --- | --- |
| Buscar texto parcial, sin mayúsculas, vacío=todos | Consultas, mapeo y texto Java | TE, RATÓN, no coincidencia; datos y stock comprobados. |
| Stock máximo inclusivo y no negativo | Parámetros y reglas | Máximo10/4 y rechazo negativo. |
| % y _ literales | Separación de entrada/SQL y filtrado Java válido | No actúan como comodines en suite Base. |
| Porcentaje−50..100 con dos decimales | BigDecimal/escala/comparación | −50 aceptado, −50,01/100,01/0,001 rechazados. |
| Precio redondeado, rango, no cambiar nombre/stock | Aritmética decimal/servicio | Teclado+10%=2849c; Ratón−50%=648c; exceso del precio máximo conserva catálogo. |
| Recepción individual válida y justificante | CRUD, parámetros, historial y tx | +3 a1250c: stock13, total3750c, mismo precio y persistencia al reabrir. |
| Existencia/unidades/coste/overflow | Validaciones y Math exact | Cero, inexistente, duplicadas, stock Integer.MAX_VALUE y total long que no cabe. Costes0/−1/100000001c rechazados en prueba; resto de límites revisados en código. |
| Individual atómica | Misma conexión/commit/rollback | Trigger de fixture tras insertar justificante: conserva stock10 y cero registros. |
| Varias líneas, no repetir, al menos una | Colecciones, Set, bucle | Lista vacía y repetida rechazadas. |
| Presupuesto positivo y límite inclusivo | Suma exacta y comparación | 12000c aceptados;11999/0 rechazados sin cambios. |
| Última línea inválida o escritura intermedia revierte todo | Coordinación/rollback del tema | Segundo id999 y trigger en segundo producto: stocks10/5 e historial vacío. |
| Historial, causa, conservar guiado | Repositorios/servicios/errores | Causa SQLException comprobada; suites de CRUD/ventas existentes siguen pasando. |

## Pruebas y comandos ejecutados

JDK25 y Maven Wrapper del proyecto. MySQL real8.4.13 compartido por root en127.0.0.1:3308; únicamente esquemas propios qa_spring_UUID creados/eliminados por fixtures con guardia de nombre. Root gestiona la vida del servidor.

Entorno Spring utilizado:

```powershell
$env:QA_MYSQL_ADMIN_URL='jdbc:mysql://127.0.0.1:3308/mysql?sslMode=DISABLED&allowPublicKeyRetrieval=true&serverTimezone=UTC'
$env:QA_MYSQL_ADMIN_USER='root'
$env:QA_MYSQL_ADMIN_PASSWORD='qa_root_local'
```

Desde cada carpeta de variante se ejecutó `.\mvnw.cmd -q package` para iniciales; para soluciones se ejecutó package con la suite correspondiente activa. Resultado final:

| Variante | Comando adicional a package | Pruebas aprendizaje ejecutadas | Resultado |
| --- | --- | ---: | --- |
| spring/guiado-inicial | ninguno |0;5 omitidas | Compila/empaqueta; ComponentesTest2 pasan. |
| spring/guiado-solucion | -Daprendizaje.fase=todas |5 | PASS,0 fallos/errores. |
| spring/final-inicial | ninguno |0;10 omitidas | Compila/empaqueta; componentes/reglas4 pasan. |
| spring/final-solucion | -Daprendizaje.nivel=todos |10 | PASS,0 fallos/errores. |
| jdbc/guiado-inicial | ninguno |0;10 omitidas | Compila/empaqueta; infraestructura1 pasa. |
| jdbc/guiado-solucion | -Daprendizaje.fase=todas |10 | PASS; también8 tests históricos de servicio. |
| jdbc/final-inicial | ninguno |0;11 omitidas | Compila/empaqueta;9 tests históricos pasan. |
| jdbc/final-solucion | -Daprendizaje.nivel=todos |11 | PASS; también16 tests históricos de servicio. |

Las suites históricas Spring MySqlIntegrationTest/SeguridadIntegrationTest siguen siendo optativas con qa.mysql=true y no se reejecutaron en esta ronda final; sus casos de CRUD/MVC/rollback tienen cobertura nueva parcialmente coincidente. Las pruebas gráficas JavaFX qa.ui no se activaron. No confundir esas omisiones con verificaciones superadas.

Comandos exactos para CI o comprobación de todas las fases, desde cada solución del tema:

```powershell
.\mvnw.cmd -q '-Daprendizaje.fase=todas' '-Dtest=AprendizajeGuiadoTest' test
.\mvnw.cmd -q '-Daprendizaje.nivel=todos' '-Dtest=AprendizajeFinalTest' test
```

Para una fase individual sustituir todas por2,3,4 o5; para un nivel, todos por base,aplicacion o reto. No se necesita qa.mysql=true para activar las nuevas suites. En IntelliJ los mismos valores se escriben en VM options de la configuración JUnit; no se pide programar pruebas, triggers o concurrencia.

Se contrastaron los nuevos enlaces API Java25 Math/Set y la semántica transaccional oficial Framework6.2. Comprobación estática final: PASS de Markdown (cierres de código), rutas locales de ambas carpetas, cinco fichas por tema, SVG XML420x650 y fuente mínima18, y suites de aprendizaje idénticas entre inicial/solución. Los enlaces a ZIP y destinos HTML/PDF requieren la construcción posterior de root. Tras normalizar finales de archivo, git diff --check no detecta errores.

Incidencia propia corregida: el primer test final JDBC intentó sobreescribir una firma registrar que el starter dejaba al diseño del alumno; no compiló. Se cambió la avería a un trigger SQLite de fixture, evitando añadir una API obligatoria al repositorio. Después los ocho package y la suite funcional completaron correctamente. No se publicó ese estado intermedio.

## Carga orientativa interna, no medida

Hipótesis: alumnado con Java de primero, SQL básico y en Spring el catálogo Hibernate ya trabajado; entorno preparado y frontend entregado. Son rangos de diseño, no una medición de aula ni un calendario. Se separa el esfuerzo del guiado y sus explicaciones del de la práctica final; no se suman para afirmar que toda la final cabe en las horas ordinarias.

| Tema/etapa | Rango de esfuerzo estimado | Apoyo y riesgo |
| --- | --- | --- |
| Spring1 |15–25min | Recorrido de3archivos y arranque; más si hay incidencias de entorno. |
| Spring2 |20–35min | Dos consultas y prueba con datos preparados. |
| Spring3 |60–100min | Validar, crear y editar; versión y persistencia requieren razonamiento. |
| Spring4 |30–50min | Operación multi-repositorio y fallo posterior a escribir preparado. |
| Spring5 |15–25min | Observar perfiles/eventos preparados, sin programar infraestructura. |
| Spring explicación/lectura transversal y revisión |60–120min adicionales al trabajo de programación | Parte de esta lectura se entrelaza con las etapas: no es tiempo medido ni debe contarse dos veces. |
| Spring final Base |30–50min de esfuerzo adicional | Podría iniciarse o completarse parcialmente en clase según el grupo. |
| Spring final Aplicación |60–90min adicionales | Probablemente trabajo autónomo, con devolución de dudas según el grupo. |
| Spring final Reto |90–150min adicionales | Probablemente trabajo autónomo; coordinación/conflictos no deben simplificarse para ajustar una tabla. |
| JDBC1 |20–35min | Conexión/schema preparados, lectura del propietario. |
| JDBC2 |35–60min | Cursor, mapeo y consulta; primer resultado reducido. |
| JDBC3 |70–110min | CRUD separado de validación; parámetros y claves. |
| JDBC4 |30–50min | Dos escrituras sin cerrar/confirmar conexión prestada. |
| JDBC5 |60–100min | Caso atómico y fallo posterior a escritura preparado. |
| JDBC explicación/lectura transversal y revisión |90–150min adicionales al trabajo de programación | Parte se integra con las etapas; ritmo variable según SQL/Java previo. |
| JDBC final Base |45–75min de esfuerzo adicional | Podría trabajarse parcialmente en clase según avance. |
| JDBC final Aplicación |75–120min adicionales | Probablemente requiere trabajo autónomo además de la puesta en común. |
| JDBC final Reto |120–180min adicionales | Trabajo autónomo probable, manteniendo requisitos de atomicidad y casos límite. |

Las6h de Spring y11h de JDBC son la referencia ordinaria aprobada, no horas libres que podamos asignar de nuevo al total de final. Estos rangos NO justifican que todas las explicaciones, guiado, evaluación y tres niveles finales se terminen dentro del aula. No se han supuesto horas intermodulares adicionales. La planificación concreta —qué parte de Base entra en clase y qué extensión autónoma se espera— debe revisarla el docente con datos del grupo. Se ha reducido trabajo incidental mediante frontend y pruebas preparados, sin rebajar la dificultad del reto ni convertir Ampliación en prerrequisito.
## Datos para la guía manual de root

No son comprobaciones gráficas realizadas en esta ronda.

### JDBC

- Ejecutable: es.mjsplayground.acda.jdbc.almacen.app.Launcher. SQLite de trabajo externo. Reset seguro: otra carpeta de trabajo o VM option -Dalmacen.db con archivo nuevo en carpeta existente; no borrar una base del docente.
- Datos iniciales: id1 Teclado stock10 precio2590c; id2 Ratón stock20 precio1295c; id3 Monitor stock5 precio15900c.
- Guiado solución: vender2 del id1 deja8, total5180c e historial1. Otra base nueva: vender11 rechaza y conserva10/0ventas. Editar/cancelar y eliminar/cancelar sin cambios; producto con ventas no se elimina.
- Final Base aislado: TE/max10→Teclado; monitor/max4→vacío; +10% id1→2849c y stock10. Aplicación aislada: recibir3 del id1 a1250c→stock13/justificante3750c/precio2590c.
- Reto en base nueva: id1 dos unidades a1000c + id3 una a10000c, presupuesto12000c→stocks12/6,2justificantes,total12000c. Con11999c o segunda línea inexistente no cambia nada.
- Fallos posteriores a escritura: ejecutar las suites preparadas; no preparar el trigger en la base de UI. Límites: precio1..100000000c, stock int no negativo, unidades positivas, presupuesto long positivo y sin overflow.

### Spring

- Ejecutable CatalogoApplication. Arranque estándar3309/schema spring_guiado o spring_final; en la instancia compartida3308 usar MYSQL_URL hacia un schema manual propio y MYSQL_USER/PASSWORD autorizados. Crear ese schema antes de arrancar; no apuntar a los esquemas de tests ni a datos ajenos.
- Datos se crean con botón explícito sólo en base completamente vacía: editoriales Horizonte/Norte; Java y datos29,90stock10 e Historias locales18,50stock5. Si hay datos previos, se rechaza sin borrar ni resembrar.
- Guiado: crear editorial de prueba y libro ISBN13/title/price/stock; editar en dos pestañas y rechazar el formulario antiguo. Para probar demo, borrar sólo ese libro y después su editorial propios, o usar otro schema vacío.
- Perfil practica: maxStock50. Base final con libro stock49, reponer1→50; repetir con versión antigua o intentar otra unidad actual→rechazo, stock50.
- Presupuesto: libro25,00stock10, cantidad4→subtotal100, descuento5,total95, stock10 y cero pedidos. Otro libro10,01/cantidad10→100,10/5,01/95,09. La vista ya transporta versiones y admite0 para dejar una fila sin seleccionar.
- Reto: dos libros con stock10, confirmar una unidad de cada uno y consultar instantáneas. Usar de nuevo exactamente la clave con versiones actuales→conflicto sin nuevos descuentos; usar clave que cambia mayúsculas es otra solicitud. Cambiar precio/título del catálogo no cambia el pedido anterior.
- Concurrencia y fallo tras stock: suite preparada nivel reto, no hace falta coordinar manualmente hilos o manipular tablas. Security/perfiles avanzados y Docker no son tareas nuevas de frontend.

## Límites y siguiente puerta

Preparado para revisión independiente de root: fuentes/documentos y suites funcionales. Quedan a root snapshots, lectura independiente, navegación real, figura móvil/PDF, ZIP, workflows y Pages. No se declara superada en esta ronda la interfaz gráfica, IntelliJ interactivo, JDBC contra MySQL, demos completas de Ampliación ni despliegue Docker.

## Contraste independiente de root

- Reejecutados test_spring_real.sh con pruebas históricas y nuevas activadas, test_learning_local.sh jdbc, tres pruebas UI JavaFX JDBC y navegador real en cuatro JAR Spring (guiado/final soluciones, final inicial y arranque/formulario guiado inicial). Sin fallos. Procesos y esquemas exclusivos de esas comprobaciones cerrados, sin tocar el servidor compartido.
- Alineada la firma orientativa del guiado con el nombre completo real que ordena por título/id; evita que el alumno añada una consulta diferente al TODO.
- Snapshot de publicación y revisión gráfica/PDF/ZIP se registran en ESTADO-DESARROLLO.md; las cifras previas del autor no se atribuyen a estas verificaciones posteriores.
