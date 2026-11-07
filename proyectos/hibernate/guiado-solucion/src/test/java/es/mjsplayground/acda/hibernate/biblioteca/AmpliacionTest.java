package es.mjsplayground.acda.hibernate.biblioteca;

import static org.junit.jupiter.api.Assertions.*;

import es.mjsplayground.acda.hibernate.biblioteca.config.HibernateFactory;
import es.mjsplayground.acda.hibernate.biblioteca.demo.*;
import es.mjsplayground.acda.hibernate.biblioteca.model.service.BibliotecaService;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/** Fixture técnica: JDBC aquí solo administra un esquema QA propio, no enseña el CRUD. */
@EnabledIfSystemProperty(named = "qa.mysql", matches = "true")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AmpliacionTest {
  private final String esquema =
      "qa_hibernate_demo_" + UUID.randomUUID().toString().replace("-", "");
  private final String adminUrl =
      System.getenv().getOrDefault("QA_MYSQL_ADMIN_URL", "jdbc:mysql://127.0.0.1:3308/mysql");
  private final String usuario = System.getenv().getOrDefault("QA_MYSQL_ADMIN_USER", "root");
  private final String clave =
      System.getenv().getOrDefault("QA_MYSQL_ADMIN_PASSWORD", "qa_root_local");
  private String url;
  private SessionFactory factory;

  @BeforeAll
  void crearEsquemaPropio() throws Exception {
    assertTrue(esquema.matches("qa_hibernate_demo_[a-f0-9]{32}"));
    try (Connection c = DriverManager.getConnection(adminUrl, usuario, clave);
        Statement s = c.createStatement()) {
      s.executeUpdate(
          "CREATE DATABASE `" + esquema + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs");
    }
    String servidor = adminUrl.substring(0, adminUrl.lastIndexOf('/'));
    url = servidor + "/" + esquema + "?serverTimezone=UTC";
    factory = HibernateFactory.crear(url, usuario, clave, "update");
    new BibliotecaService(() -> factory).datosDemo();
  }

  @AfterAll
  void limpiarSoloEsquemaPropio() throws Exception {
    if (factory != null) factory.close();
    if (url == null) return;
    if (!esquema.matches("qa_hibernate_demo_[a-f0-9]{32}"))
      throw new IllegalStateException("Esquema fuera de QA");
    try (Connection c = DriverManager.getConnection(adminUrl, usuario, clave);
        Statement s = c.createStatement()) {
      s.executeUpdate("DROP DATABASE `" + esquema + "`");
    }
  }

  @Test
  void joinsProyeccionesAgregacionPaginaYSqlDevuelvenDatosCoherentes() {
    assertEquals(3, ConsultasDemo.librosYAutores(factory).size());
    var proyeccion = ConsultasDemo.proyeccion(factory);
    assertEquals(2, proyeccion.size());
    assertEquals("Historias locales", proyeccion.getFirst().titulo());
    BigDecimal total =
        ConsultasDemo.totales(factory).stream()
            .map(ConsultasDemo.TotalEditorial::importe)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    assertEquals(0, new BigDecimal("48.40").compareTo(total));
    assertEquals(List.of("Historias locales"), ConsultasDemo.pagina(factory, 0, 1));
    assertEquals(List.of("Java y datos"), ConsultasDemo.pagina(factory, 1, 1));
    assertTrue(ConsultasDemo.pagina(factory, 2, 1).isEmpty());
    assertThrows(IllegalArgumentException.class, () -> ConsultasDemo.pagina(factory, -1, 1));
    assertEquals(2, ConsultasDemo.recuentoNativo(factory));
  }

  @Test
  void joinFetchReduceSentenciasSinPerderLibrosNiAsociaciones() {
    var ordinaria = ConsultasDemo.medirCarga(factory, false);
    var fetch = ConsultasDemo.medirCarga(factory, true);
    assertEquals(2, ordinaria.libros());
    assertEquals(ordinaria.libros(), fetch.libros());
    assertTrue(ordinaria.sentencias() > fetch.sentencias(), ordinaria + " / " + fetch);
    assertEquals(1, fetch.sentencias());
    org.slf4j.LoggerFactory.getLogger(AmpliacionTest.class)
        .info("Carga comparada: ordinaria={}, JOIN FETCH={}", ordinaria, fetch);
  }

  @Test
  void xmlMapeaClaseSinAnotacionesYRecuperaDesdeOtraSesion() {
    try (SessionFactory xml = XmlDemo.configurar(url, usuario, clave, "create-drop")) {
      Long id =
          xml.fromTransaction(
              s -> {
                NotaXml nota = new NotaXml("Texto & XML");
                s.persist(nota);
                return nota.getId();
              });
      assertNotNull(id);
      String texto = xml.fromTransaction(s -> s.find(NotaXml.class, id).getTexto());
      assertEquals("Texto & XML", texto);
    }
  }
}
