package es.mjsplayground.acda.firestore.reservas;

import static org.junit.jupiter.api.Assertions.*;

import com.google.cloud.firestore.Firestore;
import es.mjsplayground.acda.firestore.reservas.api.ReservaRequest;
import es.mjsplayground.acda.firestore.reservas.model.entity.Reserva;
import es.mjsplayground.acda.firestore.reservas.model.service.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "base|aplicacion|reto|todos")
class NivelesAprendizajeTest {
  static final String PROYECTO = "demo-niveles-" + UUID.randomUUID().toString().substring(0, 8);

  @DynamicPropertySource
  static void entorno(DynamicPropertyRegistry r) {
    r.add("reservas.firestore.modo", () -> "emulador");
    r.add("reservas.firestore.proyecto", () -> PROYECTO);
  }

  @Autowired ReservaService servicio;
  @Autowired Firestore db;

  @AfterEach
  void limpiarSoloDatosPropios() throws Exception {
    assertTrue(PROYECTO.startsWith("demo-niveles-"));
    assertEquals(PROYECTO, db.getOptions().getProjectId());
    for (var doc : db.collection("reservas").get().get().getDocuments())
      doc.getReference().delete().get();
  }

  ReservaRequest r(String sala, int inicio, int fin, String persona) {
    return new ReservaRequest(
        sala,
        "2048-03-20",
        inicio,
        fin,
        persona,
        2,
        List.of("Proyector"),
        Map.of("actividad", "Taller"));
  }

  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "base|todos")
  void cancelacionIdempotenteFiltroExactoYEdicionRechazada() {
    Reserva a = servicio.crear(r("SALA-A", 8, 9, "Recepción"));
    Reserva cancelada = servicio.cancelar(a.id(), "Cambio de actividad");
    assertEquals("CANCELADA", cancelada.estado());
    assertEquals(a.equipo(), cancelada.equipo());
    assertEquals(a.detalles(), cancelada.detalles());
    assertEquals(
        400,
        assertThrows(ReservaException.class, () -> servicio.cancelar(a.id(), "x".repeat(121)))
            .estado());
    assertEquals(
        "Cambio de actividad", servicio.cancelar(a.id(), "Otro motivo").motivoCancelacion());
    assertEquals(
        400, assertThrows(ReservaException.class, () -> servicio.cancelar(a.id(), " ")).estado());
    assertEquals(
        404,
        assertThrows(ReservaException.class, () -> servicio.cancelar("ausente", "Cambio"))
            .estado());
    assertEquals(
        409,
        assertThrows(
                ReservaException.class,
                () -> servicio.actualizar(a.id(), r("SALA-A", 8, 9, "Recepción")))
            .estado());
    assertEquals(List.of(cancelada), servicio.filtrar(null, null, null, "CANCELADA", "Recepción"));
    assertTrue(servicio.filtrar(null, null, null, "CANCELADA", "recepción").isEmpty());
    assertEquals(
        400,
        assertThrows(ReservaException.class, () -> servicio.filtrar(null, null, null, "OTRO", null))
            .estado());
    assertEquals(cancelada, servicio.obtener(a.id()));
    servicio.eliminar(a.id());
    assertEquals(
        404, assertThrows(ReservaException.class, () -> servicio.obtener(a.id())).estado());
  }

  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "aplicacion|todos")
  void adyacenciaConflictoYDosGuardadosConcurrentes() throws Exception {
    Reserva a = servicio.crear(r("SALA-A", 8, 10, "Persona A")),
        b = servicio.crear(r("SALA-A", 10, 11, "Persona B"));
    assertFalse(servicio.disponible("SALA-A", "2048-03-20", 8, 9));
    assertTrue(servicio.disponible("SALA-A", "2048-03-20", 11, 12));
    assertEquals(
        409,
        assertThrows(
                ReservaException.class,
                () -> servicio.actualizar(b.id(), r("SALA-A", 9, 11, "Persona B")))
            .estado());
    assertEquals(b, servicio.obtener(b.id()));
    servicio.cancelar(a.id(), "Liberar franja");
    assertTrue(servicio.disponible("SALA-A", "2048-03-20", 8, 10));
    assertEquals(b, servicio.actualizar(b.id(), r("SALA-A", 10, 11, "Persona B")));
    assertEquals(
        400,
        assertThrows(
                ReservaException.class, () -> servicio.disponible("SALA-A", "2048-03-20", 12, 11))
            .estado());
    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch salida = new CountDownLatch(1);
    try {
      Callable<Integer> crear =
          () -> {
            salida.await();
            try {
              servicio.crear(r("SALA-B", 14, 16, "Concurrente"));
              return 201;
            } catch (ReservaException e) {
              return e.estado();
            }
          };
      Future<Integer> x = pool.submit(crear), y = pool.submit(crear);
      salida.countDown();
      List<Integer> estados = List.of(x.get(40, TimeUnit.SECONDS), y.get(40, TimeUnit.SECONDS));
      assertEquals(1, estados.stream().filter(v -> v == 201).count());
      assertEquals(1, estados.stream().filter(v -> v == 409).count());
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  @EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "reto|todos")
  void cupoNormalizadoYPaginasSinRepetirNiExigirCursorExistente() {
    Reserva a = servicio.crear(r("SALA-A", 8, 9, "Coordinación"));
    servicio.crear(r("SALA-A", 9, 10, "coordinación"));
    assertEquals(
        409,
        assertThrows(
                ReservaException.class,
                () -> servicio.crear(r("SALA-B", 12, 13, "  COORDINACIÓN  ")))
            .estado());
    servicio.cancelar(a.id(), "Liberar cupo");
    servicio.crear(r("SALA-B", 12, 13, "COORDINACIÓN"));
    Reserva otra =
        servicio.crear(
            new ReservaRequest(
                "SALA-A", "2048-03-21", 8, 9, "COORDINACIÓN", 2, List.of(), Map.of()));
    assertEquals(
        409,
        assertThrows(
                ReservaException.class,
                () -> servicio.actualizar(otra.id(), r("SALA-A", 14, 15, "coordinación")))
            .estado());
    assertEquals(otra, servicio.obtener(otra.id()));
    servicio.eliminar(otra.id());
    assertEquals(3, servicio.pagina(20, null).elementos().size());
    assertNull(servicio.pagina(20, null).siguienteCursor());
    var primera = servicio.pagina(2, null);
    assertEquals(primera, servicio.pagina(2, null));
    assertEquals(2, primera.elementos().size());
    assertNotNull(primera.siguienteCursor());
    String cursor = primera.siguienteCursor();
    servicio.eliminar(cursor);
    var segunda = servicio.pagina(2, cursor);
    assertEquals(1, segunda.elementos().size());
    assertNull(segunda.siguienteCursor());
    assertTrue(segunda.elementos().getFirst().id().compareTo(cursor) > 0);
    assertEquals(segunda, servicio.pagina(2, cursor));
    for (int limite : List.of(0, 21))
      assertEquals(
          400, assertThrows(ReservaException.class, () -> servicio.pagina(limite, null)).estado());
    assertEquals(
        400, assertThrows(ReservaException.class, () -> servicio.pagina(2, "../x")).estado());
  }
}
