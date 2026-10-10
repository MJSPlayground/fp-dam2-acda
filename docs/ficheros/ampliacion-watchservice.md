# Ampliación: monitorizar un directorio

Una herramienta puede necesitar reaccionar cuando llegan archivos a una carpeta sin preguntar repetidamente al usuario. [`WatchService`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/nio/file/WatchService.html) permite registrar un directorio y recibir eventos de creación, modificación y borrado. Los eventos son una **señal para volver a comprobar el estado**, no una garantía de que el archivo ya está completo ni una copia fiel de cada operación.

```java
try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
    Path carpeta = Path.of("entrada");
    carpeta.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
    while (!Thread.currentThread().isInterrupted()) {
        WatchKey key = watcher.take();
        for (WatchEvent<?> event : key.pollEvents()) {
            if (event.kind() == OVERFLOW) {
                System.out.println("Eventos perdidos: volver a explorar la carpeta");
                continue;
            }
            Path afectada = carpeta.resolve((Path) event.context());
            System.out.println(event.kind() + ": " + afectada);
        }
        if (!key.reset()) break;
    }
}
```

El fragmento se sitúa en un método que maneja `IOException` e `InterruptedException` y usa imports estáticos de `StandardWatchEventKinds`. `take()` bloquea hasta que hay una clave; `pollEvents()` consume los eventos y `reset()` permite recibir otros. Si aparece `OVERFLOW`, se perdieron eventos y se necesita una exploración completa para reconstruir el estado.

El registro habitual cubre solo **un directorio**, no todos sus descendientes. Para un árbol, registra subdirectorios y añade los nuevos cuando aparezcan. El orden, la latencia y la agrupación de eventos dependen del sistema operativo. Un archivo recién creado puede seguir siendo escrito por otro proceso: espera una condición de estabilidad o coordina un renombrado final antes de importarlo.

**Exploración:** ejecuta el monitor, crea un CSV, modifícalo y bórralo. Anota los eventos observados; no supongas que serán idénticos en dos sistemas. El código de referencia se encuentra en la [solución pública](https://github.com/MJSPlayground/fp-dam2-acda/tree/main/proyectos/ficheros/solucion).
