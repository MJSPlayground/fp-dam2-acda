"""Prueba el backend MongoDB real y el recorrido de su frontend preparado.

Requiere aplicación iniciada contra la base QA. Limpia únicamente equipos propios.
"""
from __future__ import annotations

import argparse
from concurrent.futures import ThreadPoolExecutor
import json
from pathlib import Path
import uuid
from urllib.error import HTTPError
from urllib.parse import urlencode
from urllib.request import Request, urlopen

from playwright.sync_api import sync_playwright


def check(base, label):
    owner = "QA-" + uuid.uuid4().hex[:10]
    created = set()

    def api(method, path, body=None, content_type="application/json"):
        data = body if isinstance(body, bytes) else (None if body is None else json.dumps(body).encode())
        request = Request(base + "/api" + path, data=data, method=method, headers={"Content-Type": content_type})
        try:
            response = urlopen(request, timeout=20)
        except HTTPError as error:
            response = error
        raw = response.read()
        return response.status, json.loads(raw) if raw else None, response.headers

    try:
        status, sedes, _ = api("GET", "/sedes")
        assert status == 200 and any(sede["id"] == "CENTRO" for sede in sedes)
        team_body = {"nombre": owner, "categoria": "SENIOR", "sedeId": "CENTRO"}
        status, team, headers = api("POST", "/equipos", team_body)
        assert status == 201, (status, team)
        identifier = team["id"]
        created.add(identifier)
        assert headers["Location"].endswith("/api/equipos/" + identifier)
        player_body = {"nombre": "Álex, defensa", "dorsal": 7, "posicion": "MEDIO"}
        status, team, _ = api("POST", f"/equipos/{identifier}/jugadores", player_body)
        assert status == 200 and len(team["jugadores"]) == 1, (status, team)
        player = team["jugadores"][0]
        player_id = player["id"]
        stats_path = f"/equipos/{identifier}/jugadores/{player_id}/estadisticas"
        assert api("PATCH", stats_path, {"goles": 2, "asistencias": 1})[0] == 200
        status, saved, _ = api("PUT", f"/equipos/{identifier}", team_body | {"nombre": owner + " editado"})
        assert status == 200 and saved["jugadores"][0]["estadisticas"] == {"partidos": 1, "goles": 2, "asistencias": 1}
        status, saved, _ = api("PUT", f"/equipos/{identifier}/jugadores/{player_id}", player_body | {"nombre": "Álex actualizado"})
        assert status == 200 and saved["jugadores"][0]["estadisticas"]["goles"] == 2
        checks = [
            (api("GET", "/equipos/no-valido"), 400),
            (api("GET", "/equipos/000000000000000000000000"), 404),
            (api("GET", "/equipos?tamanio=0"), 400),
            (api("PATCH", "/sedes"), 405),
            (api("POST", "/equipos", b"texto", "text/plain"), 415),
            (api("POST", "/equipos", team_body | {"categoria": None}), 400),
            (api("POST", f"/equipos/{identifier}/jugadores", player_body), 409),
            (api("POST", f"/equipos/{identifier}/jugadores", player_body | {"dorsal": 8.5}), 400),
            (api("PATCH", stats_path, {"goles": -1, "asistencias": 0}), 400),
        ]
        for (status, error, headers), expected in checks:
            assert status == expected, (expected, status, error)
            assert error["estado"] == expected and {"mensaje", "ruta", "instante"} <= error.keys()
            if expected == 405:
                assert "GET" in headers["Allow"]
        query = urlencode({"categoria": "SENIOR", "posicion": "MEDIO", "pagina": 0, "tamanio": 20})
        status, page, _ = api("GET", "/equipos?" + query)
        assert status == 200 and any(equipo["id"] == identifier for equipo in page["elementos"])
        assert all(equipo["categoria"] == "SENIOR" and any(j["posicion"] == "MEDIO" for j in equipo["jugadores"]) for equipo in page["elementos"])
        with ThreadPoolExecutor(max_workers=5) as executor:
            results = list(executor.map(lambda _: api("PATCH", stats_path, {"goles": 1, "asistencias": 0})[0], range(20)))
        assert results == [200] * 20, results
        _, after, _ = api("GET", f"/equipos/{identifier}")
        assert after["jugadores"][0]["estadisticas"] == {"partidos": 21, "goles": 22, "asistencias": 1}, after

        captures = Path(__file__).resolve().parent.parent / "qa-captures"
        captures.mkdir(exist_ok=True)
        with sync_playwright() as playwright:
            browser = playwright.chromium.launch()
            try:
                page = browser.new_page(viewport={"width": 1366, "height": 900})
                errors = []
                page.on("pageerror", lambda error: errors.append(str(error)))
                page.goto(base, wait_until="networkidle")
                page.locator("#tamanio").fill("20")
                page.locator("#filtrar").click()
                row = page.locator("#equipos tr").filter(has_text=owner)
                row.wait_for()
                row.get_by_role("button", name="Editar", exact=True).click()
                page.locator('#equipoForm [name="nombre"]').fill(owner + " desde UI")
                page.locator('#equipoForm button[type="submit"]').click()
                page.locator("#equipoDialog").wait_for(state="hidden")
                row = page.locator("#equipos tr").filter(has_text=owner)
                row.get_by_role("button", name="Ver plantilla", exact=True).click()
                page.locator("#plantilla").wait_for(state="visible")
                player_row = page.locator("#jugadores tr").filter(has_text="Álex actualizado")
                player_row.wait_for()
                player_row.get_by_role("button", name="Editar", exact=True).click()
                page.locator('#jugadorForm [name="nombre"]').fill("Álex desde UI")
                page.locator('#jugadorForm button[type="submit"]').click()
                page.locator("#jugadorDialog").wait_for(state="hidden")
                _, after_ui, _ = api("GET", f"/equipos/{identifier}")
                assert after_ui["jugadores"][0]["estadisticas"] == after["jugadores"][0]["estadisticas"]
                page.locator("#nuevoJugador").click()
                assert page.locator('#jugadorForm [name="nombre"]').input_value() == ""
                page.locator('#jugadorForm [name="nombre"]').fill("No guardar")
                page.keyboard.press("Escape")
                page.locator("#jugadorDialog").wait_for(state="hidden")
                assert len(api("GET", f"/equipos/{identifier}")[1]["jugadores"]) == 1
                player_row = page.locator("#jugadores tr").filter(has_text="Álex desde UI")
                player_row.get_by_role("button", name="Estadísticas", exact=True).click()
                page.locator('#estadisticasForm [name="goles"]').fill("1")
                page.locator('#estadisticasForm [name="asistencias"]').fill("2")
                page.locator('#estadisticasForm button[type="submit"]').click()
                page.locator("#estadisticasDialog").wait_for(state="hidden")
                assert api("GET", f"/equipos/{identifier}")[1]["jugadores"][0]["estadisticas"] == {"partidos": 22, "goles": 23, "asistencias": 3}
                if label == "final":
                    page.set_viewport_size({"width": 1366, "height": 900})
                    player_row.get_by_role("button", name="Marcar no disponible", exact=True).click()
                    player_row.get_by_role("button", name="Marcar disponible", exact=True).wait_for()
                    assert not api("GET", f"/equipos/{identifier}")[1]["jugadores"][0]["disponible"]
                    page.locator("#filtroDisponible").select_option("true")
                    with page.expect_response(lambda response: f"/equipos/{identifier}/jugadores?" in response.url):
                        page.locator("#filtrarJugadores").click()
                    page.wait_for_function("document.querySelectorAll('#jugadores tr').length === 0")
                    page.locator("#filtroDisponible").select_option("false")
                    page.locator("#filtrarJugadores").click()
                    player_row.wait_for()
                    page.locator("#filtroDisponible").select_option("")
                    player_row.get_by_role("button", name="Marcar disponible", exact=True).click()
                    player_row.get_by_role("button", name="Marcar no disponible", exact=True).wait_for()
                    page.locator("#jornadaNueva").click()
                    page.locator('#jornadaForm [name="rival"]').fill("Rival QA")
                    page.locator('#jornadaForm [name="golesFavor"]').fill("1")
                    page.locator('#jornadaForm [name="golesContra"]').fill("0")
                    page.locator('#jornadaForm [name="aportaciones"]').fill("{")
                    page.locator('#jornadaForm button[type="submit"]').click()
                    page.locator("#jornadaForm .error").filter(has_text="lista JSON válida").wait_for()
                    page.locator('#jornadaForm [name="aportaciones"]').fill(json.dumps([{"jugadorId": player_id, "goles": 1, "asistencias": 0}]))
                    page.locator('#jornadaForm button[type="submit"]').click()
                    page.locator("#jornadaDialog").wait_for(state="hidden")
                    result = api("GET", f"/equipos/{identifier}")[1]
                    assert len(result["jornadas"]) == 1
                    assert result["jugadores"][0]["estadisticas"] == {"partidos": 23, "goles": 24, "asistencias": 3}
                    page.locator("#verRacha").click()
                    page.locator("#racha").filter(has_text="V").wait_for()
                    page.locator("#verClasificacion").click()
                    classification = page.locator("#clasificacion tr").filter(has_text=owner)
                    classification.wait_for()
                    assert classification.locator("td").all_text_contents()[1:4] == ["1", "3", "1"]
                    print("OK UI final MongoDB: disponibilidad/filtros, jornada con validación JSON, estadísticas coordinadas, racha y clasificación")
                for width in (1366, 390):
                    page.set_viewport_size({"width": width, "height": 900})
                    for scheme in ("light", "dark"):
                        page.emulate_media(color_scheme=scheme)
                        assert page.evaluate("document.documentElement.scrollWidth <= innerWidth + 2")
                        page.screenshot(path=str(captures / f"mongodb-app-{label}-{width}-{scheme}.png"), full_page=True)
                player_row.get_by_role("button", name="Eliminar", exact=True).click()
                page.locator("#confirmar").get_by_role("button", name="Cancelar", exact=True).click()
                assert len(api("GET", f"/equipos/{identifier}")[1]["jugadores"]) == 1
                assert not errors, errors
            finally:
                browser.close()
        print("OK MongoDB real: CRUD, consultas, 400/404/409/405 Allow/415, tipos inválidos, 20 incrementos concurrentes; UI conserva embebidos/estadísticas, cancelación y estilos responsive")
    finally:
        for identifier in created:
            api("DELETE", "/equipos/" + identifier)


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--base", default="http://127.0.0.1:8082")
    parser.add_argument("--label", choices=("guiado", "final"), default="guiado")
    args = parser.parse_args()
    check(args.base.rstrip("/"), args.label)
