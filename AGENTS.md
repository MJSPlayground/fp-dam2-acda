# AGENTS.md — DAM2 · Acceso a Datos

## Fuentes y prioridades

1. `CONTENIDOS_ACDA.md` (raíz): **índice y metodología aprobados**. Su alcance, orden de bloques y reglas específicas de ACDA prevalecen sobre materiales antiguos.
2. `PLAN-DESARROLLO.md`: prioridad actual, orden de ejecución, estrategia de commits, entrega y validación del rediseño integral.
3. `MJSPlayground/ai-workspace/TEACHING-STANDARD.md` y skills docentes actualizadas: estándares generales y procedimientos de desarrollo. Si una norma general contradice el índice ACDA, aplicar la excepción explícita de ACDA sin modificar otros módulos.
4. `zensical.toml`, proyectos, scripts, tests y contenidos del repositorio: configuración y componentes **existentes** que se deben preservar y mejorar de forma compatible.
5. Hoja de presentación y programación: referencia de tiempos y trazabilidad RA/CE. Los PDF de teoría históricos solo son fuentes secundarias, nunca sustituyen al índice vigente.

No ocultar desacuerdos técnicos o curriculares: registrarlos para revisión docente, sin reescribir el temario acordado.

## Modelo de enseñanza obligatorio

- La web y sus PDF muestran **títulos de temas**; no UT, números, bloques de intermodulares ni referencias a exámenes. Los identificadores internos de planificación sí pueden conservar la correspondencia docente.
- Cada tema tiene **un proyecto guiado incremental** integrado en la progresión de la teoría, no un codelab aislado por bloque. Se parte de frontend preparado, starter ejecutable y pasos de backend `TODO` verificables.
- Al terminar: **una práctica final autónoma del mismo proyecto**, con niveles `Base`, `Aplicación` y `Reto avanzado`. Su starter procede de la **solución completa del proyecto guiado**, sin resolver sus nuevos requisitos. Publicar la solución completa de los tres niveles.
- Una sección `Prácticas de repaso` por tema, con **cinco fichas «En preparación»** hasta que se desarrollen realmente. No inventar descargas, enunciados ni soluciones.
- Una sección `Ampliación` por tema, sin el adjetivo «profesional» ni afirmaciones públicas sobre los exámenes.
- No crear tareas evaluables de Moodle, exámenes, rúbricas ni soluciones docentes privadas en este repositorio.

## Arquitectura y calidad funcional

- Reutilizar nomenclatura `repository` desde Ficheros; `DAO` no está obsoleto, pero reservar esa explicación para el contexto. Mantener responsabilidades claras: repositorio = persistencia; servicio = validaciones, casos de uso, reglas de negocio y coordinación; UI/controlador = presentación y entrada. No saltarse el servicio.
- Introducir de forma progresiva errores de dominio bien expresados, excepciones propias cuando aporten valor, conservación de la causa de los fallos técnicos, SLF4J/Log4j2 desde Ficheros, pruebas de casos límite y ausencia de `sout` como logging habitual.
- En REST: códigos HTTP coherentes, errores estructurados y `@RestControllerAdvice` cuando se justifique. Para Firestore, colección Postman **importable y preparada íntegramente** para probar éxitos y errores; nunca subir credenciales.
- **El frontend se entrega terminado** (JavaFX o web). El alumnado no edita FXML, Thymeleaf, HTML, CSS o JavaScript; sí implementa backend, servicios, repositorios, entidades y endpoints REST que se enseñen.
- Java 25 como versión objetivo **sujeta a prueba de compatibilidad**, Maven Wrapper, ejecución sencilla desde IntelliJ, datos reproducibles, ZIPs limpios y soluciones públicas separadas del starter.
- No declarar completo un tema sin comprobar teoría, código inicial/solución, escenarios de éxito y error, UX gráfica, navegación, enlaces/descargas, compilación y exportación PDF.

## Desarrollo incremental, Git y publicación

- Seguir la secuencia y las puertas de calidad de `PLAN-DESARROLLO.md`. Se autoriza un **encargo integral** de los seis temas, pero cada tema se completa y verifica antes de avanzar al siguiente.
- Hacer **commits y push frecuentes directamente en `main`**, por páginas, funcionalidades o contenidos completos y coherentes, con código, enlaces y pruebas relacionados cuando proceda. No acumular cambios locales sin subir ni confirmar páginas con recursos rotos o lógica engañosa.
- **Autorización expresa del docente para este rediseño (2026-11-07): trabajar y hacer `push` directamente en `main`, que publica GitHub Pages.** La web aún no se usa con el alumnado y el docente revisará los resultados en producción. Se permiten publicaciones iterativas de hitos coherentes, sin aprobaciones intermedias; los temas y recursos incompletos deben mostrar «En construcción» o mantenerse fuera de navegación. No introducir enlaces rotos ni material no evaluable presentado como finalizado. La validación automática no sustituye la revisión pedagógica posterior del docente.
- Validar localmente páginas, PDF, aplicaciones y recursos **antes** de cada `push` a `main`; después comprobar GitHub Actions y revisar el sitio real de GitHub Pages publicado. Si un despliegue falla, corregir o revertir mediante nuevos commits, **nunca `force push`**.
- Mantener un registro de incidencias y capturas reales pendientes en `CAPTURAS-PENDIENTES.md` o informe asociado. Nunca inventar capturas, resultados, enlaces de documentación o pruebas ejecutadas.

## Revisión crítica

Revisar la experiencia desde un alumno que empieza: explicar conceptos antes del código, guiar la importación/ejecución desde IntelliJ, enlazar conocimientos previos y hacer visible qué está resuelto, qué debe completar y cómo comprobarlo. Señalar errores o sobreingeniería con alternativas justificadas; respetar el alcance aprobado. Dejar cada entrega técnica lista para **revisión humana**, no declararla aprobada unilateralmente.
