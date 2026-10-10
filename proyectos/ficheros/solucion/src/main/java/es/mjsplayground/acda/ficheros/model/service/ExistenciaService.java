package es.mjsplayground.acda.ficheros.model.service;

import es.mjsplayground.acda.ficheros.model.dao.ExistenciaBinariaDao;
import es.mjsplayground.acda.ficheros.model.entity.Existencia;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ExistenciaService {
    private final ExistenciaBinariaDao dao = new ExistenciaBinariaDao();
    public void guardar(Path archivo, List<Existencia> registros) throws IOException {
        validar(registros); dao.escribir(archivo, registros);
    }
    public List<Existencia> leer(Path archivo) throws IOException {
        List<Existencia> registros = dao.leer(archivo); validar(registros); return registros;
    }
    public void actualizar(Path archivo, int indice, long unidades) throws IOException {
        leer(archivo); dao.actualizar(archivo, indice, unidades);
    }
    private void validar(List<Existencia> registros) throws IOException {
        Set<Integer> ids = new HashSet<>();
        for (Existencia e : registros) {
            if (e.unidades() < 0) throw new IOException("Existencias negativas: " + e.id());
            if (!ids.add(e.id())) throw new IOException("Identificador duplicado: " + e.id());
        }
    }
    public String categoria(Existencia e) {
        return e.unidades() == 0 ? "sin stock" : e.unidades() < 10 ? "bajo" : "normal";
    }
}
