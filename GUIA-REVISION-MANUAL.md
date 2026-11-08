# Guía de revisión manual de ACDA

Guía para la revisión humana posterior a las correcciones. Documento fuera de docs; no se incluye en la web ni en sus PDF. Las pruebas realizadas y los límites figuran en ESTADO-DESARROLLO.md y los informes AUDITORIA-*.md. No presupone aprobación pedagógica.

## Preparación común

Abre cada ZIP en una carpeta independiente con IntelliJ, sincroniza Maven y selecciona JDK 25. Mantén la solución separada de tu implementación inicial. Los cuatro proyectos de un tema comparten interfaz y, cuando corresponde, servidor; no abras dos variantes simultáneamente en el mismo puerto.

Usa únicamente datos de prueba. Detener un servidor no equivale a borrar sus datos; no elimines volúmenes ni cambies ddl-auto a create para repetir una revisión. Los tests de aprendizaje usan directorios o esquemas propios. Consulta el README de la variante para propiedades de activación y diferencia pruebas ejecutadas/omitidas. Las cuentas locales de desarrollo documentadas en cada proyecto no son credenciales privadas.

En el starter, el modo ordinario debe arrancar y puede mostrar pruebas omitidas: no certifica los TODO. Activa una fase concreta al implementarla; antes de completarla es normal que su comprobación funcional falle. No actives completo/todas/todos hasta terminar el recorrido correspondiente y no modifiques los tests para aceptar un resultado incorrecto.

Las capturas de IntelliJ, Workbench, Compass y Postman se dejan para comprobación humana. Las pruebas técnicas automatizadas no sustituyen este recorrido ni la valoración de esfuerzo con alumnado.

## Ficheros — inventario

### Páginas en orden

