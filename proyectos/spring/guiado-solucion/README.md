# Servicios para el catálogo — guiado-solucion

## Ejecutar desde IntelliJ

Abre esta carpeta Maven, espera la sincronización y selecciona JDK 25. Con Docker Desktop activo, ejecuta `config.MySqlLocal.main`; Working directory debe ser esta carpeta, junto a compose.yaml. Arranca únicamente MySQL y espera SELECT 1. Si la instancia de este proyecto ya ocupa 3309, reutilízala: no arranques otro Compose sobre el mismo puerto.

Ejecuta después `CatalogoApplication.main` en `es.mjsplayground.acda.spring.catalogo`. Abre http://127.0.0.1:8080. La aplicación usa Thymeleaf y controladores MVC ya preparados; el alumnado no cambia HTML/CSS/JavaScript. Run/Debug en IntelliJ es la vía normal; los comandos de este README son alternativas técnicas.

La base predeterminada de esta variante es spring_guiado en MySQL local127.0.0.1:3309. Guiado/final usan bases diferentes en el mismo servidor. El volumen Docker spring-data es persistente; detener la aplicación no borra datos. Hibernate usa update y no hay semilla automática. Preparar demostración es una acción POST confirmada que funciona solo en base vacía; nunca vuelve a sembrar ni sobrescribe cambios al reiniciar.

Variables opcionales: MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD y PORT. Las credenciales acda/acda_local y root/qa_root_local son de desarrollo local explícitas. Compose publica únicamente loopback. No introduzcas claves reales en Git ni reutilices esas cuentas en un servidor expuesto. El script docker/init.sql crea ambos esquemas solo cuando el volumen MySQL está vacío; no es un procedimiento de migración ni de limpieza.

## Dominio y servicios

Se reutiliza un subconjunto de Hibernate: Editorial y Libro, sin arrastrar autores/detalles ni la práctica anterior. Libro: ISBN único de13 dígitos (identificador, sin cálculo de control), título1–120, precio BigDecimal0,01–10000 con hasta2 decimales, existencias0–catalogo.max-stock, editorial obligatoria y @Version. Nombre de editorial1–80 y único. Se recortan extremos; los saltos internos del título/nombre se conservan. No se elimina editorial con libros.

Spring construye los componentes y les proporciona sus dependencias por constructor. CatalogoService coordina LibroRepository, EditorialService y ReglasCatalogo. EditorialService usa su repositorio y verifica referencias. Los repositorios extienden JpaRepository; Spring Data implementa las consultas derivadas por título/editorial. La UI recibe LibroDatos construido dentro del servicio: open-in-view está desactivado y no accede a relaciones LAZY tras cerrar la transacción.

CatalogoConfig registra un Clock UTC mediante @Bean y CatalogoProperties mediante configuración externa. ReglasCatalogo es @Component; las reglas se comprueban otra vez en servicio, aunque el formulario tenga Bean Validation. La colaboración entre servicios no crea dependencias circulares: el servicio de editorial no necesita llamar al de catálogo.

Los métodos de consulta usan @Transactional(readOnly=true); las escrituras declaran @Transactional. La llamada externa al bean pasa por el proxy Spring. Una anotación no crea mágicamente otra transacción al llamar a un método propio con this. Los servicios colaboradores se ejecutan dentro de la transacción existente cuando corresponde; no se capturan y ocultan errores para seguir confirmando una operación inválida.

CambioCatalogo es un evento local. AvisosCatalogo lo recibe con @TransactionalEventListener(AFTER_COMMIT) y llama al contrato Notificador. NotificadorLog escribe un aviso local; NotificadorSilencioso lo omite bajo el perfil silencioso. Se sustituye el componente mediante DI, sin cambiar los servicios ni fingir otra persistencia. Los eventos de una transacción revertida NO producen aviso de confirmación. No se envían correos ni mensajes externos.

## Formularios y navegación

/libros muestra el catálogo y filtros. Nuevo abre un formulario aparte; Guardar correcto vuelve al listado; Cancelar vuelve sin escribir. Un error conserva los campos y muestra mensajes. La versión oculta del libro impide guardar un formulario antiguo sobre una modificación posterior. Eliminar y Preparar demostración requieren confirmación, con Cancelar enfocado y Escape seguro. /editoriales permite mantener el catálogo relacionado.

Los controladores preparados enlazan formularios a solicitudes del servicio, nunca a escrituras directas del repositorio. Thymeleaf escapa el texto y añade el token CSRF a las acciones POST. Las plantillas y estilos tienen tema claro/oscuro, vista móvil y foco de teclado. No se programa un frontend nuevo como tarea del alumnado.

