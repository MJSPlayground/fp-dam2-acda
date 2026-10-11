"""Comprueba contenido y compilación real de ambos ZIP en carpetas limpias."""
from __future__ import annotations

import os
import subprocess
import sys
import tempfile
import zipfile
from pathlib import Path

from package_projects import DESTINATION, EXCLUDED_DIRS, PROJECTS, REQUIRED


def check(name: str) -> None:
    archive_path = DESTINATION / name
    assert archive_path.is_file(), archive_path
    with tempfile.TemporaryDirectory(prefix="acda-zip-") as temp:
        root = Path(temp)
        with zipfile.ZipFile(archive_path) as archive:
            names = set(archive.namelist())
            assert archive.testzip() is None, name
            assert REQUIRED <= names, REQUIRED - names
            assert "src/main/java/es/mjsplayground/acda/ficheros/codelab/PrepararDatos.java" in names
            assert "src/main/java/es/mjsplayground/acda/ficheros/codelab/ExportadorCsv.java" in names
            assert all(not (set(Path(item).parts) & EXCLUDED_DIRS) for item in names)
            assert all(not item.endswith((".zip", ".log", ".tmp", ".class")) for item in names)
            source = archive.read("src/main/java/es/mjsplayground/acda/ficheros/codelab/InventarioCodelab.java").decode()
            if name.endswith("inicial.zip"):
                assert "TODO 5" in source and "TODO 9" in source
                assert "Files.walkFileTree(" not in source and "ExportadorCsv.guardar(informe, filas);" not in source
                assert "src/test/java/es/mjsplayground/acda/ficheros/codelab/InventarioCodelabTest.java" not in names
            else:
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
