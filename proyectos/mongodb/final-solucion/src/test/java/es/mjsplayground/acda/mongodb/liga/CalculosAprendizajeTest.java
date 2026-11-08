package es.mjsplayground.acda.mongodb.liga;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import es.mjsplayground.acda.mongodb.liga.model.entity.*;
import es.mjsplayground.acda.mongodb.liga.model.repository.EquipoRepository;
import es.mjsplayground.acda.mongodb.liga.model.service.LigaService;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/** Casos preparados de cálculo; no hay algoritmo de clasificación/racha en la fixture. */
@EnabledIfSystemProperty(named = "aprendizaje.nivel", matches = "reto|todos")
class CalculosAprendizajeTest {
  String id(int n) {
    return String.format("%024x", n);
  }

  Equipo equipo(int n, String nombre, int favor, int contra) {
    return new Equipo(
        id(n),
        nombre,
        "SENIOR",
        "CENTRO",
        List.of(),
        n == 1
            ? List.of()
            : List.of(new Jornada("j", "2044-01-01", "Rival", favor, contra, List.of())),
        0L);
  }

  @Test
  void todosLosDesempatesIncluidaLaIdentidad() {
    var repo = mock(EquipoRepository.class);
    var servicio = new LigaService(repo, null, null);
    // Los nombres repetidos son entradas de una lectura simulada: no quitamos el índice único.
    when(repo.findAll())
        .thenReturn(
            List.of(
                equipo(1, "Z", 0, 0),
                equipo(6, "A", 3, 1),
                equipo(3, "Z", 2, 0),
                equipo(4, "Z", 3, 1),
                equipo(2, "Z", 1, 0),
                equipo(5, "A", 3, 1)));
    assertEquals(
        List.of(id(5), id(6), id(4), id(3), id(2), id(1)),
        servicio.clasificacion().stream().map(LigaService.Clasificacion::equipoId).toList());
  }

  @Test
  void ultimosCincoPorFechaEIdNoPorOrdenDeInsercion() {
    var repo = mock(EquipoRepository.class);
    var servicio = new LigaService(repo, null, null);
    List<Jornada> jornadas =
        List.of(
            new Jornada("f", "2044-01-05", "R", 1, 0, List.of()),
            new Jornada("a", "2044-01-01", "R", 0, 1, List.of()),
            new Jornada("d", "2044-01-03", "R", 2, 0, List.of()),
            new Jornada("c", "2044-01-02", "R", 0, 0, List.of()),
            new Jornada("b", "2044-01-01", "R", 3, 0, List.of()),
            new Jornada("e", "2044-01-04", "R", 0, 2, List.of()));
    when(repo.findById(id(1)))
        .thenReturn(
            Optional.of(new Equipo(id(1), "Equipo", "SENIOR", "CENTRO", List.of(), jornadas, 0L)));
    assertEquals(List.of("V", "E", "V", "D", "V"), servicio.racha(id(1)).resultados());
  }
}
