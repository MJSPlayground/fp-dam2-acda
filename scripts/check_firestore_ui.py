"""Comprueba páginas Firestore y sus proyectos/Postman descargables."""
import json
from check_topic_ui import check_topic
from test_project_packages import check_postman_urls, postman_requests

PAGES = ("documentos", "configuracion", "persistencia", "consultas", "rest", "proyecto", "practica-final", "repaso", "ampliacion")


def inspect_archive(archive, names, name):
    assert {"firebase.json", "postman/reservas.postman_collection.json"} <= names, name
    collection = json.loads(archive.read("postman/reservas.postman_collection.json"))
    check_postman_urls(collection["item"])
    assert collection["info"]["schema"].endswith("/v2.1.0/collection.json")
    items = list(postman_requests(collection["item"]))
    assert len(items) >= 6, "Faltan escenarios Postman"
    methods = {item["request"]["method"] for item in items}
    assert {"GET", "POST", "DELETE"} <= methods, methods
    assert any(item.get("event") for item in items), "Colección sin comprobaciones de respuesta"


if __name__ == "__main__":
    check_topic("firestore", PAGES, "reservas", inspect_archive)