- [Manejo de ficheros y directorios](https://mjsplayground.github.io/fp-dam2-acda/ficheros/) — [fuente](docs/ficheros/index.md).
- [Ficheros, directorios y rutas](https://mjsplayground.github.io/fp-dam2-acda/ficheros/rutas/) — [fuente](docs/ficheros/rutas.md).
- [Texto, CSV y codificación](https://mjsplayground.github.io/fp-dam2-acda/ficheros/texto-csv/) — [fuente](docs/ficheros/texto-csv.md).
- [JSON y objetos Java con Jackson](https://mjsplayground.github.io/fp-dam2-acda/ficheros/json/) — [fuente](docs/ficheros/json.md).
- [Binarios, acceso aleatorio y serialización](https://mjsplayground.github.io/fp-dam2-acda/ficheros/binarios/) — [fuente](docs/ficheros/binarios.md).
- [Repositorios, servicios, errores y logging](https://mjsplayground.github.io/fp-dam2-acda/ficheros/arquitectura/) — [fuente](docs/ficheros/arquitectura.md).
- [Proyecto guiado: inventario local de productos](https://mjsplayground.github.io/fp-dam2-acda/ficheros/proyecto/) — [fuente](docs/ficheros/proyecto.md).
- [Nuevas funciones del inventario](https://mjsplayground.github.io/fp-dam2-acda/ficheros/practica-final/) — [fuente](docs/ficheros/practica-final.md).
- [Prácticas de repaso](https://mjsplayground.github.io/fp-dam2-acda/ficheros/repaso/) — [fuente](docs/ficheros/repaso.md).
- [Ampliación](https://mjsplayground.github.io/fp-dam2-acda/ficheros/ampliacion/) — [fuente](docs/ficheros/ampliacion.md).

### Cuatro descargas independientes

- [Guiado inicial](https://mjsplayground.github.io/fp-dam2-acda/descargas/codigo/inventario-guiado-inicial.zip) — [README](proyectos/ficheros/guiado-inicial/README.md).
- [Guiado solución](https://mjsplayground.github.io/fp-dam2-acda/descargas/codigo/inventario-guiado-solucion.zip) — [README](proyectos/ficheros/guiado-solucion/README.md).
- [Práctica final inicial](https://mjsplayground.github.io/fp-dam2-acda/descargas/codigo/inventario-final-inicial.zip) — [README](proyectos/ficheros/final-inicial/README.md).
- [Práctica final solución](https://mjsplayground.github.io/fp-dam2-acda/descargas/codigo/inventario-final-solucion.zip) — [README](proyectos/ficheros/final-solucion/README.md).

### Arranque y configuración

Clase: `es.mjsplayground.acda.ficheros.inventario.app.Launcher.main`. Sin servidor; carpeta externa seleccionada en la UI. Las dependencias exactas siguen fijadas en el POM; no sustituirlas para esta revisión. La interfaz está preparada en las cuatro variantes.

### Datos iniciales y restablecimiento seguro

El guiado abre tres productos en memoria: Teclado 10/25,90€, Ratón 20/12,95€ y Monitor 5/159,00€. Escoge una carpeta de prueba nueva con Usar carpeta y pulsa Copiar ejemplos; los originales del classpath no se modifican ni se sobrescriben copias existentes. Reiniciar restablece el catálogo inicial en memoria; para recuperar un catálogo guardado hay que cargarlo expresamente. Conserva aparte los archivos que quieras revisar y no borres datos previos para repetir un caso.

### Casos manuales y resultados observables

| Recorrido | Resultado esperado |
| --- | --- |
| Starter antes de completar fases | Ventana operativa, catálogo en memoria y avisos de funciones pendientes; nunca confirma un guardado inexistente. |
| Listar la carpeta tras copiar ejemplos | Dos archivos CSV directos; no recorre subcarpetas. |
| Guiado: guardar/cargar CSV, JSON y BIN propio | Tres productos idénticos, incluidos nombres con coma/comillas/salto interior. JSON no acepta unidades fraccionarias. |
| Alta duplicada o carga cuya segunda fila tiene stock negativo | Se conserva todo el catálogo anterior; no importa parcialmente la primera fila. |
| Editar/cancelar, borrar/cancelar y rechazar precio con tres decimales | No cambia el dato anterior; el mensaje permite corregir la entrada. |
| Final Base: mínimo10 y texto vacío | Exporta Teclado y Ratón sin cambiar los tres productos actuales. `rat` exporta sólo Ratón. |
| Final Aplicación: lote público preparado, partiendo de inicial | Cuatro productos y cuatro incidencias: P01=12/24,90€; P04=3/5,90€; P02 intacto; DUP y stock inválido descartados. |
| Final Reto: previsualizar, editar después y aplicar propuesta anterior | Rechazo de propuesta obsoleta; catálogo editado intacto. |
| Final Reto: previsualizar de nuevo y aplicar con ruta de recuperación nueva | Copia previa completa y posterior aplicación; recuperar BIN devuelve el estado anterior. Archivo ya existente/copia fallida no aplica cambios. |

### Pruebas preparadas

Abre `FasesGuiadoTest` en IntelliJ y configura VM options `-Dfase=rutas`; continúa con csv-lectura/csv-escritura/crud/carga/json/bin. `completo` las activa todas. Final: `FasesFinalTest` con `-Dfase=base`, aplicacion o reto. Los tests crean archivos temporales propios, por lo que no hay que inventar archivos corruptos ni cambiar permisos del sistema. El fixture pequeño del test de conciliación usa4 productos/2 incidencias: no confundirlo con el lote público4/4.

Alternativa técnica: `mvnw.cmd -Dtest=FasesGuiadoTest -Dfase=completo test` y `mvnw.cmd -Dtest=FasesFinalTest -Dfase=completo test`. Las soluciones pasan esas suites; en el starter actívalas sólo tras implementar la fase. Pruebas UI optativas con `-Dqa.ui=true`, entorno gráfico. No modificar frontend ni tests para ocultar un fallo.

**Revisión humana:** importación/Run reales en IntelliJ, comodidad de formularios y comprensión de carga candidata/recuperación. La comprobación técnica no mide cuánto tarda un grupo en aprenderlo.

## Hibernate — biblioteca

### Páginas en orden

- [Herramientas ORM y JPA con Hibernate](https://mjsplayground.github.io/fp-dam2-acda/hibernate/) — [fuente](docs/hibernate/index.md).
- [Arquitectura, ORM y Jakarta Persistence](https://mjsplayground.github.io/fp-dam2-acda/hibernate/arquitectura/) — [fuente](docs/hibernate/arquitectura.md).
- [MySQL, Maven y configuración de Hibernate](https://mjsplayground.github.io/fp-dam2-acda/hibernate/configuracion/) — [fuente](docs/hibernate/configuracion.md).
- [Entidades y mapeo](https://mjsplayground.github.io/fp-dam2-acda/hibernate/entidades/) — [fuente](docs/hibernate/entidades.md).
- [Sesiones, estados, CRUD y transacciones](https://mjsplayground.github.io/fp-dam2-acda/hibernate/persistencia/) — [fuente](docs/hibernate/persistencia.md).
- [Relaciones y ciclo de vida](https://mjsplayground.github.io/fp-dam2-acda/hibernate/relaciones/) — [fuente](docs/hibernate/relaciones.md).
- [Consultas HQL y JPQL básicas](https://mjsplayground.github.io/fp-dam2-acda/hibernate/consultas/) — [fuente](docs/hibernate/consultas.md).
- [Proyecto guiado: catálogo de biblioteca](https://mjsplayground.github.io/fp-dam2-acda/hibernate/proyecto/) — [fuente](docs/hibernate/proyecto.md).
- [Ampliación del catálogo](https://mjsplayground.github.io/fp-dam2-acda/hibernate/practica-final/) — [fuente](docs/hibernate/practica-final.md).
- [Prácticas de repaso](https://mjsplayground.github.io/fp-dam2-acda/hibernate/repaso/) — [fuente](docs/hibernate/repaso.md).
- [Ampliación](https://mjsplayground.github.io/fp-dam2-acda/hibernate/ampliacion/) — [fuente](docs/hibernate/ampliacion.md).

### Cuatro descargas independientes

- [Guiado inicial](https://mjsplayground.github.io/fp-dam2-acda/descargas/codigo/biblioteca-guiado-inicial.zip) — [README](proyectos/hibernate/guiado-inicial/README.md).
- [Guiado solución](https://mjsplayground.github.io/fp-dam2-acda/descargas/codigo/biblioteca-guiado-solucion.zip) — [README](proyectos/hibernate/guiado-solucion/README.md).
- [Práctica final inicial](https://mjsplayground.github.io/fp-dam2-acda/descargas/codigo/biblioteca-final-inicial.zip) — [README](proyectos/hibernate/final-inicial/README.md).
- [Práctica final solución](https://mjsplayground.github.io/fp-dam2-acda/descargas/codigo/biblioteca-final-solucion.zip) — [README](proyectos/hibernate/final-solucion/README.md).

### Arranque y configuración

Clase: `es.mjsplayground.acda.hibernate.biblioteca.app.Launcher.main`. MySQL 8.4.13 en3308; config.MySqlLocal.main, Working directory junto al POM; esquemas biblioteca_guiado/biblioteca_final. Las dependencias exactas siguen fijadas en el POM; no sustituirlas para esta revisión. La interfaz está preparada en las cuatro variantes.

### Datos iniciales y restablecimiento seguro

Arranca MySQL con el helper y después Launcher. Preparar demostración sólo funciona si todos los catálogos están vacíos. Crea Horizonte/Norte y autores Alex Ríos/Luz Vega: Java y datos29,90€, Horizonte, ambos autores, detalle Español/320; Historias locales18,50€, Norte, Alex, sin detalle. Reiniciar no vuelve a sembrar ni elimina registros. Si ya tienes datos, trabaja con ellos o crea exclusivamente registros de revisión; no borres el volumen ni uses create-drop para forzar una base vacía.

Las pruebas por fases crean un esquema UUID propio y lo eliminan al finalizar. Es la forma reproducible de restablecer el ensayo exacto sin vaciar biblioteca_guiado/final. No apuntes la aplicación a los esquemas efímeros de los tests.

### Casos manuales y resultados observables

| Caso | Resultado esperado |
| --- | --- |
| Starter incompleto | Ventana y formularios preparados; asociaciones/TODO pendientes no aparentan persistencia terminada. |
| 1:N editorial-libros | Horizonte relaciona Java y datos; cambiar editorial cambia propietario del libro y conserva autores/detalle. |
| M:N autores-libros | Alex participa en ambos libros; retirar a Luz de Java elimina el vínculo y conserva el autor. |
| 1:1 libro-detalle | Quitar detalle elimina el dependiente y conserva el libro. Dos fichas no comparten un mismo detalle. |
| HQL con datos preparados | Sin filtro2 libros; Java+Horizonte1; Java+Norte0. |
| ISBN repetido, precio inválido o formulario antiguo | Rechazo, datos anteriores intactos y causa técnica preservada cuando corresponde. |
| Final Base: nueva edición | Identidad/ISBN nuevos; mismos autores/editorial/precio, detalle de identidad diferente. Editar copia no altera original. |
| Final Aplicación: traslado de29,90€ con10% | Precio26,91€, editorial nueva; selección rechazada conserva todos los libros. |
| Final Reto: fusión con nombre de una tercera editorial | Falla después de cambios preparados; al consultar de nuevo ninguna entidad queda trasladada/eliminada parcialmente. |
| Final Reto: fusión válida | Destino reúne libros, origen desaparece; autores compartidos, precios y detalles conservados. |

### Pruebas preparadas

`FasesGuiadoTest` permite `-Dfase=uno-muchos`, muchos-muchos o uno-uno sin MySQL; mapeos reúne esas comprobaciones. Después catalogos/repositorio/consultas/servicio/rollback utilizan MySQL y `-Dqa.mysql=true`. `completo` activa todos los hitos. Final: `FasesFinalTest`, fase=base/aplicacion/reto/completo y qa.mysql=true. El ensayo de rollback ya observa una inserción real tras flush y provoca después el error; no hay que detener Docker ni programar averías.

La conexión administrativa de desarrollo se configura mediante `QA_MYSQL_ADMIN_URL`, `QA_MYSQL_ADMIN_USER`, `QA_MYSQL_ADMIN_PASSWORD` como explica README. Sólo se utilizan esquemas propios de fixture. Si no preparas ese entorno, la prueba omitida no cuenta como verificación. Las soluciones han pasado todas las fases; las pruebas UI de los iniciales funcionan también después de completar su backend.

**Revisión humana:** Run desde IntelliJ, visualización de relaciones en Workbench y comprensión del propietario/cascadas. Versiones y transacción de apoyo se entregan preparadas; no convertir su infraestructura en una nueva actividad obligatoria.

## Spring — catálogo por componentes

### Páginas en orden

- [Acceso a datos mediante componentes Spring](https://mjsplayground.github.io/fp-dam2-acda/spring/) — [fuente](docs/spring/index.md).
- [Componentes e inyección de dependencias](https://mjsplayground.github.io/fp-dam2-acda/spring/componentes/) — [fuente](docs/spring/componentes.md).
- [Spring Boot y configuración](https://mjsplayground.github.io/fp-dam2-acda/spring/configuracion/) — [fuente](docs/spring/configuracion.md).
- [Repositorios con Spring Data JPA](https://mjsplayground.github.io/fp-dam2-acda/spring/persistencia/) — [fuente](docs/spring/persistencia.md).
- [Servicios, reglas y colaboración](https://mjsplayground.github.io/fp-dam2-acda/spring/servicios/) — [fuente](docs/spring/servicios.md).
- [Transacciones y conflictos](https://mjsplayground.github.io/fp-dam2-acda/spring/transacciones/) — [fuente](docs/spring/transacciones.md).
- [Proyecto guiado: servicios para el catálogo](https://mjsplayground.github.io/fp-dam2-acda/spring/proyecto/) — [fuente](docs/spring/proyecto.md).
- [Práctica final: reglas y servicios del catálogo](https://mjsplayground.github.io/fp-dam2-acda/spring/practica-final/) — [fuente](docs/spring/practica-final.md).
- [Prácticas de repaso](https://mjsplayground.github.io/fp-dam2-acda/spring/repaso/) — [fuente](docs/spring/repaso.md).
- [Ampliación](https://mjsplayground.github.io/fp-dam2-acda/spring/ampliacion/) — [fuente](docs/spring/ampliacion.md).

### Cuatro descargas independientes

- [Guiado inicial](https://mjsplayground.github.io/fp-dam2-acda/descargas/codigo/catalogo-guiado-inicial.zip) — [README](proyectos/spring/guiado-inicial/README.md).
- [Guiado solución](https://mjsplayground.github.io/fp-dam2-acda/descargas/codigo/catalogo-guiado-solucion.zip) — [README](proyectos/spring/guiado-solucion/README.md).
- [Práctica final inicial](https://mjsplayground.github.io/fp-dam2-acda/descargas/codigo/catalogo-final-inicial.zip) — [README](proyectos/spring/final-inicial/README.md).
- [Práctica final solución](https://mjsplayground.github.io/fp-dam2-acda/descargas/codigo/catalogo-final-solucion.zip) — [README](proyectos/spring/final-solucion/README.md).

### Arranque y configuración

Clase: `es.mjsplayground.acda.spring.catalogo.CatalogoApplication.main`. MySQL 8.4.13 en3309; config.MySqlLocal.main; esquemas spring_guiado/spring_final. HTTP 8080. Las dependencias exactas siguen fijadas en el POM; no sustituirlas para esta revisión. La interfaz está preparada en las cuatro variantes.

### Datos y restablecimiento seguro

El botón Preparar demostración sólo actúa sobre un catálogo vacío: Horizonte/Norte; Java y datos29,90€ stock 10 e Historias locales18,50€ stock 5. Si ya existen registros se rechaza sin borrarlos. Usa datos propios de revisión y elimina sólo esos libros/editoriales desde la UI si quieres repetir un pequeño caso; conserva pedidos anteriores. No borres el volumen ni fuerces ddl-auto=create. Para un estado inicial exacto, los tests preparados crean un esquema qa_spring_UUID independiente y lo eliminan al terminar.

### Casos y resultados observables

| Recorrido | Resultado esperado |
| --- | --- |
| Primer recorrido del guiado | Solo controlador preparado → CatalogoService.listar → LibroRepository; dos consultas por título/editorial. No hace falta programar configuración, UI ni eventos. |
| Guiado: validar, guardar, editar y cancelar | Datos correctos persistidos; errores conservan formulario. Dos pestañas: segundo envío de versión antigua rechazado. |
| Demo repetida o interrupción preparada tras crear editoriales | No resembrar ni dejar registros parciales. No cortar el servidor: fase4 ya trae el fallo. |
| Perfiles silencioso y practica | Persistencia igual sin aviso; stock máximo50 con practica. No programar notificadores/listener. |
| Final Base: stock 49, reponer1 bajo practica | Stock 50; otra unidad o versión antigua rechazadas sin cambios. |
| Final Aplicación: libro25,00€, cantidad 4, stock 10 | Subtotal 100, descuento 5,total 95; stock 10 y cero pedidos nuevos. Con10,01€×10:100,10/5,01/95,09. |
| Final Reto: una unidad de cada uno de dos libros stock 10 | Stocks9/9 y un pedido con snapshot. Repetición exacta de clave con versiones actuales: conflicto y sin nuevos descuentos. |
| Clave que solo cambia mayúsculas | Otra solicitud distinta; unicidad sensible a mayúsculas. |
| Cambiar título/precio después de confirmar | El pedido conserva sus líneas e importes originales. |

### Pruebas preparadas y límites

`AprendizajeGuiadoTest` con VM options `-Daprendizaje.fase=2`,3,4,5 o todas; `AprendizajeFinalTest` con `-Daprendizaje.nivel=base`,aplicacion,reto o todos. MySQL local del proyecto3309 por defecto, cuenta administrativa local documentada; otra instancia mediante QA_MYSQL_ADMIN_URL/USER/PASSWORD. No modifican spring_guiado/final y no necesitan qa.mysql=true para estas nuevas suites. Los tests ya preparan trigger tardío, formulario obsoleto y concurrencia: no hay que programarlos.

Para contrastar todas en solución: `mvnw.cmd -Daprendizaje.fase=todas -Dtest=AprendizajeGuiadoTest test` y `mvnw.cmd -Daprendizaje.nivel=todos -Dtest=AprendizajeFinalTest test`. Los tests históricos MySQL/seguridad usan aparte qa.mysql=true.

Revisión opcional de Ampliación: perfil seguro, lector consulta y gestor escribe, POST sin CSRF rechazado; JAR y Docker según README. No son nuevas tareas de frontend. **Límite conocido:** una caída real de MySQL puede esperar30,1 s antes del500 por el timeout de Hikari; recuperación y persistencia se comprobaron. En esta auditoría se repiten UI/JAR y pruebas; no se atribuye un nuevo despliegue Docker si no se ejecuta.

## JDBC — almacén

### Páginas en orden

- [Conexiones a bases de datos relacionales (JDBC)](https://mjsplayground.github.io/fp-dam2-acda/jdbc/) — [fuente](docs/jdbc/index.md).
- [Conectores y conexiones](https://mjsplayground.github.io/fp-dam2-acda/jdbc/conexiones/) — [fuente](docs/jdbc/conexiones.md).
- [Instrucciones SQL y resultados](https://mjsplayground.github.io/fp-dam2-acda/jdbc/consultas/) — [fuente](docs/jdbc/consultas.md).
- [Repositorios, servicios y errores](https://mjsplayground.github.io/fp-dam2-acda/jdbc/repositorios/) — [fuente](docs/jdbc/repositorios.md).
- [Transacciones y ventas atómicas](https://mjsplayground.github.io/fp-dam2-acda/jdbc/transacciones/) — [fuente](docs/jdbc/transacciones.md).
- [Proyecto guiado: almacén y ventas](https://mjsplayground.github.io/fp-dam2-acda/jdbc/proyecto/) — [fuente](docs/jdbc/proyecto.md).
- [Operaciones de almacén](https://mjsplayground.github.io/fp-dam2-acda/jdbc/practica-final/) — [fuente](docs/jdbc/practica-final.md).
- [Prácticas de repaso](https://mjsplayground.github.io/fp-dam2-acda/jdbc/repaso/) — [fuente](docs/jdbc/repaso.md).
- [Ampliación](https://mjsplayground.github.io/fp-dam2-acda/jdbc/ampliacion/) — [fuente](docs/jdbc/ampliacion.md).

### Cuatro descargas independientes

- [Guiado inicial](https://mjsplayground.github.io/fp-dam2-acda/descargas/codigo/almacen-guiado-inicial.zip) — [README](proyectos/jdbc/guiado-inicial/README.md).
- [Guiado solución](https://mjsplayground.github.io/fp-dam2-acda/descargas/codigo/almacen-guiado-solucion.zip) — [README](proyectos/jdbc/guiado-solucion/README.md).
- [Práctica final inicial](https://mjsplayground.github.io/fp-dam2-acda/descargas/codigo/almacen-final-inicial.zip) — [README](proyectos/jdbc/final-inicial/README.md).
- [Práctica final solución](https://mjsplayground.github.io/fp-dam2-acda/descargas/codigo/almacen-final-solucion.zip) — [README](proyectos/jdbc/final-solucion/README.md).

### Arranque y configuración

Clase: `es.mjsplayground.acda.jdbc.almacen.app.Launcher.main`. SQLite en datos/almacen.db; VM option -Dalmacen.db permite una ruta nueva. No servidor. Las dependencias exactas siguen fijadas en el POM; no sustituirlas para esta revisión. La interfaz está preparada en las cuatro variantes.

### Datos iniciales y restablecimiento seguro

SQLite crea datos/almacen.db relativo al Working directory. Para un ensayo nuevo sin borrar otro, usa VM options `-Dalmacen.db=C:/carpeta-de-pruebas/revision1.db` con carpeta existente y archivo nuevo. Repite cambiando el nombre del archivo. La semilla contiene id 1 Teclado stock 10/2590 c; id 2 Ratón 20/1295 c; id 3 Monitor 5/15900 c. El precio interno está en céntimos.

### Casos y resultados observables

| Recorrido | Resultado esperado |
| --- | --- |
| Starter incompleto | Abre UI y comunica etapas pendientes, sin fingir catálogo consultado ni venta guardada. |
| Guiado: consulta, alta, edición y cancelación | Tres registros; nombre con apóstrofo guardado correctamente; cancelación no cambia datos. |
| Venta de2 teclados desde base nueva | Stock 8, una venta y total 51,80€. |
| Venta de11 desde otra base nueva | Stock 10 e historial vacío. No reutilizar el estado de la venta anterior para esperar10. |
| Borrar producto con ventas | Rechazo; historial conservado. |
| Final Base: TE y máximo10 | Teclado. Monitor con máximo4 no aparece. Aplicar+10% aTeclado:2849 c, stock 10. |
| Final Aplicación desde base nueva: recibir3 Teclados a1250 c | Stock 13; justificante3750 c; precio de catálogo2590 c. |
| Final Reto:2 Teclados×1000 c y1 Monitor×10000 c, presupuesto12000 c | Stocks12/6; dos justificantes; total 12000 c. Con11999 c o segundo idinexistente: sin cambios. |

### Pruebas preparadas

`AprendizajeGuiadoTest`, VM options `-Daprendizaje.fase=2`,3,4,5 o todas; `AprendizajeFinalTest`, `-Daprendizaje.nivel=base`,aplicacion,reto o todos. Cada caso crea SQLite temporal propia, sin tocar el archivo de la UI. Las fases comprueban propiedad de la conexión, stock/historial y SQL parametrizado; las averías ya provocan fallo después de escribir. No crear triggers en el catálogo de trabajo.

Las mismas alternativas Wrapper de Spring activan esas clases en JDBC. Las soluciones pasan10 casos guiados y11 finales nuevos. Las pruebas JavaFX existentes se habilitan con qa.ui=true en entorno gráfico. Los iniciales no exigen mantener UnsupportedOperationException al resolver tareas.

**Revisión humana:** Run desde IntelliJ, claridad de euros frente a céntimos y comprensión de quién abre/cierra/confirma una conexión. MySQL sigue siendo ejemplo de servidor, no un segundo almacenamiento obligatorio del almacén.

## Cloud Firestore — reservas

### Páginas en orden

- [Bases de datos documentales con Cloud Firestore](https://mjsplayground.github.io/fp-dam2-acda/firestore/) — [fuente](docs/firestore/index.md).
- [Documentos, colecciones y relaciones](https://mjsplayground.github.io/fp-dam2-acda/firestore/documentos/) — [fuente](docs/firestore/documentos.md).
- [Configuración y acceso seguro](https://mjsplayground.github.io/fp-dam2-acda/firestore/configuracion/) — [fuente](docs/firestore/configuracion.md).
- [Persistencia y reglas de reserva](https://mjsplayground.github.io/fp-dam2-acda/firestore/persistencia/) — [fuente](docs/firestore/persistencia.md).
- [Consultas, índices y paginación](https://mjsplayground.github.io/fp-dam2-acda/firestore/consultas/) — [fuente](docs/firestore/consultas.md).
- [HTTP, REST y respuestas de la API](https://mjsplayground.github.io/fp-dam2-acda/firestore/rest/) — [fuente](docs/firestore/rest.md).
- [Proyecto guiado: reservas de espacios](https://mjsplayground.github.io/fp-dam2-acda/firestore/proyecto/) — [fuente](docs/firestore/proyecto.md).
- [Ampliación del sistema de reservas](https://mjsplayground.github.io/fp-dam2-acda/firestore/practica-final/) — [fuente](docs/firestore/practica-final.md).
- [Prácticas de repaso](https://mjsplayground.github.io/fp-dam2-acda/firestore/repaso/) — [fuente](docs/firestore/repaso.md).
- [Ampliación](https://mjsplayground.github.io/fp-dam2-acda/firestore/ampliacion/) — [fuente](docs/firestore/ampliacion.md).

### Cuatro descargas independientes

- [Guiado inicial](https://mjsplayground.github.io/fp-dam2-acda/descargas/codigo/reservas-guiado-inicial.zip) — [README](proyectos/firestore/guiado-inicial/README.md).
- [Guiado solución](https://mjsplayground.github.io/fp-dam2-acda/descargas/codigo/reservas-guiado-solucion.zip) — [README](proyectos/firestore/guiado-solucion/README.md).
- [Práctica final inicial](https://mjsplayground.github.io/fp-dam2-acda/descargas/codigo/reservas-final-inicial.zip) — [README](proyectos/firestore/final-inicial/README.md).
- [Práctica final solución](https://mjsplayground.github.io/fp-dam2-acda/descargas/codigo/reservas-final-solucion.zip) — [README](proyectos/firestore/final-solucion/README.md).

### Arranque y configuración

Clase: `es.mjsplayground.acda.firestore.reservas.ReservasApplication.main`. config.Emulador.main primero en8085; proyecto demo-acda-reservas, sin claves. HTTP 8080. Las dependencias exactas siguen fijadas en el POM; no sustituirlas para esta revisión. La interfaz está preparada en las cuatro variantes.

### Datos y restablecimiento seguro

Emulador oficial8085, proyecto demo-acda-reservas, sin claves privadas. Sus datos se pierden al detenerlo salvo exportación explícita; no reiniciarlo como arreglo de un TODO. Para repetir manualmente elimina sólo tus reservas de revisión desde la UI/API, o arranca un emulador vacío cuando hayas conservado tu trabajo. Las pruebas por fases usan proyectos demo-fase-UUID/demo-niveles-UUID separados y no necesitan limpiar la colección de la aplicación.

Importa `postman/reservas.postman_collection.json`. Selecciona carpeta00 Primer contacto HTTP antes de los TODO: devuelve los recursos preparados y comunica501 para lo pendiente. Después sigue las carpetas por fase y modifica sólo baseUrl, IDs y datos indicados. En soluciones el recorrido completo crea/consulta/edita/borra; guarda IDs automáticamente. No construir peticiones a mano ni cambiar frontend.

### Casos y resultados observables

| Recorrido | Datos y resultado |
| --- | --- |
| Guiado | SALA-A,2027-02-15,10–12, responsableCoordinación,8 asistentes; equipo ["Proyector, HD"], detalles {"actividad":"Taller","contacto":"Recepción"}. Alta201+Location; GET conserva todo. |
| Edición de sólo responsable | Conserva mapa/lista;13 asistentes enSALA-A→400 sin cambios. DELETE204 y posteriorGET404. |
| Conversión de documento incompatible | Prueba preparada con inicio9.5 rechaza el documento; no trunca a9. Long fuera de int también se rechaza con causa conservada. No introducir corrupción manual en la colección de trabajo. |
| Final Base: cancelar con Cambio y repetir con Otro | Mismo ID y primer motivo; vacío/121 caracteres→400, IDinexistente→404, editar cancelada→409. |
| Final Aplicación: misma sala/día10–12,11–13,12–13 | Primera aceptada, solapada rechazada, adyacente aceptada; cancelar libera la primera franja. |
| Final Reto: dos activas de Coordinación/coordinación en el mismo día | Sumando salas se alcanza cupo; tercera→409; cancelar libera cupo. |
| Paginación: tres documentos/tamaño2 | Continuación sin repetir; eliminar el ID que sirvió de cursor no invalida su posición. Tamaño0/21 y cursor../x→400. |

### Pruebas preparadas

`FasesAprendizajeTest` con `-Daprendizaje.fase=conversion`, alta-lectura, repositorio, consultas, servicio o rest; todas activa todos. Alta-lectura no depende de implementar antes eliminar: su fixture limpia mediante SDK preparado. `NivelesAprendizajeTest`, `-Daprendizaje.nivel=base`,aplicacion,reto o todos. Las fases que usan SDK necesitan emulador. Los mecanismos de conflicto/concurrencia están escritos y no requieren inventar fallos de almacenamiento.

Las suites históricas del emulador se activan aparte con qa.emulador=true; los scripts de CI activan también fases/niveles en soluciones. Colecciones probadas con Newman: starter carpeta00 trespeticiones/tresaserciones; guiado16/23; final33/52. El número depende del recorrido elegido, no indica que el starter ya resuelva sus fases.

**Revisión humana:** importación visual de Postman y Run de emulador/aplicación en IntelliJ. IAM, credenciales e índices remotos requieren un proyecto cloud real; no están validados por el emulador. La API local no se expone a Internet como parte de esta revisión.

## MongoDB — equipos y competición

### Páginas en orden

- [Bases de datos NoSQL con MongoDB](https://mjsplayground.github.io/fp-dam2-acda/mongodb/) — [fuente](docs/mongodb/index.md).
- [Documentos, BSON y modelado](https://mjsplayground.github.io/fp-dam2-acda/mongodb/documentos/) — [fuente](docs/mongodb/documentos.md).
- [MongoDB con Docker y Compass](https://mjsplayground.github.io/fp-dam2-acda/mongodb/configuracion/) — [fuente](docs/mongodb/configuracion.md).
- [Persistencia con Spring Data MongoDB](https://mjsplayground.github.io/fp-dam2-acda/mongodb/persistencia/) — [fuente](docs/mongodb/persistencia.md).
- [Consultas, paginación e índices](https://mjsplayground.github.io/fp-dam2-acda/mongodb/consultas/) — [fuente](docs/mongodb/consultas.md).
- [Atomicidad, conflictos y errores](https://mjsplayground.github.io/fp-dam2-acda/mongodb/atomicidad/) — [fuente](docs/mongodb/atomicidad.md).
- [Proyecto guiado: equipos y jugadores](https://mjsplayground.github.io/fp-dam2-acda/mongodb/proyecto/) — [fuente](docs/mongodb/proyecto.md).
- [Gestión de competición](https://mjsplayground.github.io/fp-dam2-acda/mongodb/practica-final/) — [fuente](docs/mongodb/practica-final.md).
- [Prácticas de repaso](https://mjsplayground.github.io/fp-dam2-acda/mongodb/repaso/) — [fuente](docs/mongodb/repaso.md).
- [Ampliación](https://mjsplayground.github.io/fp-dam2-acda/mongodb/ampliacion/) — [fuente](docs/mongodb/ampliacion.md).

### Cuatro descargas independientes

- [Guiado inicial](https://mjsplayground.github.io/fp-dam2-acda/descargas/codigo/jugadores-guiado-inicial.zip) — [README](proyectos/mongodb/guiado-inicial/README.md).
- [Guiado solución](https://mjsplayground.github.io/fp-dam2-acda/descargas/codigo/jugadores-guiado-solucion.zip) — [README](proyectos/mongodb/guiado-solucion/README.md).
- [Práctica final inicial](https://mjsplayground.github.io/fp-dam2-acda/descargas/codigo/jugadores-final-inicial.zip) — [README](proyectos/mongodb/final-inicial/README.md).
- [Práctica final solución](https://mjsplayground.github.io/fp-dam2-acda/descargas/codigo/jugadores-final-solucion.zip) — [README](proyectos/mongodb/final-solucion/README.md).

### Arranque y configuración

Clase: `es.mjsplayground.acda.mongodb.liga.LigaApplication.main`. config.MongoLocal.main con Docker; MongoDB 8.0.34 en27018. HTTP 8080; acda_liga/acda_liga_final. Las dependencias exactas siguen fijadas en el POM; no sustituirlas para esta revisión. La interfaz está preparada en las cuatro variantes.

### Datos y restablecimiento seguro

Docker/Mongo en27018, aplicación8080; guiado acda_liga y final acda_liga_final. Las sedes CENTRO y NORTE están preparadas. Usa equipos de revisión con nombres únicos; elimina sólo esos equipos al terminar, sin borrar volúmenes ni documentos ajenos en Compass. Las pruebas crean bases acda_qa_fases_UUID/acda_qa_niveles_UUID y verifican su nombre antes de limpiarlas.

### Casos y resultados observables

| Recorrido | Datos y resultado |
| --- | --- |
| Guiado | Equipo SENIOR/CENTRO; Alex dorsal 7 MEDIO, Luz dorsal 8 DEFENSA. Dorsal repetido→409. Editar nombre conserva plantilla y estadísticas. |
| Estadísticas | Incremento2 goles/1 asistencia→1 partido/2 goles/1 asistencia. El helper avanzado está completo; el alumno valida y lo invoca desde LigaService.sumarEstadisticas, sin programar MongoTemplate. |
| Copia antigua y concurrencia | Tests preparados rechazan guardado obsoleto y verifican incrementos sin pérdidas. No hacer read–modify–save desprotegido ni programar hilos como tarea extra. |
| Final Base | Luz no disponible; filtrar por goles/disponibilidad true/false. Cambiar selección conserva cifras; jugador ajeno→404. |
| Final Aplicación | Jornada j1,2027-02-15,rivalNorte,2–1; Alex1 gol/1 asistencia, Luz1 gol/0 asistencias. Rechazar mientras Luz no esté disponible y aceptar después; ambos suman unpartido. |
| Repetir jornada o aportación incoherente | Repetirj1 no suma; marcador 2 con aportaciones que suman1 rechaza sin cambios.0–0 sin aportaciones sólo añade resultado. |
| Final Reto | Secuencia victoria/empate/derrota→4 puntos. Últimoscinco por fecha/ID; orden de llegada no decide la racha. Tests aportan empates completos para todos los criterios, incluido ID. |

### Pruebas preparadas y revisión restante

Guiado `FasesAprendizajeTest`: `-Daprendizaje.fase=consultas`,equipos,jugadores,estadisticas o todas. Cada fixture prepara lo necesario aunque el TODO siguiente siga pendiente. Final: ejecutar tests del paquete con `-Daprendizaje.nivel=base`,aplicacion,reto o todos; incluye NivelesAprendizajeTest y CalculosAprendizajeTest. El test de cálculos entrega datos, no una implementación de clasificación.

Activa qa.mongo=true para las integraciones históricas además de los nuevos casos si deseas comprobar todo. Los algoritmos de competición se resuelven con Java/colecciones; no requieren Aggregation Pipeline. La frontera30+1 jornadas fue revisada en código, sin añadir en esta ronda una nueva ejecución específica: mantenerla como comprobación humana pendiente, sin atribuir un PASS automático. El límite acumulado de estadísticas sí está probado en la suite existente.

**Revisión humana:** Run desde IntelliJ y consulta de documentos en Compass, claridad de embebidos/referencias y dificultad del algoritmo con alumnado. No confundir las cinco demostraciones de Ampliación con requisitos del proyecto.
