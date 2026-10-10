#!/usr/bin/env python3
"""Exporta PDF por sección y un PDF completo a partir del HTML generado por Zensical.

La navegación de zensical.toml determina el orden; no se duplican apuntes Markdown.
Python >= 3.11. Requiere beautifulsoup4, playwright y un Chromium compatible.
"""
from __future__ import annotations

import functools
import html
import re
import shutil
import subprocess
import sys
import threading
import tomllib
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urljoin, urlparse

from bs4 import BeautifulSoup, Tag
from playwright.sync_api import Error as PlaywrightError, sync_playwright
from pypdf import PdfReader
from pdf_layout import make_print_html

ROOT = Path(__file__).resolve().parent.parent
DOCS = ROOT / "docs"
SITE = ROOT / "site"
DOWNLOADS = DOCS / "descargas"
PRINT_CSS = ((ROOT / "scripts" / "print.css").read_text(encoding="utf-8") + "\n" + (DOCS / "stylesheets" / "colors.css").read_text(encoding="utf-8"))


def config():
    with (ROOT / "zensical.toml").open("rb") as handle:
        return tomllib.load(handle)["project"]


def find_sections(project):
    """Cada sección de nav ocupa una carpeta con index.md, como guia/ o ut01/."""
    sections = []
    for item in project["nav"]:
        if not isinstance(item, dict):
            continue
        for title, branch in item.items():
            if not isinstance(branch, list):
                continue
            pages = flatten_nav(branch)
            if not pages:
                continue
            roots = {Path(page).parts[0] for page in pages}
            if len(roots) != 1:
                raise ValueError(f"Las páginas de '{title}' deben compartir una carpeta.")
            slug = roots.pop()
            if not re.fullmatch(r"[a-z0-9][a-z0-9_-]*", slug, flags=re.I):
                raise ValueError(f"Identificador de sección no válido: {slug}")
            if f"{slug}/index.md" not in pages:
                raise ValueError(f"Se requiere {slug}/index.md en nav.")
            if any(previous[0] == slug for previous in sections):
                raise ValueError(f"Sección duplicada: {slug}")
            sections.append((slug, title, pages))
    if not sections:
        raise ValueError("No se encontraron secciones en nav.")
    return sections


def flatten_nav(node):
    """Recorre secciones anidadas sin cambiar el orden."""
    result = []
    for item in node:
        if isinstance(item, str):
            if item.endswith(".md"):
                result.append(item)
        elif isinstance(item, dict):
            for value in item.values():
                if isinstance(value, str) and value.endswith(".md"):
                    result.append(value)
                elif isinstance(value, list):
                    result.extend(flatten_nav(value))
    return result


def site_page(markdown_file):
    p = Path(markdown_file)
    if p.name == "index.md":
        return SITE / p.parent / "index.html"
    primary = SITE / p.with_suffix("") / "index.html"
    fallback = SITE / p.with_suffix(".html")
    if primary.is_file():
        return primary
    if fallback.is_file():
        return fallback
    raise FileNotFoundError(f"No existe la página compilada: {markdown_file} ({primary})")


def normalise_link(target, original_url):
    if not target or target.startswith(("#", "data:", "mailto:", "tel:", "javascript:")):
        return target
    if urlparse(target).scheme or target.startswith("//"):
        return target
    return urljoin(original_url, target)


def article_markup(source):
    """Conserva el HTML que produjo Zensical (admonitions, código, tablas, etc)."""
    path = site_page(source)
    soup = BeautifulSoup(path.read_text(encoding="utf-8"), "html.parser")
    article = soup.select_one("article.md-content__inner") or soup.select_one(".md-content__inner")
    if article is None:
        raise ValueError(f"Zensical no ha generado un artículo reconocible en: {path}")

    # En una impresión continua, las pestañas se imprimen todas y con su etiqueta.
    for tabs in article.select(".tabbed-set"):
        labels = [label.get_text(" ", strip=True) for label in tabs.select(".tabbed-labels label")]
        blocks = tabs.select(".tabbed-content > .tabbed-block")
        for index, block in enumerate(blocks):
            if index < len(labels):
                heading = soup.new_tag("p", attrs={"class": "print-tab-title"})
                heading.string = labels[index]
                block.insert(0, heading)

    for details in article.select("details"):
        details["open"] = "open"

    # El origen de cada recurso debe seguir siendo la página original:
    # p.ej. ../../assets/demo/figure.svg se convierte en /assets/demo/figure.svg.
    original_url = "http://127.0.0.1:PORT/" + path.relative_to(SITE).as_posix()
    for tag in article.find_all(True):
        if not isinstance(tag, Tag):
            continue
        if tag.has_attr("src"):
            tag["src"] = normalise_link(tag["src"], original_url)
        if tag.has_attr("href"):
            address = tag["href"]
            # No incrustar enlaces a localhost (efímeros) en los PDF descargados.
            if address and not urlparse(address).scheme and not address.startswith(("#", "//", "mailto:", "tel:")):
                del tag["href"]
            else:
                tag["href"] = normalise_link(address, original_url)
        if tag.name == "h1":
            tag.name = "h2"
        elif tag.name == "h2":
            tag.name = "h3"
        elif tag.name == "h3":
            tag.name = "h4"

    # Los botones de navegación no aportan nada en papel.
    for button in article.select("a.md-button, a.headerlink"):
        button.decompose()

    return str(article)


class QuietHandler(SimpleHTTPRequestHandler):
    def log_message(self, fmt, *args):
        pass


def start_local_server():
    handler = functools.partial(QuietHandler, directory=str(SITE))
    server = ThreadingHTTPServer(("127.0.0.1", 0), handler)
    thread = threading.Thread(target=server.serve_forever, daemon=True)
    thread.start()
    return server


