package es.mjsplayground.acda.spring.catalogo;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import es.mjsplayground.acda.spring.catalogo.model.repository.EditorialRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"seguro", "silencioso"})
@EnabledIfSystemProperty(named = "qa.mysql", matches = "true")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SeguridadIntegrationTest {
  static final MySqlEsquema db = new MySqlEsquema();

  @DynamicPropertySource
  static void propiedades(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", db::url);
    r.add("spring.datasource.username", () -> db.usuario);
    r.add("spring.datasource.password", () -> db.clave);
  }

  @Autowired MockMvc mvc;
  @Autowired EditorialRepository editoriales;

  @AfterAll
  void limpiar() throws Exception {
    db.eliminar();
  }

  @AfterEach
  void limpiarDatos() {
    editoriales.deleteAll();
  }

  @Test
  void loginRealPermisosYCsrf() throws Exception {
    mvc.perform(get("/libros"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
    mvc.perform(formLogin().user("gestor").password("incorrecta"))
        .andExpect(unauthenticated())
        .andExpect(redirectedUrl("/login?error"));
    mvc.perform(formLogin().user("lector").password("lector_local"))
        .andExpect(authenticated().withUsername("lector"));
    mvc.perform(get("/libros").with(user("lector").roles("LECTOR"))).andExpect(status().isOk());
    mvc.perform(
            post("/editoriales/guardar")
                .with(user("lector").roles("LECTOR"))
                .with(csrf())
                .param("nombre", "Prohibida"))
        .andExpect(status().isForbidden());
    assertEquals(0, editoriales.count());
    mvc.perform(
            post("/editoriales/guardar")
                .with(user("gestor").roles("GESTOR"))
                .param("nombre", "Sin CSRF"))
        .andExpect(status().isForbidden());
    assertEquals(0, editoriales.count());
    mvc.perform(
            post("/editoriales/guardar")
                .with(user("gestor").roles("GESTOR"))
                .with(csrf())
                .param("nombre", "Permitida"))
        .andExpect(status().is3xxRedirection());
    assertEquals(1, editoriales.count());
  }

  @Test
  void logoutPostProtegidoYCierraSesion() throws Exception {
    MvcResult login =
        mvc.perform(formLogin().user("gestor").password("gestor_local"))
            .andExpect(authenticated())
            .andReturn();
    MockHttpSession sesion = (MockHttpSession) login.getRequest().getSession(false);
    assertNotNull(sesion);
    mvc.perform(post("/logout").session(sesion)).andExpect(status().isForbidden());
    mvc.perform(post("/logout").session(sesion).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(unauthenticated());
    assertTrue(sesion.isInvalid());
  }
}
