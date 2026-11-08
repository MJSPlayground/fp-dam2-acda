package es.mjsplayground.acda.hibernate.biblioteca;

import static org.junit.jupiter.api.Assertions.*;

import es.mjsplayground.acda.hibernate.biblioteca.model.service.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named = "qa.mysql", matches = "true")
class FasesFinalTest {
  @Test
  @EnabledIfSystemProperty(named = "fase", matches = "base|completo")
  void nuevaEdicionConDetalleIndependiente() throws Exception {
    try (var db = new ApoyoFasesMysql()) {
      var d = db.preparar();
      var s = new BibliotecaService(() -> db.factory);
      LibroDatos original = s.obtener(d.libro());
      long id = s.duplicar(d.libro(), "9780000000003", "Edición nueva");
      var copia = s.obtener(id);
      assertNotEquals(original.detalleId(), copia.detalleId());
      assertEquals(original.autorIds(), copia.autorIds());
      assertEquals(original, s.obtener(d.libro()));
    }
  }

  @Test
  @EnabledIfSystemProperty(named = "fase", matches = "aplicacion|completo")
  void trasladoIntegroYRechazoSinCambios() throws Exception {
    try (var db = new ApoyoFasesMysql()) {
      var d = db.preparar();
      var s = new BibliotecaService(() -> db.factory);
      var antes = s.obtener(d.libro());
      assertThrows(
          BibliotecaException.class,
          () -> s.trasladar(List.of(d.libro(), Long.MAX_VALUE), d.destino(), new BigDecimal("10")));
      assertEquals(antes, s.obtener(d.libro()));
      s.trasladar(List.of(d.libro()), d.destino(), new BigDecimal("10"));
      assertEquals(new BigDecimal("26.91"), s.obtener(d.libro()).precio());
      assertEquals(d.destino(), s.obtener(d.libro()).editorialId());
    }
  }

  @Test
  @EnabledIfSystemProperty(named = "fase", matches = "reto|completo")
  void fusionConFalloIntermedioReal() throws Exception {
    try (var db = new ApoyoFasesMysql()) {
      var d = db.preparar();
      var s = new BibliotecaService(() -> db.factory);
      s.guardarEditorial(null, "Nombre ocupado");
      var antes = s.obtener(d.libro());
      BibliotecaException e =
          assertThrows(
              BibliotecaException.class,
              () -> s.fusionar(d.origen(), d.destino(), "Nombre ocupado"));
      assertNotNull(e.getCause());
      assertEquals(antes, s.obtener(d.libro()));
      assertTrue(s.editoriales().stream().anyMatch(x -> x.id() == d.origen()));
      assertEquals(1, s.fusionar(d.origen(), d.destino(), "Unida"));
      assertEquals(d.destino(), s.obtener(d.libro()).editorialId());
      assertEquals(2, db.contar("Autor"));
    }
  }
}
