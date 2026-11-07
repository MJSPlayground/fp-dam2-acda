# Almacén y ventas — proyecto inicial

Abre como Maven en IntelliJ, selecciona JDK 25 y ejecuta `app.Launcher.main`. La interfaz está preparada: no necesitas modificarla. El esquema, los tres productos iniciales y la conexión se entregan resueltos para poder arrancar; examina `config.Database` en la primera etapa.

El mensaje «Etapa pendiente» es intencionado: todavía no hay listado ni persistencia de operaciones de usuario. No es una simulación de guardado. Las tablas se llenarán al implementar las etapas.

1. **Conexiones:** identifica URL, apertura, PRAGMA y cierre en Database. La base se guarda fuera del classpath en `datos/almacen.db`.
2. **Consultas:** completa TODO 2 de `JdbcProductoRepository.listar`, recorre ResultSet y pulsa Actualizar. Deben aparecer Teclado (10), Ratón (20) y Monitor (5).
3. **CRUD:** completa TODO 3 de repositorio y `AlmacenService.guardar`. Prueba alta, edición y eliminación de un producto sin ventas. Nombre de 1–80 caracteres, precio 0,01–1.000.000 euros y stock no negativo. Un nombre con apóstrofo debe funcionar. El nombre duplicado debe conservar la causa técnica del error.
4. **Operación coordinada:** completa TODO 4 en ambos repositorios. No cierres la conexión que recibe un repositorio. No confirmes operaciones desde estos métodos.
5. **Transacciones:** completa TODO 5 del servicio. Vender dos teclados debe dejar ocho y registrar 51,80 euros. En otra prueba independiente con una base recién inicializada (elige otra ruta con -Dalmacen.db), una venta de once debe conservar diez y no registrar nada. Prueba rollback provocando un fallo al registrar la venta después de descontar stock. La solución incluye un test que inyecta ese fallo sin alterar datos reales.

Importes en céntimos long, fechas UTC; la vista convierte entrada y presenta mensajes. Los repositorios hacen SQL, el servicio valida reglas y controla la transacción. La vista captura errores de etapa y no pretende que una operación pendiente haya tenido éxito.

QA opcional: `.\mvnw.cmd clean test`; el test del inicial comprueba infraestructura y TODO pendiente. Las pruebas de requisitos completos están en la solución y se pueden consultar al completar los pasos. Ejecución alternativa: `.\mvnw.cmd javafx:run`.
