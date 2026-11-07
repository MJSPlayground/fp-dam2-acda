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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnabledIfSystemProperty(named = "qa.mongo", matches = "true")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MongoIntegrationTest {
  static final String DB = "acda_qa_" + UUID.randomUUID().toString().replace("-", "");

  @DynamicPropertySource
  static void db(DynamicPropertyRegistry r) {
    r.add("spring.data.mongodb.database", () -> DB);
  }

  @Autowired LigaService service;
  @Autowired EquipoRepository equipos;
  @Autowired MongoTemplate mongo;
  @Autowired TestRestTemplate http;

  @AfterAll
  void limpiarBasePropia() {
    assertEquals(DB, mongo.getDb().getName());
    assertTrue(DB.startsWith("acda_qa_"));
    mongo.getDb().drop();
  }

  Equipo nuevo() {
    return service.crear(new EquipoRequest("QA-" + UUID.randomUUID(), "JUNIOR", "CENTRO"));
  }

  Equipo conJugador() {
    Equipo e = nuevo();
    return service.agregarJugador(e.id(), new JugadorRequest("Alex", 7, "MEDIO"));
  }

  @Test
  void crudDocumentalConsultasIndicesYHttp() {
    Equipo e = conJugador();
    try {
      assertEquals(24, e.id().length());
      assertEquals(1, e.jugadores().size());
      assertNotNull(e.version());
      assertTrue(
          service.listar("JUNIOR", "MEDIO", 0, 20).elementos().stream()
              .anyMatch(x -> x.id().equals(e.id())));
      assertTrue(mongo.indexOps(Equipo.class).getIndexInfo().stream().anyMatch(i -> i.isUnique()));
      assertEquals(
          400,
          http.postForEntity(
                  "/api/equipos/" + e.id() + "/jugadores",
                  Map.of("nombre", "X", "dorsal", 1.5, "posicion", "MEDIO"),
                  String.class)
              .getStatusCode()
              .value());
      assertEquals(
          404,
          http.getForEntity("/api/equipos/000000000000000000000000", String.class)
              .getStatusCode()
              .value());
      Equipo mod = service.actualizar(e.id(), new EquipoRequest(e.nombre(), "SENIOR", "NORTE"));
      assertEquals("NORTE", mod.sedeId());
      assertEquals(1, mod.jugadores().size());
      assertEquals(
          409,
          assertThrows(
                  LigaException.class,
                  () -> service.agregarJugador(e.id(), new JugadorRequest("Otro", 7, "DEFENSA")))
              .estado());
    } finally {
      service.eliminar(e.id());
    }
  }

  @Test
  void incrementoInvalidaInstantaneaYNoPierdeActualizaciones() throws Exception {
    Equipo e = conJugador();
    String j = e.jugadores().getFirst().id();
    try {
      Equipo antigua = e;
      service.sumarEstadisticas(e.id(), j, new EstadisticasRequest(1, 0));
      assertThrows(OptimisticLockingFailureException.class, () -> equipos.save(antigua));
      ExecutorService pool = Executors.newFixedThreadPool(4);
      try {
        List<Callable<Void>> tareas = new ArrayList<>();
        for (int n = 0; n < 20; n++)
          tareas.add(
              () -> {
                service.sumarEstadisticas(e.id(), j, new EstadisticasRequest(1, 0));
                return null;
              });
        for (Future<Void> t : pool.invokeAll(tareas)) t.get(15, TimeUnit.SECONDS);
      } finally {
        pool.shutdownNow();
      }
      Equipo actual = service.obtener(e.id());
      assertEquals(21, actual.jugadores().getFirst().estadisticas().goles());
      assertEquals(21, actual.jugadores().getFirst().estadisticas().partidos());
      assertTrue(actual.version() > antigua.version());
    } finally {
      service.eliminar(e.id());
    }
  }

  @Test
  void dorsalConcurrenteEsUnico() throws Exception {
    Equipo e = nuevo();
    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch listo = new CountDownLatch(1);
    Callable<Integer> alta =
        () -> {
          listo.await();
          try {
            service.agregarJugador(e.id(), new JugadorRequest("Concurrente", 9, "MEDIO"));
            return 200;
          } catch (LigaException conflicto) {
            return conflicto.estado();
          } catch (OptimisticLockingFailureException conflicto) {
            return 409;
          }
        };
    try {
      Future<Integer> a = pool.submit(alta), b = pool.submit(alta);
      listo.countDown();
      List<Integer> resultados = List.of(a.get(10, TimeUnit.SECONDS), b.get(10, TimeUnit.SECONDS));
      assertEquals(1, resultados.stream().filter(x -> x == 200).count());
      assertEquals(1, resultados.stream().filter(x -> x == 409).count());
      assertEquals(1, service.obtener(e.id()).jugadores().size());
    } finally {
      pool.shutdownNow();
      service.eliminar(e.id());
    }
  }

  @Test
  void plantillaYContadoresRespetanFrontera() {
    Equipo e = nuevo();
    try {
      for (int n = 1; n <= 20; n++)
        service.agregarJugador(e.id(), new JugadorRequest("Jugador" + n, n, "MEDIO"));
      assertEquals(20, service.obtener(e.id()).jugadores().size());
      assertEquals(
          409,
          assertThrows(
                  LigaException.class,
                  () -> service.agregarJugador(e.id(), new JugadorRequest("Extra", 21, "MEDIO")))
              .estado());
      Equipo lleno = service.obtener(e.id());
      String jugador = lleno.jugadores().getFirst().id();
      mongo.updateFirst(
          org.springframework.data.mongodb.core.query.Query.query(
              org.springframework.data.mongodb.core.query.Criteria.where("id").is(e.id())),
          new org.springframework.data.mongodb.core.query.Update()
              .set("jugadores.0.estadisticas", new Estadisticas(9999, 99999, 99999)),
          Equipo.class);
      service.sumarEstadisticas(e.id(), jugador, new EstadisticasRequest(1, 1));
      assertEquals(
          409,
          assertThrows(
                  LigaException.class,
                  () -> service.sumarEstadisticas(e.id(), jugador, new EstadisticasRequest(0, 0)))
              .estado());
      var finales = service.obtener(e.id()).jugadores().getFirst().estadisticas();
      assertEquals(10000, finales.partidos());
      assertEquals(100000, finales.goles());
      assertEquals(100000, finales.asistencias());
    } finally {
      service.eliminar(e.id());
    }
  }
}
