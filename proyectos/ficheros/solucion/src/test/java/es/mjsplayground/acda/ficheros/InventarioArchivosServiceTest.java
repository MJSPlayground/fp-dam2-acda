package es.mjsplayground.acda.ficheros;

import es.mjsplayground.acda.ficheros.cli.Inventariar;
import es.mjsplayground.acda.ficheros.cli.OrganizarDirectorios;
import es.mjsplayground.acda.ficheros.model.service.InventarioArchivosService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Assumptions;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class InventarioArchivosServiceTest {
    @TempDir Path temp;

    @Test void recorridoFiltroOrdenEstadisticasYEmpate() throws Exception {
        Path raiz = Files.createDirectory(temp.resolve("entrada"));
        Files.writeString(raiz.resolve("clientes.csv"), "1234");
        Files.writeString(raiz.resolve("Z.CSV"), "1234");
        Files.writeString(raiz.resolve("nota.txt"), "123456789");
        Files.createDirectory(raiz.resolve("vacio"));
        Path p = raiz;
        for (int i = 1; i <= 4; i++) p = Files.createDirectory(p.resolve("nivel" + i));
        Files.writeString(p.resolve("profundo.csv"), "123");
        Files.writeString(p.getParent().resolve("nivel3.csv"), "12");
        var r = new InventarioArchivosService().crear(raiz, ".cSv");
        assertEquals(3, r.archivos().size());
        assertEquals(10, r.bytes());
        assertEquals(Path.of("Z.CSV"), r.mayor());
        assertEquals("Z.CSV", r.archivos().getFirst().relativa().toString());
        assertTrue(r.archivos().stream().noneMatch(a -> a.relativa().toString().contains("profundo")));
        assertTrue(r.incidencias().isEmpty());
    }

    @Test void raizInvalidaYCarpetaVacia() throws Exception {
        var service = new InventarioArchivosService();
        assertThrows(IOException.class, () -> service.crear(temp.resolve("ausente"), "csv"));
        assertThrows(IllegalArgumentException.class, () -> service.crear(temp, ""));
        var r = service.crear(temp, "csv");
        assertTrue(r.archivos().isEmpty());
        assertEquals(0, r.bytes());
        assertNull(r.mayor());
    }

    @Test void exportacionFueraDeRaizYCopiaControlada() throws Exception {
        Path raiz = Files.createDirectory(temp.resolve("entrada"));
        Files.writeString(raiz.resolve("clientes.csv"), "uno,dos\n");
        Path trabajo = temp.resolve("trabajo");
        OrganizarDirectorios.main(new String[]{raiz.toString(), trabajo.toString()});
        assertEquals("uno,dos\n", Files.readString(trabajo.resolve("copias/clientes.csv")));
        assertThrows(java.nio.file.FileAlreadyExistsException.class,
                () -> OrganizarDirectorios.main(new String[]{raiz.toString(), trabajo.toString()}));
        Files.writeString(raiz.resolve("clientes.csv"), "nuevo\n");
        OrganizarDirectorios.main(new String[]{raiz.toString(), trabajo.toString(), "--reemplazar"});
        assertEquals("nuevo\n", Files.readString(trabajo.resolve("copias/clientes.csv")));
        Path salida = trabajo.resolve("inventario.csv");
        Inventariar.main(new String[]{raiz.toString(), "csv", salida.toString()});
        String csv = Files.readString(salida);
        assertTrue(csv.contains("ruta,bytes,modificado"));
        assertTrue(csv.contains("clientes.csv,6,"));
        assertEquals(1, new InventarioArchivosService().crear(raiz, "csv").archivos().size());
        assertThrows(IllegalArgumentException.class,
                () -> Inventariar.main(new String[]{raiz.toString(), "csv", raiz.resolve("inventario.csv").toString()}));
    }

    @Test void registraDirectorioInaccesibleYContinuaCuandoElSistemaLoPermite() throws Exception {
        Path raiz = Files.createDirectory(temp.resolve("entrada"));
        Files.writeString(raiz.resolve("bien.csv"), "dato");
        Path cerrada = Files.createDirectory(raiz.resolve("cerrada"));
        Files.writeString(cerrada.resolve("oculto.csv"), "dato");
        try {
            Set<PosixFilePermission> originales = Files.getPosixFilePermissions(cerrada);
            try {
                Files.setPosixFilePermissions(cerrada, Set.of());
                Assumptions.assumeFalse(Files.isReadable(cerrada), "El usuario puede leer aun sin permisos POSIX");
                var r = new InventarioArchivosService().crear(raiz, "csv");
                assertEquals(1, r.archivos().size());
                assertEquals(Path.of("bien.csv"), r.archivos().getFirst().relativa());
                assertFalse(r.incidencias().isEmpty());
                assertTrue(r.incidencias().getFirst().contains("cerrada"));
            } finally {
                Files.setPosixFilePermissions(cerrada, originales);
            }
        } catch (UnsupportedOperationException ex) {
            Assumptions.assumeTrue(false, "Sistema sin permisos POSIX");
        }
    }
}
