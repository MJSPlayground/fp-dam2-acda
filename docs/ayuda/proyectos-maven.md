# Abrir y ejecutar proyectos Maven en IntelliJ IDEA

Los proyectos descargables contienen `pom.xml`, `mvnw`, `mvnw.cmd` y `.mvn/wrapper/`. Necesitas un **JDK 25** instalado. Para realizar las prácticas, trabaja desde IntelliJ IDEA.

## Importar el proyecto

1. Descarga o clona el repositorio y descomprime el ZIP si corresponde. Identifica **la carpeta concreta del proyecto** que contiene `pom.xml`, por ejemplo `proyectos/ficheros/codelab-inicial`.
2. En IntelliJ, elige **File → Open** y selecciona esa carpeta. Confirma la apertura y espera a que Maven importe el `pom.xml` y sincronice las dependencias. La ventana Maven permite recargar el proyecto si la sincronización falla.
3. En **File → Project Structure → Project**, selecciona el **JDK 25**. Si no aparece, añade el JDK instalado. Comprueba también que Maven en el IDE utilice ese JDK.

## Ejecutar un programa

Abre la clase indicada por la práctica dentro de `src/main/java`. Busca `public static void main(String[] args)`, pulsa el triángulo verde del margen y elige **Run**. Lee la salida y los errores en la ventana **Run** inferior. Cada vez que modifiques código, pulsa Run otra vez.

Las rutas relativas parten del **Working directory** de la configuración de ejecución. Para los proyectos de este módulo debe ser la carpeta que contiene su `pom.xml`. Compruébalo en **Run → Edit Configurations** y, si no encuentras un archivo, compara con la ruta que imprime `Path.of("").toAbsolutePath()`. Los archivos generados suelen aparecer en el panel **Project**; actualiza la carpeta si no se muestran de inmediato. Los datos modificables van fuera de `src/main/resources`.

Para seguir un fallo, haz clic en el margen de una línea para poner un punto de interrupción, pulsa **Debug** junto al mismo `main`, observa variables y rutas, avanza con *Step Over* y continúa con *Resume Program*. La primera línea de tu código en la traza de error suele indicar dónde investigar. `NoSuchFileException` señala una ruta ausente; `AccessDeniedException`, un problema de acceso.

## Comprobación técnica opcional

Maven Wrapper permite al docente ejecutar pruebas desde la carpeta del `pom.xml`: `.\mvnw.cmd test` en Windows o `./mvnw test` en Linux/macOS. No es un paso necesario para completar la práctica. `target/` contiene resultados de compilación y no se distribuye.
