# Componentes e inyección de dependencias

Una aplicación de catálogo necesita buscar libros, comprobar precios y guardar cambios. Podemos separar esas tareas en clases, como hicimos con [repositorios y servicios](../ficheros/arquitectura.md). El siguiente problema es **construir esas clases y conectarlas** sin repetir la misma preparación en cada pantalla.

## Qué es un componente

Un componente ofrece una responsabilidad mediante un contrato. Su código interno queda encapsulado: quien pide una búsqueda necesita conocer sus parámetros y su resultado, no cómo se abre una conexión. Una interfaz de Java expresa ese contrato; una clase lo implementa. No todas las clases necesitan una interfaz adicional: una separación que no facilita una sustitución ni aclara una responsabilidad añade trabajo.

**Composición** significa que un objeto utiliza otros objetos para realizar su tarea. Un servicio de catálogo colabora con un repositorio; no hereda de él. Esto permite cambiar la persistencia conservando las reglas de negocio. Reutilizar un componente requiere también respetar sus condiciones: una operación que espera una transacción abierta no funciona correctamente en cualquier contexto.

Antes de Spring, podríamos conectar objetos a mano. Este fragmento ilustrativo recibe la dependencia en el constructor:

```java
class Buscador {
    private final FuenteTitulos fuente;

    Buscador(FuenteTitulos fuente) {
        this.fuente = fuente;
    }

    List<String> buscar(String texto) {
        return fuente.buscar(texto.strip());
    }
}

interface FuenteTitulos {
    List<String> buscar(String texto);
}
```

`Buscador` conoce `FuenteTitulos`, pero no decide qué implementación recibe. Una prueba puede entregarle una fuente en memoria y la aplicación una fuente de base de datos. La inyección de dependencias ya existe aquí; Spring automatiza la construcción y la coordinación.

## IoC y el contenedor

**Inversión de control (IoC)** significa que la aplicación delega en un contenedor parte de la creación y conexión de objetos. Un objeto administrado por Spring se llama **bean**. Sigue siendo un objeto Java: lo especial es quién lo construye y gestiona. El contenedor habitual es `ApplicationContext`, que conoce los beans disponibles y sus dependencias. Consulta la [introducción oficial a IoC y beans](https://docs.spring.io/spring-framework/reference/6.2/core/beans/introduction.html).

En el arranque, Spring lee la configuración, localiza componentes, resuelve sus constructores y crea los objetos necesarios. Si falta una dependencia o hay dos candidatos sin criterio de elección, el arranque falla con un diagnóstico. La aplicación no debe continuar con un servicio conectado a medias.

Por defecto, un bean tiene alcance *singleton*: se comparte una instancia **por contenedor**, no una instancia universal en toda la JVM. Por eso los servicios no deben almacenar en campos el formulario o el usuario de una petición. Dos personas podrían utilizar el mismo objeto a la vez. Las variables de cada operación pertenecen a sus parámetros y variables locales.

## Anotaciones que expresan responsabilidades

Las siguientes anotaciones permiten detectar clases dentro de los paquetes explorados:

| Anotación | Responsabilidad habitual |
| --- | --- |
| `@Component` | Componente genérico, por ejemplo un observador de eventos. |
| `@Service` | Caso de uso y reglas de negocio. |
| `@Repository` | Persistencia; también participa en la traducción de excepciones de acceso a datos. |
| `@Configuration` | Clase que declara configuración Java y métodos `@Bean`. |

Una entidad `@Entity` representa datos persistentes. **No es un servicio singleton** y no se convierte en bean por esa anotación. Una interfaz Spring Data tampoco necesita una clase propia anotada `@Repository`: Spring registra su implementación generada.

La [detección de componentes](https://docs.spring.io/spring-framework/reference/6.2/core/beans/classpath-scanning.html) depende de los paquetes. La clase principal se sitúa en el paquete raíz del proyecto; así sus subpaquetes quedan incluidos en la búsqueda habitual de Boot.

## Inyección por constructor

La clase declara lo que necesita y Spring se lo proporciona. En este ejemplo reducido, el servicio recibe una fuente; la implementación de esa fuente debe estar registrada como bean:

```java
@Service
class ConsultaTitulos {
    private final FuenteTitulos fuente;

    ConsultaTitulos(FuenteTitulos fuente) {
        this.fuente = fuente;
    }

    List<String> buscar(String texto) {
        return fuente.buscar(texto.strip());
    }
}
```

Con un único constructor no hace falta `@Autowired`. El campo `final` deja visible que la dependencia es obligatoria y no se sustituirá durante la operación. En una prueba ordinaria podemos escribir `new ConsultaTitulos(fuenteFalsa)`; la clase no necesita arrancar Spring para comprobar una regla independiente del contenedor.

No escribimos `new ConsultaTitulos(...)` desde el controlador de la aplicación Spring: recibirlo mediante el contenedor permite que se apliquen también sus servicios, como las transacciones.

## Cuándo usar `@Bean`

Una biblioteca externa puede ofrecer una clase cuyo código no podemos anotar. Un método `@Bean` permite construirla explícitamente. Este ejemplo configura un reloj sustituible, útil para probar operaciones dependientes de la fecha:

```java
@Configuration
class ConfiguracionTiempo {
    @Bean
    Clock reloj() {
        return Clock.systemUTC();
    }
}
```

Un servicio que recibe `Clock` puede usar `LocalDate.now(reloj)`. En una prueba se le entrega `Clock.fixed(...)` y la fecha deja de depender del momento de ejecución. Los nombres `Clock` y `LocalDate` pertenecen a `java.time`. Véase la [configuración Java con `@Bean`](https://docs.spring.io/spring-framework/reference/6.2/core/beans/java/bean-annotation.html).

## Ciclo de vida y dependencias circulares

Tras construir un bean, Spring puede ejecutar inicialización; al cerrar el contexto, puede liberar recursos. `@PostConstruct` y `@PreDestroy` sirven para tareas de ciclo de vida cuando son necesarias. No sustituyen un caso de uso: no debemos introducir datos de ejemplo en cada construcción ni asumir que una transacción del servicio ya está activa en su inicializador.

Si `ServicioA` necesita `ServicioB` y este necesita `ServicioA`, aparece un ciclo. No se corrige ocultándolo con campos inyectados. Hay que revisar las responsabilidades: un coordinador puede depender de ambos, y los dos servicios dejar de depender entre sí. Dibujar las flechas antes de programar ayuda a encontrar ese problema.

En [el proyecto guiado](proyecto.md), identifica quién construye los servicios y qué dependencias aparecen en sus constructores antes de completar el acceso a datos.
