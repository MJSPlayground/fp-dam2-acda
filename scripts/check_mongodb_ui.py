"""Recorrido de MongoDB y sus cuatro descargables con Compose y Postman."""
import json
from check_topic_ui import check_topic
from test_project_packages import check_postman_urls, postman_requests

PAGES = ("documentos", "configuracion", "persistencia", "consultas", "atomicidad", "proyecto", "practica-final", "repaso", "ampliacion")


def inspect_archive(archive, names, name):
    assert "compose.yaml" in names, name
    assert "src/main/resources/static/index.html" in names, name
    collections = [member for member in names if member.startswith("postman/") and member.endswith(".postman_collection.json")]
    assert len(collections) == 1, name
    collection = json.loads(archive.read(collections[0]))
    check_postman_urls(collection["item"])
    assert collection["info"]["schema"].endswith("/v2.1.0/collection.json")
    items = list(postman_requests(collection["item"]))
    assert {"GET", "POST", "DELETE"} <= {item["request"]["method"] for item in items}
    assert any(item.get("event") for item in items), "Colección sin comprobaciones de respuesta"


if __name__ == "__main__":
    check_topic("mongodb", PAGES, "jugadores", inspect_archive)
