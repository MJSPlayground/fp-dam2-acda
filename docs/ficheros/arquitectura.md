# Repositorios, servicios, errores y logging

La interfaz preparada permite abrir un formulario o elegir un archivo, pero no debería decidir cómo leer CSV ni cuándo aceptar un stock. Separar esas responsabilidades ayuda a entender el programa y a comprobar sus reglas sin depender de la ventana.

## Las capas, con un ejemplo

```text
Usuario selecciona un archivo
            ↓
Interfaz JavaFX: recoge la ruta y presenta mensajes
            ↓
InventarioService: valida el catálogo candidato y decide si sustituir
            ↓
Repositorio CSV / JSON / binario: lectura y conversión
            ↓
Archivo externo
```

`model.entity` contiene los datos de producto. `model.repository` contiene el acceso técnico a cada formato. `model.service` contiene los casos de uso y sus reglas. `config` prepara colaboradores como los repositorios y los filtros. `app` proporciona la clase que se ejecuta.

No hay un servidor de aplicaciones ni hace falta aprender Spring o JPA para esta separación. Son clases Java que se construyen y colaboran mediante llamadas normales.

## Un contrato común para los formatos

CSV, JSON y binario tienen detalles técnicos distintos, pero el servicio necesita operaciones parecidas: leer una lista candidata y guardar una lista. Una interfaz puede expresar ese contrato:

```java
public interface InventarioRepository {
    List<Producto> leer(Path archivo) throws IOException;
    void guardar(Path archivo, List<Producto> productos) throws IOException;
}
```

Cada implementación conoce su formato. El servicio selecciona el repositorio adecuado y aplica las mismas reglas a sus productos. No debemos repetir validación de códigos o precios dentro de cada botón.

En otros proyectos encontrarás el nombre DAO para organizar acceso a datos; sigue siendo válido. Aquí utilizamos Repository de forma consistente, sin convertir el cambio de nombre en una arquitectura más compleja de lo necesario.

## La entidad no valida por aparecer en una tabla

Un `Producto` guarda código, nombre, unidades y precio. Sus métodos de acceso permiten consultar esos valores. Que exista un objeto no demuestra que represente un producto aceptable: podría contener nombre vacío o precio negativo.

El servicio comprueba las invariantes antes de aceptar un alta o un catálogo. También protege la identidad: editar un producto no permite cambiar su código como si fuese un campo más. Para introducir otro código se crea otro producto, conservando reglas claras de duplicados.

La interfaz puede advertir inmediatamente de texto vacío o de una cantidad no entera. El servicio vuelve a validar los datos porque también puede recibir llamadas de pruebas o de otro punto del programa.

### Comprobar importes con BigDecimal

[`BigDecimal`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html) representa decimales. `signum()` indica si el valor es negativo, cero o positivo; `compareTo` compara numéricamente y devuelve un número negativo, cero o positivo. No se comparan importes con `==`.

```java
BigDecimal precio = new BigDecimal("4.50");
boolean positivo = precio.signum() > 0;
boolean dentroDelMaximo = precio.compareTo(new BigDecimal("1000000")) <= 0;
int decimales = precio.scale();
```

Aquí las dos condiciones son verdaderas y la escala es dos. `new BigDecimal("4.5").compareTo(precio)` devuelve cero: representan el mismo valor numérico aunque sus escalas sean distintas. **Escala** no significa precisión total: cuenta la posición decimal de la representación; la precisión cuenta sus dígitos significativos.

El contrato del inventario rechaza una escala mayor que dos, sin redondear la entrada. Después de validarla, `setScale(2)` normaliza un importe como `4.5` a `4.50`. No se usa ese método para ocultar una entrada como `4.567`: debe rechazarse antes. La validación también comprueba que el valor sea positivo y no supere el máximo.

## Sustituir solo después de comprobarlo todo

La carga del guiado es una operación de sustitución completa. Su orden conceptual es:

1. Leer el archivo y construir candidatos.
2. Comprobar cada producto y la unicidad de códigos.
3. Preparar la nueva lista.
4. Sustituir el estado en memoria únicamente si todo ha terminado correctamente.

Si se sustituyese dentro del bucle, un error al final dejaría el catálogo parcialmente cargado. Una lista temporal permite preservar el estado anterior sin necesitar una base de datos.

Guardar un archivo y modificar la tabla tampoco son la misma operación. Los cambios del catálogo viven en memoria hasta que se exportan; volver a abrir la aplicación no debe presentarse como recuperación automática de un archivo si no se ha guardado y elegido su carga.

## Errores con contexto y causa

