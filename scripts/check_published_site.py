"""Comprueba Pages después del despliegue; no se ejecuta durante la construcción."""
from __future__ import annotations

import argparse
from io import BytesIO
from pathlib import Path
import tomllib
from urllib.parse import urljoin, urlparse
from zipfile import ZipFile

from playwright.sync_api import sync_playwright
from pypdf import PdfReader
from package_projects import PROJECTS, REQUIRED, included, has_learning_tests


ROOT = Path(__file__).resolve().parent.parent


def nav_pages(node):
    if isinstance(node, str) and node.endswith(".md"):
        yield node
    elif isinstance(node, dict):
        for value in node.values():
            yield from nav_pages(value)
    elif isinstance(node, list):
        for value in node:
            yield from nav_pages(value)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--topic", action="append", help="Limita la comprobación a este tema; se puede repetir")
    args = parser.parse_args()
    with (ROOT / "zensical.toml").open("rb") as handle:
        config = tomllib.load(handle)["project"]
    base = config["site_url"].rstrip("/") + "/"
    pages = list(dict.fromkeys(nav_pages(config["nav"])))
    topics = {path.split("/")[0] for path in pages}
    selected = set(args.topic or topics)
    assert selected <= topics, f"Temas desconocidos: {selected - topics}"
    pages = [path for path in pages if path.split("/")[0] in selected]
    expected = {name for name, source in PROJECTS.items() if source.relative_to(ROOT / "proyectos").parts[0] in selected}
    downloads = set()
    with sync_playwright() as playwright:
        browser = playwright.chromium.launch()
        try:
            page = browser.new_page(viewport={"width": 1366, "height": 900})
            errors = []
            page.on("pageerror", lambda error: errors.append(str(error)))
            response = page.goto(base, wait_until="networkidle")
            assert response and response.status == 200 and page.locator("h1").is_visible(), base
            for path in pages:
                local = Path(path)
                route = str(local.parent if local.name == "index.md" else local.with_suffix("")).replace("\\", "/")
                response = page.goto(urljoin(base, route + "/"), wait_until="networkidle")
                assert response and response.status == 200 and page.locator("h1").is_visible(), path
                heading = next(line[2:].strip() for line in (ROOT / "docs" / path).read_text(encoding="utf-8-sig").splitlines() if line.startswith("# "))
                assert " ".join(page.locator("h1").inner_text().split()) == " ".join(heading.split()), path
                assert "En construcción" not in page.locator("article").inner_text(), path
                downloads.update(page.locator('article a[download][href$=".zip"]').evaluate_all("els => els.map(e => e.href)"))
                if local.name == "index.md":
                    (ROOT / "qa-captures").mkdir(exist_ok=True)
                    page.screenshot(path=str(ROOT / "qa-captures" / f"publico-{local.parent.name}.png"))
                print(f"OK página publicada: {route}", flush=True)
            assert {urlparse(url).path.rsplit("/", 1)[-1] for url in downloads} == expected, "Descargas públicas distintas del manifiesto"
            for url in sorted(downloads):
                assert url.startswith(base), url
                response = page.request.get(url)
                assert response.status == 200, url
                with ZipFile(BytesIO(response.body())) as archive:
                    names = set(archive.namelist())
                    assert archive.testzip() is None and REQUIRED <= names, url
                    assert has_learning_tests(names), f"ZIP sin las comprobaciones de aprendizaje: {url}"
                    assert all(included(Path(name)) for name in names), url
                print(f"OK ZIP publicado: {url.rsplit('/', 1)[-1]}", flush=True)
            for topic in sorted(selected | {"documentacion-completa"}):
                response = page.request.get(urljoin(base, f"descargas/{topic}.pdf"))
                assert response.status == 200 and response.body().startswith(b"%PDF"), topic
                count = len(PdfReader(BytesIO(response.body())).pages)
                assert count > 1, topic
                print(f"OK PDF publicado: {topic}, {count} páginas", flush=True)
            assert not errors, errors
        finally:
            browser.close()
    print(f"OK Pages real: portada y {len(pages)} páginas de navegación, {len(downloads)} ZIP y {len(selected) + 1} PDF")


if __name__ == "__main__":
    main()
