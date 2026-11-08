"""Detecta lenguaje de mantenimiento interno en las páginas para estudiantes.

Revisa Markdown y componentes de docs y navegación. --site comprueba también
el texto renderizado; --pdf comprueba los PDF generados. No prohíbe «commit»
de una transacción, historial de negocio, autoría ni documentación de GitHub.
"""
from __future__ import annotations

import argparse
from pathlib import Path
import re


ROOT = Path(__file__).resolve().parent.parent
RULES = {
    "responsabilidad interna": r"\b(?:docente|profesor(?:a|ado)?|profesorado)\b",
    "aprobación o currículo interno": r"\b(?:índice|temario|planificación)\s+(?:intern[oa]|aprobad[oa])|revisión\s+pedagógica|\bUT\s*\d+\b|\bRA\d+\b",
    "registro de desarrollo": r"CONTENIDOS_ACDA|PLAN-DESARROLLO|ESTADO-DESARROLLO|\bworkflow(?:s)?\b|GitHub\s+Actions|\bagentes?\s+(?:de\s+IA|revisores?|implementadores?)\b",
    "estado de publicación": r"material(?:es)?\s+(?:preparad[oa]s?|list[oa]s?)\s+para\s+revisión|material(?:es)?\s+históric[oa]s?|archivo\s+histórico\s+del?\s+temario|referencias/ficheros-historico|versiones?\s+históricas?\s+del?\s+(?:temario|material|contenido)|contenido\s+crecerá|bloques\s+se\s+desarrollarán",
}
PATTERNS = {name: re.compile(pattern, re.IGNORECASE) for name, pattern in RULES.items()}


def findings(text):
    return [name for name, pattern in PATTERNS.items() if pattern.search(text)]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--site", action="store_true")
    parser.add_argument("--pdf", action="store_true")
    args = parser.parse_args()
    errors = []
    sources = sorted(p for p in (ROOT / "docs").rglob("*") if p.suffix in {".md", ".html", ".svg", ".js"})
    sources.append(ROOT / "zensical.toml")
    for path in sources:
        for number, line in enumerate(path.read_text(encoding="utf-8-sig").splitlines(), 1):
            for reason in findings(line):
                errors.append(f"{path.relative_to(ROOT)}:{number}: {reason}: {line.strip()[:150]}")
    if args.site:
        from bs4 import BeautifulSoup
        pages = list((ROOT / "site").rglob("*.html"))
        assert pages, "Construye el sitio antes de usar --site"
        for path in pages:
            soup = BeautifulSoup(path.read_text(encoding="utf-8"), "html.parser")
            for hidden in soup.select("script, style"):
                hidden.decompose()
            for reason in findings(soup.get_text(" ", strip=True)):
                errors.append(f"{path.relative_to(ROOT)}: {reason} en texto renderizado")
    if args.pdf:
        from pypdf import PdfReader
        files = list((ROOT / "docs/descargas").glob("*.pdf"))
        assert files, "Genera los PDF antes de usar --pdf"
        for path in files:
            for number, page in enumerate(PdfReader(path).pages, 1):
                text = " ".join((page.extract_text() or "").split())
                for reason in findings(text):
                    errors.append(f"{path.name}:{number}: {reason} en PDF")
    if errors:
        raise SystemExit("Revisar posibles referencias internas (corregir según contexto):\n" + "\n".join(errors))
    print(f"OK voz del estudiante: {len(sources)} fuentes" + (", HTML" if args.site else "") + (", PDF" if args.pdf else ""))


if __name__ == "__main__":
    main()
