package es.mjsplayground.acda.ficheros;

import es.mjsplayground.acda.ficheros.model.service.TarifasService;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class TarifasTest {
    @TempDir Path carpeta;
    private Path muestra(String recurso) throws Exception {
        Path archivo = carpeta.resolve(recurso);
        try (InputStream in = getClass().getResourceAsStream("/data/" + recurso)) {
            assertNotNull(in); Files.copy(in, archivo);
        }
        return archivo;
    }
    @Test void xmlYJsonProducenLaMismaProyeccion() throws Exception {
        TarifasService s = new TarifasService();
        var json = s.leerJson(muestra("tarifas.json"), "hogar");
        var xml = s.leerXml(muestra("tarifas.xml"), "hogar");
        assertEquals(3, json.filas().size()); assertEquals(json.filas(), xml.filas());
        assertEquals(1, json.incidencias().size());
        Path csv = carpeta.resolve("tarifas.csv"), reconstruido = carpeta.resolve("tarifas-reconstruidas.json");
        s.escribirCsv(csv, json); s.reconstruirJson(csv, reconstruido);
        assertTrue(Files.readString(reconstruido).contains("Taza, blanca"));
    }
}