def launch_chromium(playwright):
    errors = []
    for channel in (None, "chrome", "msedge"):
        try:
            return playwright.chromium.launch(headless=True, **({"channel": channel} if channel else {}))
        except PlaywrightError as exc:
            errors.append(f"{channel or 'chromium'}: {str(exc).splitlines()[0]}")
    raise RuntimeError(
        "No se encontró un navegador Chromium compatible. "
        "Ejecuta 'python -m playwright install chromium --only-shell'. "
        + " | ".join(errors)
    )


def build_site():
    binary = Path(sys.executable).with_name("zensical.exe" if sys.platform == "win32" else "zensical")
    command = str(binary) if binary.is_file() else shutil.which("zensical")
    if not command:
        raise RuntimeError("Instala Zensical: python -m pip install -r requirements.txt")
    subprocess.run([command, "build", "--clean"], check=True, cwd=ROOT)


def export():
    project = config()
    sections = find_sections(project)
    print("Secciones detectadas:", ", ".join(name for name, _, _ in sections), flush=True)

    DOWNLOADS.mkdir(parents=True, exist_ok=True)
    # Evita PDFs obsoletos cuando se retire una UT.
    for pdf in DOWNLOADS.glob("*.pdf"):
        pdf.unlink()
    build_site()

    jobs = [(slug, title, [(slug, title, pages)]) for slug, title, pages in sections]
    jobs.append(("documentacion-completa", "Documentación completa", sections))

    # Los HTML temporales viven solo en site/, nunca como copias de los apuntes.
    print_dir = SITE / "_impresion_temporal"
    print_dir.mkdir(exist_ok=True)
    for filename, title, subset in jobs:
        (print_dir / f"{filename}.html").write_text(
            make_print_html(subset, title, article_markup, project, PRINT_CSS, all_units=(filename == "documentacion-completa")),
            encoding="utf-8",
        )

    server = start_local_server()
    try:
        with sync_playwright() as playwright:
            browser = launch_chromium(playwright)
            try:
                page = browser.new_page(viewport={"width": 1150, "height": 900})
                origin = f"http://127.0.0.1:{server.server_port}"
                for filename, title, subset in jobs:
                    url = f"{origin}/_impresion_temporal/{filename}.html"
                    response = page.goto(url, wait_until="load", timeout=60_000)
                    if response is None or not response.ok:
                        raise RuntimeError(f"No se pudo abrir el HTML de impresión: {url}")
                    # Sustituye el puerto simbólico en recursos reescritos.
                    page.eval_on_selector_all(
                        "[src], [href]",
                        """(els, port) => els.forEach(el => {
                          for(const attr of ['src', 'href']) {
                            const old = el.getAttribute(attr);
                            if(old && old.includes(':PORT/')) {
                              el.setAttribute(attr, old.replace(':PORT/', ':' + port + '/'));
                            }
                          }
                        })""",
                        server.server_port,
                    )
                    # Enlaces e imágenes ya están cargados? Dar tiempo a repintar assets.
                    page.evaluate("""() => Promise.all(
                        [...document.images].map(img => img.decode().catch(() => null))
                    )""")
                    pdf_path = DOWNLOADS / f"{filename}.pdf"
                    page.pdf(
                        path=str(pdf_path),
                        format="A4",
                        print_background=True,
                        outline=True,
                        tagged=True,
                        display_header_footer=True,
                        header_template="<div></div>",
                        footer_template=(
                            '<div style="font-size:8px;color:#64748b;width:100%;'
                            'padding:0 15mm;display:flex;justify-content:space-between;">'
                            "<span>MJS Playground · Documentación</span>"
                            '<span><span class="pageNumber"></span> / '
                            '<span class="totalPages"></span></span></div>'
                        ),
                        margin={"top": "15mm", "bottom": "20mm", "left": "15mm", "right": "15mm"},
                    )
                    doc = PdfReader(str(pdf_path))
                    if not doc.pages:
                        raise AssertionError(f"El PDF está vacío: {pdf_path}")
                    extracted = "\n".join(p.extract_text() or "" for p in doc.pages)
                    for section_id, section_title, _ in subset:
                        if section_title.casefold() not in extracted.casefold():
                            raise AssertionError(f"Falta la sección {section_title} en {pdf_path.name}")
                    if "Índice" not in extracted or "Fecha de generación" not in extracted or "Autor:" not in extracted:
                        raise AssertionError(f"Portada o índice no detectado en {pdf_path.name}")
                    cover_fields = [project.get("site_author", "")]
                    contacts = project.get("extra", {})
                    cover_fields.extend(contacts.get(key, "") for key in ("author_email_work", "author_email_personal"))
                    for field in filter(None, cover_fields):
                        if field not in extracted:
                            raise AssertionError(f"Dato de autor/contacto ausente en {pdf_path.name}: {field}")
                    pdf_links = sum(
                        len(page.get("/Annots", [])) for page in doc.pages
                    )
                    if pdf_links < len(subset):
                        raise AssertionError(f"Faltan enlaces interactivos de índice en {pdf_path.name}: {pdf_links}")
                    print(f"OK {pdf_path.name}: {len(doc.pages)} páginas, {pdf_path.stat().st_size // 1024} KiB", flush=True)
            finally:
                browser.close()
    finally:
        server.shutdown()
        server.server_close()

    # Zensical copia a site/ los PDF para publicarlos junto con la web.
    build_site()
    assert all((SITE / "descargas" / f"{filename}.pdf").is_file() for filename, _, _ in jobs)
    print(f"Sitio final compilado con {len(sections)} PDF de sección y 1 PDF completo.", flush=True)


if __name__ == "__main__":
    export()
