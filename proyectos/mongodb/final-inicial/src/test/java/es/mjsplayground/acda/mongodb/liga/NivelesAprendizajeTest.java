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
@EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "base|aplicacion|reto|todos")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NivelesAprendizajeTest {
  static final String DB = "acda_qa_niveles_" + UUID.randomUUID().toString().replace("-", "");

  @DynamicPropertySource
  static void db(DynamicPropertyRegistry r) {
    r.add("spring.data.mongodb.database", () -> DB);
  }

  @Autowired LigaService servicio;
  @Autowired EquipoRepository equipos;
  @Autowired MongoTemplate mongo;

  @AfterEach
  void limpiarFixture() {
    assertEquals(DB, mongo.getDb().getName());
    equipos.deleteAll();
  }

  @AfterAll
  void cerrar() {
    assertTrue(DB.matches("acda_qa_niveles_[a-f0-9]{32}"));
    assertEquals(DB, mongo.getDb().getName());
    mongo.getDb().drop();
  }

  Equipo fixture() {
    return equipos.insert(
        new Equipo(
            null,
            "Equipo fase",
            "SENIOR",
            "CENTRO",
            List.of(
                new Jugador("a", "Alex", 7, "MEDIO", true, new Estadisticas(2, 3, 1)),
                new Jugador("b", "Luz", 8, "DEFENSA", false, new Estadisticas(0, 0, 0))),
            List.of(),
            null));
  }

  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "base|todos")
  void filtrarYDisponibilidadConservanEstadisticas() {
    Equipo e = fixture();
    assertEquals(List.of(e.jugadores().getFirst()), servicio.filtrarJugadores(e.id(), 3, null));
    assertTrue(servicio.filtrarJugadores(e.id(), 3, false).isEmpty());
    servicio.disponibilidad(e.id(), "a", false);
    Equipo actualizado = servicio.obtener(e.id());
    assertEquals(new Estadisticas(2, 3, 1), actualizado.jugadores().getFirst().estadisticas());
    assertFalse(actualizado.jugadores().getFirst().disponible());
    servicio.disponibilidad(e.id(), "a", false);
    assertEquals(actualizado.jugadores(), servicio.obtener(e.id()).jugadores());
    assertEquals(
        400,
        assertThrows(LigaException.class, () -> servicio.filtrarJugadores(e.id(), -1, null))
            .estado());
    assertEquals(
        404,
        assertThrows(LigaException.class, () -> servicio.disponibilidad(e.id(), "ajeno", true))
            .estado());
  }

  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "aplicacion|todos")
  void jornadaRechazadaNoCambiaNingunCampoYRepetidaNoSuma() {
    Equipo e = fixture();
    JornadaRequest incorrecta =
        new JornadaRequest("j1", "2044-03-20", "Rival", 2, 1, List.of(new Aportacion("a", 1, 0)));
    assertEquals(
        400,
        assertThrows(LigaException.class, () -> servicio.registrarJornada(e.id(), incorrecta))
            .estado());
    assertEquals(e, servicio.obtener(e.id()));
    JornadaRequest correcta =
        new JornadaRequest("j1", "2044-03-20", "Rival", 2, 1, List.of(new Aportacion("a", 2, 1)));
    servicio.registrarJornada(e.id(), correcta);
    Equipo confirmado = servicio.obtener(e.id());
    assertEquals(new Estadisticas(3, 5, 2), confirmado.jugadores().getFirst().estadisticas());
    assertEquals(1, confirmado.jornadas().size());
    assertEquals(
        409,
        assertThrows(LigaException.class, () -> servicio.registrarJornada(e.id(), correcta))
            .estado());
    assertEquals(confirmado, servicio.obtener(e.id()));
    assertEquals(
        409,
        assertThrows(LigaException.class, () -> servicio.eliminarJugador(e.id(), "a")).estado());
  }

  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "aplicacion|todos")
  void mismaJornadaConcurrenteConfirmaSoloUna() throws Exception {
    Equipo e = fixture();
    JornadaRequest r =
        new JornadaRequest(
            "concurrente", "2044-03-20", "Rival", 1, 0, List.of(new Aportacion("a", 1, 0)));
    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch salida = new CountDownLatch(1);
    Callable<Integer> intento =
        () -> {
          salida.await();
          try {
            servicio.registrarJornada(e.id(), r);
            return 200;
          } catch (LigaException x) {
            return x.estado();
          } catch (OptimisticLockingFailureException x) {
            return 409;
          }
        };
    try {
      Future<Integer> a = pool.submit(intento), b = pool.submit(intento);
      salida.countDown();
      List<Integer> resultados = List.of(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS));
      assertEquals(1, resultados.stream().filter(x -> x == 200).count());
      assertEquals(1, resultados.stream().filter(x -> x == 409).count());
      assertEquals(1, servicio.obtener(e.id()).jornadas().size());
      assertEquals(
          new Estadisticas(3, 4, 1),
          servicio.obtener(e.id()).jugadores().getFirst().estadisticas());
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "reto|todos")
  void calculosNoNecesitanQueElRegistroDeJornadasEsteResuelto() {
    // Fixture preparada: el alumno prueba sus cálculos sin programar carga, concurrencia ni
    // pipelines.
    List<Jornada> historia =
        List.of(
            new Jornada("c", "2044-03-22", "Rival", 1, 3, List.of()),
            new Jornada("a", "2044-03-20", "Rival", 2, 1, List.of()),
            new Jornada("b", "2044-03-21", "Rival", 0, 0, List.of()));
    Equipo e =
        equipos.insert(new Equipo(null, "A equipo", "SENIOR", "CENTRO", List.of(), historia, null));
    Equipo vacio =
        equipos.insert(
            new Equipo(null, "B equipo", "SENIOR", "CENTRO", List.of(), List.of(), null));
    assertEquals(List.of("V", "E", "D"), servicio.racha(e.id()).resultados());
    assertTrue(servicio.racha(vacio.id()).resultados().isEmpty());
    var tabla = servicio.clasificacion();
    assertEquals(e.id(), tabla.getFirst().equipoId());
    assertEquals(4, tabla.getFirst().puntos());
    assertEquals(3, tabla.getFirst().jugados());
    assertEquals(-1, tabla.getFirst().diferencia());
    assertEquals(0, tabla.getLast().puntos());
  }
}
