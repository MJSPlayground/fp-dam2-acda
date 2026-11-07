"""Recorrido común de temas: responsive, temas de color y descargas reales."""
from __future__ import annotations

import functools
import threading
import zipfile
from http.server import ThreadingHTTPServer
from pathlib import Path

from playwright.sync_api import sync_playwright
from check_site_ui import SITE, PREFIX, SiteHandler
from package_projects import REQUIRED, included


def check_topic(topic, pages, download_prefix, inspect_archive):
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
                        for slug in pages:
                            response = page.goto(base + f"{topic}/{slug}/", wait_until="load")
                            assert response and response.ok and page.locator("h1").is_visible(), (topic, slug)
                            if page.locator("body").get_attribute("data-md-color-scheme") != scheme:
                                page.locator('.md-header__option label:visible').click()
                            assert page.evaluate("document.documentElement.scrollWidth <= innerWidth + 2"), (topic, slug, width, scheme)
                            if slug == "repaso":
                                assert page.locator("article").inner_text().count("En preparación") == 5
                            if slug in ("proyecto", "practica-final"):
                                page.screenshot(path=str(captures / f"{topic}-{slug}-{width}-{scheme}.png"), full_page=True)
                                page.screenshot(path=str(captures / f"{topic}-{slug}-{width}-{scheme}-viewport.png"))
                for kind, slug in (("guiado", "proyecto"), ("final", "practica-final")):
                    page.goto(base + f"{topic}/{slug}/", wait_until="load")
                    for state in ("inicial", "solucion"):
                        name = f"{download_prefix}-{kind}-{state}.zip"
                        link = page.locator(f'article a[href$="{name}"]')
                        assert link.count() == 1 and link.get_attribute("download") == name, name
                        link.focus()
                        assert link.evaluate("e => e.matches(':focus-visible')"), name
                        with page.expect_download() as event:
                            link.click()
                        download = event.value
                        assert download.suggested_filename == name and download.failure() is None
                        with zipfile.ZipFile(download.path()) as archive:
                            names = set(archive.namelist())
                            assert archive.testzip() is None and REQUIRED <= names
                            assert all(included(Path(member)) for member in names)
                            inspect_archive(archive, names, name)
                assert not errors, errors
            finally:
                browser.close()
        print(f"OK {topic}: {len(pages)} páginas móvil/escritorio claro/oscuro; 4 ZIP descargados e inspeccionados")
    finally:
        server.shutdown()
        server.server_close()
