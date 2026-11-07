package es.mjsplayground.acda.firestore.reservas;

import static org.junit.jupiter.api.Assertions.*;

import es.mjsplayground.acda.firestore.reservas.api.ReservaRequest;
import es.mjsplayground.acda.firestore.reservas.model.entity.Reserva;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnabledIfSystemProperty(named = "qa.emulador", matches = "true")
class EmulatorIntegrationTest {
  @Autowired TestRestTemplate http;

  @Test
  void crudRealYApi() {
    String fecha = "2040-01-" + String.format("%02d", 1 + new Random().nextInt(28));
    var body =
        new ReservaRequest(
            "SALA-A",
            fecha,
            8,
            9,
            "qa-" + UUID.randomUUID(),
            2,
            List.of("Proyector"),
            Map.of("notas", "Prueba real"));
    var alta = http.postForEntity("/api/reservas", body, Reserva.class);
    assertEquals(HttpStatus.CREATED, alta.getStatusCode());
    assertNotNull(alta.getHeaders().getLocation());
    String id = alta.getBody().id();
    try {
      Reserva recuperada = http.getForObject("/api/reservas/" + id, Reserva.class);
      assertEquals(body.equipo(), recuperada.equipo());
      assertEquals(body.detalles(), recuperada.detalles());
      var nuevo =
          new ReservaRequest("SALA-A", fecha, 8, 9, body.responsable(), 3, List.of(), Map.of());
      assertEquals(
          200,
          http.exchange(
                  "/api/reservas/" + id, HttpMethod.PUT, new HttpEntity<>(nuevo), Reserva.class)
              .getStatusCode()
              .value());
      var consulta =
          http.getForEntity(
              "/api/reservas?espacio=SALA-A&fecha=" + fecha + "&minAsistentes=3", Reserva[].class);
      assertEquals(200, consulta.getStatusCode().value());
      assertTrue(Arrays.stream(consulta.getBody()).anyMatch(r -> r.id().equals(id)));
      assertEquals(
          404, http.getForEntity("/api/reservas/ausente", String.class).getStatusCode().value());
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      String fraccion =
          "{\"espacio\":\"SALA-A\",\"fecha\":\"2040-01-01\",\"inicio\":9.5,\"fin\":10,\"responsable\":\"x\",\"asistentes\":2,\"equipo\":[],\"detalles\":{}}";
      assertEquals(
          400,
          http.postForEntity("/api/reservas", new HttpEntity<>(fraccion, headers), String.class)
              .getStatusCode()
              .value());
      assertEquals(
          400, http.postForEntity("/api/reservas", Map.of(), String.class).getStatusCode().value());
    } finally {
      assertEquals(
          204,
          http.exchange("/api/reservas/" + id, HttpMethod.DELETE, HttpEntity.EMPTY, Void.class)
              .getStatusCode()
              .value());
    }
  }
}
