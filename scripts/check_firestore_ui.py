"""Comprueba páginas Firestore renderizadas y sus proyectos/Postman descargables."""
from __future__ import annotations

import functools
import json
import threading
import zipfile
from http.server import ThreadingHTTPServer
from pathlib import Path

from playwright.sync_api import sync_playwright
from check_site_ui import SITE, PREFIX, SiteHandler
from test_project_packages import check_postman_urls

PAGES = ("documentos", "configuracion", "persistencia", "consultas", "rest", "proyecto", "practica-final", "repaso", "ampliacion")


def requests(items):
    for item in items:
        if "request" in item:
            yield item
        yield from requests(item.get("item", []))


def check():
    server = ThreadingHTTPServer(("127.0.0.1", 0), functools.partial(SiteHandler, directory=str(SITE)))
    threading.Thread(target=server.serve_forever, daemon=True).start()
    base = f"http://127.0.0.1:{server.server_port}{PREFIX}"
    captures = SITE.parent / "qa-captures"
    captures.mkdir(exist_ok=True)
    try:
        with sync_playwright() as playwright:
            browser = playwright.chromium.launch()
            try:
                page = browser.new_page(accept_downloads=True)
                errors = []
                page.on("pageerror", lambda error: errors.append(str(error)))
                for width in (1366, 390):
                    page.set_viewport_size({"width": width, "height": 844})
                    for scheme in ("default", "slate"):
                        for slug in PAGES:
                            response = page.goto(base + f"firestore/{slug}/", wait_until="load")
                            assert response and response.ok and page.locator("h1").is_visible(), slug
                            if page.locator("body").get_attribute("data-md-color-scheme") != scheme:
                                page.locator('.md-header__option label:visible').click()
                            assert page.evaluate("document.documentElement.scrollWidth <= innerWidth + 2"), (slug, width, scheme)
                            if slug == "repaso":
                                assert page.locator("article").inner_text().count("En preparación") == 5
                            if slug in ("rest", "proyecto", "practica-final"):
                                page.screenshot(path=str(captures / f"firestore-{slug}-{width}-{scheme}.png"), full_page=True)
                                if width == 390:
                                    page.screenshot(path=str(captures / f"firestore-{slug}-{width}-{scheme}-viewport.png"))
                for kind, slug in (("guiado", "proyecto"), ("final", "practica-final")):
                    page.goto(base + f"firestore/{slug}/", wait_until="load")
                    for state in ("inicial", "solucion"):
                        name = f"reservas-{kind}-{state}.zip"
                        link = page.locator(f'article a[href$="{name}"]')
                        assert link.count() == 1, (slug, name)
                        assert link.get_attribute("download") == name, name
                        link.focus()
                        assert link.evaluate("e => e.matches(':focus-visible')"), name
                        with page.expect_download() as event:
                            link.click()
                        download = event.value
                        assert download.suggested_filename == name and download.failure() is None
                        with zipfile.ZipFile(download.path()) as archive:
                            names = set(archive.namelist())
                            assert archive.testzip() is None
                            assert {"pom.xml", "mvnw", "mvnw.cmd", "README.md", "firebase.json", "postman/reservas.postman_collection.json"} <= names
                            assert not any({"target", "node_modules", ".firebase"} & set(Path(n).parts) or n.endswith((".class", ".db", ".log")) for n in names)
                            collection = json.loads(archive.read("postman/reservas.postman_collection.json"))
                            check_postman_urls(collection["item"])
                            assert collection["info"]["schema"].endswith("/v2.1.0/collection.json")
                            items = list(requests(collection["item"]))
                            assert len(items) >= 6, "Faltan escenarios Postman"
                            methods = {item["request"]["method"] for item in items}
                            assert {"GET", "POST", "DELETE"} <= methods, methods
                            assert any(item.get("event") for item in items), "Colección sin comprobaciones de respuesta"
                assert not errors, errors
            finally:
                browser.close()
        print("OK Firestore: 9 páginas en móvil/escritorio y claro/oscuro; 4 ZIP con emulador y Postman descargados y examinados")
    finally:
        server.shutdown()
        server.server_close()


if __name__ == "__main__":
    check()
