# Abrir y ejecutar proyectos Maven

Los proyectos descargables tienen `pom.xml`, `mvnw`, `mvnw.cmd` y `.mvn/wrapper/`. Necesitas un **JDK 25**, no solo un entorno de ejecución. Comprueba en una terminal `java -version` y `javac -version`; ambos deben señalar la versión 25. Si la terminal aún usa otra versión, ajusta `JAVA_HOME` al JDK instalado y abre una terminal nueva. El wrapper descarga la versión de Maven fijada por el proyecto cuando hace falta.

## Importación en IntelliJ IDEA

Abre la **carpeta que contiene `pom.xml`** mediante *File → Open*. Espera a que IntelliJ detecte el proyecto Maven y resuelva dependencias. En *File → Project Structure → Project* selecciona el JDK 25. En la configuración Maven comprueba que el importador y el ejecutor usan ese JDK; una terminal con Java 25 y un IDE con otro JDK producirán errores distintos. Si aparece un fallo de dependencia, abre la ventana Maven, recarga el proyecto y lee el primer error completo antes de cambiar versiones.

Desde la terminal situada en la carpeta del `pom.xml`, ejecuta:

```powershell
.\mvnw.cmd test
```

En Linux o macOS utiliza `./mvnw test`. Un proyecto JavaFX ofrece además `.\mvnw.cmd javafx:run`. Los resultados de compilación aparecen en `target/`, que **no** se distribuye. El código fuente está en `src/main/java`, los tests en `src/test/java` y las muestras inmutables en `src/main/resources/data/`.

## Localizar resultados y depurar

Los programas que reciben una ruta de salida crean archivos **externos** al JAR. Cuando la ruta es relativa, se interpreta desde el directorio de trabajo de esa ejecución; muéstrala con `Path.of("").toAbsolutePath()` si no encuentras el resultado. En IntelliJ revisa *Working directory* en la configuración de ejecución. No intentes modificar `src/main/resources` desde un programa empaquetado.

Para investigar una excepción, localiza la primera línea de tu código en la traza, inspecciona los argumentos de ruta y usa un punto de interrupción antes de la lectura. Diferencia `NoSuchFileException` (ruta no localizada), `AccessDeniedException` (acceso denegado), `MalformedInputException` (decodificación) y errores de estructura CSV, XML o JSON. Cambiar el nombre del archivo o la codificación sin examinar el mensaje puede ocultar el fallo original.
