# Solución: organizar e inventariar directorios

Esta es la versión terminada de `InventarioCodelab.java` del [proyecto inicial](../codelab-inicial/README.md). El proyecto está separado para que el starter siga incompleto.

Abre **esta carpeta**, que contiene `pom.xml`, mediante **File → Open** de IntelliJ IDEA. Espera a que Maven sincronice las dependencias y selecciona el JDK 25. Comprueba que **Working directory** es esta carpeta. Abre `src/main/java/es/mjsplayground/acda/ficheros/codelab/InventarioCodelab.java` y pulsa el triángulo verde junto a `main` → **Run**, sin argumentos.

El mismo `main` llama a `PrepararDatos` para crear la muestra si falta, copia `clientes.csv` sin reemplazar una copia existente, muestra el primer nivel, recorre hasta cuatro niveles, filtra por extensión, ordena, calcula el total y el mayor, exporta el informe y comunica incidencias. `ArchivoInventariado` y `Resultado` son registros internos de esa clase; solo guardan los datos que usa el programa. `PrepararDatos` es la clase auxiliar que genera la muestra, y también se puede ejecutar por separado con Run.

En la ventana **Run** deben aparecer **4 archivos, 22 bytes** y `historico/2025/marzo.CsV` como mayor. Consulta `trabajo-codelab/salida/copias/clientes.csv` y `trabajo-codelab/salida/inventario.csv` en el panel **Project**. El informe se regenera al ejecutar de nuevo; la copia y los datos de entrada se conservan. Para otra muestra, indica una ruta de carpeta que todavía no exista a `PrepararDatos` mediante **Program arguments**.

Para probar otra entrada, salida y extensión, añade los tres argumentos en **Run → Edit Configurations → Program arguments**. Un cuarto argumento `--reemplazar` autoriza sustituir la copia. La salida debe quedar fuera de la entrada.

## Comprobación técnica opcional

El docente puede ejecutar `.\mvnw.cmd test` en Windows o `./mvnw test` en Linux/macOS desde esta carpeta. El recorrido normal del alumnado usa Run.
