# Estado del desarrollo

## Encargo y punto actual

Desarrollo integral autorizado en main. Orden: JDBC → Firestore → MongoDB → Ficheros → Hibernate → Spring.

- Repositorios del módulo y AI Workspace sincronizados mediante `git pull --ff-only`.
- índice, plan, configuración y canalización inspeccionados.
- Se reutilizan identidad visual, Zensical, exportador PDF, controles de navegación y Maven Wrapper.
- Material histórico de Ficheros conservado en Git y retirado de navegación; seis temas visibles como En construcción.
- JDBC: producción de teoría y proyecto guiado en curso; aún no terminado.

## Validación y siguiente paso

Java disponible: Temurin 25.0.4.1; Python 3.13.7 y dependencias Zensical/Playwright/pypdf disponibles.
Hito de navegación: exportación de 7 PDF de sección y uno global; `scripts/check_site_ui.py` superado con escritorio/móvil, claro/oscuro, teclado y descargas; `scripts/test_project_packages.py` superado para las dos descargas históricas desde carpetas temporales; `scripts/check_links.py` superado (43 páginas, 104 enlaces, incluyendo páginas JDBC en preparación). Inspeccionadas captura real de Ficheros en escritorio y página renderizada de su PDF.

Pendiente: validación completa del proyecto JDBC y práctica final, PDF JDBC con teoría integrada, publicación y comprobación Actions/Pages.
Siguiente paso: terminar conexiones, CRUD, servicios y venta atómica de Gestión de almacén; después práctica final y cierre completo de JDBC antes de Firestore.

No se declara terminado ni aprobado pedagógicamente ningún tema nuevo.
