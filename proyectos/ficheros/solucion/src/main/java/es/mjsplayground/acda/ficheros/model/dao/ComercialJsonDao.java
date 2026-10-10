package es.mjsplayground.acda.ficheros.model.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.mjsplayground.acda.ficheros.model.entity.EstadoComercial;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ComercialJsonDao {
    private final ObjectMapper mapper = new ObjectMapper();
    public EstadoComercial leer(Path archivo) throws IOException {
        return Files.exists(archivo) ? mapper.readValue(archivo.toFile(), EstadoComercial.class) : EstadoComercial.vacio();
    }
    public void guardar(Path archivo, EstadoComercial estado) throws IOException {
        Path destino = archivo.toAbsolutePath();
        Files.createDirectories(destino.getParent());
        Path temp = Files.createTempFile(destino.getParent(), ".comercial-", ".tmp");
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(temp.toFile(), estado);
            try { Files.move(temp, destino, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException ex) { Files.move(temp, destino, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temp); }
    }
}
