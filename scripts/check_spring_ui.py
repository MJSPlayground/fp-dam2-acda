"""Comprueba Spring y cuatro descargables Maven con Thymeleaf y MySQL."""
from check_topic_ui import check_topic

PAGES = ("componentes", "configuracion", "persistencia", "servicios", "transacciones", "proyecto", "practica-final", "repaso", "ampliacion")


def inspect_archive(archive, names, name):
    assert {"compose.yaml", "Dockerfile"} <= names, name
    assert any(member.startswith("src/main/java/") and member.endswith("CatalogoApplication.java") for member in names), name
    assert any(member.startswith("src/main/resources/templates/") and member.endswith(".html") for member in names), name
    assert any(member.startswith("src/main/resources/static/") and member.endswith(".css") for member in names), name


if __name__ == "__main__":
    check_topic("spring", PAGES, "catalogo", inspect_archive)
