# Acceso a Datos · 2.º DAM

Web pública de aprendizaje de Acceso a Datos de MJS Playground. [CONTENIDOS_ACDA.md](CONTENIDOS_ACDA.md) es el **índice aprobado** y la referencia de contenidos y metodología. El sitio está en proceso de revisión: el material histórico publicado puede no coincidir todavía con el nuevo índice. Consulta [PLAN-DESARROLLO.md](PLAN-DESARROLLO.md) para el orden de implementación **JDBC → Firestore → MongoDB → Ficheros → Hibernate → Spring**, los hitos, el trabajo directo en `main` y el control de calidad. Cada tema tendrá un proyecto guiado incremental y una práctica final con tres niveles; los cinco proyectos opcionales de repaso por tema quedan pendientes.

**Web:** https://mjsplayground.github.io/fp-dam2-acda/

## Requisitos y desarrollo local

- Python 3.11 o posterior y Chromium para Playwright.
- Para los proyectos Java de `proyectos/ficheros/`: JDK 25 y Maven Wrapper incluido.

En Windows, desde la raíz:

```powershell
py -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
.\.venv\Scripts\python.exe -m playwright install chromium --only-shell
.\.venv\Scripts\python.exe scripts\export_pdfs.py
.\.venv\Scripts\python.exe scripts\check_site_ui.py
.\.venv\Scripts\python.exe scripts\check_links.py
.\.venv\Scripts\zensical.exe serve
```

Abre http://127.0.0.1:8000/fp-dam2-acda/. Los PDF se generan en `docs/descargas/` y se copian al sitio final en `site/descargas/`.

## Organización

`docs/` contiene la portada, una carpeta por tema (`ficheros`, `hibernate`, `spring`, `jdbc`, `firestore`, `mongodb`) y `ayuda/`. Al desarrollar contenidos, los bloques y páginas se añadirán dentro de su tema siguiendo el índice aprobado y se registrarán en `zensical.toml`. Rutas, paquetes Java y PDF usarán identificadores temáticos estables.

Los proyectos del tema histórico de ficheros están en `proyectos/ficheros/` y serán adaptados al nuevo alcance cuando corresponda. No se incluyen evaluaciones reservadas, datos del alumnado ni credenciales.

`scripts/export_pdfs.py` genera los PDF de cada sección y el temario completo. `scripts/check_site_ui.py` comprueba navegación, recursos y descargas con Chromium. Cada push a `main` puede validar y publicar el sitio en GitHub Pages. **El docente ha autorizado el rediseño directamente en `main`**, con commits y pushes frecuentes por hitos completos, validación previa y comprobación del despliegue. La revisión pedagógica se realizará sobre la web publicada antes de difundirla al alumnado. El workflow también admite ejecución manual.

Los apuntes originales se publicarán con CC BY-NC-ND 4.0 como política por defecto; el código didáctico tendrá sus permisos propios cuando se incorpore.
