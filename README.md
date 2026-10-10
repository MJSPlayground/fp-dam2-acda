# MJS Docs Template

Plantilla **privada y reutilizable** de documentación con Zensical Modern para módulos de Formación Profesional y documentación de aplicaciones.

## Contenido

- Diseño MJS, buscador, claro/oscuro, navegación anidada y estilos comunes.
- Colores propios de cada proyecto en `docs/stylesheets/colors.css`: se comparten entre web y PDF.
- PDF por sección y PDF global, con portada, autor, fecha, contactos e índice interactivo.
- Icono contextual de descarga en la cabecera; los PDF se regeneran al **compilar/publicar**, no al hacer clic.
- GitHub Actions valida y genera artefactos privados. **No publica ninguna web.**

## Reutilizar

1. Marca este repositorio como **Template repository** en Settings. Entonces podrás elegir **Use this template** para crear otro repositorio.
2. Configura el nuevo repositorio **privado por defecto** y revisa expresamente antes de hacer público un módulo.
3. Modifica `zensical.toml`: título, descripción, URL definitiva, autor, contactos y navegación `nav`.
4. Cambia `docs/stylesheets/colors.css` para ajustar los colores; mantén el CSS y JS comunes.
5. Sustituye `docs/guia/` y `docs/referencia/` por tus propios capítulos y modifica `nav`.

Cada sección de `nav` tiene una **carpeta con index.md**. Dentro pueden existir otras páginas y submenús colapsables, sin límite específico del diseño. El PDF respeta el orden de la navegación.

**Módulos FP:** organizar la documentación pública **por nombres estables de tema, sin numeración ni referencias a UT**. Por ejemplo, carpetas `ficheros/`, `hibernate/`, `spring/` y títulos «Manejo de ficheros», «JPA con Hibernate», «Spring». Descargas `ficheros.pdf`, `hibernate.pdf` y `documentacion-completa.pdf`. La correspondencia entre temas y unidades de trabajo pertenece a la planificación del docente, no a la navegación, URLs o código.

**Documentación técnica:** usa `guia/`, `arquitectura/`, `api/`... Se generan los PDF correspondientes, sin modificar los scripts. La portada se mantiene en `docs/index.md` y se abre desde el logo o el título (sin pestaña Inicio).

## Ejecutar en Windows

Desde VS Code, en la raíz del repositorio:

~~~powershell
py -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
.\.venv\Scripts\python.exe scripts\export_pdfs.py
.\.venv\Scripts\zensical.exe serve
~~~

Visita http://127.0.0.1:8000/. Si falta un navegador Chromium compatible:

~~~powershell
.\.venv\Scripts\python.exe -m playwright install chromium --only-shell
~~~

Vuelve a ejecutar `scripts/export_pdfs.py` tras cambiar contenido cuando necesites actualizar los PDF locales. En producción, este paso **debe preceder** a la publicación del sitio, de modo que los PDF correspondan a la versión publicada.

## Estructura técnica

| Archivo | Responsabilidad |
| --- | --- |
| `zensical.toml` | Título, metadatos, navegación |
| `docs/stylesheets/colors.css` | Paleta personalizable por módulo/proyecto |
| `docs/stylesheets/extra.css` | Estilos reutilizables |
| `docs/javascripts/extra.js` | Casillas interactivas y menú PDF |
| `scripts/export_pdfs.py` | Compilación y exportación PDF |
| `scripts/pdf_layout.py` | Portada e índice navegable |
| `scripts/print.css` | Impresión A4 |
| `.github/workflows/validate.yml` | Tests, sitio compilado y PDF adjuntos |

## Publicación y derechos

La plantilla **no contiene despliegue de GitHub Pages**; se preparará y revisará al publicar el primer módulo real. La licencia abierta no se aplica automáticamente a todo el código. Los apuntes originales de FP publicados utilizarán CC BY-NC-ND 4.0 como política por defecto; el código didáctico tendrá sus permisos independientes. Las aplicaciones personales siguen privadas por defecto y pueden destinarse a comercialización futura.

No incluir datos identificables del alumnado, soluciones reservadas, exámenes futuros ni credenciales.

**Mantenimiento:** los cambios futuros en la plantilla no se propagan automáticamente a repositorios derivados; no creamos infraestructura adicional hasta que sea necesario.
