# Abrir y ejecutar proyectos Maven en IntelliJ IDEA

Los proyectos descargables contienen `pom.xml`, `mvnw`, `mvnw.cmd` y `.mvn/wrapper/`. Necesitas un **JDK 25** instalado. Para realizar las prácticas, trabaja desde IntelliJ IDEA.

## Preparar el JDK

El JDK incluye el compilador y las herramientas de Java; un entorno que solo permita ejecutar programas no basta para desarrollar. Puedes instalar [Eclipse Temurin](https://adoptium.net/installation/) escogiendo la versión 25, tu sistema y su arquitectura. No necesitas desinstalar otros JDK para seleccionar el que utiliza este proyecto.

Si IntelliJ no lo detecta, abre **File → Project Structure → SDKs → Add JDK from disk** y señala la carpeta del JDK. La opción **Download JDK** permite obtenerlo desde el propio IDE. Después selecciónalo en **Project → SDK**. Consulta [la configuración oficial de SDKs](https://www.jetbrains.com/help/idea/sdk.html).

Comprueba también **Settings → Build, Execution, Deployment → Build Tools → Maven → Runner → JRE**: las pruebas Maven deben usar un JDK compatible. Esta opción está descrita en la [configuración del ejecutor Maven](https://www.jetbrains.com/help/idea/maven-runner.html). `release version 25 not supported` indica que está compilando un JDK anterior; no lo arregles bajando la versión del POM. `UnsupportedClassVersionError` indica que el Java que ejecuta el programa es demasiado antiguo para sus clases.

## Importar el proyecto

1. Descarga o clona el repositorio y descomprime el ZIP si corresponde. Identifica **la carpeta concreta del proyecto** que contiene `pom.xml`, por ejemplo `proyectos/ficheros/guiado-inicial`.
2. En IntelliJ, elige **File → Open** y selecciona esa carpeta. Confirma la apertura y espera a que Maven importe el `pom.xml` y sincronice las dependencias. La ventana Maven permite recargar el proyecto si la sincronización falla.
3. En **File → Project Structure → Project**, selecciona el **JDK 25**. Si no aparece, añade el JDK instalado. Comprueba también que Maven en el IDE utilice ese JDK.

## Ejecutar un programa

Abre la clase indicada por la práctica dentro de `src/main/java`. Busca `public static void main(String[] args)`, pulsa el triángulo verde del margen y elige **Run**. Lee la salida y los errores en la ventana **Run** inferior. Cada vez que modifiques código, pulsa Run otra vez.

Al ejecutar una clase desde el margen, IntelliJ puede crear o seleccionar **otra configuración**. Para repetir siempre el arranque de la aplicación, selecciona la configuración de su `Launcher` o clase principal en la barra superior y usa su botón Run. Comprueba la clase en **Run → Edit Configurations** si aparece un programa distinto del esperado. Consulta la [documentación de configuraciones de ejecución](https://www.jetbrains.com/help/idea/run-debug-configuration.html).

Las rutas relativas parten del **Working directory** de la configuración de ejecución. Para los proyectos de este módulo debe ser la carpeta que contiene su `pom.xml`. Compruébalo en **Run → Edit Configurations** y, si no encuentras un archivo, compara con la ruta que imprime `Path.of("").toAbsolutePath()`. Los archivos generados suelen aparecer en el panel **Project**; actualiza la carpeta si no se muestran de inmediato. Los datos modificables van fuera de `src/main/resources`.

Para seguir un fallo, haz clic en el margen de una línea para poner un punto de interrupción, pulsa **Debug** junto al mismo `main`, observa variables y rutas, avanza con *Step Over* y continúa con *Resume Program*. La primera línea de tu código en la traza de error suele indicar dónde investigar. `NoSuchFileException` señala una ruta ausente; `AccessDeniedException`, un problema de acceso.

## Comprobación técnica opcional

Maven Wrapper permite al docente ejecutar pruebas desde la carpeta del `pom.xml`: `.\mvnw.cmd test` en Windows o `./mvnw test` en Linux/macOS. No es un paso necesario para completar la práctica. `target/` contiene resultados de compilación y no se distribuye.

## Importar clases y mantener el editor

En **Settings → Editor → General → Auto Import → Java**, revisa **Add unambiguous imports on the fly** y **Optimize imports on the fly**. La primera ayuda con clases sin ambigüedad; la segunda mantiene limpios los imports. Si varias clases comparten nombre, elige la que corresponde al ejemplo. Importar una clase no instala su biblioteca: las dependencias se declaran en `pom.xml` y se sincronizan con Maven. Referencia: [autoimportación de IntelliJ IDEA](https://www.jetbrains.com/help/idea/creating-and-optimizing-imports.html).

Atajos del mapa Windows predeterminado: `Alt+Enter` para acciones sugeridas, `Ctrl+Alt+O` para optimizar imports, `Shift+F6` para renombrar, doble `Shift` para buscar, `Shift+F10` para ejecutar la configuración seleccionada, `Shift+F9` para depurar y `F8` para avanzar sin entrar en un método. Comprueba **Settings → Keymap** si tu teclado o configuración difieren; la [referencia oficial de atajos Windows](https://www.jetbrains.com/help/idea/reference-keymap-win-default.html) recoge las acciones disponibles.
