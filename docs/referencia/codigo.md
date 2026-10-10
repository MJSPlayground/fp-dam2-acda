# Ejemplos de código

=== "Java"

    ~~~java
    System.out.println("Hola");
    ~~~

=== "Kotlin"

    ~~~kotlin
    println("Hola")
    ~~~

!!! success "Componente común"
    En la web se alternan las pestañas. En el PDF aparecen ambas versiones y sus títulos.

## Explicaciones sobre el propio código

La primera vez que aparece `Path.of`, explicamos antes qué representa una ruta. Después podemos utilizar una **anotación interactiva** para ampliar una línea concreta, sin llenar el ejemplo de comentarios extensos:

```java
import java.nio.file.Path;

Path archivo = Path.of("data", "visitas.csv"); // (1)!
System.out.println(archivo);
```

1. `Path.of` **construye una ruta**, pero no comprueba que el archivo exista ni lo abre. Aquí la ruta es relativa al directorio de trabajo del proceso.

Este recurso es opcional: la explicación imprescindible debe seguir siendo comprensible aunque el estudiante no abra la anotación.
