# Acceso a Datos · 2.º DAM

Web pública de aprendizaje de Acceso a Datos de MJS Playground. [CONTENIDOS_ACDA.md](CONTENIDOS_ACDA.md) es el **índice aprobado** y la referencia de contenidos y metodología. Los seis temas están desarrollados y preparados para revisión docente. Consulta [PLAN-DESARROLLO.md](PLAN-DESARROLLO.md) para el orden de implementación **JDBC → Firestore → MongoDB → Ficheros → Hibernate → Spring**, los hitos, el trabajo directo en `main` y el control de calidad. Cada tema incluye un proyecto guiado incremental y una práctica final con tres niveles, sus starters y sus soluciones públicas; las cinco fichas de repaso por tema permanecen En preparación, sin proyectos opcionales desarrollados.

**Web:** https://mjsplayground.github.io/fp-dam2-acda/

## Requisitos y desarrollo local

- Python 3.11 o posterior y Chromium para Playwright.
- Para los proyectos Java de `proyectos/`: JDK 25 y Maven Wrapper incluido. Cada proyecto documenta sus servicios y opciones de ejecución en su README.

En Windows, desde la raíz:

```powershell
py -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
.\.venv\Scripts\python.exe -m playwright install chromium --only-shell
.\.venv\Scripts\python.exe scripts\export_pdfs.py
.\.venv\Scripts\python.exe scripts\package_projects.py
.\.venv\Scripts\python.exe scripts\package_projects.py --check
.\.venv\Scripts\python.exe scripts\check_site_ui.py
.\.venv\Scripts\python.exe scripts\check_links.py
.\.venv\Scripts\zensical.exe serve
```

Abre http://127.0.0.1:8000/fp-dam2-acda/. Los PDF se generan en `docs/descargas/` y se copian al sitio final en `site/descargas/`.

## Organización

`docs/` contiene la portada, una carpeta por tema (`ficheros`, `hibernate`, `spring`, `jdbc`, `firestore`, `mongodb`) y `ayuda/`. Al desarrollar contenidos, los bloques y páginas se añadirán dentro de su tema siguiendo el índice aprobado y se registrarán en `zensical.toml`. Rutas, paquetes Java y PDF usarán identificadores temáticos estables.

Las páginas históricas de Ficheros se conservan en `referencias/ficheros-historico/`, fuera de la publicación y del buscador. Los proyectos históricos permanecen en `proyectos/ficheros/` como fuente de componentes reutilizables. No se incluyen evaluaciones reservadas, datos del alumnado ni credenciales.

Los proyectos publicados de cada tema se organizan en `guiado-inicial`, `guiado-solucion`, `final-inicial` y `final-solucion`. El [registro de desarrollo](ESTADO-DESARROLLO.md) identifica los temas disponibles, commits, pruebas reales y límites pendientes. Los ZIP individuales se generan en `site/descargas/codigo/`; para probarlos desde carpetas limpias, ejecuta `scripts/test_project_packages.py` con el Python del entorno.

`scripts/export_pdfs.py` genera los PDF de cada sección y el temario completo. `scripts/check_site_ui.py` comprueba navegación, recursos y descargas con Chromium. Cada push a `main` puede validar y publicar el sitio en GitHub Pages. **El docente ha autorizado el rediseño directamente en `main`**, con commits y pushes frecuentes por hitos completos, validación previa y comprobación del despliegue. La revisión pedagógica se realizará sobre la web publicada antes de difundirla al alumnado. El workflow también admite ejecución manual.

Después de que Actions termine de desplegar, `scripts/check_published_site.py` comprueba la portada y las páginas de navegación en GitHub Pages, descarga e inspecciona los ZIP enlazados y abre los PDF publicados. Se ejecuta con el Python del entorno; `--topic hibernate` limita el recorrido a un tema y, sin ese argumento, recorre todos. Esta comprobación de la web pública se realiza después del despliegue, fuera del trabajo de construcción de CI.

Los apuntes originales se publicarán con CC BY-NC-ND 4.0 como política por defecto; el código didáctico tendrá sus permisos propios cuando se incorpore.
