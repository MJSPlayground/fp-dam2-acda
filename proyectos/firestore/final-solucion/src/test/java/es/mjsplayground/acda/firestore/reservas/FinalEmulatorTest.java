package es.mjsplayground.acda.firestore.reservas;

import static org.junit.jupiter.api.Assertions.*;

import es.mjsplayground.acda.firestore.reservas.api.ReservaRequest;
import es.mjsplayground.acda.firestore.reservas.model.entity.Reserva;
import es.mjsplayground.acda.firestore.reservas.model.service.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnabledIfSystemProperty(named = "qa.emulador", matches = "true")
class FinalEmulatorTest {
  @Autowired ReservaService service;

  ReservaRequest r(String espacio, String fecha, int inicio, int fin, String responsable) {
    return new ReservaRequest(espacio, fecha, inicio, fin, responsable, 2, List.of(), Map.of());
  }

  @Test
  void concurrenciaSolapamientoParcialAceptaSoloUna() throws Exception {
    String nombre = "concurrente-" + UUID.randomUUID();
    String fecha = "2051-03-11";
    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch inicio = new CountDownLatch(1);
    List<Callable<Integer>> tareas =
        List.of(
            () -> {
              inicio.await();
              return intentar(r("SALA-A", fecha, 9, 11, nombre));
            },
            () -> {
              inicio.await();
              return intentar(r("SALA-A", fecha, 10, 12, nombre));
            });
    try {
      Future<Integer> a = pool.submit(tareas.get(0)), b = pool.submit(tareas.get(1));
      inicio.countDown();
      List<Integer> estados = List.of(a.get(30, TimeUnit.SECONDS), b.get(30, TimeUnit.SECONDS));
      assertEquals(1, estados.stream().filter(x -> x == 201).count());
      assertEquals(1, estados.stream().filter(x -> x == 409).count());
      assertEquals(1, service.filtrar(null, fecha, null, null, nombre).size());
    } finally {
      pool.shutdownNow();
      service.filtrar(null, fecha, null, null, nombre).forEach(x -> service.eliminar(x.id()));
    }
  }

  int intentar(ReservaRequest r) {
    try {
      service.crear(r);
      return 201;
    } catch (ReservaException e) {
      return e.estado();
    }
  }

  @Test
  void cupoCancelacionIdempotenciaYAdyacencia() {
    String nombre = "cupo-" + UUID.randomUUID();
    String fecha = "2051-03-12";
    try {
      Reserva a = service.crear(r("SALA-A", fecha, 8, 9, nombre));
      Reserva b = service.crear(r("SALA-A", fecha, 9, 10, nombre));
      assertFalse(service.disponible("SALA-A", fecha, 8, 9));
      assertTrue(service.disponible("SALA-A", fecha, 10, 11));
      assertEquals(
          409,
          assertThrows(
                  ReservaException.class, () -> service.crear(r("SALA-B", fecha, 10, 11, nombre)))
              .estado());
      assertEquals("CANCELADA", service.cancelar(a.id(), "Cambio de agenda").estado());
      assertEquals("Cambio de agenda", service.cancelar(a.id(), "Otro motivo").motivoCancelacion());
      assertTrue(service.disponible("SALA-A", fecha, 8, 9));
      assertEquals(
          409,
          assertThrows(
                  ReservaException.class,
                  () -> service.actualizar(a.id(), r("SALA-A", fecha, 8, 9, nombre)))
              .estado());
      service.crear(r("SALA-B", fecha, 10, 11, nombre));
      assertEquals(
          400, assertThrows(ReservaException.class, () -> service.pagina(0, null)).estado());
      assertEquals(
          400, assertThrows(ReservaException.class, () -> service.pagina(5, "../x")).estado());
      var p = service.pagina(1, null);
      assertEquals(1, p.elementos().size());
      assertNotNull(p.siguienteCursor());
      var segunda = service.pagina(1, p.siguienteCursor());
      assertNotEquals(p.elementos().getFirst().id(), segunda.elementos().getFirst().id());
    } finally {
      service.filtrar(null, fecha, null, null, nombre).forEach(x -> service.eliminar(x.id()));
    }
  }
}
