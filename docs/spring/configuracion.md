# Spring Boot y configuración

**Spring Framework** proporciona el contenedor, la inyección de dependencias, las transacciones y otras herramientas. **Spring Boot** prepara una aplicación basada en ese ecosistema: selecciona dependencias compatibles, aplica configuración según las bibliotecas y propiedades disponibles y permite iniciar un servidor web integrado. Boot utiliza Spring; no reemplaza sus conceptos.

## Qué aporta un starter

Un *starter* es una dependencia Maven que agrupa bibliotecas para una tarea. El de Spring Data JPA reúne persistencia JPA; el de web incorpora el soporte de peticiones y el servidor; Thymeleaf genera las páginas de la interfaz preparada. No debemos añadir versiones independientes de cada biblioteca copiadas de tutoriales de otras generaciones.

En el proyecto descargable, revisa `pom.xml` y conserva sus versiones comprobadas. Los paquetes JPA y de validación utilizan `jakarta.*`; no mezcles imports antiguos `javax.persistence` con estas dependencias. La [estructura de una aplicación Boot](https://docs.spring.io/spring-boot/3.5/reference/using/structuring-your-code.html) explica la organización recomendada.

## La clase principal

Una aplicación Boot tiene un `main`. Este ejemplo esquemático muestra la estructura; la guía identifica el nombre real del proyecto:

```java
@SpringBootApplication
public class CatalogoApplication {
    public static void main(String[] args) {
        SpringApplication.run(CatalogoApplication.class, args);
    }
}
```

`@SpringBootApplication` agrupa configuración, búsqueda de componentes y autoconfiguración. `run` crea el contexto y arranca el servidor. La aplicación permanece ejecutándose para atender al navegador; cerrar la pestaña no detiene el servidor.

La **autoconfiguración** toma decisiones condicionadas por el entorno. Con JPA, un controlador MySQL y las propiedades de conexión, Boot puede preparar el acceso a datos y la gestión de transacciones. No inventa la contraseña ni las reglas del catálogo. Si declaramos determinada configuración propia, algunas decisiones automáticas dejan de aplicarse. El informe de condiciones con `debug=true` ayuda a saber por qué una configuración se activó; no es necesario leerlo entero para empezar.

## Abrir y ejecutar

Importa el proyecto Maven en IntelliJ siguiendo [la ayuda de proyectos](../ayuda/proyectos-maven.md), selecciona el JDK indicado en su README y espera a que se resuelvan las dependencias. Prepara MySQL con los archivos del proyecto y [la ayuda de contenedores](../ayuda/contenedores.md). Ejecuta el `main` con el triángulo **Run** y abre la dirección de la guía.

El mensaje de servidor iniciado confirma que la aplicación escucha peticiones. Comprueba también que puede consultar su base de datos: iniciar un proceso no demuestra que todas sus operaciones estén implementadas. Un puerto ocupado indica que otro proceso ya está escuchando allí; detén la ejecución anterior o cambia el puerto de forma coherente.

## Propiedades y configuración externa

`src/main/resources/application.properties` contiene valores de configuración. El proyecto utiliza estas variables. Se abrevia únicamente la URL; consulta sus parámetros completos en el archivo:

```properties
server.port=${PORT:8080}
spring.datasource.url=${MYSQL_URL:jdbc:mysql://127.0.0.1:3309/spring_guiado}
spring.datasource.username=${MYSQL_USER:acda}
spring.datasource.password=${MYSQL_PASSWORD:acda_local}
spring.jpa.open-in-view=false
```

`${MYSQL_USER:acda}` toma la variable de entorno `MYSQL_USER`, y utiliza `acda` si no existe. Así el mismo programa puede ejecutarse en entornos diferentes sin editar sus clases. Los valores de demostración locales no son credenciales para un servidor público. No subas contraseñas reales al repositorio.

`open-in-view=false` evita mantener disponible el contexto de persistencia durante el renderizado de la página. El servicio debe preparar los datos que necesita la vista dentro de su operación; si esta intenta recorrer una relación perezosa ya cerrada, el error revela que faltan datos en la salida del servicio. Recupera [el alcance de las sesiones y las relaciones](../hibernate/relaciones.md).

La [configuración externa de Boot](https://docs.spring.io/spring-boot/3.5/reference/features/external-config.html) detalla la prioridad de propiedades. Si cambias un archivo y no observas el resultado, comprueba si una variable de entorno o un argumento de ejecución lo está sobrescribiendo.

## Perfiles

Un perfil agrupa configuración para una situación: desarrollo, pruebas o una demostración de seguridad. `application-practica.properties` se aplica al activar `practica`, además de las propiedades generales. El perfil no crea por sí solo una base de datos aislada ni convierte una aplicación insegura en segura.

Desde IntelliJ, un argumento de programa como este activa un perfil:

```text
--spring.profiles.active=practica
```

También puede usarse la variable `SPRING_PROFILES_ACTIVE`. En el proyecto, `practica` cambia el máximo de stock a 50; `diagnostico` aumenta el detalle del logging. El perfil `silencioso` sustituye la notificación y `seguro` activa usuarios y permisos. Un componente con `@Profile("silencioso")` solo se registra con ese perfil activo. Conviene comprobar los perfiles anunciados al arrancar antes de interpretar una prueba. Consulta [Profiles](https://docs.spring.io/spring-boot/3.5/reference/features/profiles.html).

No cambies `ddl-auto` a `create` para resolver un fallo de conexión: ese ajuste puede recrear las tablas y perder datos. Usa el esquema y las instrucciones del README. La configuración de pruebas debe señalar a una base distinta de la aplicación.

## Diagnóstico de arranque

| Síntoma | Qué comprobar |
| --- | --- |
| Falta un bean | Anotación, paquete explorado y perfil activo. |
| Hay varios candidatos | Qué implementación corresponde; `@Qualifier` o `@Primary` solo si la elección está justificada. |
| No conecta con MySQL | Contenedor listo, puerto, base y credenciales. |
| No encuentra una propiedad | Nombre de variable y configuración externa efectiva. |
| No puede iniciar el servidor | Puerto ocupado o error anterior del contexto. |

Lee la primera causa útil de la traza, sin quedarte únicamente con el último mensaje de apagado. La [ayuda de diagnóstico](../ayuda/diagnostico.md) explica cómo inspeccionar excepciones, logging y breakpoints.
