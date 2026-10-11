"""Comprueba navegación, recursos y descargas del sitio final bajo GitHub Pages."""
from __future__ import annotations

import functools
import io
import zipfile
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
    ("hibernate", "Hibernate", "Herramientas de mapeo objeto-relacional (ORM) y JPA con Hibernate"),
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
                for path in ("ficheros/rutas/", "ficheros/guiada-csv/", "ficheros/final-gestor/", "ficheros/ampliacion-watchservice/"):
                    response = page.goto(base + path, wait_until="load")
                    assert response and response.ok and page.locator("h1").is_visible(), path
                    assert page.locator(".md-sidebar--primary").count() == 1, path
                    assert page.locator(".md-tabs__link").count() == len(SECTIONS), path
                page.goto(base + "ficheros/rutas/", wait_until="load")
                assert page.locator(".md-annotation").count() >= 1
                assert page.locator(".md-tooltip__inner").filter(has_text="Path.of").count() >= 1
                tabs = page.locator(".md-tabs__link").all()
                assert all(tab.bounding_box() and tab.bounding_box()["x"] + tab.bounding_box()["width"] <= 1366 for tab in tabs), "Pestañas desbordadas a 1366 px"
                page.set_viewport_size({"width": 768, "height": 900})
                page.reload(wait_until="load")
                assert page.locator("h1").is_visible(), "Contenido no visible en pantalla menor"
                # Piloto Bloque 1: pie nativo, figuras y codelab en varios tamaños.
                captures = SITE.parent / "qa-captures"
                captures.mkdir(exist_ok=True)
                for width in (1366, 390):
                    page.set_viewport_size({"width": width, "height": 844})
                    for path, expected in (("ficheros/", 1), ("ficheros/rutas/", 2)):
                        page.goto(base + path, wait_until="load")
                        links = page.locator(".md-footer__link")
                        assert links.count() == expected, path
                        boxes = [link.bounding_box() for link in links.all()]
                        assert all(box and box["width"] <= 280 and box["height"] < 100 for box in boxes), (path, width, boxes)
                        assert all(link.locator(".md-footer__title").is_visible() for link in links.all()), (path, width, "Título del pie oculto")
                        if expected == 1:
                            assert boxes[0]["x"] + boxes[0]["width"] > width - 90, "El enlace único siguiente debe quedar a la derecha"
                        elif width > 600:
                            assert boxes[0]["x"] + boxes[0]["width"] < boxes[1]["x"], "Los enlaces deben ocupar extremos opuestos"
                        assert page.evaluate("document.documentElement.scrollWidth <= window.innerWidth + 2"), (path, width)
                    page.goto(base + "ficheros/rutas/", wait_until="load")
                    figure = page.locator("figure.mjs-figure")
                    assert figure.evaluate("el => Math.abs((el.getBoundingClientRect().left + el.getBoundingClientRect().right) / 2 - (el.parentElement.getBoundingClientRect().left + el.parentElement.getBoundingClientRect().right) / 2) < 3"), "La figura no está centrada"
                    page.goto(base + "ficheros/", wait_until="load")
                    page.screenshot(path=str(captures / f"footer-unico-{width}.png"), full_page=True)
                sequence = [
                    ("ficheros/rutas/", "ficheros/gestion-recorrido/"),
                    ("ficheros/gestion-recorrido/", "ficheros/listar-recorrer/"),
                    ("ficheros/listar-recorrer/", "ficheros/guiada-directorios/"),
                    ("ficheros/guiada-directorios/", "ficheros/texto/"),
                    ("ficheros/ampliacion-escrituras-fiables/", "hibernate/"),
                ]
                for source, destination in sequence:
                    page.goto(base + source, wait_until="load")
                    next_link = page.locator(".md-footer__link--next")
                    assert next_link.count() == 1 and next_link.get_attribute("href")
                    next_link.click()
                    assert page.url == base + destination, f"Pie roto: {source} → {destination}"
                page.set_viewport_size({"width": 1366, "height": 768})
                page.goto(base + "ficheros/rutas/", wait_until="load")
                assert page.locator("figure.mjs-figure img").count() == 1
                assert page.locator("figure.mjs-figure figcaption").is_visible()
                assert page.locator("figure.mjs-figure img").evaluate("img => img.complete && img.naturalWidth > 0")
                page.locator(".md-footer__link--next").focus()
                assert page.locator(".md-footer__link--next").evaluate("el => getComputedStyle(el).outlineStyle !== 'none'")
                page.locator("figure.mjs-figure img").hover()
                page.wait_for_timeout(300)
                assert page.locator("figure.mjs-figure img").evaluate("el => getComputedStyle(el).transform !== 'none'")
                page.emulate_media(reduced_motion="reduce")
                assert page.locator("figure.mjs-figure img").evaluate("el => getComputedStyle(el).transform === 'none'")
                page.emulate_media(reduced_motion="no-preference")
                page.screenshot(path=str(captures / "rutas-desktop.png"), full_page=True)
                page.goto(base + "ficheros/guiada-directorios/", wait_until="load")
                assert page.locator(".mjs-task input[type=checkbox]").count() == 11
                cards = page.locator("a.mjs-code-resource")
                assert cards.count() == 2
                assert page.locator("a.mjs-resource-source").count() == 2
                assert page.locator("article").get_by_text("Generador de datos", exact=True).count() == 0
                for i, kind in enumerate(("inicial", "solucion")):
                    filename = f"inventario-directorios-{kind}.zip"
                    card = cards.nth(i)
                    assert card.get_attribute("href") == f"{PREFIX}descargas/codigo/{filename}"
                    assert card.locator(".mjs-code-resource__format").inner_text() == "ZIP"
                    card.focus()
                    page.keyboard.press("Tab")
                    page.keyboard.press("Shift+Tab")
                    assert card.evaluate("el => el.matches(':focus-visible') && getComputedStyle(el).outlineStyle !== 'none'"), kind
                    with page.expect_download() as info:
                        card.click()
                    downloaded = info.value
                    assert downloaded.suggested_filename == filename and downloaded.failure() is None
                    with zipfile.ZipFile(downloaded.path()) as archive:
                        names = set(archive.namelist())
                        assert archive.testzip() is None and {"pom.xml", "README.md", "mvnw", "mvnw.cmd", ".mvn/wrapper/maven-wrapper.jar", ".mvn/wrapper/maven-wrapper.properties"} <= names
                        source = archive.read("src/main/java/es/mjsplayground/acda/ficheros/codelab/InventarioCodelab.java").decode()
                        assert ("TODO 5" in source) == (kind == "inicial")
                        assert ("Files.walkFileTree(" in source) == (kind == "solucion")
                assert page.locator("figure.mjs-figure img").evaluate("img => img.complete && img.naturalWidth > 0")
                details = page.locator("article details")
                assert details.count() >= 2 and all(not details.nth(i).evaluate("el => el.open") for i in range(details.count()))
                details.first.locator("summary").click()
                assert details.first.evaluate("el => el.open")
                box = page.locator("#b1-p1")
                box.check()
                assert "1 de 11" in page.locator(".mjs-progress-count").inner_text()
                page.reload(wait_until="load")
                assert page.locator("#b1-p1").is_checked(), "No persiste el avance"
                page.locator("#b1-p1").uncheck()
                page.screenshot(path=str(captures / "codelab-desktop.png"), full_page=True)
                page.set_viewport_size({"width": 390, "height": 844})
                page.reload(wait_until="load")
                assert page.locator("h1").is_visible()
                assert page.evaluate("document.documentElement.scrollWidth <= window.innerWidth + 2"), "Desbordamiento móvil"
                assert cards.first.is_visible() and cards.first.bounding_box()["width"] <= 390
                assert all(card.locator(".mjs-code-resource__format").evaluate("el => getComputedStyle(el).whiteSpace === 'nowrap'") for card in cards.all())
                page.screenshot(path=str(captures / "codelab-mobile.png"), full_page=True)
                page.goto(base + "ficheros/rutas/", wait_until="load")
                assert page.evaluate("document.documentElement.scrollWidth <= window.innerWidth + 2"), "Figura desborda en móvil"
                page.screenshot(path=str(captures / "rutas-mobile.png"), full_page=True)
                page.set_viewport_size({"width": 1366, "height": 768})
                page.locator('.md-header__option label:visible').click()
                assert page.locator('body').get_attribute('data-md-color-scheme') == 'slate'
                page.screenshot(path=str(captures / "rutas-dark.png"), full_page=True)
                for width in (1366, 390):
                    page.set_viewport_size({"width": width, "height": 844})
                    page.goto(base + "ficheros/guiada-directorios/", wait_until="load")
                    for scheme in ("slate", "default"):
                        if page.locator("body").get_attribute("data-md-color-scheme") != scheme:
                            page.locator('.md-header__option label:visible').click()
                        assert page.locator("body").get_attribute("data-md-color-scheme") == scheme
                        assert page.evaluate("document.documentElement.scrollWidth <= window.innerWidth + 2")
                        assert all(card.is_visible() for card in cards.all())
                        assert page.locator("figure.mjs-figure img").evaluate("img => img.complete && img.naturalWidth > 0")
                        page.screenshot(path=str(captures / f"codelab-{width}-{scheme}.png"), full_page=True)
                assert not errors, "Errores JavaScript: " + "; ".join(errors)
                print("OK: navegación, estilos, recursos y PDF verificados.", flush=True)
            finally:
                browser.close()
    finally:
        server.shutdown()
        server.server_close()


if __name__ == "__main__":
    check()
