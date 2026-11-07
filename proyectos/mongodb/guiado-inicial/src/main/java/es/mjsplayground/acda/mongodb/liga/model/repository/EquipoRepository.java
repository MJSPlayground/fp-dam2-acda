package es.mjsplayground.acda.mongodb.liga.model.repository;

import es.mjsplayground.acda.mongodb.liga.model.entity.Equipo;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EquipoRepository extends MongoRepository<Equipo, String> {
  // TODO2: transforma estos apoyos default en consultas derivadas o @Query de Spring Data.
  // El contrato lo usa la interfaz preparada; no cambies las firmas.
  default Page<Equipo> findByCategoriaOrderByNombreAsc(String categoria, Pageable pageable) {
    throw new UnsupportedOperationException("TODO2: consulta derivada por categoría");
  }

  default Page<Equipo> porPosicion(String posicion, Pageable pageable) {
    throw new UnsupportedOperationException("TODO2: consulta @Query por posición embebida");
  }

  default Page<Equipo> porCategoriaYPosicion(String categoria, String posicion, Pageable pageable) {
    throw new UnsupportedOperationException("TODO2: consulta combinada");
  }
}
