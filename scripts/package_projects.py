#!/usr/bin/env python3
"""Empaqueta proyectos Maven individuales en el sitio ya construido."""
from __future__ import annotations

import argparse
import re
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DESTINATION = ROOT / "site/descargas/codigo"
PROJECTS = {
    "inventario-directorios-inicial.zip": ROOT / "proyectos/ficheros/codelab-inicial",
    "inventario-directorios-solucion.zip": ROOT / "proyectos/ficheros/codelab-solucion",
}
REQUIRED = {"pom.xml", "README.md", "mvnw", "mvnw.cmd", ".mvn/wrapper/maven-wrapper.properties", ".mvn/wrapper/maven-wrapper.jar"}
EXCLUDED_DIRS = {"target", ".idea", ".m2-cache", ".venv", ".git", "trabajo-codelab", "qa-captures", "__pycache__"}
EXCLUDED_SUFFIXES = {".zip", ".log", ".tmp", ".bak", ".swp", ".pyc", ".iml", ".png", ".webp"}


def included(path: Path) -> bool:
    if any(part in EXCLUDED_DIRS for part in path.parts):
        return False
    if path.suffix.lower() in EXCLUDED_SUFFIXES or path.name.startswith((".env", "~")):
        return False
    if re.search(r"(?i)(secret|credential|password|token)", path.name):
        return False
    return True


def package(name: str, project: Path) -> Path:
    files = sorted((p for p in project.rglob("*") if p.is_file() and included(p.relative_to(project))), key=lambda p: p.as_posix())
    names = {p.relative_to(project).as_posix() for p in files}
    missing = REQUIRED - names
    if missing or not any(n.startswith("src/main/java/") and n.endswith(".java") for n in names):
        raise ValueError(f"Proyecto incompleto {project}: {missing}")
    if name.endswith("inicial.zip") and any("solucion" in n.lower() for n in names):
        raise ValueError("El proyecto inicial contiene una solución")
    DESTINATION.mkdir(parents=True, exist_ok=True)
    target = DESTINATION / name
    with zipfile.ZipFile(target, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
        for path in files:
            rel = path.relative_to(project).as_posix()
            info = zipfile.ZipInfo(rel, date_time=(2020, 1, 1, 0, 0, 0))
            info.compress_type = zipfile.ZIP_DEFLATED
            info.external_attr = ((0o755 if rel == "mvnw" else 0o644) << 16)
            archive.writestr(info, path.read_bytes(), compress_type=zipfile.ZIP_DEFLATED, compresslevel=9)
    print(f"{target.relative_to(ROOT)}: {target.stat().st_size} bytes, {len(files)} archivos")
    return target


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true", help="Comprueba ZIP existentes sin generarlos")
    args = parser.parse_args()
    if args.check:
        for name in PROJECTS:
            target = DESTINATION / name
            if not target.is_file():
                raise FileNotFoundError(target)
            with zipfile.ZipFile(target) as archive:
                names = set(archive.namelist())
                assert not archive.testzip(), f"ZIP corrupto: {name}"
                assert REQUIRED <= names and any(n.startswith("src/main/java/") for n in names), name
                assert all(included(Path(n)) for n in names), name
                if name.endswith("inicial.zip"):
                    source = archive.read("src/main/java/es/mjsplayground/acda/ficheros/codelab/InventarioCodelab.java").decode("utf-8")
                    assert "TODO 5" in source and "Files.walkFileTree(" not in source
            print(f"OK {name}: {target.stat().st_size} bytes")
    else:
        for name, project in PROJECTS.items():
            package(name, project)


if __name__ == "__main__":
    main()
