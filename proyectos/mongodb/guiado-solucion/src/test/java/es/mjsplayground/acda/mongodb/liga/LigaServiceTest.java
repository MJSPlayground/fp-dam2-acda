package es.mjsplayground.acda.mongodb.liga;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.mjsplayground.acda.mongodb.liga.api.*;
import es.mjsplayground.acda.mongodb.liga.model.repository.*;
import es.mjsplayground.acda.mongodb.liga.model.service.*;
import java.util.*;
import org.junit.jupiter.api.*;

class LigaServiceTest {
  EquipoRepository equipos = mock(EquipoRepository.class);
  SedeRepository sedes = mock(SedeRepository.class);
  EstadisticasRepository stats = mock(EstadisticasRepository.class);
  LigaService service = new LigaService(equipos, sedes, stats);

  @BeforeEach
  void configurar() {
    when(sedes.existsById("CENTRO")).thenReturn(true);
  }

  @Test
  void validaEquipoIncluyendoNulos() {
    for (EquipoRequest r :
        List.of(
            new EquipoRequest(" ", "JUNIOR", "CENTRO"),
            new EquipoRequest("A", null, "CENTRO"),
            new EquipoRequest("A", "JUNIOR", null),
            new EquipoRequest("A", "OTRA", "CENTRO")))
      assertEquals(400, assertThrows(LigaException.class, () -> service.crear(r)).estado());
    verifyNoInteractions(equipos);
  }

  @Test
  void validaJugadorIncluyendoNulos() {
    for (JugadorRequest r :
        List.of(
            new JugadorRequest("A", 1, null),
            new JugadorRequest("A", 0, "MEDIO"),
            new JugadorRequest(" ", 1, "MEDIO")))
      assertEquals(
          400, assertThrows(LigaException.class, () -> service.agregarJugador("abc", r)).estado());
    verifyNoInteractions(equipos);
  }

  @Test
  void idYFiltros() {
    assertEquals(400, assertThrows(LigaException.class, () -> service.obtener("no-id")).estado());
    assertEquals(
        400, assertThrows(LigaException.class, () -> service.listar(null, null, -1, 10)).estado());
    assertEquals(
        400, assertThrows(LigaException.class, () -> service.listar(null, null, 0, 21)).estado());
    verifyNoInteractions(equipos);
  }

  @Test
  void ausencia404() {
    when(equipos.findById("000000000000000000000000")).thenReturn(Optional.empty());
    assertEquals(
        404,
        assertThrows(LigaException.class, () -> service.obtener("000000000000000000000000"))
            .estado());
  }
}
