package es.mjsplayground.acda.ficheros.inventario.model.repository;

import es.mjsplayground.acda.ficheros.inventario.config.*;
import es.mjsplayground.acda.ficheros.inventario.model.entity.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class BinProductoRepository implements InventarioRepository {
  public List<Producto> leer(Path archivo) throws IOException {
    // TODO 3: serializar instantánea propia y recuperar usando FiltroSerializacion.abrirSeguro
    // preparado
    throw new UnsupportedOperationException(
        "Etapa pendiente: 3: serializar instantánea propia y recuperar usando"
            + " FiltroSerializacion.abrirSeguro preparado");
  }

  public void guardar(Path archivo, List<Producto> productos) throws IOException {
    // TODO 3: serializar instantánea propia y recuperar usando FiltroSerializacion.abrirSeguro
    // preparado
    throw new UnsupportedOperationException(
        "Etapa pendiente: 3: serializar instantánea propia y recuperar usando"
            + " FiltroSerializacion.abrirSeguro preparado");
  }
}
