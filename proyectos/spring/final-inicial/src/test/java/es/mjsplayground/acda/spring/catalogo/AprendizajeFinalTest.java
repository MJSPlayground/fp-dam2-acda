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
@Import(AprendizajeFinalTest.Eventos.class)
@EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "base|aplicacion|reto|todos")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AprendizajeFinalTest {
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

  @Autowired PedidoRepository pedidosRepo;
  @Autowired PresupuestoService presupuestos;
  @Autowired PedidoService pedidos;
  @Autowired ExistenciasService existencias;

  @AfterEach
  void limpiarDatos() {
    pedidosRepo.deleteAll();
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
  @EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "base|todos")
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
  @EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "base|todos")
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
  @EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "base|todos")
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

  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "base|todos")
  void reposicionLimiteYVersionSinPresupuesto() {
    LibroDatos l = libro(editorial(), 49, "10.00");
    existencias.reponer(l.id(), l.version(), 1);
    LibroDatos actual = catalogo.obtener(l.id());
    assertEquals(50, actual.stock());
    assertThrows(ReglaCatalogoException.class, () -> existencias.reponer(l.id(), l.version(), 1));
    assertThrows(ReglaCatalogoException.class, () -> existencias.reponer(actual.id(), actual.version(), 1));
    assertEquals(50, catalogo.obtener(l.id()).stock());
  }

  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "aplicacion|todos")
  void reposicionPresupuestoLimitesYNoMutacion() {
    Long e = editorial();
    LibroDatos l = libro(e, 10, "25.00");
    existencias.reponer(l.id(), l.version(), 5);
    LibroDatos actual = catalogo.obtener(l.id());
    assertEquals(15, actual.stock());
    assertThrows(ReglaCatalogoException.class, () -> existencias.reponer(l.id(), l.version(), 1));
    assertThrows(
        ReglaCatalogoException.class, () -> existencias.reponer(l.id(), actual.version(), 100));
    var p = presupuestos.calcular(List.of(new LineaSolicitud(l.id(), 4, actual.version())));
    assertEquals(new BigDecimal("100.00"), p.subtotal());
    assertEquals(new BigDecimal("5.00"), p.descuento());
    assertEquals(new BigDecimal("95.00"), p.total());
    assertEquals(15, catalogo.obtener(l.id()).stock());
    assertEquals(0, pedidosRepo.count());
  }

  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "reto|todos")
  void pedidoClaveSensibleYRollbackTrasStockReal() {
    Long e = editorial();
    LibroDatos a = libro(e, 10, "20.00"), b = libro(e, 10, "30.00");
    var primero =
        pedidos.confirmar(
            "Pedido_A",
            List.of(
                new LineaSolicitud(a.id(), 1, a.version()),
                new LineaSolicitud(b.id(), 1, b.version())));
    assertEquals(9, catalogo.obtener(a.id()).stock());
    observador.cambios.clear();
    LibroDatos actualA = catalogo.obtener(a.id()), actualB = catalogo.obtener(b.id());
    RuntimeException fallo =
        assertThrows(
            RuntimeException.class,
            () ->
                pedidos.confirmar(
                    "Pedido_A",
                    List.of(
                        new LineaSolicitud(a.id(), 2, actualA.version()),
                        new LineaSolicitud(b.id(), 2, actualB.version()))));
    assertNotNull(fallo.getCause());
    assertEquals(9, catalogo.obtener(a.id()).stock());
    assertEquals(9, catalogo.obtener(b.id()).stock());
    assertEquals(1, pedidosRepo.count());
    assertTrue(observador.cambios.isEmpty());
    pedidos.confirmar("pedido_a", List.of(new LineaSolicitud(a.id(), 1, actualA.version())));
    assertEquals(2, pedidosRepo.count());
    assertEquals(8, catalogo.obtener(a.id()).stock());
    assertEquals("Pedido_A", primero.clave());
  }

  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "reto|todos")
  void pedidoConcurrenteNoDuplicaClaveNiStock() throws Exception {
    Long e = editorial();
    LibroDatos a = libro(e, 10, "20.00");
    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch inicio = new CountDownLatch(1);
    Callable<Boolean> accion =
        () -> {
          inicio.await();
          try {
            pedidos.confirmar("concurrente", List.of(new LineaSolicitud(a.id(), 1, a.version())));
            return true;
          } catch (RuntimeException conflicto) {
            return false;
          }
        };
    try {
      Future<Boolean> x = pool.submit(accion), y = pool.submit(accion);
      inicio.countDown();
      assertNotEquals(x.get(20, TimeUnit.SECONDS), y.get(20, TimeUnit.SECONDS));
      assertEquals(1, pedidosRepo.count());
      assertEquals(9, catalogo.obtener(a.id()).stock());
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "reto|todos")
  void paginasOperacionesYPedidosRenderizan() throws Exception {
    Long e = editorial();
    libro(e, 5, "1.00");
    mvc.perform(get("/operaciones"))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("Confirmar pedido")));
    mvc.perform(get("/pedidos")).andExpect(status().isOk());
  }
  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "aplicacion|todos")
  void presupuestoUmbralesRedondeoInmutabilidadYRechazos() {
    Long e = editorial();
    LibroDatos bajo = libro(e, 5, "99.90"), redondeo = libro(e, 10, "10.01");
    var sinDescuento = presupuestos.calcular(List.of(new LineaSolicitud(bajo.id(), 1, bajo.version())));
    assertEquals(new BigDecimal("0.00"), sinDescuento.descuento());
    var solicitud = new ArrayList<LineaSolicitud>();
    solicitud.add(new LineaSolicitud(redondeo.id(), 10, redondeo.version()));
    var calculado = presupuestos.calcular(solicitud);
    solicitud.clear();
    assertEquals(1, calculado.lineas().size());
    assertEquals(new BigDecimal("100.10"), calculado.subtotal());
    assertEquals(new BigDecimal("5.01"), calculado.descuento());
    assertEquals(new BigDecimal("95.09"), calculado.total());
    assertThrows(UnsupportedOperationException.class, () -> calculado.lineas().clear());
    var linea = new LineaSolicitud(bajo.id(), 1, bajo.version());
    assertThrows(ReglaCatalogoException.class, () -> presupuestos.calcular(List.of(linea, linea)));
    assertThrows(ReglaCatalogoException.class, () -> presupuestos.calcular(List.of()));
    for (int cantidad : new int[]{0, -1, 101, 6}) {
      assertThrows(ReglaCatalogoException.class, () -> presupuestos.calcular(List.of(new LineaSolicitud(bajo.id(), cantidad, bajo.version()))));
    }
    assertThrows(ReglaCatalogoException.class, () -> presupuestos.calcular(List.of(new LineaSolicitud(bajo.id(), 1, -1L))));
    assertThrows(NoEncontradoException.class, () -> presupuestos.calcular(List.of(new LineaSolicitud(Long.MAX_VALUE, 1, 0L))));
    assertEquals(5, catalogo.obtener(bajo.id()).stock());
    assertEquals(10, catalogo.obtener(redondeo.id()).stock());
    assertEquals(0, pedidosRepo.count());
    var veinteUna = new ArrayList<LineaSolicitud>();
    for (int i=0; i<21; i++) {
      LibroDatos otro = libro(e, 1, "1.00");
      veinteUna.add(new LineaSolicitud(otro.id(), 1, otro.version()));
    }
    assertEquals(20, presupuestos.calcular(veinteUna.subList(0, 20)).lineas().size());
    assertThrows(ReglaCatalogoException.class, () -> presupuestos.calcular(veinteUna));
    assertEquals(0, pedidosRepo.count());
  }

  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "reto|todos")
  void historialConservaInstantaneaYRechazaClaveInvalida() {
    LibroDatos l = libro(editorial(), 5, "20.00");
    var lineas = List.of(new LineaSolicitud(l.id(), 1, l.version()));
    for (String clave : new String[]{"", "con espacio", "x".repeat(61)}) {
      assertThrows(ReglaCatalogoException.class, () -> pedidos.confirmar(clave, lineas));
    }
    assertEquals(5, catalogo.obtener(l.id()).stock());
    pedidos.confirmar("instantanea", lineas);
    LibroDatos actual = catalogo.obtener(l.id());
    catalogo.guardar(new LibroSolicitud(actual.id(), actual.version(), actual.isbn(), "Titulo posterior", new BigDecimal("25.00"), actual.stock(), actual.editorialId()));
    var guardado = pedidos.listar().getFirst();
    assertEquals(l.titulo(), guardado.lineas().getFirst().titulo());
    assertEquals(new BigDecimal("20.00"), guardado.lineas().getFirst().precioUnitario());
    assertEquals(new BigDecimal("20.00"), guardado.total());
  }

}
