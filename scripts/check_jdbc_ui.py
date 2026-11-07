"""Comprueba el recorrido JDBC publicado en el sitio local y sus cuatro ZIP."""
import functools
import threading
import zipfile
from http.server import ThreadingHTTPServer
from pathlib import Path

from playwright.sync_api import sync_playwright
from check_site_ui import SITE, PREFIX, SiteHandler


def check():
    server = ThreadingHTTPServer(("127.0.0.1", 0), functools.partial(SiteHandler, directory=str(SITE)))
    threading.Thread(target=server.serve_forever, daemon=True).start()
    base = f"http://127.0.0.1:{server.server_port}{PREFIX}"
    captures = SITE.parent / "qa-captures"
    captures.mkdir(exist_ok=True)
    pages = ("conexiones", "consultas", "repositorios", "transacciones", "proyecto", "practica-final", "repaso", "ampliacion")
    try:
        with sync_playwright() as p:
            browser = p.chromium.launch()
            page = browser.new_page(accept_downloads=True)
            errors = []
            page.on("pageerror", lambda e: errors.append(str(e)))
            for width in (1366, 390):
                page.set_viewport_size({"width": width, "height": 844})
                for scheme in ("default", "slate"):
                    for slug in pages:
                        response = page.goto(base + f"jdbc/{slug}/", wait_until="load")
                        assert response.ok and page.locator("h1").is_visible(), slug
                        if page.locator("body").get_attribute("data-md-color-scheme") != scheme:
                            page.locator('.md-header__option label:visible').click()
                        assert page.evaluate("document.documentElement.scrollWidth <= innerWidth + 2"), (slug, width, scheme)
                        if slug in ("proyecto", "consultas", "practica-final"):
                            page.screenshot(path=str(captures / f"jdbc-{slug}-{width}-{scheme}.png"), full_page=True)
            for kind, slug in (("guiado", "proyecto"), ("final", "practica-final")):
                page.goto(base + f"jdbc/{slug}/", wait_until="load")
                links = page.locator('article a[download]')
                assert links.count() == 2, (slug, links.count())
                for state in ("inicial", "solucion"):
                    name = f"almacen-{kind}-{state}.zip"
                    link = page.locator(f'article a[href$="{name}"]')
                    link.focus()
                    assert link.evaluate("e => e.matches(':focus-visible')"), name
                    with page.expect_download() as event:
                        link.click()
                    download = event.value
                    assert download.suggested_filename == name and download.failure() is None
                    with zipfile.ZipFile(download.path()) as archive:
                        names = archive.namelist()
                        assert archive.testzip() is None
                        assert {"pom.xml", "mvnw", "mvnw.cmd", "README.md"} <= set(names)
                        assert not any("target" in Path(n).parts or n.endswith((".class", ".db", ".log")) for n in names)
                        assert any(n.endswith("branding/mjs-playground.png") for n in names), name
            assert not errors, errors
            browser.close()
        print("OK JDBC: 8 páginas en móvil/escritorio y claro/oscuro; 4 ZIP descargados y examinados")
    finally:
        server.shutdown()
        server.server_close()


if __name__ == "__main__":
    check()
