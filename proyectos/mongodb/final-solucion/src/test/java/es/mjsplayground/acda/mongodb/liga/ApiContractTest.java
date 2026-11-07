package es.mjsplayground.acda.mongodb.liga;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import es.mjsplayground.acda.mongodb.liga.api.*;
import es.mjsplayground.acda.mongodb.liga.model.service.*;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ApiContractTest {
  LigaService service = mock(LigaService.class);
  MockMvc api =
      MockMvcBuilders.standaloneSetup(new LigaController(service))
          .setControllerAdvice(new ApiErrors())
          .build();

  @Test
  void entrada400() throws Exception {
    api.perform(post("/api/equipos").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.estado").value(400));
    verifyNoInteractions(service);
  }

  @Test
  void noExiste404() throws Exception {
    when(service.obtener("000000000000000000000000"))
        .thenThrow(new LigaException(404, "Equipo no encontrado"));
    api.perform(get("/api/equipos/000000000000000000000000"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.ruta").exists());
  }

  @Test
  void fallo500SinDetalle() throws Exception {
    when(service.obtener("000000000000000000000000"))
        .thenThrow(new IllegalStateException("secreto"));
    api.perform(get("/api/equipos/000000000000000000000000"))
        .andExpect(status().isInternalServerError())
        .andExpect(
            jsonPath("$.mensaje")
                .value("No se pudo completar la operación. Consulta el registro del servidor."));
  }

  @Test
  void protocolo405ConAllow() throws Exception {
    api.perform(patch("/api/sedes"))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(header().exists("Allow"));
  }
}
