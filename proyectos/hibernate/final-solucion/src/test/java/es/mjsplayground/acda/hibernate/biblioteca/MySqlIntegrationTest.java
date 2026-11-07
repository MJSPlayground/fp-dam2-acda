package es.mjsplayground.acda.hibernate.biblioteca;

import static org.junit.jupiter.api.Assertions.*;

import es.mjsplayground.acda.hibernate.biblioteca.config.HibernateFactory;
import es.mjsplayground.acda.hibernate.biblioteca.model.entity.*;
import es.mjsplayground.acda.hibernate.biblioteca.model.service.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import org.hibernate.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named = "qa.mysql", matches = "true")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MySqlIntegrationTest {
  String schema = "qa_hibernate_" + UUID.randomUUID().toString().replace("-", "");
  SessionFactory factory;
  BibliotecaService service;
  String admin =
      System.getenv()
          .getOrDefault(
              "QA_MYSQL_ADMIN_URL",
              "jdbc:mysql://127.0.0.1:3308/mysql?sslMode=DISABLED&allowPublicKeyRetrieval=true&serverTimezone=UTC");
  String usuario = System.getenv().getOrDefault("QA_MYSQL_ADMIN_USER", "root"),
      clave = System.getenv().getOrDefault("QA_MYSQL_ADMIN_PASSWORD", "qa_root_local");
  String url;

  @BeforeAll
  void preparar() throws Exception {
    try (Connection c = DriverManager.getConnection(admin, usuario, clave);
        Statement st = c.createStatement()) {
      st.executeUpdate(
          "CREATE DATABASE " + schema + " CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs");
    }
    url =
        admin.substring(0, admin.lastIndexOf('/') + 1)
            + schema
            + "?sslMode=DISABLED&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    factory = HibernateFactory.crear(url, usuario, clave, "update");
    service = new BibliotecaService(() -> factory);
  }

  @AfterAll
  void limpiar() throws Exception {
    if (factory != null) factory.close();
    assertTrue(schema.matches("qa_hibernate_[a-f0-9]{32}"));
    try (Connection c = DriverManager.getConnection(admin, usuario, clave);
        Statement st = c.createStatement()) {
      st.executeUpdate("DROP DATABASE " + schema);
    }
  }

  long editorial(String prefijo) {
    return service.guardarEditorial(null, prefijo + UUID.randomUUID());
  }

  long autor() {
    return service.guardarAutor(null, "Autor" + UUID.randomUUID());
  }

  String isbn() {
    return "978"
        + String.format(
            "%010d", Math.abs(UUID.randomUUID().getLeastSignificantBits() % 10000000000L));
  }

  long libro(long editorial, List<Long> autores, boolean detalle) {
    return service.guardarLibro(
        null,
        null,
        new LibroSolicitud(
            isbn(),
            "Java, \"datos\"\nsegunda línea",
            new BigDecimal("29.90"),
            editorial,
            autores,
            detalle ? "Español" : null,
            detalle ? 320 : null));
  }

  long contar(String entidad) {
    try (Session s = factory.openSession()) {
      return s.createSelectionQuery("select count(x) from " + entidad + " x", Long.class)
          .getSingleResult();
    }
  }

  @Test
  void tresRelacionesCrudCascadasYLazy() {
    long e = editorial("Relaciones"), a = autor(), b = autor();
    long detallesAntes = contar("Detalle");
    long l = libro(e, List.of(a, b), true);
    LibroDatos dato = service.obtener(l);
    assertEquals(e, dato.editorialId());
    assertEquals(List.of(a, b).stream().sorted().toList(), dato.autorIds());
    assertNotNull(dato.detalleId());
    try (Session sesion = factory.openSession()) {
      assertTrue(
          sesion.find(Editorial.class, e).getLibros().stream().anyMatch(x -> x.getId() == l));
      assertTrue(sesion.find(Autor.class, a).getLibros().stream().anyMatch(x -> x.getId() == l));
    }
    assertThrows(BibliotecaException.class, () -> service.eliminarAutor(a));
    assertThrows(BibliotecaException.class, () -> service.eliminarEditorial(e));
    Libro separado;
    try (Session s = factory.openSession()) {
      separado = s.find(Libro.class, l);
      assertFalse(Hibernate.isInitialized(separado.getAutores()));
    }
    assertThrows(LazyInitializationException.class, () -> separado.getAutores().size());
    service.guardarLibro(
        l,
        dato.version(),
        new LibroSolicitud(
            dato.isbn(), dato.titulo(), new BigDecimal("30.00"), e, List.of(a), null, null));
    assertEquals(detallesAntes, contar("Detalle"));
    assertEquals(List.of(a), service.obtener(l).autorIds());
    service.eliminarLibro(l);
    assertTrue(service.autores().stream().anyMatch(x -> x.id() == a));
    assertTrue(service.autores().stream().anyMatch(x -> x.id() == b));
    service.eliminarAutor(a);
    service.eliminarAutor(b);
    service.eliminarEditorial(e);
  }

  @Test
  void hqlParametrizadoYSnapshotDeVersion() {
    long e = editorial("HQL"), a = autor(), l = libro(e, List.of(a), false);
    try {
      var viejo = service.obtener(l);
      assertTrue(service.listar("Java", e).stream().anyMatch(x -> x.id() == l));
      assertTrue(service.listar("' or 1=1 --", e).isEmpty());
      service.guardarLibro(
          l,
          viejo.version(),
          new LibroSolicitud(viejo.isbn(), "Editado", viejo.precio(), e, List.of(a), null, null));
      assertThrows(
          BibliotecaException.class,
          () ->
              service.guardarLibro(
                  l,
                  viejo.version(),
                  new LibroSolicitud(
                      viejo.isbn(), "Obsoleto", viejo.precio(), e, List.of(a), null, null)));
      assertEquals("Editado", service.obtener(l).titulo());
    } finally {
      service.eliminarLibro(l);
      service.eliminarAutor(a);
      service.eliminarEditorial(e);
    }
  }

  @Test
  void errorTecnicoConservaCausaYRollback() {
    long e = editorial("Unicidad"), a = autor(), l = libro(e, List.of(a), true);
    try {
      var original = service.obtener(l);
      long antes = contar("Libro"), detallesAntes = contar("Detalle");
      BibliotecaException error =
          assertThrows(
              BibliotecaException.class,
              () ->
                  service.guardarLibro(
                      null,
                      null,
                      new LibroSolicitud(
                          original.isbn(),
                          "Duplicado",
                          new BigDecimal("1.00"),
                          e,
                          List.of(a),
                          "Inglés",
                          20)));
      assertNotNull(error.getCause());
      assertEquals(antes, contar("Libro"));
      assertEquals(detallesAntes, contar("Detalle"));
      assertEquals(original, service.obtener(l));
    } finally {
      service.eliminarLibro(l);
      service.eliminarAutor(a);
      service.eliminarEditorial(e);
    }
  }

  @Test
  void reiniciarFactoryNoRecreaDatos() {
    long e = editorial("Persistente"), a = autor(), l = libro(e, List.of(a), false);
    try {
      try (SessionFactory segunda = HibernateFactory.crear(url, usuario, clave, "update")) {
        var otra = new BibliotecaService(() -> segunda);
        assertEquals(service.obtener(l), otra.obtener(l));
      }
    } finally {
      service.eliminarLibro(l);
      service.eliminarAutor(a);
      service.eliminarEditorial(e);
    }
  }

  @Test
  void duplicarCopiaDetalleSinCompartirloYTrasladaSeleccion() {
    long e = editorial("Origen"),
        destino = editorial("Destino"),
        a = autor(),
        l = libro(e, List.of(a), true);
    Long copia = null;
    try {
      var original = service.obtener(l);
      copia = service.duplicar(l, isbn(), "Nueva edición");
      var nueva = service.obtener(copia);
      assertNotEquals(original.detalleId(), nueva.detalleId());
      assertEquals(original.autorIds(), nueva.autorIds());
      assertTrue(
          service.filtrarAvanzado(a, new BigDecimal("20"), new BigDecimal("40")).stream()
              .anyMatch(x -> x.id() == l));
      service.trasladar(List.of(l, copia), destino, new BigDecimal("10"));
      assertEquals(new BigDecimal("26.91"), service.obtener(l).precio());
      assertEquals(destino, service.obtener(copia).editorialId());
      assertThrows(
          BibliotecaException.class, () -> service.trasladar(List.of(l), destino, BigDecimal.ZERO));
    } finally {
      if (copia != null) service.eliminarLibro(copia);
      service.eliminarLibro(l);
      service.eliminarAutor(a);
      service.eliminarEditorial(e);
      service.eliminarEditorial(destino);
    }
  }

  @Test
  void fusionConFalloIntermedioRevierteLibrosBorradoYNombre() {
    long origen = editorial("Origen"),
        destino = editorial("Destino"),
        tercera = editorial("Nombre ocupado"),
        a = autor(),
        l = libro(origen, List.of(a), true);
    try {
      var original = service.obtener(l);
      String nombreTercera =
          service.editoriales().stream()
              .filter(x -> x.id() == tercera)
              .findFirst()
              .orElseThrow()
              .nombre();
      BibliotecaException error =
          assertThrows(
              BibliotecaException.class, () -> service.fusionar(origen, destino, nombreTercera));
      assertNotNull(error.getCause());
      assertEquals(original, service.obtener(l));
      assertTrue(service.editoriales().stream().anyMatch(x -> x.id() == origen));
      String nuevo = "Fusionada" + UUID.randomUUID();
      assertEquals(1, service.fusionar(origen, destino, nuevo));
      assertEquals(destino, service.obtener(l).editorialId());
      assertEquals(original.autorIds(), service.obtener(l).autorIds());
      assertEquals(original.detalleId(), service.obtener(l).detalleId());
      assertEquals(original.precio(), service.obtener(l).precio());
      assertTrue(service.editoriales().stream().noneMatch(x -> x.id() == origen));
    } finally {
      service.eliminarLibro(l);
      service.eliminarAutor(a);
      if (service.editoriales().stream().anyMatch(x -> x.id() == origen))
        service.eliminarEditorial(origen);
      service.eliminarEditorial(destino);
      service.eliminarEditorial(tercera);
    }
  }

  @Test
  void dosSesionesNoSobrescribenUnaVersionNueva() {
    long e = editorial("Version"), a = autor(), l = libro(e, List.of(a), false);
    try {
      try (Session antigua = factory.openSession()) {
        Transaction tx = antigua.beginTransaction();
        Libro obsoleto = antigua.find(Libro.class, l);
        var vigente = service.obtener(l);
        service.guardarLibro(
            l,
            vigente.version(),
            new LibroSolicitud(
                vigente.isbn(), "Vigente", new BigDecimal("31.00"), e, List.of(a), null, null));
        obsoleto.setTitulo("Perdido");
        RuntimeException error = assertThrows(RuntimeException.class, tx::commit);
        assertTrue(
            error instanceof jakarta.persistence.OptimisticLockException
                || error instanceof org.hibernate.StaleStateException
                || error.getCause() instanceof org.hibernate.StaleStateException);
        if (tx.isActive()) tx.rollback();
      }
      assertEquals("Vigente", service.obtener(l).titulo());
      assertEquals(new BigDecimal("31.00"), service.obtener(l).precio());
    } finally {
      service.eliminarLibro(l);
      service.eliminarAutor(a);
      service.eliminarEditorial(e);
    }
  }
}
