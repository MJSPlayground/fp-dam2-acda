# Plan de desarrollo integral de ACDA

> **Estado:** encargo integral aprobado para ejecución asistida por Minion. Este archivo describe la prioridad de producción; el alcance y los contenidos siguen definidos en `CONTENIDOS_ACDA.md`.
>
> **Regla:** desarrollar y comprobar **un tema completo cada vez**. No es necesario pedir nuevas órdenes entre temas si se cumplen sus controles de calidad; el orden es obligatorio salvo bloqueo documentado.

## 1. Secuencia de implementación

| Orden de producción | Referencia docente interna | Carpeta web | Resultado |
| --- | --- | --- | --- |
| 1 | UT5 · JDBC | `docs/jdbc/` | Tema completo, proyecto guiado incremental, práctica final Base/Aplicación/Reto avanzado, solución, Ampliación y repaso pendiente |
| 2 | UT6 · Cloud Firestore | `docs/firestore/` | Ídem, con API REST y colección Postman importable y validada |
| 3 | UT7 · MongoDB | `docs/mongodb/` | Ídem, reaprovechando conocimientos Spring/REST |
| 4 | UT1 · Ficheros | `docs/ficheros/` | Revisión y sustitución gradual del material histórico según el índice nuevo |
| 5 | UT2 · Hibernate | `docs/hibernate/` | Tema completo con relaciones 1:1, 1:N, M:N, transacciones y HQL básico |
| 6 | UT3 · Spring | `docs/spring/` | Tema completo de componentes y Spring Data JPA |

Las carpetas, los nombres de los temas en la navegación y los enlaces públicos **no contienen identificadores UT**; los códigos docentes aparecen solo en este plan interno y documentos de trazabilidad.

## 2. Estado visible de la web

- Desde el inicio, conservar las seis entradas de temas y la sección de Ayuda, con estilo, navegación, colores y PDF compatibles con el sitio actual.
- Las páginas de temas aún no rediseñados mostrarán una **página honesta «En construcción»**, con el título del tema y un mensaje breve; no aparentar contenido nuevo ya validado. Los materiales históricos existentes se pueden conservar en Git como referencia y respaldo, sin enlazarlos como temario vigente si son contradictorios.
- En cada tema, crear `Prácticas de repaso` y cinco fichas `En preparación`: no inventar trabajos completos, ZIPs ni soluciones.
- Las páginas obligatorias de **Ampliación** previstas en el índice **sí** deben producirse con teoría real, ejemplos comprobables y recursos justificados; no confundirlas con los cinco proyectos de repaso aplazados.
- Para una página parcialmente construida, no mostrar enlaces de descarga falsos: mantenerla fuera de navegación de publicación hasta completar y verificar.

## 3. Definición de «tema terminado»

Un tema queda `listo para revisión` únicamente si dispone de:

1. Teoría suficiente y progresiva de todos sus bloques, con ilustraciones útiles, ejemplos comprobados y enlaces cruzados pertinentes.
2. **Un único proyecto incremental** con interfaz completa, punto de entrada, datos, proyecto inicial ejecutable y TODOs graduados a lo largo de las páginas; las instrucciones indican el resultado observable de cada etapa.
3. Solución completa del proyecto guiado, compilable, ejecutable y enlazada desde el contenido.
4. **Una práctica final** que parte de la solución del guiado y añade requisitos nuevos en tres niveles: `Base`, `Aplicación` y `Reto avanzado`. El starter no revela el algoritmo; la interfaz de las nuevas funciones ya está implementada.
5. Solución completa y verificada de los tres niveles de la práctica final, separada del starter, con datos de prueba y resultados.
6. Bloque `Ampliación` con los contenidos expresamente aprobados.
7. Bloque `Prácticas de repaso` con exactamente cinco fichas `En preparación`, hasta una fase posterior.
8. Todas las descargas, rutas internas, tests, casos límite, scripts y PDF verificables. Si no es posible ejecutar una comprobación, registrarlo como pendiente sin darla por hecha.

Todas las interfaces serán **JavaFX o web preparadas**, sin trabajo de frontend por parte del alumnado. Los controladores REST sí podrán requerir implementación por el alumno, porque forman parte del backend estudiado.

## 4. Flujo recomendado para Minion