## Propiedades y perfiles

Predeterminado: catalogo.max-stock=1000. Perfil practica: máximo50, para observar una política configurada sin cambiar Java. Perfil silencioso: alternativa de Notificador sin avisos. Perfil diagnostico: DEBUG de la aplicación y SQL, sin habilitar volcado de credenciales/parámetros. Puedes combinarlos en Environment variables de la configuración Run: `SPRING_PROFILES_ACTIVE=practica,silencioso`.

El perfil seguro activa autenticación MVC, con formulario de login estándar de Spring Security. Cuentas locales de ejemplo: lector/lector_local (lectura) y gestor/gestor_local (escritura). Las claves pueden reemplazarse con CATALOGO_LECTOR_PASSWORD/CATALOGO_GESTOR_PASSWORD y se codifican con BCrypt. No son cuentas de producción. Todos los POST requieren CSRF; también se conserva esa protección en modo local sin login. Logout es POST protegido, no un enlace GET.

En el perfil seguro, las páginas requieren autenticación y las escrituras el rol GESTOR; lector no puede escribir aunque manipule el HTML o fabrique una petición con token válido. La protección de la UI acompaña a la del servidor, no la sustituye. Los métodos GET no modifican datos.

## Ampliación ejecutable: pruebas, JAR y Docker

Desde esta carpeta, `.\mvnw.cmd clean test` ejecuta reglas y contextos pequeños de componentes sin MySQL. `.\mvnw.cmd -Dqa.mysql=true test` usa MySQL REAL y prueba CRUD, formularios, perfiles, eventos, versiones, seguridad, CSRF y rollback. Los tests crean esquemas NUEVOS qa_spring_UUID y eliminan solo esos esquemas, con comprobación de nombre. No limpian spring_guiado/spring_final. Variables QA_MYSQL_ADMIN_URL/QA_MYSQL_ADMIN_USER/QA_MYSQL_ADMIN_PASSWORD permiten otra instancia; por defecto root/qa_root_local en3309. Sin la propiedad qa.mysql, esas pruebas se omiten explícitamente.

`.\mvnw.cmd package` genera target/catalogo-guiado-solucion-1.0.0.jar; alternativa de ejecución: `java -jar target/catalogo-guiado-solucion-1.0.0.jar`. Puedes añadir `--spring.profiles.active=seguro`. El JAR contiene aplicación, plantillas y servidor, pero sigue necesitando MySQL. No afirmes que ejecutar tests aislados demuestra navegación real: la entrega se complementa con revisión en navegador.

Para Docker local: genera el JAR anterior y ejecuta `docker compose --profile app up --build -d` desde esta carpeta. Dockerfile usa Eclipse Temurin25 JRE y usuario sin privilegios. La conexión DENTRO del contenedor es mysql:3306/spring_guiado, no localhost:3309. La aplicación queda en http://127.0.0.1:8088. `app` es un perfil de Compose, independiente del perfil Spring `seguro`. En PowerShell puedes definir `$env:SPRING_PROFILES_ACTIVE="seguro"` antes de levantar el perfil app. No hay comandos de push ni publicación de imágenes o de la aplicación.

El Compose tiene el mismo nombre acda-spring en las variantes: usa una a la vez para el servicio app; las bases permanecen separadas. Parar servicios conserva el volumen. No uses opciones que borren volúmenes para repetir un ejercicio. La configuración prepara un entorno de estudio local; no presume despliegue público ni mantenimiento de una instalación real.

Spring Boot3.5.16 gestiona sus versiones compatibles de Hibernate, Spring Data y Spring Security; no se fuerza Hibernate7 del tema anterior. Java25 probado. Esta es la última versión OSS de la rama3.5, fijada para el encargo docente; las referencias técnicas son de esa rama. Documentación: https://docs.spring.io/spring-boot/3.5/system-requirements.html ; https://docs.spring.io/spring-data/jpa/reference/ ; https://docs.spring.io/spring-security/reference/6.5/servlet/authentication/passwords/form.html ; https://docs.spring.io/spring-security/reference/6.5/servlet/exploits/csrf.html .

## Alcance de esta variante

Solución completa del guiado: CRUD y consultas derivadas, validación y coordinación de servicios, eventos tras commit y perfiles. Los tests de integración comprueban el recorrido MVC, CSRF activo por defecto, perfil seguro real y ausencia de semilla repetida.
