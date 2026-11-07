package es.mjsplayground.acda.mongodb.liga.model.service;

import es.mjsplayground.acda.mongodb.liga.api.*;
import es.mjsplayground.acda.mongodb.liga.model.entity.*;
import es.mjsplayground.acda.mongodb.liga.model.repository.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class LigaService {
  protected final EquipoRepository equipos;
  protected final SedeRepository sedes;
  protected final EstadisticasRepository estadisticas;
  protected static final Set<String> CATEGORIAS = Set.of("JUNIOR", "SENIOR"),
      POSICIONES = Set.of("PORTERO", "DEFENSA", "MEDIO", "DELANTERO");

  public LigaService(
      EquipoRepository equipos, SedeRepository sedes, EstadisticasRepository estadisticas) {
    this.equipos = equipos;
    this.sedes = sedes;
    this.estadisticas = estadisticas;
  }

  public List<Sede> sedes() {
    return sedes.findAll(Sort.by("nombre"));
  }

  public void id(String id) {
    if (id == null || !id.matches("[a-fA-F0-9]{24}"))
      throw new LigaException(400, "Identificador de equipo no válido");
  }

  public Equipo obtener(String id) {
    id(id);
    return equipos.findById(id).orElseThrow(() -> new LigaException(404, "Equipo no encontrado"));
  }

  public record Pagina(List<Equipo> elementos, long totalElementos, int totalPaginas, int pagina) {}

  public Pagina listar(String categoria, String posicion, int pagina, int tamanio) {
    if (pagina < 0 || pagina > 10000 || tamanio < 1 || tamanio > 20)
      throw new LigaException(400, "Página desde0 y tamaño entre1 y20");
    if (categoria != null && !CATEGORIAS.contains(categoria))
      throw new LigaException(400, "Categoría no válida");
    if (posicion != null && !POSICIONES.contains(posicion))
      throw new LigaException(400, "Posición no válida");
    Pageable p = PageRequest.of(pagina, tamanio, Sort.by("nombre", "id"));
    Page<Equipo> datos;
    if (categoria != null && posicion != null)
      datos = equipos.porCategoriaYPosicion(categoria, posicion, p);
    else if (categoria != null) datos = equipos.findByCategoriaOrderByNombreAsc(categoria, p);
    else if (posicion != null) datos = equipos.porPosicion(posicion, p);
    else datos = equipos.findAll(p);
    return new Pagina(datos.getContent(), datos.getTotalElements(), datos.getTotalPages(), pagina);
  }

  protected void validar(EquipoRequest r) {
    if (r == null
        || r.nombre() == null
        || r.nombre().isBlank()
        || r.nombre().strip().length() > 60
        || r.categoria() == null
        || !CATEGORIAS.contains(r.categoria())
        || r.sedeId() == null
        || !sedes.existsById(r.sedeId()))
      throw new LigaException(
          400, "Nombre1–60, categoría JUNIOR/SENIOR y sede existente obligatorios");
  }

  public Equipo crear(EquipoRequest r) {
    validar(r);
    return equipos.insert(
        new Equipo(
            null, r.nombre().strip(), r.categoria(), r.sedeId(), List.of(), List.of(), null));
  }

  public Equipo actualizar(String id, EquipoRequest r) {
    validar(r);
    Equipo e = obtener(id);
    return equipos.save(
        new Equipo(
            e.id(),
            r.nombre().strip(),
            r.categoria(),
            r.sedeId(),
            e.jugadores(),
            e.jornadas(),
            e.version()));
  }

  public void eliminar(String id) {
    Equipo e = obtener(id);
    equipos.delete(e);
  }

  protected void validarJugador(JugadorRequest r) {
    if (r == null
        || r.nombre() == null
        || r.nombre().isBlank()
        || r.nombre().strip().length() > 60
        || r.dorsal() < 1
        || r.dorsal() > 99
        || r.posicion() == null
        || !POSICIONES.contains(r.posicion()))
      throw new LigaException(400, "Jugador: nombre1–60, dorsal1–99 y posición válida");
  }

  protected Jugador jugador(Equipo e, String id) {
    return e.jugadores().stream()
        .filter(j -> j.id().equals(id))
        .findFirst()
        .orElseThrow(() -> new LigaException(404, "Jugador no encontrado en este equipo"));
  }

  public Equipo agregarJugador(String id, JugadorRequest r) {
    validarJugador(r);
    Equipo e = obtener(id);
    if (e.jugadores().size() >= 20)
      throw new LigaException(409, "La plantilla admite como máximo20 jugadores");
    if (e.jugadores().stream().anyMatch(j -> j.dorsal() == r.dorsal()))
      throw new LigaException(409, "El dorsal ya está ocupado");
    List<Jugador> lista = new ArrayList<>(e.jugadores());
    lista.add(
        new Jugador(
            UUID.randomUUID().toString(),
            r.nombre().strip(),
            r.dorsal(),
            r.posicion(),
            true,
            new Estadisticas(0, 0, 0)));
    return equipos.save(conJugadores(e, lista));
  }

  public Equipo editarJugador(String id, String jugadorId, JugadorRequest r) {
    validarJugador(r);
    Equipo e = obtener(id);
    Jugador anterior = jugador(e, jugadorId);
    if (e.jugadores().stream().anyMatch(j -> !j.id().equals(jugadorId) && j.dorsal() == r.dorsal()))
      throw new LigaException(409, "El dorsal ya está ocupado");
    List<Jugador> lista =
        e.jugadores().stream()
            .map(
                j ->
                    j.id().equals(jugadorId)
                        ? new Jugador(
                            j.id(),
                            r.nombre().strip(),
                            r.dorsal(),
                            r.posicion(),
                            anterior.disponible(),
                            anterior.estadisticas())
                        : j)
            .toList();
    return equipos.save(conJugadores(e, lista));
  }

  public Equipo eliminarJugador(String id, String jugadorId) {
    Equipo e = obtener(id);
    if (e.jornadas().stream()
        .flatMap(j -> j.aportaciones().stream())
        .anyMatch(a -> a.jugadorId().equals(jugadorId)))
      throw new LigaException(409, "El jugador tiene historial de competición y no se elimina");
    jugador(e, jugadorId);
    return equipos.save(
        conJugadores(e, e.jugadores().stream().filter(j -> !j.id().equals(jugadorId)).toList()));
  }

  protected Equipo conJugadores(Equipo e, List<Jugador> lista) {
    return new Equipo(
        e.id(),
        e.nombre(),
        e.categoria(),
        e.sedeId(),
        List.copyOf(lista),
        e.jornadas(),
        e.version());
  }

  public List<Jugador> filtrarJugadores(String id, int minGoles, Boolean disponibles) {
    if (minGoles < 0 || minGoles > 100000)
      throw new LigaException(400, "Mínimo de goles entre0 y100000");
    return obtener(id).jugadores().stream()
        .filter(j -> j.estadisticas().goles() >= minGoles)
        .filter(j -> disponibles == null || j.disponible() == disponibles)
        .sorted(Comparator.comparing(Jugador::nombre).thenComparing(Jugador::id))
        .toList();
  }

  public Equipo disponibilidad(String id, String jugadorId, Boolean disponible) {
    if (disponible == null) throw new LigaException(400, "Indica disponible true/false");
    Equipo e = obtener(id);
    jugador(e, jugadorId);
    return equipos.save(
        conJugadores(
            e,
            e.jugadores().stream()
                .map(
                    j ->
                        j.id().equals(jugadorId)
                            ? new Jugador(
                                j.id(),
                                j.nombre(),
                                j.dorsal(),
                                j.posicion(),
                                disponible,
                                j.estadisticas())
                            : j)
                .toList()));
  }

  public Equipo registrarJornada(String id, JornadaRequest r) {
    Equipo e = obtener(id);
    if (r == null
        || r.id() == null
        || !r.id().matches("[A-Za-z0-9_-]{1,60}")
        || r.rival() == null
        || r.rival().isBlank()
        || r.rival().strip().length() > 60
        || r.golesFavor() < 0
        || r.golesFavor() > 100
        || r.golesContra() < 0
        || r.golesContra() > 100
        || r.aportaciones() == null
        || r.aportaciones().size() > 20)
      throw new LigaException(400, "Datos de jornada no válidos");
    try {
      if (!java.time.LocalDate.parse(r.fecha()).toString().equals(r.fecha()))
        throw new IllegalArgumentException();
    } catch (Exception ex) {
      throw new LigaException(400, "Fecha de jornada ISO no válida");
    }
    if (e.jornadas().size() >= 30) throw new LigaException(409, "Máximo30 jornadas por equipo");
    if (e.jornadas().stream().anyMatch(j -> j.id().equals(r.id())))
      throw new LigaException(
          409, "La jornada ya está registrada; no se suman estadísticas dos veces");
    Set<String> vistos = new HashSet<>();
    int goles = 0, asistencias = 0;
    for (Aportacion a : r.aportaciones()) {
      if (a == null
          || a.jugadorId() == null
          || !vistos.add(a.jugadorId())
          || a.goles() < 0
          || a.goles() > 100
          || a.asistencias() < 0
          || a.asistencias() > 100)
        throw new LigaException(400, "Aportaciones repetidas o no válidas");
      Jugador j = jugador(e, a.jugadorId());
      if (!j.disponible()) throw new LigaException(409, "Un participante no está disponible");
      if (j.estadisticas().partidos() >= 10000
          || j.estadisticas().goles() > 100000 - a.goles()
          || j.estadisticas().asistencias() > 100000 - a.asistencias())
        throw new LigaException(409, "Límite estadístico alcanzado");
      goles += a.goles();
      asistencias += a.asistencias();
    }
    if (goles != r.golesFavor() || asistencias > r.golesFavor())
      throw new LigaException(
          400,
          "Los goles de participantes deben coincidir con el marcador y las asistencias no"
              + " superarlos");
    List<Jugador> jugadores =
        e.jugadores().stream()
            .map(
                j -> {
                  Aportacion a =
                      r.aportaciones().stream()
                          .filter(x -> x.jugadorId().equals(j.id()))
                          .findFirst()
                          .orElse(null);
                  return a == null
                      ? j
                      : new Jugador(
                          j.id(),
                          j.nombre(),
                          j.dorsal(),
                          j.posicion(),
                          j.disponible(),
                          new Estadisticas(
                              j.estadisticas().partidos() + 1,
                              j.estadisticas().goles() + a.goles(),
                              j.estadisticas().asistencias() + a.asistencias()));
                })
            .toList();
    List<Jornada> jornadas = new ArrayList<>(e.jornadas());
    jornadas.add(
        new Jornada(
            r.id(),
            r.fecha(),
            r.rival().strip(),
            r.golesFavor(),
            r.golesContra(),
            List.copyOf(r.aportaciones())));
    return equipos.save(
        new Equipo(
            e.id(),
            e.nombre(),
            e.categoria(),
            e.sedeId(),
            jugadores,
            List.copyOf(jornadas),
            e.version()));
  }

  public record Clasificacion(
      String equipoId,
      String nombre,
      int jugados,
      int puntos,
      int golesFavor,
      int golesContra,
      int diferencia) {}

  public List<Clasificacion> clasificacion() {
    return equipos.findAll().stream()
        .map(
            e -> {
              int puntos = 0, favor = 0, contra = 0;
              for (Jornada j : e.jornadas()) {
                puntos +=
                    j.golesFavor() > j.golesContra()
                        ? 3
                        : j.golesFavor() == j.golesContra() ? 1 : 0;
                favor += j.golesFavor();
                contra += j.golesContra();
              }
              return new Clasificacion(
                  e.id(), e.nombre(), e.jornadas().size(), puntos, favor, contra, favor - contra);
            })
        .sorted(
            Comparator.comparingInt(Clasificacion::puntos)
                .reversed()
                .thenComparing(Comparator.comparingInt(Clasificacion::diferencia).reversed())
                .thenComparing(Comparator.comparingInt(Clasificacion::golesFavor).reversed())
                .thenComparing(Clasificacion::nombre)
                .thenComparing(Clasificacion::equipoId))
        .toList();
  }

  public record Racha(List<String> resultados) {}

  public Racha racha(String id) {
    List<Jornada> ordenadas =
        obtener(id).jornadas().stream()
            .sorted(Comparator.comparing(Jornada::fecha).thenComparing(Jornada::id))
            .toList();
    return new Racha(
        ordenadas.subList(Math.max(0, ordenadas.size() - 5), ordenadas.size()).stream()
            .map(
                j ->
                    j.golesFavor() > j.golesContra()
                        ? "V"
                        : j.golesFavor() == j.golesContra() ? "E" : "D")
            .toList());
  }

  public Equipo sumarEstadisticas(String id, String jugadorId, EstadisticasRequest r) {
    Equipo e = obtener(id);
    jugador(e, jugadorId);
    if (r == null || r.goles() < 0 || r.goles() > 20 || r.asistencias() < 0 || r.asistencias() > 20)
      throw new LigaException(400, "Goles y asistencias entre0 y20");
    Equipo actualizado = estadisticas.sumar(id, jugadorId, r.goles(), r.asistencias());
    if (actualizado == null)
      throw new LigaException(
          409, "Jugador eliminado durante la operación o límite estadístico alcanzado");
    return actualizado;
  }
}
