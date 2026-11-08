"""Casos que distinguen referencias internas de conceptos del temario."""
import unittest
from check_student_voice import findings


class StudentVoiceTest(unittest.TestCase):
    def test_internal_process(self):
        for text in ("Disponible para revisión docente", "Sigue el índice aprobado", "Los workflows verifican el material", "Archivo histórico del temario", "Consulta CONTENIDOS_ACDA.md", "UT5 — JDBC"):
            with self.subTest(text=text):
                self.assertTrue(findings(text))

    def test_student_useful_references(self):
        for text in ("© Mikel Aingeru Jorge Soteras", "GitHub contiene el código de la solución", "conexion.commit() confirma la transacción", "El nivel READ_COMMITTED", "La instantánea histórica conserva el precio", "El archivo histórico de movimientos conserva los importes", "Si una clave se expone, borrarla en otro commit no la revoca", "Cinco fichas En preparación", "Prepara el logger de SLF4J"):
            with self.subTest(text=text):
                self.assertFalse(findings(text))


if __name__ == "__main__":
    unittest.main()
