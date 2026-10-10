# Laboratorio de ficheros — soluciones públicas

Proyecto Maven con entradas de consola para las prácticas de formatos y una aplicación JavaFX para el gestor. Requiere JDK 25. Desde esta carpeta: `.\mvnw.cmd test` en Windows o `./mvnw test` en Linux/macOS. Para la interfaz: `.\mvnw.cmd javafx:run`.

Los ejemplos inmutables residen en `src/main/resources/data/`. `CopiarMuestras` los lee desde el classpath y los copia a una carpeta externa para experimentar. Pasa rutas externas como argumentos a las herramientas y usa una carpeta distinta para resultados; no edites los recursos empaquetados. El código se organiza en `model.entity`, `model.dao`, `model.service`, `cli` y `gestor` según su responsabilidad.

Para ejecutar una clase de consola en PowerShell, por ejemplo:

```powershell
.\mvnw.cmd -q exec:java '-Dexec.mainClass=es.mjsplayground.acda.ficheros.cli.CopiarMuestras' '-Dexec.args=trabajo/muestras'
.\mvnw.cmd -q exec:java '-Dexec.mainClass=es.mjsplayground.acda.ficheros.cli.ConvertirClientes' '-Dexec.args=csv trabajo/muestras/clientes.csv json trabajo/clientes.json'
```

Usa el mismo patrón con las clases siguientes y los argumentos indicados en cada `main`:

| Actividad | Clase |
| --- | --- |
| Organización guiada | `OrganizarDirectorios` |
| Inventario de rutas | `Inventariar` |
| Informe CSV guiado | `InformeCsvGuiado` |
| Fusión CSV | `FusionarCsv` |
| XML ⇄ CSV de tres columnas | `ClientesBasicos` |
| JSON anidado → CSV | `JsonContactosACsv` |
| Tarifas anidadas JSON/XML → CSV | `ConvertirTarifas` |
| Registros binarios y actualización | `RegistrosBinarios` |
| Serialización propia con filtro | `SerializacionDemo` |
| Informe binario | `InformeBinario` |
| Conversor CSV/XML/JSON y final fácil | `ConvertirClientes` |
| Inventario de almacenes | `CombinarInventario` |
| Streaming, WatchService, FileChannel y temporal | `AmpliacionesDemo` |
| Gestor comercial | `GestorApp` con `javafx:run` |

`ComercialService` guarda el estado en `~/MJSPlayground/comercial.json` por defecto. Se puede pasar otra ruta externa como argumento de JavaFX. Los tests usan directorios temporales y no tocan esa ruta.
