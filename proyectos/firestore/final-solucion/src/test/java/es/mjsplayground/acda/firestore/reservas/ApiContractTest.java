package es.mjsplayground.acda.firestore.reservas;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import es.mjsplayground.acda.firestore.reservas.api.*;
import es.mjsplayground.acda.firestore.reservas.model.repository.*;
import es.mjsplayground.acda.firestore.reservas.model.service.*;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ApiContractTest {
  ReservaService service = mock(ReservaService.class);
  MockMvc api =
      MockMvcBuilders.standaloneSetup(new ReservaController(service))
          .setControllerAdvice(new ApiErrors())
          .build();

  @Test
  void cuerpoInvalido400SinInvocarServicio() throws Exception {
    api.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.estado").value(400));
    verifyNoInteractions(service);
  }

  @Test
  void jsonRoto400() throws Exception {
    api.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON).content("{"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void noExiste404Estructurado() throws Exception {
    when(service.obtener("ausente")).thenThrow(new ReservaException(404, "Reserva no encontrada"));
    api.perform(get("/api/reservas/ausente"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.ruta").value("/api/reservas/ausente"))
        .andExpect(jsonPath("$.instante").exists());
  }

  @Test
  void backendFalla500SinFiltrarCausa() throws Exception {
    when(service.obtener("id"))
        .thenThrow(
            new PersistenciaException("detalle privado", new IllegalStateException("secreto")));
    api.perform(get("/api/reservas/id"))
        .andExpect(status().isInternalServerError())
        .andExpect(
            jsonPath("$.mensaje")
                .value("No se pudo completar la operación. Consulta el registro del servidor."));
  }

  @Test
  void metodoInadecuado405() throws Exception {
    api.perform(patch("/api/espacios"))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(header().exists("Allow"));
  }
}
