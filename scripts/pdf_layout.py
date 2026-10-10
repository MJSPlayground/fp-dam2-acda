"""Portada e índice PDF genérico a partir de nav de Zensical."""
from __future__ import annotations
from datetime import date
from html import escape
from pathlib import Path

DOCS = Path(__file__).resolve().parent.parent / "docs"

def page_title(source):
    if Path(source).name == "index.md":
        return "Introducción"
    for line in (DOCS / source).read_text(encoding="utf-8").splitlines():
        if line.startswith("# "):
            return line[2:].strip()
    return Path(source).stem.replace("-", " ").capitalize()

def section_navigation(project, slug):
    def contains_index(branch):
        for entry in branch:
            if entry == f"{slug}/index.md":
                return True
            if isinstance(entry, dict) and any(isinstance(value, list) and contains_index(value) for value in entry.values()):
                return True
        return False
    for item in project["nav"]:
        if isinstance(item, dict):
            for branch in item.values():
                if isinstance(branch, list) and contains_index(branch):
                    return branch
    raise ValueError(f"No existe navegación para {slug}")

def navigation_index(items, anchors):
    lines = []
    for item in items:
        if isinstance(item, str):
            link = anchors.get(item)
            if link:
                name = page_title(item)
                lines.append(f'<li class="print-index-page"><a href="#{escape(link)}">{escape(name)}</a></li>')
        elif isinstance(item, dict):
            for label, value in item.items():
                if isinstance(value, str):
                    link = anchors.get(value)
                    if link:
                        lines.append(f'<li class="print-index-page"><a href="#{escape(link)}">{escape(label)}</a></li>')
                elif isinstance(value, list):
                    lines.append(f'<li class="print-index-group"><span>{escape(label)}</span>{navigation_index(value, anchors)}</li>')
    return '<ol class="print-index-list">' + '\n'.join(lines) + '</ol>' if lines else ""

def make_print_html(sections, title, article_markup, project, print_css, *, all_units=False):
    extra = project.get("extra", {})
    contacts = []
    for key, label in (("author_email_work", "Correo laboral"),
                       ("author_email_personal", "Correo MJS Playground")):
        if extra.get(key):
            contacts.append(f'<span><strong>{label}:</strong> {escape(extra[key])}</span>')
    chapters = []
    toc = []
    counter = 0
    for slug, name, pages in sections:
        blocks, anchors = [], {}
        for source in pages:
            counter += 1
            chapter_id = f"pdf-page-{counter}"
            anchors[source] = chapter_id
            blocks.append(f'<section class="print-chapter" id="{chapter_id}">' + article_markup(source) + "</section>")
        toc.append(f'<li class="print-index-unit"><a href="#pdf-{slug}"><strong>{escape(name)}</strong></a>'
                   + navigation_index(section_navigation(project, slug), anchors) + "</li>")
        chapters.append(f'<section class="print-unit" id="pdf-{slug}"><header class="print-unit-header">'
                        '<span class="print-unit-kicker">MJS Playground · Acceso a Datos</span>'
                        f'<h1>{escape(name)}</h1></header>' + '\n'.join(blocks) + "</section>")
    heading = "Índice general" if all_units else "Índice de la sección"
    type_name = "Documentación completa" if all_units else "Sección"
    return f"""<!doctype html><html lang="es"><head><meta charset="utf-8">
<title>{escape(title)}</title><style>{print_css}</style></head><body>
<section class="print-document-cover" aria-label="Portada">
<img src="/assets/branding/mjs-playground.svg" alt="MJS Playground">
<span class="print-cover-kicker">{type_name}</span>
<h1>{escape(title)}</h1><p class="print-cover-site">{escape(project.get("site_name", ""))}</p>
<div class="print-cover-meta">
<span><strong>Autor:</strong> {escape(project.get("site_author", "MJS Playground"))}</span>
{''.join(contacts)}
<span><strong>Fecha de generación:</strong> {date.today().strftime("%d/%m/%Y")}</span>
</div></section>
<section class="print-index"><h1>{heading}</h1>
<p class="print-index-intro">Selecciona un apartado para navegar al contenido.</p>
<ol class="print-index-units">{''.join(toc)}</ol></section>
<main class="md-typeset">{''.join(chapters)}</main>
</body></html>"""
