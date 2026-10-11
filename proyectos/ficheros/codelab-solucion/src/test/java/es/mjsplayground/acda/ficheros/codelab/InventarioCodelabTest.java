package es.mjsplayground.acda.ficheros.codelab;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class InventarioCodelabTest {
    @TempDir Path temporal;

    @Test void muestraOrdenFiltroYConservacion() throws Exception {
        Path entrada = temporal.resolve("entrada");
        PrepararDatos.main(new String[]{entrada.toString()});
        Path ventas = entrada.resolve("ventas.CSV");
        Files.writeString(ventas, "X,Y\n", StandardCharsets.UTF_8);
        PrepararDatos.main(new String[]{entrada.toString()});
        assertEquals("X,Y\n", Files.readString(ventas));
        Path salida = temporal.resolve("salida");
        var r = InventarioCodelab.ejecutar(entrada, salida, "csv", false);
        assertEquals(4, r.archivos().size());
        assertEquals(22, r.totalBytes());
        assertEquals(Path.of("historico/2025/marzo.CsV"), r.mayor().relativa());
        assertEquals(5, Files.readAllLines(r.informe()).size());
        assertEquals("clientes.csv", r.archivos().getFirst().relativa().toString());
        Files.writeString(salida.resolve("copias/clientes.csv"), "mis cambios");
        var repeticion = InventarioCodelab.ejecutar(entrada, salida, "csv", false);
        assertEquals("mis cambios", Files.readString(salida.resolve("copias/clientes.csv")));
        assertFalse(repeticion.incidencias().isEmpty());
        var txt = InventarioCodelab.ejecutar(entrada, temporal.resolve("texto"), "txt", false);
        assertEquals(1, txt.archivos().size());
        assertEquals(8, txt.totalBytes());
    }

    @Test void profundidadEmpateVacioYErrores() throws Exception {
        Path entrada = Files.createDirectory(temporal.resolve("otra"));
        Path salida = temporal.resolve("resultado");
        var vacio = InventarioCodelab.ejecutar(entrada, salida, "csv", false);
        assertTrue(vacio.archivos().isEmpty());
        assertNull(vacio.mayor());
        assertEquals(0, vacio.totalBytes());
        assertEquals(1, Files.readAllLines(vacio.informe()).size());
        Files.writeString(entrada.resolve("b.csv"), "1234");
        Files.writeString(entrada.resolve("a.csv"), "1234");
        Path profundo = Files.createDirectories(entrada.resolve("uno/dos/tres/cuatro"));
        Files.writeString(profundo.resolve("fuera.csv"), "mucho texto");
        var r = InventarioCodelab.ejecutar(entrada, salida, "csv", false);
        assertEquals(2, r.archivos().size());
        assertEquals(Path.of("a.csv"), r.mayor().relativa());
        assertTrue(r.incidencias().stream().anyMatch(i -> i.contains("No se pudo copiar clientes.csv")));
        assertThrows(IllegalArgumentException.class,
                () -> InventarioCodelab.ejecutar(entrada, entrada.resolve("dentro"), "csv", false));
        assertFalse(Files.exists(entrada.resolve("dentro")));
        assertThrows(IllegalArgumentException.class,
                () -> InventarioCodelab.ejecutar(entrada, salida, "../csv", false));
        assertThrows(java.io.IOException.class,
                () -> InventarioCodelab.ejecutar(temporal.resolve("ausente"), salida, "csv", false));
    }
}
