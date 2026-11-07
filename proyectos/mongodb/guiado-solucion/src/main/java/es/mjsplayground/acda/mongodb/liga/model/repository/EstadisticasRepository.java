package es.mjsplayground.acda.mongodb.liga.model.repository;

import es.mjsplayground.acda.mongodb.liga.model.entity.*;
import org.springframework.data.mongodb.core.*;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Repository;

@Repository
public class EstadisticasRepository {
  private final MongoTemplate mongo;

  public EstadisticasRepository(MongoTemplate mongo) {
    this.mongo = mongo;
  }

  public Equipo sumar(String equipoId, String jugadorId, int goles, int asistencias) {
    Criteria jugador =
        Criteria.where("id")
            .is(jugadorId)
            .and("estadisticas.partidos")
            .lt(10000)
            .and("estadisticas.goles")
            .lte(100000 - goles)
            .and("estadisticas.asistencias")
            .lte(100000 - asistencias);
    Query q = Query.query(Criteria.where("_id").is(equipoId).and("jugadores").elemMatch(jugador));
    Update u =
        new Update()
            .inc("jugadores.$.estadisticas.partidos", 1)
            .inc("jugadores.$.estadisticas.goles", goles)
            .inc("jugadores.$.estadisticas.asistencias", asistencias)
            .inc("version", 1);
    return mongo.findAndModify(q, u, FindAndModifyOptions.options().returnNew(true), Equipo.class);
  }
}
