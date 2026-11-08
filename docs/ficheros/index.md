# Manejo de ficheros y directorios

Aprenderás a localizar archivos y conservar información con texto, CSV, JSON y serialización Java. Construiremos un inventario local de productos sobre una interfaz JavaFX ya preparada, sin servidores adicionales.

## Antes de empezar

Necesitas Java básico: clases, colecciones, bucles y excepciones. Prepara [Maven y el JDK en IntelliJ](../ayuda/proyectos-maven.md) y descarga el [inventario inicial](proyecto.md#preparar-y-ejecutar) antes de estudiar las rutas. No necesitas un servidor de base de datos.

## Recorrido

1. [Ficheros y rutas](rutas.md): ubicaciones, operaciones y listado sencillo de una carpeta.
2. [Texto y CSV](texto-csv.md): UTF-8, lectura por líneas y registros con comillas correctas.
3. [JSON y Jackson](json.md): objetos Java, arrays, conversiones y validación.
4. [Binarios y serialización](binarios.md): flujos, demostración de acceso aleatorio y copias propias.
5. [Capas, errores y logging](arquitectura.md): responsabilidades que iremos aplicando a lo largo del proyecto.

El [proyecto guiado de inventario](proyecto.md) incorpora etapas a esta teoría. Su descarga incluye interfaz, datos, configuración y TODO del backend. Las explicaciones de capas y logging se consultan desde los primeros pasos, sin esperar a terminar todos los formatos.

Después, una sola [práctica final](practica-final.md) añade exportación de selecciones, conciliación, previsualización y recuperación con niveles Base, Aplicación y Reto avanzado. Encontrarás [cinco fichas de repaso en preparación](repaso.md) y [Ampliación](ampliacion.md) sobre XML, streaming, observación de cambios y escritura fiable.

Utiliza [preparación Maven y Run](../ayuda/proyectos-maven.md) para importar el proyecto y [diagnóstico y pruebas](../ayuda/diagnostico.md) para trabajar con autonomía. Abre las soluciones en otra carpeta para contrastarlas sin sobrescribir tu trabajo.
