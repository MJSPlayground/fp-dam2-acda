package es.mjsplayground.acda.ficheros.model.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.mjsplayground.acda.ficheros.model.entity.Cliente;
import es.mjsplayground.acda.ficheros.model.entity.Importacion;
import es.mjsplayground.acda.ficheros.model.entity.Incidencia;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class JsonClienteDao implements ClienteDao {
    private final ObjectMapper mapper = new ObjectMapper();
    private static String texto(JsonNode n, String nombre) {
        JsonNode valor = n.path(nombre);
        return valor.isTextual() ? valor.textValue() : null;
    }
    @Override public Importacion leer(Path origen) throws IOException {
        JsonNode raiz = mapper.readTree(origen.toFile());
        if (!raiz.isArray()) throw new IOException("Se esperaba un array JSON: " + origen);
        List<Cliente> clientes = new ArrayList<>(); List<Incidencia> incidencias = new ArrayList<>();
        for (int i = 0; i < raiz.size(); i++) {
            JsonNode n = raiz.get(i);
            if (!n.isObject() || texto(n, "id") == null || texto(n, "nombre") == null || texto(n, "ciudad") == null) {
                incidencias.add(new Incidencia(origen.toString(), i + 1, "Objeto o campos obligatorios incorrectos"));
                continue;
            }
            clientes.add(new Cliente(texto(n, "id"), texto(n, "nombre"), texto(n, "ciudad"), texto(n, "correo")));
        }
        return new Importacion(clientes, incidencias);
    }
    @Override public void escribir(Path destino, List<Cliente> clientes) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(destino.toFile(), clientes);
    }
}
