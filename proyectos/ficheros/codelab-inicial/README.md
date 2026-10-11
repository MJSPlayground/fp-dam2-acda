# Proyecto inicial: organizar e inventariar directorios

Abre esta carpeta, la que contiene `pom.xml`, mediante **File → Open** en IntelliJ IDEA. Espera la sincronización Maven y selecciona el JDK 25 en **File → Project Structure → Project**. En la configuración de ejecución, **Working directory** debe ser esta carpeta.

1. Abre `src/main/java/es/mjsplayground/acda/ficheros/codelab/PrepararDatos.java` y pulsa el triángulo verde de `main` → **Run**. Se crea `trabajo-codelab/entrada` y la consola indica su ruta absoluta.
2. Abre `InventarioCodelab.java` en el mismo paquete y pulsa **Run** junto a `main`, sin argumentos. Al principio solo muestra las rutas y el filtro: completa sus apartados siguiendo el [codelab publicado](https://mjsplayground.github.io/fp-dam2-acda/ficheros/guiada-directorios/).
3. Tras programar la exportación, consulta `trabajo-codelab/salida/inventario.csv` en el panel **Project**. Si no aparece, actualiza la carpeta. Se esperan cuatro CSV y 22 bytes.

**PrepararDatos** conserva la entrada si ya existe. Para crear otra muestra, indica una ruta de carpeta que todavía no exista en **Run → Edit Configurations → Program arguments**. No borres la carpeta que hayas modificado. Los tres argumentos de `InventarioCodelab` (entrada, salida, extensión) son opcionales para explorar otros datos; el uso normal desde Run no los requiere.

## Comprobación técnica opcional

El docente puede ejecutar `.\mvnw.cmd test` en Windows o `./mvnw test` en Linux/macOS desde esta carpeta. No es necesario para realizar el codelab en IntelliJ.
