"""Comprueba navegación, recursos y descargas del sitio final bajo GitHub Pages."""
from __future__ import annotations

import functools
import io
import threading
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import unquote, urljoin, urlparse

from playwright.sync_api import sync_playwright
from pypdf import PdfReader

SITE = Path(__file__).resolve().parent.parent / "site"
PREFIX = "/fp-dam2-acda/"
SECTIONS = (
    ("ficheros", "Ficheros", "Manejo de ficheros y directorios"),
    ("hibernate", "Hibernate", "Herramientas ORM y JPA con Hibernate"),
    ("spring", "Spring", "Acceso a datos mediante componentes (Spring)"),
    ("jdbc", "JDBC", "Conexiones a bases de datos relacionales (JDBC)"),
    ("firestore", "Firestore", "Bases de datos documentales con Cloud Firestore"),
    ("mongodb", "MongoDB", "Bases de datos NoSQL con MongoDB"),
    ("ayuda", "Ayuda", "Ayuda y herramientas"),
)


class SiteHandler(SimpleHTTPRequestHandler):
    def log_message(self, *_):
        pass

    def translate_path(self, path):
        if not path.startswith(PREFIX):
            return str(SITE / "__outside_site__")
        return super().translate_path("/" + path[len(PREFIX):])


def check():
    assert (SITE / "index.html").is_file(), "Ejecuta primero export_pdfs.py"
    server = ThreadingHTTPServer(("127.0.0.1", 0), functools.partial(SiteHandler, directory=str(SITE)))
    threading.Thread(target=server.serve_forever, daemon=True).start()
    base = f"http://127.0.0.1:{server.server_port}{PREFIX}"
    try:
        with sync_playwright() as playwright:
            browser = playwright.chromium.launch(headless=True)
            try:
                page = browser.new_page(viewport={"width": 1366, "height": 768}, accept_downloads=True)
                errors = []
                page.on("pageerror", lambda error: errors.append(str(error)))
                paths = [""] + [f"{slug}/" for slug, _, _ in SECTIONS]
                for path in paths:
                    response = page.goto(base + path, wait_until="load")
                    assert response and response.ok, f"Página ausente: {path}"
                    assert page.locator("h1").first.is_visible(), f"Sin título: {path}"
                    if not path:
                        assert "Acceso a Datos" in page.locator("h1").first.inner_text()
                    else:
                        slug = path.strip("/")
                        title = {section: heading for section, _, heading in SECTIONS}[slug]
                        assert title in page.locator("h1").first.inner_text(), path
                    assert page.locator(".md-tabs__link").count() == len(SECTIONS), "Navegación incompleta"
                    assert [name.strip() for name in page.locator(".md-tabs__link").all_text_contents()] == [name for _, name, _ in SECTIONS], "Orden temático incorrecto"
                    assert page.locator(".md-header__button.md-logo").evaluate("el => el.href") == base
                    assert page.locator(".mjs-home-link").evaluate("el => el.href") == base
                    assert page.locator(".mjs-pdf-trigger").count() == 1
                    if path == "ficheros/":
                        page.locator(".md-tabs__link", has_text="Hibernate").click()
                        assert page.url == base + "hibernate/", "No se puede navegar entre temas"
                        page.go_back(wait_until="load")

                    # Comprueba todos los enlaces y recursos internos visibles en el HTML.
                    checked = set()
                    for target in page.locator("a[href], img[src], script[src], link[href]").all():
                        attr = "src" if target.evaluate("el => el.hasAttribute('src')") else "href"
                        raw = target.get_attribute(attr)
                        if not raw or raw.startswith(("#", "data:", "mailto:", "tel:")):
                            continue
                        url = urljoin(page.url, raw)
                        parsed = urlparse(url)
                        if parsed.netloc != urlparse(base).netloc:
                            continue
                        assert unquote(parsed.path).startswith(PREFIX), f"Recurso fuera del proyecto: {url}"
                        clean = parsed._replace(fragment="").geturl()
                        if clean not in checked:
                            result = page.request.get(clean)
                            assert result.ok, f"Enlace o recurso roto: {clean} ({result.status})"
                            checked.add(clean)

                    page.locator(".mjs-pdf-trigger").click()
                    links = page.locator(".mjs-pdf-menu a")
                    names = ([path.strip("/") + ".pdf"] if path else []) + ["documentacion-completa.pdf"]
                    assert links.count() == len(names), (path, names)
                    for i, filename in enumerate(names):
                        address = base + "descargas/" + filename
                        assert links.nth(i).get_attribute("href") == address
                        result = page.request.get(address)
                        assert result.ok and result.headers.get("content-type", "").startswith("application/pdf")
                        reader = PdfReader(io.BytesIO(result.body()))
                        assert reader.pages, f"PDF vacío: {filename}"
                        with page.expect_download() as download_info:
                            links.nth(i).click()
                        download = download_info.value
                        assert download.suggested_filename == filename
                        assert download.failure() is None, filename
                        if i + 1 < len(names):
                            page.locator(".mjs-pdf-trigger").click()
                    page.locator(".md-header__button.md-logo").click()
                    assert page.url == base, "El logo no vuelve a la portada"
                    print(f"OK {PREFIX}{path}: enlaces, recursos y {len(names)} descargas", flush=True)
                captures = SITE.parent / "qa-captures"
                captures.mkdir(exist_ok=True)
                current_pages = ("ficheros/rutas/", "ficheros/texto-csv/", "ficheros/proyecto/", "ficheros/practica-final/", "ficheros/ampliacion/")
                for path in current_pages:
                    response = page.goto(base + path, wait_until="load")
                    assert response and response.ok and page.locator("h1").is_visible(), path
                    assert page.locator(".md-sidebar--primary").count() == 1, path
                    assert page.locator(".md-tabs__link").count() == len(SECTIONS), path
                tabs = page.locator(".md-tabs__link").all()
                assert all(tab.bounding_box() and tab.bounding_box()["x"] + tab.bounding_box()["width"] <= 1366 for tab in tabs), "Pestañas desbordadas"
                page.set_viewport_size({"width": 768, "height": 900})
                page.reload(wait_until="load")
                assert page.locator("h1").is_visible()
                for width in (1366, 390):
                    page.set_viewport_size({"width": width, "height": 844})
                    for path, expected in (("ficheros/", 1), ("ficheros/rutas/", 2)):
                        page.goto(base + path, wait_until="load")
                        links = page.locator(".md-footer__link")
                        assert links.count() == expected, path
                        boxes = [link.bounding_box() for link in links.all()]
                        assert all(box and box["width"] <= 280 and box["height"] < 100 for box in boxes), (path, width, boxes)
                        assert all(link.locator(".md-footer__title").is_visible() for link in links.all()), path
                        if expected == 1:
                            assert boxes[0]["x"] + boxes[0]["width"] > width - 90
                        elif width > 600:
                            assert boxes[0]["x"] + boxes[0]["width"] < boxes[1]["x"]
                        assert page.evaluate("document.documentElement.scrollWidth <= innerWidth + 2"), path
                    page.screenshot(path=str(captures / f"ficheros-navegacion-{width}.png"), full_page=True)
                sequence = ("rutas", "texto-csv", "json", "binarios", "arquitectura", "proyecto", "practica-final", "repaso", "ampliacion")
                for previous, following in zip(sequence, sequence[1:]):
                    page.goto(base + f"ficheros/{previous}/", wait_until="load")
                    next_link = page.locator(".md-footer__link--next")
                    assert next_link.get_attribute("href") and next_link.evaluate("el => el.href") == base + f"ficheros/{following}/", (previous, following)
                page.locator(".md-footer__link--next").focus()
                assert page.locator(".md-footer__link--next").evaluate("el => getComputedStyle(el).outlineStyle !== 'none'")
                page.set_viewport_size({"width": 1366, "height": 844})
                page.goto(base + "ficheros/rutas/", wait_until="load")
                assert page.locator(".md-annotation").count() >= 1, "La anotación preparada no se ha renderizado"
                figure = page.locator("figure.mjs-figure").first
                picture = figure.locator("img").first
                assert picture.evaluate("img => img.complete && img.naturalWidth > 0")
                assert picture.get_attribute("alt") and figure.locator("figcaption").inner_text().strip()
                assert figure.evaluate("el => Math.abs((el.getBoundingClientRect().left + el.getBoundingClientRect().right) / 2 - (el.parentElement.getBoundingClientRect().left + el.parentElement.getBoundingClientRect().right) / 2) < 3"), "Figura descentrada"
                picture.hover()
                page.wait_for_timeout(300)
                assert picture.evaluate("el => getComputedStyle(el).transform !== 'none'")
                page.emulate_media(reduced_motion="reduce")
                assert picture.evaluate("el => getComputedStyle(el).transform === 'none'")
                page.emulate_media(reduced_motion="no-preference")
                page.goto(base + "ficheros/proyecto/", wait_until="load")
                boxes = page.locator('.mjs-task input[type="checkbox"]')
                total = boxes.count()
                assert total > 0, "La guía no contiene la progresión preparada"
                first = boxes.first
                first.check()
                assert page.locator(".mjs-progress-count").inner_text() == f"1 de {total} pasos"
                page.reload(wait_until="load")
                assert boxes.first.is_checked(), "No persiste el avance"
                boxes.first.uncheck()
                page.reload(wait_until="load")
                assert not boxes.first.is_checked(), "No persiste el reinicio de la etapa"
                assert page.locator(".mjs-progress-count").inner_text() == f"0 de {total} pasos"
                details = page.locator("article details")
                assert details.count() > 0, "Faltan las ayudas desplegables de la guía"
                assert all(not details.nth(i).evaluate("el => el.open") for i in range(details.count()))
                details.first.locator("summary").click()
                assert details.first.evaluate("el => el.open")
                download = page.locator('article a[download]').first
                download.focus()
                page.keyboard.press("Tab")
                page.keyboard.press("Shift+Tab")
                assert download.evaluate("el => el.matches(':focus-visible') && getComputedStyle(el).outlineStyle !== 'none'")
                for width in (1366, 390):
                    page.set_viewport_size({"width": width, "height": 844})
                    for scheme in ("default", "slate"):
                        for path in ("ficheros/rutas/", "ficheros/proyecto/"):
                            page.goto(base + path, wait_until="load")
                            if page.locator("body").get_attribute("data-md-color-scheme") != scheme:
                                page.locator('.md-header__option label:visible').click()
                            assert page.locator("body").get_attribute("data-md-color-scheme") == scheme
                            assert page.evaluate("document.documentElement.scrollWidth <= innerWidth + 2")
                            page.screenshot(path=str(captures / f"ficheros-{path.split('/')[1]}-{width}-{scheme}.png"), full_page=True)
                            page.screenshot(path=str(captures / f"ficheros-{path.split('/')[1]}-{width}-{scheme}-viewport.png"))
                assert not errors, "Errores JavaScript: " + "; ".join(errors)
                print("OK: navegación, estilos, recursos y PDF verificados.", flush=True)
            finally:
                browser.close()
    finally:
        server.shutdown()
        server.server_close()


if __name__ == "__main__":
    check()
