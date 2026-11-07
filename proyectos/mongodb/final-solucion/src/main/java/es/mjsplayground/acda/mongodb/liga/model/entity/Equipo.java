package es.mjsplayground.acda.mongodb.liga.model.entity;

import java.util.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("equipos")
@CompoundIndexes({
  @CompoundIndex(name = "categoria_nombre", def = "{'categoria':1,'nombre':1}"),
  @CompoundIndex(name = "posicion_nombre", def = "{'jugadores.posicion':1,'nombre':1}")
})
public record Equipo(
    @Id String id,
    @Indexed(unique = true) String nombre,
    String categoria,
    String sedeId,
    List<Jugador> jugadores,
    List<Jornada> jornadas,
    @Version Long version) {
  public Equipo {
    jugadores = jugadores == null ? List.of() : List.copyOf(jugadores);
    jornadas = jornadas == null ? List.of() : List.copyOf(jornadas);
  }
}
