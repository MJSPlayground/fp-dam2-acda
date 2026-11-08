package es.mjsplayground.acda.spring.catalogo;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import es.mjsplayground.acda.spring.catalogo.config.CatalogoProperties;
import es.mjsplayground.acda.spring.catalogo.model.repository.*;
import es.mjsplayground.acda.spring.catalogo.model.service.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.event.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"practica", "silencioso"})
@Import(AprendizajeGuiadoTest.Eventos.class)
@EnabledIfSystemProperty(named = "aprendizaje.fase", matches = "[2345]|todas")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AprendizajeGuiadoTest {
  static final MySqlEsquema db = new MySqlEsquema();

  @DynamicPropertySource
  static void propiedades(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", db::url);
    r.add("spring.datasource.username", () -> db.usuario);
    r.add("spring.datasource.password", () -> db.clave);
  }

  static class Observador {
    final List<CambioCatalogo> cambios = new CopyOnWriteArrayList<>();

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void observar(CambioCatalogo c) {
      cambios.add(c);
    }
  }

  @TestConfiguration
  static class Eventos {
    @Bean
    Observador observador() {
      return new Observador();
    }
  }

  @Autowired CatalogoService catalogo;
  @Autowired EditorialService editoriales;
  @Autowired LibroRepository libros;
  @Autowired EditorialRepository editorialesRepo;
  @Autowired MockMvc mvc;
  @Autowired Observador observador;
  @Autowired CatalogoProperties props;

  @AfterAll
  void limpiarBase() throws Exception {
    db.eliminar();
  }

  @AfterEach
  void limpiarDatos() {
    libros.deleteAll();
    editorialesRepo.deleteAll();
    observador.cambios.clear();
  }

  Long editorial() {
    return editoriales.guardar(null, "Editorial-" + UUID.randomUUID());
  }

  String isbn() {
    return "978"
        + String.format(
            "%010d", Math.abs(UUID.randomUUID().getLeastSignificantBits() % 10000000000L));
  }

  LibroDatos libro(Long editorial, int stock, String precio) {
    Long id =
        catalogo.guardar(
            new LibroSolicitud(
                null,
                null,
                isbn(),
                "Título, \"datos\"\nsegunda línea",
                new BigDecimal(precio),
                stock,
                editorial));
    return catalogo.obtener(id);
  }

  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.fase", matches = "2|todas")
  void consultasConDatosPreparadosSinNecesitarGuardarDelAlumno() {
    var e = editorialesRepo.saveAndFlush(new es.mjsplayground.acda.spring.catalogo.model.entity.Editorial("Consulta"));
    libros.saveAndFlush(new es.mjsplayground.acda.spring.catalogo.model.entity.Libro("9780000000001", "Java inicial", new BigDecimal("12.50"), 3, e));
    assertEquals(1, catalogo.listar("JAVA", e.getId()).size());
    assertTrue(catalogo.listar("sin coincidencia", null).isEmpty());
  }

  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.fase", matches = "4|todas")
  void falloPreparadoTrasEscribirEditorialesRevierteDemo() throws Exception {
    try (var c = java.sql.DriverManager.getConnection(db.url(), db.usuario, db.clave); var st = c.createStatement()) {
      st.execute("CREATE TRIGGER fallo_demo BEFORE INSERT ON libro FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Fallo preparado despues de editoriales'");
    }
    try {
      RuntimeException error = assertThrows(RuntimeException.class, catalogo::demo);
      assertNotNull(error.getCause(), "Conservar la causa tecnica");
      assertEquals(0, editorialesRepo.count(), "Las editoriales escritas tambien se revierten");
      assertEquals(0, libros.count());
      assertTrue(observador.cambios.isEmpty(), "No hay evento de exito tras rollback");
    } finally {
      try (var c = java.sql.DriverManager.getConnection(db.url(), db.usuario, db.clave); var st = c.createStatement()) {
        st.execute("DROP TRIGGER IF EXISTS fallo_demo");
      }
    }
  }

  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.fase", matches = "3|5|todas")
  void crudDerivadasVersionEventoYPerfil() {
    assertEquals(50, props.maxStock());
    Long e = editorial();
    LibroDatos l = libro(e, 10, "29.90");
    assertTrue(catalogo.listar("datos", e).stream().anyMatch(x -> x.id().equals(l.id())));
    observador.cambios.clear();
    catalogo.guardar(
        new LibroSolicitud(
            l.id(), l.version(), l.isbn(), l.titulo(), new BigDecimal("30.00"), 11, e));
    assertEquals(1, observador.cambios.size());
    assertThrows(
        ReglaCatalogoException.class,
        () ->
            catalogo.guardar(
                new LibroSolicitud(l.id(), l.version(), l.isbn(), "Viejo", l.precio(), 10, e)));
    assertEquals(l.titulo(), catalogo.obtener(l.id()).titulo());
    assertEquals(1, observador.cambios.size());
    assertThrows(ReglaCatalogoException.class, () -> editoriales.eliminar(e));
  }

  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.fase", matches = "3|todas")
  void formulariosMvcCsrfTiposYMensajes() throws Exception {
    Long e = editorial();
    mvc.perform(get("/libros"))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("Libros")));
    mvc.perform(get("/libros/nuevo"))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("_csrf")));
    mvc.perform(post("/libros/guardar").param("isbn", isbn())).andExpect(status().isForbidden());
    mvc.perform(
            post("/libros/guardar")
                .with(csrf())
                .param("isbn", isbn())
                .param("titulo", "Válido")
                .param("precio", "1.001")
                .param("stock", "2")
                .param("editorialId", e.toString()))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasFieldErrors("libroForm", "precio"));
    assertEquals(0, libros.count());
    mvc.perform(
            post("/libros/guardar")
                .with(csrf())
                .param("isbn", isbn())
                .param("titulo", "Título\ncon salto")
                .param("precio", "12.50")
                .param("stock", "2")
                .param("editorialId", e.toString()))
        .andExpect(status().is3xxRedirection());
    assertEquals(1, libros.count());
    mvc.perform(get("/libros").param("editorial", "abc")).andExpect(status().isBadRequest());
    mvc.perform(post("/libros").with(csrf()))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(header().exists("Allow"));
    mvc.perform(get("/no-existe")).andExpect(status().isNotFound());
  }

  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.fase", matches = "4|todas")
  void demoNoVuelveASembrar() {
    catalogo.demo();
    assertEquals(2, libros.count());
    LibroDatos l = catalogo.listar("", null).getFirst();
    catalogo.guardar(
        new LibroSolicitud(
            l.id(),
            l.version(),
            l.isbn(),
            "Editado por usuario",
            l.precio(),
            l.stock(),
            l.editorialId()));
    assertThrows(ReglaCatalogoException.class, catalogo::demo);
    assertEquals(2, libros.count());
    assertEquals("Editado por usuario", catalogo.obtener(l.id()).titulo());
  }
}
