"""Prueba HTTP y flujos de la aplicación real de reservas contra el emulador.

Requiere backend iniciado. No utiliza credenciales ni sustituye el repositorio por mocks.
Solo elimina las reservas que crea esta ejecución.
"""
from __future__ import annotations

import argparse
import json
import uuid
from pathlib import Path

from playwright.sync_api import sync_playwright


def check(base: str, label: str = "guiado"):
    responsable = "QA-" + uuid.uuid4().hex[:10]
    seed = uuid.uuid4().int
    date = f"{2100 + seed % 6000:04d}-{1 + (seed // 6000) % 12:02d}-{1 + (seed // 72000) % 28:02d}"
    body = {"espacio": "SALA-A", "fecha": date, "inicio": 10, "fin": 12,
            "responsable": responsable, "asistentes": 8,
            "equipo": ["Proyector, HDMI", "Cable\nlargo"],
            "detalles": {"actividad": "Taller", "contacto": "Recepción", "notas": "Prueba real"}}
    captures = Path(__file__).resolve().parent.parent / "qa-captures"
    captures.mkdir(exist_ok=True)
    created = set()
    with sync_playwright() as playwright:
        browser = playwright.chromium.launch()
        page = browser.new_page(viewport={"width": 1366, "height": 900})
        errors = []
        page.on("pageerror", lambda error: errors.append(str(error)))
        api = page.request
        try:
            response = api.post(base + "/api/reservas", data=body)
            assert response.status == 201, response.text()
            reservation = response.json()
            identifier = reservation["id"]
            created.add(identifier)
            assert response.headers["location"].endswith("/api/reservas/" + identifier)
            assert api.get(base + "/api/reservas/" + identifier).json()["responsable"] == responsable
            for _ in range(2):
                assert api.put(base + "/api/reservas/" + identifier, data=body).status == 200
            filters = api.get(base + "/api/reservas", params={"espacio": "SALA-A", "fecha": body["fecha"], "minAsistentes": 5})
            assert filters.ok and any(item["id"] == identifier for item in filters.json()), filters.text()
            for response, status in (
                (api.post(base + "/api/reservas", data=body | {"asistentes": 13}), 400),
                (api.get(base + "/api/reservas/qa-inexistente"), 404),
                (api.get(base + "/api/reservas", params={"minAsistentes": 0}), 400),
                (api.post(base + "/api/reservas", data="{", headers={"Content-Type": "application/json"}), 400),
            ):
                assert response.status == status, (status, response.status, response.text())
                assert {"estado", "mensaje", "ruta", "instante"} <= response.json().keys()

            # A UI edit of one field must retain arbitrary list/map data from the API.
            page.goto(base, wait_until="networkidle")
            row = page.locator("#reservas tr").filter(has_text=responsable)
            row.get_by_role("button", name="Editar", exact=True).click()
            assert json.loads(page.locator('[name="equipo"]').input_value()) == body["equipo"]
            assert json.loads(page.locator('[name="detalles"]').input_value()) == body["detalles"]
            page.locator('[name="responsable"]').fill(responsable + " API")
            page.locator('#formulario button[type="submit"]').click()
            page.locator("#editor").wait_for(state="hidden")
            edited = api.get(base + "/api/reservas/" + identifier).json()
            assert edited["equipo"] == body["equipo"], "La edición UI alteró los elementos de la lista"
            assert edited["detalles"] == body["detalles"], "La edición UI perdió campos del mapa"
            assert api.delete(base + "/api/reservas/" + identifier).status == 204
            created.remove(identifier)
            assert api.get(base + "/api/reservas/" + identifier).status == 404

            page.goto(base, wait_until="networkidle")
            page.locator("#nuevo").click()
            dialog = page.locator("#editor")
            assert dialog.is_visible()
            page.locator('[name="fecha"]').fill(body["fecha"])
            page.locator('[name="responsable"]').fill(responsable)
            page.locator('[name="asistentes"]').fill("13")
            page.locator('[name="equipo"]').fill("{")
            page.locator('#formulario button[type="submit"]').click()
            page.locator("#errorFormulario").filter(has_text="lista JSON válida").wait_for()
            assert dialog.is_visible()
            page.locator('[name="equipo"]').fill("[]")
            page.locator('#formulario button[type="submit"]').click()
            page.locator("#errorFormulario").filter(has_text="capacidad").wait_for()
            assert dialog.is_visible(), "El formulario debe conservarse ante un error"
            page.locator('[name="asistentes"]').fill("8")
            page.locator('#formulario button[type="submit"]').click()
            dialog.wait_for(state="hidden")
            row = page.locator("#reservas tr").filter(has_text=responsable)
            row.wait_for()
            records = api.get(base + "/api/reservas", params={"fecha": body["fecha"]}).json()
            created.update(record["id"] for record in records if record["responsable"] == responsable)
            page.locator("#nuevo").click()
            assert page.locator('[name="responsable"]').input_value() == ""
            page.locator('[name="responsable"]').fill("Sin guardar")
            page.keyboard.press("Escape")
            dialog.wait_for(state="hidden")
            row.get_by_role("button", name="Editar", exact=True).click()
            page.locator('[name="responsable"]').fill(responsable + " editado")
            page.locator('#formulario button[type="submit"]').click()
            dialog.wait_for(state="hidden")
            row = page.locator("#reservas tr").filter(has_text=responsable + " editado")
            row.wait_for()
            for width in (1366, 390):
                page.set_viewport_size({"width": width, "height": 900})
                for scheme in ("light", "dark"):
                    page.emulate_media(color_scheme=scheme)
                    assert page.evaluate("document.documentElement.scrollWidth <= innerWidth + 2")
                    page.screenshot(path=str(captures / f"firestore-app-{label}-{width}-{scheme}.png"), full_page=True)
            row.get_by_role("button", name="Eliminar", exact=True).click()
            confirm = page.locator("#confirmacion")
            confirm.get_by_role("button", name="Cancelar", exact=True).click()
            assert row.count() == 1
            row.get_by_role("button", name="Eliminar", exact=True).click()
            confirm.get_by_role("button", name="Eliminar", exact=True).click()
            row.wait_for(state="detached")
            if label == "final":
                page.set_viewport_size({"width": 1366, "height": 900})
                first = api.post(base + "/api/reservas", data=body | {"inicio": 8, "fin": 9, "responsable": responsable + " A"})
                assert first.status == 201, first.text()
                created.add(first.json()["id"])
                second = api.post(base + "/api/reservas", data=body | {"inicio": 9, "fin": 10, "responsable": responsable + " B"})
                assert second.status == 201, second.text()
                created.add(second.json()["id"])
                page.locator("#actualizar").click()
                first_row = page.locator("#reservas tr").filter(has_text=responsable + " A")
                first_row.wait_for()
                page.locator("#dispFecha").fill(date)
                page.locator("#dispInicio").fill("8")
                page.locator("#dispFin").fill("9")
                page.locator("#consultar").click()
                page.locator("#mensaje").filter(has_text="ocupado").wait_for()
                first_row.get_by_role("button", name="Cancelar", exact=True).click()
                page.locator("#motivoCancelacion").fill("Cambio de actividad QA")
                page.locator("#formCancelacion button[type=submit]").click()
                page.locator("#cancelacion").wait_for(state="hidden")
                page.locator("#mensaje").filter(has_text="Reserva cancelada").wait_for()
                page.locator("#consultar").click()
                page.locator("#mensaje").filter(has_text="Espacio disponible").wait_for()
                page.locator("#filtroFecha").fill(date)
                page.locator("#filtroEstado").select_option("CANCELADA")
                page.locator("#filtroResponsable").fill(responsable + " A")
                page.locator("#filtrar").click()
                page.locator("#mensaje").filter(has_text="1 reservas").wait_for()
                assert page.locator("#reservas tr").count() == 1
                assert "CANCELADA" in page.locator("#reservas tr").inner_text()
                expected = sorted(api.get(base + "/api/reservas").json(), key=lambda reservation: reservation["id"])
                page.locator("#limite").fill("1")
                page.locator("#primera").click()
                page.locator("#mensaje").filter(has_text="Vista paginada").wait_for()
                assert page.locator("#reservas tr").count() == 1
                assert page.locator("#reservas td:nth-child(4)").inner_text() == expected[0]["responsable"]
                assert page.locator("#siguiente").is_enabled()
                with page.expect_response(lambda response: "/api/reservas/pagina" in response.url):
                    page.locator("#siguiente").click()
                page.locator("#reservas td:nth-child(4)").filter(has_text=expected[1]["responsable"]).wait_for()
                assert page.locator("#reservas tr").count() == 1
                assert page.locator("#reservas td:nth-child(4)").inner_text() == expected[1]["responsable"]
                print("OK UI final: disponibilidad ocupada/libre, cancelación con motivo, filtros combinados y paginación sin repetición")
            assert not errors, errors
            print("OK Firestore real: CRUD, filtros, errores 400/404, PUT idempotente; edición UI conserva listas/mapas, JSON inválido rechazado; alta/error/cancelación/eliminación; móvil/escritorio y claro/oscuro")
        finally:
            for identifier in created:
                api.delete(base + "/api/reservas/" + identifier)
            browser.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--base", default="http://127.0.0.1:8080")
    parser.add_argument("--label", choices=("guiado", "final"), default="guiado")
    args = parser.parse_args()
    check(args.base.rstrip("/"), args.label)
