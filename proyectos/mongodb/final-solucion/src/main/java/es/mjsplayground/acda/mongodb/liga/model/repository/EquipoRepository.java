package es.mjsplayground.acda.mongodb.liga.model.repository;

import es.mjsplayground.acda.mongodb.liga.model.entity.Equipo;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.repository.*;

public interface EquipoRepository extends MongoRepository<Equipo, String> {
  Page<Equipo> findByCategoriaOrderByNombreAsc(String categoria, Pageable pageable);

  @Query("{'jugadores.posicion':?0}")
  Page<Equipo> porPosicion(String posicion, Pageable pageable);

  @Query("{'categoria':?0,'jugadores.posicion':?1}")
  Page<Equipo> porCategoriaYPosicion(String categoria, String posicion, Pageable pageable);
}
