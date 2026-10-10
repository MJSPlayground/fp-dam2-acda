package es.mjsplayground.acda.ficheros;

import es.mjsplayground.acda.ficheros.model.entity.Cliente;
import es.mjsplayground.acda.ficheros.model.entity.Producto;
import es.mjsplayground.acda.ficheros.model.service.ComercialService;
import es.mjsplayground.acda.ficheros.model.service.FusionCsvService;
import es.mjsplayground.acda.ficheros.model.service.InventarioComercialService;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class ServiciosTest {
    @TempDir Path carpeta;
    @Test void fusionCsvValidaYRespetaPrimeraFuente() throws Exception {
        Path a = carpeta.resolve("a.csv"), b = carpeta.resolve("b.csv");
        Files.writeString(a, "id,nombre,ciudad,importe\n1,Local A,Pamplona,2.50\n2,Mal,Pamplona,-1\n", StandardCharsets.UTF_8);
        Files.writeString(b, "id,nombre,ciudad,importe\n1,Local B,Pamplona,7.00\n3,Otro,Pamplona,3.25\n", StandardCharsets.UTF_8);
        var r = new FusionCsvService().fusionar(List.of(a, b), "Pamplona");
        assertEquals(2, r.filtrados().size()); assertEquals("Local A", r.filtrados().getFirst().nombre());
        assertEquals(new BigDecimal("5.75"), r.total()); assertEquals(2, r.incidencias().size());
    }
    @Test void inventarioCombinaExistenciasYDetectaConflicto() throws Exception {
        Path csv = carpeta.resolve("a.csv"), json = carpeta.resolve("b.json");
        Files.writeString(csv, "codigo,nombre,almacen,unidades,precio\nP1,Taza,Norte,2,4.50\n", StandardCharsets.UTF_8);
        Files.writeString(json, "[{\"codigo\":\"P1\",\"nombre\":\"Taza\",\"almacen\":\"Sur\",\"unidades\":3,\"precio\":4.50},"
                + "{\"codigo\":\"P1\",\"nombre\":\"Taza\",\"almacen\":\"Sur\",\"unidades\":2,\"precio\":5.00}]", StandardCharsets.UTF_8);
        var entradas = new LinkedHashMap<Path, String>(); entradas.put(csv, "csv"); entradas.put(json, "json");
        var r = new InventarioComercialService().combinar(entradas, 6);
        assertEquals(5, r.productos().getFirst().unidades());
        assertEquals(0, new BigDecimal("22.50").compareTo(r.valorTotal()));
        assertEquals(1, r.incidencias().size());
    }
    @Test void ventaEsPersistenteYProtegeExistencias() throws Exception {
        Path archivo = carpeta.resolve("comercial.json"); ComercialService s = new ComercialService(archivo);
        s.guardarCliente(new Cliente("C1", "Ana", "Pamplona", "ana@example.org"));
        s.guardarProducto(new Producto("P1", "Taza", 4, new BigDecimal("2.50")));
        s.guardarProducto(new Producto("P2", "Cuaderno", 2, new BigDecimal("3.00")));
        assertThrows(IllegalArgumentException.class, () -> s.registrar("V0", "C1", "P1", 5));
        assertEquals(4, s.estado().productos().getFirst().unidades());
        var v = s.registrar("V1", "C1", java.util.Map.of("P1", 3, "P2", 1));
        assertEquals(new BigDecimal("10.50"), v.importe());
        assertEquals(2, v.lineas().size());
        assertEquals(2, s.resumen(s.estado().operaciones()).size());
        assertEquals(1, new ComercialService(archivo).estado().productos().getFirst().unidades());
        assertThrows(IllegalArgumentException.class, () -> s.eliminarCliente("C1"));
    }
}
