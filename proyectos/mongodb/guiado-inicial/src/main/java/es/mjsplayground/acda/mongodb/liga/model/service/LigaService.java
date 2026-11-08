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
    // TODO 3a: crear equipo con sede válida y plantilla vacía; guardar el agregado con su versión.
    throw new UnsupportedOperationException(
        "Etapa pendiente: 3: implementar caso de uso con validaciones y guardado optimista");
  }

  public Equipo actualizar(String id, EquipoRequest r) {
    // TODO 3a: editar solo datos del equipo y conservar plantilla/versión; guardar el agregado con
    // su versión.
    throw new UnsupportedOperationException(
        "Etapa pendiente: 3: implementar caso de uso con validaciones y guardado optimista");
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
    // TODO 3b: añadir jugador sin repetir dorsal ni superar la plantilla; guardar el agregado con
    // su versión.
    throw new UnsupportedOperationException(
        "Etapa pendiente: 3: implementar caso de uso con validaciones y guardado optimista");
  }

  public Equipo editarJugador(String id, String jugadorId, JugadorRequest r) {
    // TODO 3b: editar datos personales conservando identidad y estadísticas; guardar el agregado
    // con su versión.
    throw new UnsupportedOperationException(
        "Etapa pendiente: 3: implementar caso de uso con validaciones y guardado optimista");
  }

  public Equipo eliminarJugador(String id, String jugadorId) {
    // TODO 3b: retirar únicamente el jugador indicado; guardar el agregado con su versión.
    throw new UnsupportedOperationException(
        "Etapa pendiente: 3: implementar caso de uso con validaciones y guardado optimista");
  }

  protected Equipo conJugadores(Equipo e, List<Jugador> lista) {
    return new Equipo(
        e.id(), e.nombre(), e.categoria(), e.sedeId(), List.copyOf(lista), e.version());
  }

  public Equipo sumarEstadisticas(String id, String jugadorId, EstadisticasRequest r) {
    // TODO 4: comprobar equipo/jugador e incrementos (0..20), delegar en el helper
    // EstadisticasRepository.sumar YA IMPLEMENTADO y convertir resultado nulo en conflicto.
    throw new UnsupportedOperationException(
        "Etapa pendiente: 4: coordinar estadísticas desde el servicio");
  }
}
