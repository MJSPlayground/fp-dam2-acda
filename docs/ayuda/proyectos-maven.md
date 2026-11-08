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

## Aplicaciones con interfaz preparada

En los proyectos **JavaFX**, ejecuta el `Launcher` indicado por la guía. Esa clase abre la ventana de escritorio y Maven proporciona las bibliotecas necesarias; conserva las dependencias del proyecto. Si falta JavaFX, comprueba primero la sincronización y el JDK seleccionado. La [guía oficial de JavaFX con Maven](https://openjfx.io/openjfx-docs/) explica esta integración.

En los proyectos **web**, la clase principal inicia un servidor: mantén su proceso activo y abre en el navegador la dirección del README. Cerrar una pestaña no detiene el servidor. Cuando el proyecto utiliza Thymeleaf, el servidor procesa las plantillas antes de enviarlas al navegador; la aplicación se usa desde su URL, con la interfaz ya entregada. Si otra configuración ocupa el puerto, identifica cuál está ejecutándose y detén la que corresponda antes de arrancar otra variante.

## Maven Wrapper: las mismas pruebas sin instalar Maven

El **Wrapper** descarga y utiliza la versión de Maven indicada por el proyecto, aunque no tengas Maven instalado globalmente. Las bibliotecas y las versiones se resuelven desde `pom.xml`; no tienes que añadir JAR a mano.

Puedes ejecutar las pruebas desde IntelliJ o, si prefieres la terminal, desde la carpeta del `pom.xml`: `.\mvnw.cmd test` en Windows o `./mvnw test` en Linux/macOS. Son dos formas de lanzar el mismo trabajo, no dos pasos obligatorios. `target/` contiene las clases compiladas, informes de pruebas y paquetes generados.

Las pruebas que necesitan una interfaz gráfica o un servidor pueden aparecer omitidas cuando ese entorno no está preparado. Sigue la sección de pruebas del README: indica qué clase ejecutar y qué propiedad habilita cada grupo. Una prueba omitida todavía no ha comprobado tu código; vuelve a ejecutarla con los requisitos indicados.

## Importar clases y mantener el editor

En **Settings → Editor → General → Auto Import → Java**, revisa **Add unambiguous imports on the fly** y **Optimize imports on the fly**. La primera ayuda con clases sin ambigüedad; la segunda mantiene limpios los imports. Si varias clases comparten nombre, elige la que corresponde al ejemplo. Importar una clase no instala su biblioteca: las dependencias se declaran en `pom.xml` y se sincronizan con Maven. Referencia: [autoimportación de IntelliJ IDEA](https://www.jetbrains.com/help/idea/creating-and-optimizing-imports.html).

Atajos del mapa Windows predeterminado: `Alt+Enter` para acciones sugeridas, `Ctrl+Alt+O` para optimizar imports, `Shift+F6` para renombrar, doble `Shift` para buscar, `Shift+F10` para ejecutar la configuración seleccionada, `Shift+F9` para depurar y `F8` para avanzar sin entrar en un método. Comprueba **Settings → Keymap** si tu teclado o configuración difieren; la [referencia oficial de atajos Windows](https://www.jetbrains.com/help/idea/reference-keymap-win-default.html) recoge las acciones disponibles.
