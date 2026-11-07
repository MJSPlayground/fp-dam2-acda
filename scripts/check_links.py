"""Comprueba todos los enlaces locales del sitio y rutas GitHub del propio repositorio."""
from __future__ import annotations

from pathlib import Path
from urllib.parse import unquote, urljoin, urlparse
from bs4 import BeautifulSoup

ROOT = Path(__file__).resolve().parent.parent
SITE = ROOT / "site"
PREFIX = "/fp-dam2-acda/"
REPO = "github.com"


def check():
    pages = [p for p in SITE.rglob("index.html") if "_impresion_temporal" not in p.parts]
    assert pages, "Compila el sitio antes de comprobar enlaces"
    comprobados = 0
    fragmentos = 0
    ids_by_path = {}
    missing_fragments = []
    for page in pages:
        rel = page.relative_to(SITE).as_posix()
        url = "https://mjsplayground.github.io" + PREFIX + rel
        soup = BeautifulSoup(page.read_text(encoding="utf-8"), "html.parser")
        assert soup.select_one("article h1"), f"Sin título: {rel}"
        for tag in soup.select("article a[href], article img[src]"):
            target = tag.get("href") or tag.get("src")
            if not target or target.startswith(("mailto:", "tel:", "data:")):
                continue
            parsed = urlparse(urljoin(url, target))
            if parsed.netloc == "mjsplayground.github.io":
                assert parsed.path.startswith(PREFIX), f"Enlace fuera del sitio: {rel} → {target}"
                local = SITE / unquote(parsed.path[len(PREFIX):])
                if local.is_dir():
                    local /= "index.html"
                assert local.is_file(), f"Enlace roto: {rel} → {target}"
                comprobados += 1
                fragment = unquote(parsed.fragment).split(":~:text=", 1)[0]
                if fragment and local.suffix.lower() == ".html":
                    if local not in ids_by_path:
                        destination = soup if local == page else BeautifulSoup(local.read_text(encoding="utf-8"), "html.parser")
                        ids_by_path[local] = {element["id"] for element in destination.select("[id]")}
                        ids_by_path[local].update(element["name"] for element in destination.select("a[name]"))
                    fragmentos += 1
                    if fragment not in ids_by_path[local]:
                        missing_fragments.append(f"{rel} → {target} (no existe #{fragment})")
            elif parsed.netloc == REPO and any(parsed.path.startswith(f"/MJSPlayground/fp-dam2-acda/{kind}/main/") for kind in ("tree", "blob")):
                local = ROOT / unquote(parsed.path.split("/main/", 1)[1])
                assert local.exists(), f"Proyecto enlazado inexistente: {rel} → {target}"
                comprobados += 1
    assert not missing_fragments, "Fragmentos HTML rotos:\n" + "\n".join(missing_fragments)
    print(f"OK: {len(pages)} páginas, {comprobados} enlaces internos/proyectos y {fragmentos} fragmentos HTML comprobados")


if __name__ == "__main__":
    check()
