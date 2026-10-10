package es.mjsplayground.acda.ficheros;

import es.mjsplayground.acda.ficheros.cli.AmpliacionesDemo;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class AmpliacionesTest {
    @TempDir Path carpeta;
    @Test void streamingRespetaComillasYFiltra() throws Exception {
        Path origen = carpeta.resolve("datos.csv"), destino = carpeta.resolve("pares.csv");
        Files.writeString(origen, "id,nombre\n1,Uno\n2,\"Dos, centro\"\n3,Tres\n4,Cuatro\n", StandardCharsets.UTF_8);
        assertEquals(2, AmpliacionesDemo.filtrarCsv(origen, destino));
        String salida = Files.readString(destino);
        assertTrue(salida.contains("\"Dos, centro\"")); assertFalse(salida.contains("Uno"));
    }
    @Test void canalCopiaMasQueUnBuffer() throws Exception {
        Path origen = carpeta.resolve("grande.bin"), destino = carpeta.resolve("copia.bin");
        byte[] datos = new byte[190_000]; for (int i = 0; i < datos.length; i++) datos[i] = (byte) i;
        Files.write(origen, datos);
        assertEquals(datos.length, AmpliacionesDemo.copiarCanal(origen, destino));
        assertArrayEquals(datos, Files.readAllBytes(destino));
    }
    @Test void temporalReemplazaContenido() throws Exception {
        Path destino = carpeta.resolve("informe.txt"); Files.writeString(destino, "anterior");
        AmpliacionesDemo.escribirFiable(destino, "nuevo");
        assertEquals("nuevo", Files.readString(destino));
    }
}
