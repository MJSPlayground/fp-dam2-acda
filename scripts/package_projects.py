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
    "catalogo-guiado-inicial.zip": ROOT / "proyectos/spring/guiado-inicial",
    "catalogo-guiado-solucion.zip": ROOT / "proyectos/spring/guiado-solucion",
    "catalogo-final-inicial.zip": ROOT / "proyectos/spring/final-inicial",
    "catalogo-final-solucion.zip": ROOT / "proyectos/spring/final-solucion",

    "biblioteca-guiado-inicial.zip": ROOT / "proyectos/hibernate/guiado-inicial",
    "biblioteca-guiado-solucion.zip": ROOT / "proyectos/hibernate/guiado-solucion",
    "biblioteca-final-inicial.zip": ROOT / "proyectos/hibernate/final-inicial",
    "biblioteca-final-solucion.zip": ROOT / "proyectos/hibernate/final-solucion",
    "inventario-guiado-inicial.zip": ROOT / "proyectos/ficheros/guiado-inicial",
    "inventario-guiado-solucion.zip": ROOT / "proyectos/ficheros/guiado-solucion",
    "inventario-final-inicial.zip": ROOT / "proyectos/ficheros/final-inicial",
    "inventario-final-solucion.zip": ROOT / "proyectos/ficheros/final-solucion",
    "jugadores-guiado-inicial.zip": ROOT / "proyectos/mongodb/guiado-inicial",
    "jugadores-guiado-solucion.zip": ROOT / "proyectos/mongodb/guiado-solucion",
    "jugadores-final-inicial.zip": ROOT / "proyectos/mongodb/final-inicial",
    "jugadores-final-solucion.zip": ROOT / "proyectos/mongodb/final-solucion",
    "reservas-guiado-inicial.zip": ROOT / "proyectos/firestore/guiado-inicial",
    "reservas-guiado-solucion.zip": ROOT / "proyectos/firestore/guiado-solucion",
    "reservas-final-inicial.zip": ROOT / "proyectos/firestore/final-inicial",
    "reservas-final-solucion.zip": ROOT / "proyectos/firestore/final-solucion",
    "almacen-final-inicial.zip": ROOT / "proyectos/jdbc/final-inicial",
    "almacen-final-solucion.zip": ROOT / "proyectos/jdbc/final-solucion",
    "almacen-guiado-inicial.zip": ROOT / "proyectos/jdbc/guiado-inicial",
    "almacen-guiado-solucion.zip": ROOT / "proyectos/jdbc/guiado-solucion",
}
REQUIRED = {"pom.xml", "README.md", "mvnw", "mvnw.cmd", ".mvn/wrapper/maven-wrapper.properties", ".mvn/wrapper/maven-wrapper.jar"}
LEARNING_TEST_FILES = {"FasesGuiadoTest.java", "FasesFinalTest.java", "AprendizajeGuiadoTest.java", "AprendizajeFinalTest.java", "FasesAprendizajeTest.java", "NivelesAprendizajeTest.java"}


def has_learning_tests(names):
    return any(Path(name).name in LEARNING_TEST_FILES for name in names)


EXCLUDED_DIRS = {"target", ".idea", ".m2-cache", ".venv", ".git", "trabajo-codelab", "qa-captures", "__pycache__", "node_modules", ".firebase", ".cache"}
EXCLUDED_SUFFIXES = {".zip", ".log", ".tmp", ".bak", ".swp", ".pyc", ".iml", ".class", ".db", ".sqlite"}


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
        published = {path.name for path in DESTINATION.glob("*.zip")}
        assert published == set(PROJECTS), f"Descargas distintas del manifiesto vigente: {published ^ set(PROJECTS)}"
        for name, project in PROJECTS.items():
            target = DESTINATION / name
            if not target.is_file():
                raise FileNotFoundError(target)
            with zipfile.ZipFile(target) as archive:
                names = set(archive.namelist())
                assert not archive.testzip(), f"ZIP corrupto: {name}"
                assert REQUIRED <= names and any(n.startswith("src/main/java/") for n in names), name
                assert all(included(Path(n)) for n in names), name
                current = {p.relative_to(project).as_posix(): p for p in project.rglob("*") if p.is_file() and included(p.relative_to(project))}
                assert names == set(current), f"ZIP desactualizado (archivos distintos): {name}"
                for member, source_file in current.items():
                    assert archive.read(member) == source_file.read_bytes(), f"ZIP desactualizado: {name} → {member}"
            print(f"OK {name}: {target.stat().st_size} bytes")
    else:
        for name, project in PROJECTS.items():
            package(name, project)


if __name__ == "__main__":
    main()