1. Obtener la última versión de `main`, leer `CONTENIDOS_ACDA.md`, `AGENTS.md`, el estándar y skills aplicables de `ai-workspace`, e inspeccionar la configuración y recursos existentes. Trabajar en rama propia, por ejemplo `desarrollo/acda-temario-integral`.
2. Actualizar navegación y placeholders **sin destruir** fuentes históricas útiles ni deshacer configuraciones correctas. Revisar visualmente el resultado local.
3. Tomar el siguiente tema en el orden indicado. Diseñar un mapa breve `conceptos → etapas del proyecto → TODOs → pruebas → funcionalidades de práctica final`, de forma que **el código enseñado sea el código que realmente se usa**.
4. Implementar por unidades pequeñas pero **completas** (página de teoría + ejemplo + vínculo válido; etapa guiada + código inicial/solución + comprobación; página de práctica final + descargables y soluciones). Confirmar cada unidad con commit y push a la rama al superar verificaciones aplicables. Evitar commits de esqueletos rotos o trabajo desconectado del contenido.
5. Finalizar el tema con compilación/ejecución limpia, tests de servicios y errores, UI, navegación, enlaces, descargas, ZIP, PDF y registro de resultados. Marcarlo listo para revisión; pasar autónomamente al siguiente tema si no hay bloqueo grave.
6. Mantener registro de progreso versionado (se permite `ESTADO-DESARROLLO.md` en la rama) con tema actual, páginas/recursos listos, SHA de los últimos commits, pruebas reales y pendientes. Al interrumpirse el contexto o la sesión, **el siguiente agente debe poder continuar desde la rama sin rehacer pasos**.
7. Al concluir los seis temas, emitir informe final y dejar la rama lista para revisión y autorización docente. **No fusionar automáticamente en `main`**.

## 5. Validación técnica, didáctica y visual

- Reutilizar la canalización `.github/workflows/validate.yml` y los scripts `scripts/check_links.py`, `scripts/check_site_ui.py`, `scripts/export_pdfs.py`, `scripts/package_projects.py` y `scripts/test_project_packages.py`; ampliar controles si el nuevo formato los requiere. Ajustar expectativas históricas de «una guiada por bloque / tres finales» antes de usar QA.
- Ejecutar Java 25/Maven Wrapper desde **copias limpias** del starter y de las soluciones, con versiones compatibles comprobadas. No comprometer secretos ni `target/` y no empaquetar recursos ajenos.
- Probar reglas de negocio **en servicios**, excepciones propias cuando correspondan, propagación de causas técnicas, transacciones/rollback, mensajes y estados de UI. Para API REST, verificar 2xx/4xx/5xx adecuados, errores estructurados y colección Postman en especial para Firestore.
- Inspeccionar en navegador local Zensical: navegación, responsive, claro/oscuro, figuras, recursos de descarga y funcionamiento de los enlaces. Comprobar PDF renderizado e identificar capturas reales pendientes en `CAPTURAS-PENDIENTES.md`.
- La URL de GitHub Pages **solo refleja lo que esté en `main`**: úsala para revisar el estado público actual o tras integración docente, pero no para validar cambios exclusivos de la rama. Para previsualizar cambios, usar sitio local o artefacto de CI/preview de la rama comprobado.
- Proponer mejoras de UX compatibles con los estilos existentes; evitar rediseñar todo por estética o introducir dependencias innecesarias.

## 6. Commits, límites y reanudación

- Commits con nombres reconocibles, por ejemplo `docs(jdbc): explicar conexiones y preparar ejemplo`, `feat(jdbc): implementar repositorio y pruebas de CRUD`, `docs(firestore): incorporar guía REST y colección Postman`.
- **Preferir commits pequeños, con pruebas y contenidos terminados**, antes de agotar el contexto. Hacer `push` después de cada hito autónomo; no acumular grandes lotes sin subir.
- Nunca declarar «funciona» solo por la compilación de la web: contrastar starter, solución, UI y el comportamiento real.
- Si una API externa, licencia o entorno impiden una verificación, registrar la limitación exacta y continuar con partes independientes sin fingir disponibilidad.
- Las tareas evaluables, enunciados Moodle, exámenes y rúbricas están fuera de este alcance. Los 30 proyectos opcionales de repaso **no se desarrollan** en esta entrega.

## 7. Publicación y revisión humana

El repositorio actual publica `main` mediante GitHub Pages. La ejecución de Minion puede ser autónoma en su rama y producir seis temas completos, pero su incorporación a `main` y la difusión pública de nuevos materiales requieren revisión y aprobación humana. Mientras tanto, no alterar la web pública por error ni confundir una previsualización con una publicación autorizada.
