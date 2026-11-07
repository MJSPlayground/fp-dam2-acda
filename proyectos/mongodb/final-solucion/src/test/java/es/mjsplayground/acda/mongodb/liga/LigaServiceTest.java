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

  es.mjsplayground.acda.mongodb.liga.model.entity.Jornada jornada(
      String id, String fecha, int favor, int contra) {
    return new es.mjsplayground.acda.mongodb.liga.model.entity.Jornada(
        id, fecha, "Rival", favor, contra, List.of());
  }

  es.mjsplayground.acda.mongodb.liga.model.entity.Equipo equipo(
      String id,
      String nombre,
      es.mjsplayground.acda.mongodb.liga.model.entity.Jornada... jornadas) {
    return new es.mjsplayground.acda.mongodb.liga.model.entity.Equipo(
        id, nombre, "JUNIOR", "CENTRO", List.of(), List.of(jornadas), 0L);
  }

  @Test
  void clasificacionDesempataPorPuntosDiferenciaFavorYNombre() {
    var a =
        equipo(
            "000000000000000000000001",
            "A",
            jornada("1", "2030-01-01", 2, 0),
            jornada("2", "2030-01-02", 0, 1),
            jornada("3", "2030-01-03", 1, 1));
    var b =
        equipo(
            "000000000000000000000002",
            "B",
            jornada("1", "2030-01-01", 3, 1),
            jornada("2", "2030-01-02", 0, 0),
            jornada("3", "2030-01-03", 0, 1));
    var c =
        equipo(
            "000000000000000000000003",
            "C",
            jornada("1", "2030-01-01", 1, 0),
            jornada("2", "2030-01-02", 1, 0));
    var d =
        equipo(
            "000000000000000000000004",
            "D",
            jornada("1", "2030-01-01", 3, 0),
            jornada("2", "2030-01-02", 1, 1),
            jornada("3", "2030-01-03", 0, 2));
    var e =
        equipo(
            "000000000000000000000005",
            "E",
            jornada("1", "2030-01-01", 2, 0),
            jornada("2", "2030-01-02", 0, 0));
    when(equipos.findAll()).thenReturn(List.of(b, a, d, e, c));
    assertEquals(
        List.of("C", "E", "D", "A", "B"),
        service.clasificacion().stream().map(LigaService.Clasificacion::nombre).toList());
  }

  @Test
  void rachaOrdenaFechasYLimitaUltimosCinco() {
    String id = "000000000000000000000001";
    var e =
        equipo(
            id,
            "Racha",
            jornada("7", "2030-01-07", 0, 0),
            jornada("1", "2030-01-01", 1, 0),
            jornada("6", "2030-01-06", 0, 1),
            jornada("2", "2030-01-02", 0, 0),
            jornada("5", "2030-01-05", 2, 0),
            jornada("3", "2030-01-03", 0, 2),
            jornada("4", "2030-01-04", 1, 0));
    when(equipos.findById(id)).thenReturn(Optional.of(e));
    assertEquals(List.of("D", "V", "V", "D", "E"), service.racha(id).resultados());
  }
}
