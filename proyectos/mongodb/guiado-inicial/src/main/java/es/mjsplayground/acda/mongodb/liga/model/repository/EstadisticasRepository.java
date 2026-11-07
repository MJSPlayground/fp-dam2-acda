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
    // TODO 4: incremento atómico con predicados y cambio de versión
    throw new UnsupportedOperationException(
        "Etapa pendiente: 4: incremento atómico con predicados y cambio de versión");
  }
}
