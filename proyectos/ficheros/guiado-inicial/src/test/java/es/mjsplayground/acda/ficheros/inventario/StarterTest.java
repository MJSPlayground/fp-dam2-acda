package es.mjsplayground.acda.ficheros.inventario;

import static org.junit.jupiter.api.Assertions.*;

import es.mjsplayground.acda.ficheros.inventario.model.service.*;
import org.junit.jupiter.api.Test;

class StarterTest {
  @Test
  void catalogoPreparadoYFiltradoFuncionan() {
    var s = new InventarioService();
    assertEquals(3, s.listar().size());
    assertEquals(1, s.filtrar("TECL", 10).size());
  }
}
