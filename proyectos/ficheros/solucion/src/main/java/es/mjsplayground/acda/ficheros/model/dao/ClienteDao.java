package es.mjsplayground.acda.ficheros.model.dao;

import es.mjsplayground.acda.ficheros.model.entity.Cliente;
import es.mjsplayground.acda.ficheros.model.entity.Importacion;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface ClienteDao {
    Importacion leer(Path origen) throws IOException;
    void escribir(Path destino, List<Cliente> clientes) throws IOException;
}
