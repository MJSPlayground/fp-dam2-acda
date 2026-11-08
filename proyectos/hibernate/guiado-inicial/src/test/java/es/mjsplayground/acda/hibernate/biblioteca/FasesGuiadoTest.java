package es.mjsplayground.acda.hibernate.biblioteca;

import static org.junit.jupiter.api.Assertions.*;

import es.mjsplayground.acda.hibernate.biblioteca.model.entity.*;
import es.mjsplayground.acda.hibernate.biblioteca.model.repository.*;
import es.mjsplayground.acda.hibernate.biblioteca.model.service.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.*;
import org.hibernate.Session;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

class FasesGuiadoTest {
  @Test
  @EnabledIfSystemProperty(named = "fase", matches = "uno-muchos|mapeos|completo")
  void unoMuchos() throws Exception {
    assertNotNull(Libro.class.getDeclaredField("editorial").getAnnotation(ManyToOne.class));
    assertEquals(
        "editorial",
        Editorial.class.getDeclaredField("libros").getAnnotation(OneToMany.class).mappedBy());
  }

  @Test
  @EnabledIfSystemProperty(named = "fase", matches = "muchos-muchos|mapeos|completo")
  void muchosMuchos() throws Exception {
    assertNotNull(Libro.class.getDeclaredField("autores").getAnnotation(ManyToMany.class));
    assertEquals(
        "autores",
        Autor.class.getDeclaredField("libros").getAnnotation(ManyToMany.class).mappedBy());
  }

  @Test
  @EnabledIfSystemProperty(named = "fase", matches = "uno-uno|mapeos|completo")
  void unoUno() throws Exception {
    var detalle = Libro.class.getDeclaredField("detalle").getAnnotation(OneToOne.class);
    assertNotNull(detalle);
    assertTrue(detalle.orphanRemoval());
    assertTrue(Arrays.asList(detalle.cascade()).contains(CascadeType.ALL));
  }

  @Test
  @EnabledIfSystemProperty(named = "fase", matches = "catalogos|completo")
  @EnabledIfSystemProperty(named = "qa.mysql", matches = "true")
  void catalogos() throws Exception {
    try (var db = new ApoyoFasesMysql()) {
      var datos = db.preparar();
      var r = new CatalogoRepository();
      db.factory.inTransaction(
          s -> {
            assertEquals(2, r.editoriales(s).size());
            assertEquals(2, r.autores(s).size());
          });
    }
  }

  @Test
  @EnabledIfSystemProperty(named = "fase", matches = "repositorio|completo")
  @EnabledIfSystemProperty(named = "qa.mysql", matches = "true")
  void repositorio() throws Exception {
    try (var db = new ApoyoFasesMysql()) {
      var datos = db.preparar();
      var r = new LibroRepository();
      db.factory.inTransaction(
          s -> {
            Libro origen = r.buscar(s, datos.libro());
            assertEquals("Java y datos", origen.getTitulo());
            Libro nuevo =
                new Libro(
                    "9780000000003",
                    "Tercero",
                    new BigDecimal("1.00"),
                    origen.getEditorial(),
                    origen.getAutores(),
                    null);
            r.insertar(s, nuevo);
            r.flush(s);
            assertNotNull(nuevo.getId());
            r.eliminar(s, nuevo);
            r.flush(s);
          });
      assertEquals(2, db.contar("Libro"));
      assertEquals(2, db.contar("Autor"));
    }
  }

  @Test
  @EnabledIfSystemProperty(named = "fase", matches = "consultas|completo")
  @EnabledIfSystemProperty(named = "qa.mysql", matches = "true")
  void consultas() throws Exception {
    try (var db = new ApoyoFasesMysql()) {
      var datos = db.preparar();
      var r = new LibroRepository();
      db.factory.inTransaction(
          s -> {
            assertEquals(2, r.listar(s, "", null).size());
            assertEquals(1, r.listar(s, "Java", datos.origen()).size());
            assertTrue(r.listar(s, "Java", datos.destino()).isEmpty());
            assertTrue(r.listar(s, "' or 1=1 --", null).isEmpty());
          });
    }
  }

  @Test
  @EnabledIfSystemProperty(named = "fase", matches = "servicio|completo")
  @EnabledIfSystemProperty(named = "qa.mysql", matches = "true")
  void servicioYVersionPreparada() throws Exception {
    try (var db = new ApoyoFasesMysql()) {
      var datos = db.preparar();
      var servicio = new BibliotecaService(() -> db.factory);
      LibroDatos antes = servicio.obtener(datos.libro());
      var solicitud =
          new LibroSolicitud(
              antes.isbn(),
              "Editado",
              new BigDecimal("30.00"),
              datos.origen(),
              antes.autorIds(),
              antes.idioma(),
              antes.paginas());
      servicio.guardarLibro(antes.id(), antes.version(), solicitud);
      assertEquals("Editado", servicio.obtener(antes.id()).titulo());
      assertThrows(
          BibliotecaException.class,
          () -> servicio.guardarLibro(antes.id(), antes.version(), solicitud));
      assertEquals(antes.autorIds(), servicio.obtener(antes.id()).autorIds());
    }
  }

  @Test
  @EnabledIfSystemProperty(named = "fase", matches = "rollback|completo")
  @EnabledIfSystemProperty(named = "qa.mysql", matches = "true")
  void rollbackDespuesDeEscrituraReal() throws Exception {
    try (var db = new ApoyoFasesMysql()) {
      var datos = db.preparar();
      long anteriores = db.contar("Libro"), detalles = db.contar("Detalle");
      boolean[] escrito = {false};
      LibroRepository fallo =
          new LibroRepository() {
            @Override
            public void flush(Session s) {
              super.flush(s);
              escrito[0] =
                  s.createSelectionQuery("select count(l) from Libro l", Long.class)
                          .getSingleResult()
                      == anteriores + 1;
              throw new IllegalStateException("Fallo preparado después de flush");
            }
          };
      var servicio = new BibliotecaService(() -> db.factory, fallo, new CatalogoRepository());
      var solicitud =
          new LibroSolicitud(
              "9780000000003",
              "Ensayo",
              new BigDecimal("1.00"),
              datos.origen(),
              List.of(datos.autor()),
              "Español",
              20);
      BibliotecaException e =
          assertThrows(
              BibliotecaException.class, () -> servicio.guardarLibro(null, null, solicitud));
      assertTrue(escrito[0], "El fallo tiene que ocurrir después de una escritura real");
      assertInstanceOf(IllegalStateException.class, e.getCause());
      assertEquals(anteriores, db.contar("Libro"));
      assertEquals(detalles, db.contar("Detalle"));
    }
  }
}
