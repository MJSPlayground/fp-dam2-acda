# Inicio del codelab de directorios

Abre **esta carpeta**, `proyectos/ficheros/codelab-inicial`, como proyecto Maven con JDK 25. En Windows ejecuta `./mvnw.cmd` desde PowerShell; en Linux/macOS, `./mvnw`. No abras `proyectos/ficheros/solucion` para editar el ejercicio.

```powershell
.\mvnw.cmd test
.\mvnw.cmd -q exec:java '-Dexec.mainClass=es.mjsplayground.acda.ficheros.codelab.PrepararDatos' '-Dexec.args=trabajo-codelab/entrada'
.\mvnw.cmd -q exec:java '-Dexec.mainClass=es.mjsplayground.acda.ficheros.codelab.InventarioCodelab' '-Dexec.args=trabajo-codelab/entrada trabajo-codelab/salida csv'
```

`PrepararDatos` crea una carpeta nueva y falla si ya existía para conservar tus cambios. Si necesitas repetir desde cero, escoge otro nombre para la carpeta de entrada. Los datos son externos al classpath porque la práctica los organiza y puede modificarlos. El programa inicial solo valida los tres argumentos y muestra rutas; la lógica se construye en la página del codelab.