«Código repetido» es un incumplimiento de negocio. «No se puede abrir la ruta» es un problema técnico de entrada/salida. La aplicación debe comunicar ambos con claridad sin confundirlos con un catálogo vacío.

Una excepción propia puede conservar el mensaje y la causa original:

```java
public class InventarioException extends RuntimeException {
    public InventarioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
```

Si falla una lectura, el servicio puede convertir `IOException` a un error del caso de uso:

```java
catch (IOException error) {
    throw new InventarioException("No se pudo cargar el catálogo seleccionado", error);
}
```

La causa permite investigar si faltaba un archivo, se denegó acceso o el contenido era inválido. Copiar solo `error.getMessage()` perdería parte de esa información. El proyecto añade también un constructor de mensaje para errores de negocio que no envuelven un fallo técnico.

No captures cualquier excepción para continuar como si se hubiese guardado. En la UI, un error mantiene el formulario o el catálogo para que el usuario pueda corregir y volver a intentarlo.

## SLF4J y Log4j2 desde el primer proyecto

SLF4J ofrece la API de logging; Log4j2 escribe los eventos según la configuración preparada. Un logger pertenece a una clase y recibe mensajes con nivel. Este ejemplo registra un hecho después de completar la carga:

```java
private static final Logger log = LoggerFactory.getLogger(InventarioService.class);

log.info("Catálogo cargado: {} productos", productos.size());
```

Las llaves son marcadores para parámetros. No debemos anunciar la carga antes de validarla y aplicarla. La [guía de SLF4J](https://www.slf4j.org/manual.html) explica este uso y su relación con la implementación de logging.

| Nivel | Uso razonable |
| --- | --- |
| `DEBUG` | Detalles de diagnóstico que no se necesitan siempre |
| `INFO` | Operación relevante completada |
| `WARN` | Situación recuperable que merece atención |
| `ERROR` | Fallo técnico que impide la operación |

Al registrar un fallo técnico, pasar la excepción conserva su stack trace:

```java
log.error("No se pudo cargar el catálogo seleccionado", error);
```

La UI presenta un mensaje breve; el registro contiene el detalle para investigar. No se imprimen datos sensibles ni todo el catálogo sin necesidad. Evita registrar el mismo fallo en todas las capas y duplicar tres veces la misma traza.

El proyecto incluye dependencias y `src/main/resources/log4j2.xml`; no tienes que inventar la configuración para empezar. Consulta la [configuración de Log4j2](https://logging.apache.org/log4j/2.x/manual/configuration.html) y [diagnóstico y pruebas](../ayuda/diagnostico.md) cuando necesites seguir un error.

## Maven: dependencias sin copiar bibliotecas a mano

El `pom.xml` declara las bibliotecas usadas por el proyecto, entre ellas Commons CSV, Jackson y logging. IntelliJ importa el proyecto Maven y descarga esas dependencias. El código no encuentra `ObjectMapper` porque se copie su `.java`, sino porque está disponible la biblioteca correspondiente.

Maven Wrapper permite reproducir compilación y tests sin exigir Maven global. El recorrido del alumnado utiliza Run y la ventana Maven de IntelliJ; los comandos para ejecutar pruebas aparecen como alternativa en el README. La [guía de proyectos Maven](../ayuda/proyectos-maven.md) explica esa preparación.

## Comprobar capas, no solo compilación

Una prueba de servicio puede intentar un duplicado y comprobar que la lista no cambió. Otra puede inyectar un repositorio que falla y comprobar que el estado anterior permanece y la causa se conserva. Una prueba real de repositorio comprueba archivos, codificación y formatos; una de UI observa formularios, mensajes y selección.

La [guía del proyecto](proyecto.md) relaciona esos niveles con los TODO. No se considera validada una carga porque el código compila ni una interfaz porque sus servicios pasen tests.

## Un orden de implementación que permite comprobar cada capa

No completes a la vez todos los repositorios y el servicio. En el guiado, primero verifica `ArchivosRepository.listar`; después prueba el lector y escritor CSV por separado; luego incorpora alta/edición y carga completa en el servicio. JSON y binario reutilizan ese caso de uso una vez comprobados sus repositorios. Las fases `rutas`, `csv-lectura`, `csv-escritura`, `crud` y `carga` están preparadas en [la guía](proyecto.md#comprobaciones-optativas-por-fase).

Para depurar, coloca un punto de interrupción antes de sustituir la lista actual. Si la conversión falla, el flujo no debe llegar allí; si el stock es negativo, debe detenerse durante la validación del candidato. Un resultado vacío válido no se distingue de un fallo si se captura cualquier excepción y se devuelve `List.of()`: conserva la causa y deja que el servicio comunique el rechazo.
