package es.mjsplayground.acda.mongodb.liga;

import static org.junit.jupiter.api.Assertions.*;

import es.mjsplayground.acda.mongodb.liga.api.*;
import es.mjsplayground.acda.mongodb.liga.model.entity.*;
import es.mjsplayground.acda.mongodb.liga.model.repository.*;
import es.mjsplayground.acda.mongodb.liga.model.service.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@EnabledIfSystemProperty(
    named = "aprendizaje.fase",
    matches = "consultas|equipos|jugadores|estadisticas|todas")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FasesAprendizajeTest {
  static final String DB = "acda_qa_fases_" + UUID.randomUUID().toString().replace("-", "");

  @DynamicPropertySource
  static void db(DynamicPropertyRegistry r) {
    r.add("spring.data.mongodb.database", () -> DB);
  }

  @Autowired LigaService servicio;
  @Autowired EquipoRepository equipos;
  @Autowired MongoTemplate mongo;

  @AfterEach
  void vaciarSoloFixture() {
    assertEquals(DB, mongo.getDb().getName());
    equipos.deleteAll();
  }

  @AfterAll
  void cerrar() {
    assertTrue(DB.matches("acda_qa_fases_[a-f0-9]{32}"));
    assertEquals(DB, mongo.getDb().getName());
    mongo.getDb().drop();
  }

  Equipo fixture(String nombre, String categoria) {
    return equipos.insert(
        new Equipo(
            null,
            nombre,
            categoria,
            "CENTRO",
            List.of(
                new Jugador("jugador-fase", "Alex", 7, "MEDIO", true, new Estadisticas(2, 3, 1))),
            null));
  }

  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.fase", matches = "consultas|todas")
  void consultasSinNecesitarCrearDesdeServicio() {
    Equipo a = fixture("A fase", "SENIOR"), b = fixture("B fase", "JUNIOR");
    assertEquals(List.of(a), servicio.listar("SENIOR", null, 0, 20).elementos());
    assertEquals(2, servicio.listar(null, "MEDIO", 0, 20).totalElementos());
    assertEquals(List.of(b), servicio.listar("JUNIOR", "MEDIO", 0, 20).elementos());
    assertTrue(servicio.listar("SENIOR", "PORTERO", 0, 20).elementos().isEmpty());
    assertEquals(1, servicio.listar(null, null, 0, 1).elementos().size());
    assertEquals(2, servicio.listar(null, null, 0, 1).totalPaginas());
  }

  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.fase", matches = "equipos|todas")
  void equipoSeCreaYEditaSinNecesitarOperacionesDeJugador() {
    Equipo creado = servicio.crear(new EquipoRequest("Equipo fase", "SENIOR", "CENTRO"));
    assertNotNull(creado.id());
    assertTrue(creado.jugadores().isEmpty());
    Equipo conPlantilla = fixture("Con plantilla", "JUNIOR");
    Equipo editado =
        servicio.actualizar(conPlantilla.id(), new EquipoRequest("Renombrado", "SENIOR", "NORTE"));
    assertEquals(conPlantilla.jugadores(), editado.jugadores());
    assertEquals(
        400,
        assertThrows(
                LigaException.class,
                () -> servicio.crear(new EquipoRequest("X", "SENIOR", "AUSENTE")))
            .estado());
    assertEquals(2, equipos.count());
  }

  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.fase", matches = "jugadores|todas")
  void plantillaConservaEstadisticasSinNecesitarTODODeIncrementos() {
    Equipo e = fixture("Plantilla", "SENIOR");
    Equipo actual =
        servicio.editarJugador(
            e.id(), "jugador-fase", new JugadorRequest("Alex editado", 8, "DEFENSA"));
    assertEquals(new Estadisticas(2, 3, 1), actual.jugadores().getFirst().estadisticas());
    Equipo conOtro = servicio.agregarJugador(e.id(), new JugadorRequest("Luz", 9, "MEDIO"));
    assertEquals(2, conOtro.jugadores().size());
    Equipo antes = servicio.obtener(e.id());
    assertEquals(
        409,
        assertThrows(
                LigaException.class,
                () -> servicio.agregarJugador(e.id(), new JugadorRequest("Duplicado", 9, "MEDIO")))
            .estado());
    assertEquals(antes, servicio.obtener(e.id()));
    servicio.eliminarJugador(e.id(), "jugador-fase");
    assertEquals("Luz", servicio.obtener(e.id()).jugadores().getFirst().nombre());
  }

  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.fase", matches = "estadisticas|todas")
  void helperPreparadoNoPierdeIncrementosYDetectaCopiaAntigua() throws Exception {
    Equipo antigua = fixture("Contadores", "SENIOR");
    String id = antigua.id();
    servicio.sumarEstadisticas(id, "jugador-fase", new EstadisticasRequest(1, 0));
    assertThrows(OptimisticLockingFailureException.class, () -> equipos.save(antigua));
    ExecutorService pool = Executors.newFixedThreadPool(4);
    try {
      List<Callable<Void>> tareas = new ArrayList<>();
      for (int n = 0; n < 8; n++)
        tareas.add(
            () -> {
              servicio.sumarEstadisticas(id, "jugador-fase", new EstadisticasRequest(1, 0));
              return null;
            });
      for (Future<Void> f : pool.invokeAll(tareas)) f.get(15, TimeUnit.SECONDS);
    } finally {
      pool.shutdownNow();
    }
    var actual = servicio.obtener(id);
    assertEquals(new Estadisticas(11, 12, 1), actual.jugadores().getFirst().estadisticas());
    assertTrue(actual.version() > antigua.version());
    assertEquals(
        400,
        assertThrows(
                LigaException.class,
                () ->
                    servicio.sumarEstadisticas(id, "jugador-fase", new EstadisticasRequest(-1, 0)))
            .estado());
    assertEquals(actual, servicio.obtener(id));
  }
}
