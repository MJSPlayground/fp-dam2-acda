package es.mjsplayground.acda.ficheros;

import es.mjsplayground.acda.ficheros.model.dao.ExistenciaBinariaDao;
import es.mjsplayground.acda.ficheros.cli.SerializacionDemo;
import es.mjsplayground.acda.ficheros.model.entity.Cliente;
import es.mjsplayground.acda.ficheros.model.entity.Existencia;
import es.mjsplayground.acda.ficheros.model.service.ClienteService;
import es.mjsplayground.acda.ficheros.model.service.ExistenciaService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class FormatosTest {
    @TempDir Path carpeta;
    @Test void csvConComasYErroresParciales() throws Exception {
        Path entrada = carpeta.resolve("entrada.csv"), salida = carpeta.resolve("salida.json");
        Files.writeString(entrada, "id,nombre,ciudad,correo\nC1,\"Ana, María\",Pamplona,ana@example.org\n"
                + "C2,Luis,Tudela,mal\nC3,Eva,,eva@example.org\nC1,Otra,Pamplona,otra@example.org\n", StandardCharsets.UTF_8);
        var r = new ClienteService().convertir("csv", entrada, "json", salida);
        assertEquals(2, r.clientes().size());
        assertEquals(2, r.incidencias().size());
        assertEquals("Ana, María", r.clientes().getFirst().nombre());
        assertEquals(2, new ClienteService().importar("json", salida).clientes().size());
    }
    @Test void xmlEscapaYBloqueaXxe() throws Exception {
        ClienteService s = new ClienteService(); Path xml = carpeta.resolve("clientes.xml");
        s.exportar("xml", xml, List.of(new Cliente("C1", "Ana & Luis", "Pamplona", "")));
        assertTrue(Files.readString(xml).contains("Ana &amp; Luis"));
        assertEquals("Ana & Luis", s.importar("xml", xml).clientes().getFirst().nombre());
        Files.writeString(xml, "<!DOCTYPE clientes [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>"
                + "<clientes><cliente id=\"C1\"><nombre>&xxe;</nombre><ciudad>X</ciudad></cliente></clientes>");
        assertThrows(IOException.class, () -> s.importar("xml", xml));
    }
    @Test void falloEstructuralNoReemplazaSalida() throws Exception {
        Path entrada = carpeta.resolve("malo.json"), salida = carpeta.resolve("antes.csv");
        Files.writeString(entrada, "{no es JSON", StandardCharsets.UTF_8);
        Files.writeString(salida, "anterior", StandardCharsets.UTF_8);
        assertThrows(IOException.class, () -> new ClienteService().convertir("json", entrada, "csv", salida));
        assertEquals("anterior", Files.readString(salida));
    }
    @Test void binarioActualizaYRechazaTruncado() throws Exception {
        Path archivo = carpeta.resolve("datos.bin"); ExistenciaService s = new ExistenciaService();
        s.guardar(archivo, List.of(new Existencia(1, 0), new Existencia(2, 9), new Existencia(3, 10)));
        assertEquals(40, Files.size(archivo));
        s.actualizar(archivo, 1, 12);
        assertEquals(12, s.leer(archivo).get(1).unidades());
        assertThrows(IllegalArgumentException.class, () -> s.actualizar(archivo, 3, 0));
        Files.write(archivo, new byte[]{1, 2});
        assertThrows(IOException.class, () -> new ExistenciaBinariaDao().leer(archivo));
    }
    @Test void serializacionPropiaConFiltro() throws Exception {
        Path archivo = carpeta.resolve("ficha.bin");
        var ficha = new SerializacionDemo.Ficha("C1", "Ana");
        SerializacionDemo.escribir(archivo, ficha);
        assertEquals(ficha, SerializacionDemo.leerPropio(archivo));
    }
}
