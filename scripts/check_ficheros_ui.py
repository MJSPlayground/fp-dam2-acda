"""Recorrido vigente de Ficheros y sus cuatro proyectos Maven descargables."""
from check_topic_ui import check_topic

PAGES = ("rutas", "texto-csv", "json", "binarios", "arquitectura", "proyecto", "practica-final", "repaso", "ampliacion")


def inspect_archive(archive, names, name):
    assert any(member.startswith("src/main/resources/") and member.endswith(".css") for member in names), name
    assert any(member.startswith("src/main/resources/") and member.endswith("branding/mjs-playground.png") for member in names), name
    assert any(member.startswith("src/main/java/") and member.endswith("/app/Launcher.java") for member in names), name


if __name__ == "__main__":
    check_topic("ficheros", PAGES, "inventario", inspect_archive)
