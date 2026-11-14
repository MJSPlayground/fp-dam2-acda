"""Detecta lenguaje de mantenimiento interno en las páginas para estudiantes.

Revisa Markdown, componentes, navegación y README de proyectos distribuidos.
--site comprueba el texto renderizado; --pdf los PDF; --packages los README
dentro de los ZIP publicados. No prohíbe «commit»
de una transacción, historial de negocio, autoría ni documentación de GitHub.
"""
from __future__ import annotations

import argparse
from pathlib import Path
import re
import zipfile

from package_projects import PROJECTS, DESTINATION, included


ROOT = Path(__file__).resolve().parent.parent
RULES = {
    "responsabilidad interna": r"\b(?:docente|profesor(?:a|ado)?|profesorado)\b",
    "aprobación o currículo interno": r"\b(?:índice|temario|planificación)\s+(?:intern[oa]|aprobad[oa])|revisión\s+pedagógica|\bUT\s*\d+\b|\bRA\d+\b",
    "registro de desarrollo": r"CONTENIDOS_ACDA|PLAN-DESARROLLO|ESTADO-DESARROLLO|\bworkflow(?:s)?\b|GitHub\s+Actions|\bagentes?\s+(?:de\s+IA|revisores?|implementadores?)\b",
    "estado de publicación": r"material(?:es)?\s+(?:preparad[oa]s?|list[oa]s?)\s+para\s+revisión|material(?:es)?\s+históric[oa]s?|archivo\s+histórico\s+del?\s+temario|referencias/ficheros-historico|versiones?\s+históricas?\s+del?\s+(?:temario|material|contenido)|contenido\s+crecerá|bloques\s+se\s+desarrollarán",
    "notas de implementación interna": r"\bQA\s+opcional\b|\b(?:clases|código|implementaciones)\s+históric[oa]s?\b|\badaptad[oa]\s+del?\s+\w+\s+históric[oa]\b|\b(?:decisión|decisiones)\s+pedagógicas?\b|\b(?:nota|notas)\s+(?:interna|internas|de desarrollo)\b|\bauditoría\s+(?:pedagógica|del?\s+agente|de\s+materiales)\b|\bagentes? de desarrollo\b|\ben esta sesión\b",
}
PATTERNS = {name: re.compile(pattern, re.IGNORECASE) for name, pattern in RULES.items()}


def findings(text):
    return [name for name, pattern in PATTERNS.items() if pattern.search(text)]


def distributed_readmes(projects=None):
    """El manifiesto de descargas evita incluir proyectos históricos no publicados."""
    projects = PROJECTS if projects is None else projects
    return sorted({p for project in projects.values() for p in project.rglob("*")
                   if p.is_file() and p.name.lower().startswith("readme")
                   and included(p.relative_to(project))})


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--site", action="store_true")
    parser.add_argument("--pdf", action="store_true")
    parser.add_argument("--packages", action="store_true")
    args = parser.parse_args()
    errors = []
    sources = sorted(p for p in (ROOT / "docs").rglob("*") if p.suffix in {".md", ".html", ".svg", ".js"})
    sources.append(ROOT / "zensical.toml")
    sources.extend(distributed_readmes())
    for path in sources:
        for number, line in enumerate(path.read_text(encoding="utf-8-sig").splitlines(), 1):
            for reason in findings(line):
                errors.append(f"{path.relative_to(ROOT)}:{number}: {reason}: {line.strip()[:150]}")
    if args.packages:
        for name in PROJECTS:
            with zipfile.ZipFile(DESTINATION / name) as archive:
                for member in archive.namelist():
                    if not Path(member).name.lower().startswith("readme"):
                        continue
                    for number, line in enumerate(archive.read(member).decode("utf-8-sig").splitlines(), 1):
                        for reason in findings(line):
                            errors.append(f"{name}!{member}:{number}: {reason}: {line.strip()[:150]}")
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
    print(f"OK voz del estudiante: {len(sources)} fuentes" + (", HTML" if args.site else "") + (", PDF" if args.pdf else "") + (", README en ZIP" if args.packages else ""))


if __name__ == "__main__":
    main()
