"""Comprueba contenido y compilación real de ambos ZIP en carpetas limpias."""
from __future__ import annotations

import os
import json
import subprocess
import sys
import tempfile
import zipfile
from pathlib import Path
from urllib.parse import parse_qsl, urlparse

from package_projects import DESTINATION, EXCLUDED_DIRS, PROJECTS, REQUIRED


def check_postman_urls(items):
    """Postman ignores raw query text when a structured URL omits query[]."""
    for item in items:
        url = item.get("request", {}).get("url")
        if isinstance(url, dict):
            expected = set(key for key, _ in parse_qsl(urlparse(url.get("raw", "")).query))
            actual = {parameter["key"] for parameter in url.get("query", []) if not parameter.get("disabled")}
            assert expected <= actual, f"Parámetros Postman omitidos en {item['name']}: {expected - actual}"
        check_postman_urls(item.get("item", []))


def check(name: str) -> None:
    archive_path = DESTINATION / name
    assert archive_path.is_file(), archive_path
    with tempfile.TemporaryDirectory(prefix="acda-zip-") as temp:
        root = Path(temp)
        with zipfile.ZipFile(archive_path) as archive:
            names = set(archive.namelist())
            assert archive.testzip() is None, name
            assert REQUIRED <= names, REQUIRED - names
            assert any(n.startswith("src/main/java/") and n.endswith(".java") for n in names)
            assert all(not (set(Path(item).parts) & EXCLUDED_DIRS) for item in names)
            assert all(not item.endswith((".zip", ".log", ".tmp", ".class")) for item in names)
            if name.startswith("reservas-"):
                assert {"firebase.json", "postman/reservas.postman_collection.json"} <= names
                collection = json.loads(archive.read("postman/reservas.postman_collection.json"))
                assert collection["info"]["schema"].endswith("/v2.1.0/collection.json")
                assert collection["item"], "Colección Postman vacía"
                check_postman_urls(collection["item"])
                emulator = json.loads(archive.read("firebase.json"))
                assert emulator["emulators"]["firestore"]["port"] == 8085
                assert not any(b"-----BEGIN PRIVATE KEY-----" in archive.read(item) for item in names)
            if name.startswith("inventario-directorios-"):
                source = archive.read("src/main/java/es/mjsplayground/acda/ficheros/codelab/InventarioCodelab.java").decode()
            if name == "inventario-directorios-inicial.zip":
                assert "TODO 5" in source and "TODO 9" in source
                assert "Files.walkFileTree(" not in source and "ExportadorCsv.guardar(informe, filas);" not in source
                assert "src/test/java/es/mjsplayground/acda/ficheros/codelab/InventarioCodelabTest.java" not in names
            elif name == "inventario-directorios-solucion.zip":
                assert "TODO" not in source and "Files.walkFileTree(" in source
                assert "src/test/java/es/mjsplayground/acda/ficheros/codelab/InventarioCodelabTest.java" in names
            archive.extractall(root)
        assert (root / "pom.xml").is_file() and (root / ".mvn/wrapper/maven-wrapper.properties").is_file()
        command = ["cmd", "/c", "mvnw.cmd", "test", "--batch-mode"] if os.name == "nt" else ["sh", "mvnw", "test", "--batch-mode"]
        subprocess.run(command, cwd=root, check=True)
        print(f"OK {name}: {archive_path.stat().st_size} bytes, {len(names)} archivos, Maven Wrapper test", flush=True)


if __name__ == "__main__":
    for package_name in PROJECTS:
        check(package_name)
