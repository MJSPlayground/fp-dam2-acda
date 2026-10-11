# Recursos visuales pendientes · ACDA

Este archivo recoge **capturas auténticas, GIF y otros recursos visuales** que requieren intervención del docente o verificación humana. No constituye un índice de contenidos ni implica que deban producirse todas las propuestas. Se revisa cuando Codex desarrolla o valida un bloque.

**Criterios:** mostrar procesos que se comprenden mejor visualmente; no fabricar capturas de IntelliJ ni dejar enlaces a recursos inexistentes. Los esquemas conceptuales originales reproducibles podrán crearse en SVG y versionarse directamente, una vez comprobado que corresponden al código. Los GIF deben tener explicación textual y fotograma estático equivalente para PDF y movimiento reducido. Evitar datos personales y rutas privadas del equipo.

## Solicitudes concretas al docente

### [P1 · Pendiente] Captura: ejecutar el proyecto desde IntelliJ

- **Página y posición:** `docs/ficheros/guiada-directorios.md`, paso 1, junto a las instrucciones de ejecución.
- **Objetivo:** que un estudiante reconozca **qué abrir y dónde pulsar Run**, sin recurrir a Maven en terminal.
- **Tipo:** captura real PNG/WebP; **responsable:** docente.
- **Mostrar:** IntelliJ IDEA con `codelab-inicial` abierto como proyecto Maven, `PrepararDatos.java` seleccionado, triángulo Run visible, ventana **Run** con el mensaje y carpeta `trabajo-codelab/entrada` en el panel Project. Si no cabe todo con letra legible, mejor dos capturas numeradas que una panorámica ilegible.
- **Precauciones:** ocultar usuario del sistema, rutas personales, notificaciones y repositorios privados; indicar la versión de IntelliJ que se haya utilizado.
- **Destino propuesto:** `docs/assets/capturas/ficheros/intellij-preparar-datos.webp`.
- **Alt propuesto:** «IntelliJ con PrepararDatos abierto, botón Run y archivos de entrada generados».
- **Pie propuesto:** «Ejecutar PrepararDatos desde IntelliJ crea los archivos que usaremos en el inventario».
- **Estado:** no aportada. La guía textual actual debe funcionar sin ella.

### [P2 · Opcional] GIF: ejecutar y localizar la salida

- **Página y posición:** `docs/ficheros/guiada-directorios.md`, paso 1 o 2, solo si la captura P1 no basta.
- **Objetivo:** mostrar en 5–12 segundos la secuencia pulsar Run → leer consola → abrir `trabajo-codelab/entrada`.
- **Tipo:** GIF corto o vídeo optimizado capturado por el docente; **responsable:** docente.
- **Mostrar:** únicamente esa secuencia, sin cambios de configuración ni movimientos del ratón innecesarios.
- **Destino propuesto:** `docs/assets/capturas/ficheros/intellij-ejecutar-datos.gif`; fotograma/fallback `docs/assets/capturas/ficheros/intellij-preparar-datos.webp`.
- **Alt propuesto:** «Secuencia de ejecución de PrepararDatos y consulta de la carpeta de entrada en IntelliJ».
- **Pie propuesto:** «Run ejecuta el generador y permite localizar los archivos creados».
- **Estado:** pendiente de valorar tras comprobar P1. Acompañar siempre de instrucciones textuales y alternativa estática en PDF; no insertar si no aporta valor.

### [P2 · Opcional] Captura: inspeccionar variables con el depurador

- **Página y posición:** `docs/ficheros/guiada-directorios.md`, paso 9, junto a los casos de error.
- **Objetivo:** identificar un breakpoint, `Debug`, el panel Variables y un `Path` de entrada incorrecto.
- **Tipo:** captura auténtica del docente. **Destino propuesto:** `docs/assets/capturas/ficheros/intellij-debug-ruta.webp`.
- **Alt propuesto:** «IntelliJ detenido en un breakpoint y mostrando la ruta utilizada por el programa».
- **Pie propuesto:** «El depurador permite inspeccionar la ruta antes de la operación que falla».
- **Estado:** propuesta; realizar solo si la explicación textual no es suficiente.

## Diagramas que puede producir Codex sin intervención del docente

Estas entradas son **propuestas de recursos propios**, no capturas pendientes que el docente deba realizar.

- **Flujo de ejecución y responsabilidades** (`docs/ficheros/listar-recorrer.md`): SVG sencillo que represente `main → service → Files` y `ArchivoInventariado` como dato, **basado en las clases efectivamente usadas por el laboratorio común**. Aclarar que el primer codelab simplificado no necesita crear DAO/Service. Archivo sugerido: `docs/assets/figures/flujo-ficheros-servicio.svg`. Estado: propuesto; verificar precisión con el código antes de crear.
- **Figura de entrada/salida y rutas** (`docs/ficheros/rutas.md`): `docs/assets/figures/rutas-bloque1.svg` ya existe; **reutilizar y mejorar si aporta claridad**, no duplicar.

## Reglas de actualización

Cada vez que se desarrolle un bloque o tema, **revisar si hacen falta visuales adicionales** y registrar solo solicitudes concretas (prioridad, ubicación, objetivo, secuencia/estado a captar, herramienta/versión, ruta/nombre, alt, pie, estado/responsable). Codex puede generar diagramas conceptuales si las fuentes bastan, pero no capturas falsas del IDE. Si un recurso llega, validar su correspondencia con la versión actual de la herramienta y marcarlo realizado; no dejar imágenes enlazadas que no existan.
