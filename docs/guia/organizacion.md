# Organización

Cada sección tiene una carpeta y una página `index.md`; el resto de páginas y submenús se organizan desde `zensical.toml`.

| Recurso | Propósito |
| --- | --- |
| `docs/index.md` | Portada |
| `docs/guia/` | Carpeta de la sección |
| `zensical.toml` | Navegación y orden del PDF |

!!! info "Secciones anidadas"
    Puedes tener Físicas, Cámaras y Animaciones, cada uno con tantas páginas como necesites.

??? question "¿Cómo añado otra página?"
    Crea el Markdown en su carpeta y referencia su ruta en `nav`. La próxima compilación actualizará los PDF.
