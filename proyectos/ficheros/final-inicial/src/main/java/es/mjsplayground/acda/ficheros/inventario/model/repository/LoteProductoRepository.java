package es.mjsplayground.acda.ficheros.inventario.model.repository;

import com.fasterxml.jackson.databind.*;
import es.mjsplayground.acda.ficheros.inventario.model.entity.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.apache.commons.csv.*;

public class LoteProductoRepository {
  public List<FilaImportada> leer(Path archivo, Formato formato) throws IOException {
    // TODO inventario final: lectura por filas e informe conforme a requisitos
    throw new UnsupportedOperationException(
        "Etapa pendiente: inventario final: lectura por filas e informe conforme a requisitos");
  }

  public void informe(Path destino, List<Incidencia> incidencias) throws IOException {
    // TODO inventario final: lectura por filas e informe conforme a requisitos
    throw new UnsupportedOperationException(
        "Etapa pendiente: inventario final: lectura por filas e informe conforme a requisitos");
  }
}
