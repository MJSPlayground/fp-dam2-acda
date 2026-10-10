"""Verifica en Chromium la navegación, la cabecera y los PDF."""
from __future__ import annotations

import functools
import threading
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from playwright.sync_api import sync_playwright

SITE = Path(__file__).resolve().parent.parent / "site"

class Quiet(SimpleHTTPRequestHandler):
    def log_message(self, *_):
        pass

def check():
    assert (SITE / "index.html").exists(), "Ejecuta primero export_pdfs.py"
    server = ThreadingHTTPServer(("127.0.0.1", 0), functools.partial(Quiet, directory=str(SITE)))
    threading.Thread(target=server.serve_forever, daemon=True).start()
    base = f"http://127.0.0.1:{server.server_port}/"
    try:
        with sync_playwright() as playwright:
            browser = playwright.chromium.launch(headless=True)
            try:
                page = browser.new_page(viewport={"width": 1366, "height": 768})
                errors = []
                page.on("pageerror", lambda e: errors.append(str(e)))
                for url, section in [
                    ("", None),
                    ("guia/", "guia"),
                    ("guia/organizacion/", "guia"),
                    ("guia/bloques/pagina/", "guia"),
                    ("referencia/", "referencia"),
                    ("referencia/codigo/", "referencia"),
                ]:
                    response = page.goto(base + url, wait_until="load")
                    assert response and response.ok, f"Página ausente: {url}"
                    if url == "referencia/codigo/":
                        assert page.locator(".md-annotation").count() >= 1, "No existe botón de anotación de código"
                        assert page.locator(".md-tooltip__inner").filter(has_text="Path.of").count() >= 1, "No existe explicación de la anotación"
                    button = page.locator(".mjs-pdf-trigger")
                    button.wait_for(timeout=15000)
                    assert button.count() == 1, f"Icono PDF duplicado: {url}"
                    assert page.locator(".mjs-home-link").count() == 1
                    assert page.locator(".mjs-home-link").get_attribute("href") == base
                    assert not any("Inicio" in x for x in page.locator(".md-tabs__link").all_text_contents())
                    button.click()
                    links = page.locator(".mjs-pdf-menu a")
                    names = [f"{section}.pdf", "documentacion-completa.pdf"] if section else ["documentacion-completa.pdf"]
                    assert links.count() == len(names), (url, links.count(), names)
                    for i, filename in enumerate(names):
                        address = base + "descargas/" + filename
                        assert links.nth(i).get_attribute("href") == address, (url, address)
                        response = page.request.get(address)
                        assert response.ok and response.headers.get("content-type", "").startswith("application/pdf"), address
                    page.keyboard.press("Escape")
                    assert page.locator(".mjs-pdf-menu").is_hidden()
                    print(f"OK /{url}: {len(names)} descargas", flush=True)
                if errors:
                    raise AssertionError("Errores JavaScript: " + "; ".join(errors))
                print("OK: navegación y selector PDF verificados.", flush=True)
            finally:
                browser.close()
    finally:
        server.shutdown()
        server.server_close()

if __name__ == "__main__":
    check()
