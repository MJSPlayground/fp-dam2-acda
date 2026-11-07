package es.mjsplayground.acda.firestore.reservas;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.mjsplayground.acda.firestore.reservas.api.ReservaRequest;
import es.mjsplayground.acda.firestore.reservas.model.entity.*;
import es.mjsplayground.acda.firestore.reservas.model.repository.*;
import es.mjsplayground.acda.firestore.reservas.model.service.*;
import java.util.*;
import org.junit.jupiter.api.*;

class ReservaServiceTest {
  FirestoreReservaRepository repo = mock(FirestoreReservaRepository.class);
  ReservaService service = new ReservaService(repo);

  ReservaRequest solicitud(String espacio, String fecha, int inicio, int fin, int asistentes) {
    return new ReservaRequest(
        espacio,
        fecha,
        inicio,
        fin,
        "Equipo docente",
        asistentes,
        List.of("Proyector"),
        Map.of("notas", "Reunión"));
  }

  @Test
  void rechazaFechasHorariosCapacidad() {
    for (ReservaRequest r :
        List.of(
            solicitud("SALA-A", "2030-02-30", 9, 10, 2),
            solicitud("SALA-A", "2030-02-20", 10, 10, 2),
            solicitud("SALA-A", "2030-02-20", 9, 10, 13),
            solicitud("OTRO", "2030-02-20", 9, 10, 2)))
      assertEquals(400, assertThrows(ReservaException.class, () -> service.crear(r)).estado());
    verifyNoInteractions(repo);
  }

  @Test
  void ausenciaEs404YValidacionId400() {
    when(repo.obtener("ausente")).thenReturn(Optional.empty());
    assertEquals(
        404, assertThrows(ReservaException.class, () -> service.obtener("ausente")).estado());
    assertEquals(400, assertThrows(ReservaException.class, () -> service.obtener("../x")).estado());
  }

  @Test
  void filtrosInvalidosNoConsultan() {
    assertThrows(ReservaException.class, () -> service.listar(null, null, 0));
    assertThrows(ReservaException.class, () -> service.listar(null, "no-fecha", 1));
    verifyNoInteractions(repo);
  }

  @Test
  void noTragaFalloTecnico() {
    RuntimeException causa = new IllegalStateException("Disco");
    when(repo.obtener("id")).thenThrow(new PersistenciaException("Fallo", causa));
    assertSame(
        causa, assertThrows(PersistenciaException.class, () -> service.obtener("id")).getCause());
  }
}
