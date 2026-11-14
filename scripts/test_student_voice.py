"""Casos que distinguen referencias internas de conceptos del temario."""
import unittest
from pathlib import Path
from tempfile import TemporaryDirectory
from check_student_voice import findings, distributed_readmes


class StudentVoiceTest(unittest.TestCase):
    def test_internal_process(self):
        for text in ("Disponible para revisión docente", "Sigue el índice aprobado", "Los workflows verifican el material", "Archivo histórico del temario", "Consulta CONTENIDOS_ACDA.md", "UT5 — JDBC"):
            with self.subTest(text=text):
                self.assertTrue(findings(text))

    def test_student_useful_references(self):
        for text in ("© Mikel Aingeru Jorge Soteras", "GitHub contiene el código de la solución", "conexion.commit() confirma la transacción", "El nivel READ_COMMITTED", "La instantánea histórica conserva el precio", "El archivo histórico de movimientos conserva los importes", "Si una clave se expone, borrarla en otro commit no la revoca", "Cinco fichas En preparación", "Prepara el logger de SLF4J"):
            with self.subTest(text=text):
                self.assertFalse(findings(text))

    def test_distributed_development_notes(self):
        for text in ("QA opcional", "Las clases históricas se conservan intactas", "adaptada del ComercialJsonDao histórico", "Nota de desarrollo: revisar el material", "La auditoría del agente está pendiente"):
            with self.subTest(text=text):
                self.assertTrue(findings(text))

    def test_technical_contracts_are_allowed(self):
        for text in ("-Dqa.ui=true", "QA_MYSQL_ADMIN_URL", "El historial conserva la instantánea histórica", "No publiques credenciales en GitHub", "El registro de auditoría conserva quién modificó el dato"):
            with self.subTest(text=text):
                self.assertFalse(findings(text))

    def test_only_packaged_readmes_are_selected(self):
        with TemporaryDirectory() as directory:
            root = Path(directory)
            project = root / "guiado-inicial"
            for name in ("README.md", "sub/Readme.txt", "target/README.md", ".idea/README.md"):
                path = project / name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text("Texto", encoding="utf-8")
            historical = root / "historico/README.md"
            historical.parent.mkdir()
            historical.write_text("Texto", encoding="utf-8")
            self.assertEqual(distributed_readmes({"guiado.zip": project}),
                             sorted([project / "README.md", project / "sub/Readme.txt"]))


if __name__ == "__main__":
    unittest.main()
