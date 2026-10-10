package es.mjsplayground.acda.ficheros;

import es.mjsplayground.acda.ficheros.model.dao.ProductoFormatos;
import es.mjsplayground.acda.ficheros.model.entity.Producto;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class ProductoFormatosTest {
    @TempDir Path carpeta;
    @Test void tresFormatosConservanNombreYTipos() throws Exception {
        ProductoFormatos f = new ProductoFormatos();
        List<Producto> productos = List.of(new Producto("P1", "Taza, blanca", 3, new BigDecimal("4.50")));
        for (String formato : List.of("csv", "xml", "json")) {
            Path archivo = carpeta.resolve("productos." + formato);
            f.escribir(archivo, formato, productos);
            Producto leido = f.leer(archivo, formato).productos().getFirst();
            assertEquals("P1", leido.codigo()); assertEquals("Taza, blanca", leido.nombre());
            assertEquals(3, leido.unidades()); assertEquals(0, leido.precio().compareTo(new BigDecimal("4.50")));
        }
    }
    @Test void xxeYRegistroInvalido() throws Exception {
        ProductoFormatos f = new ProductoFormatos(); Path xml = carpeta.resolve("malo.xml");
        Files.writeString(xml, "<!DOCTYPE productos [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>"
                + "<productos><producto codigo=\"P1\"><nombre>&xxe;</nombre><unidades>1</unidades><precio>2</precio></producto></productos>");
        assertThrows(java.io.IOException.class, () -> f.leer(xml, "xml"));
        Path csv = carpeta.resolve("datos.csv");
        Files.writeString(csv, "codigo,nombre,unidades,precio\nP1,Taza,2,3.00\nP2,Mal,-1,4.00\nP3,Vaso,1,2.00\n", StandardCharsets.UTF_8);
        var r = f.leer(csv, "csv");
        assertEquals(3, r.productos().size()); assertEquals(0, r.incidencias().size());
        var servicio = new es.mjsplayground.acda.ficheros.model.service.ComercialService(carpeta.resolve("estado.json"));
        assertThrows(IllegalArgumentException.class, () -> servicio.guardarProducto(r.productos().get(1)));
    }
}
