"""Comprueba el tema Hibernate y sus cuatro descargables Maven con JavaFX y MySQL."""
from check_topic_ui import check_topic

PAGES = ("arquitectura", "configuracion", "entidades", "persistencia", "relaciones", "consultas", "proyecto", "practica-final", "repaso", "ampliacion")


def inspect_archive(archive, names, name):
    assert "compose.yaml" in names, name
    assert any(member.startswith("src/main/java/") and member.endswith("/app/Launcher.java") for member in names), name
    assert any(member.startswith("src/main/resources/") and member.endswith(".css") for member in names), name
    assert any(member.startswith("src/main/resources/") and member.endswith("branding/mjs-playground.png") for member in names), name


if __name__ == "__main__":
    check_topic("hibernate", PAGES, "biblioteca", inspect_archive)
