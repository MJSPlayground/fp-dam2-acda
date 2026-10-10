# Acceso a Datos · 2.º DAM

Web pública de aprendizaje de Acceso a Datos de MJS Playground. [CONTENIDOS_ACDA.md](CONTENIDOS_ACDA.md) es el índice aprobado. El tema **Manejo de ficheros y directorios** contiene los seis bloques, prácticas guiadas y autónomas, tres prácticas finales, ampliaciones y proyectos Java; los demás temas conservan sus introducciones.

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

Los proyectos inicial y solución pública del tema de ficheros están en `proyectos/ficheros/`. No se incluyen evaluaciones reservadas, datos del alumnado ni credenciales.

`scripts/export_pdfs.py` genera los PDF de cada sección y el temario completo. `scripts/check_site_ui.py` comprueba navegación, recursos y descargas con Chromium. Cada push a `main` valida el sitio y lo publica en GitHub Pages si pasan las pruebas. El workflow también admite ejecución manual.

Los apuntes originales se publicarán con CC BY-NC-ND 4.0 como política por defecto; el código didáctico tendrá sus permisos propios cuando se incorpore.
