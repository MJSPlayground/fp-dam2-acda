package es.mjsplayground.acda.spring.catalogo.model.repository;

import es.mjsplayground.acda.spring.catalogo.model.entity.Libro;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface LibroRepository extends JpaRepository<Libro, Long> {
  // TODO2: sustituye el apoyo default por la firma de consulta derivada que implementa Spring Data.
  default List<Libro> findByTituloContainingIgnoreCaseOrderByTituloAscIdAsc(String titulo) {
    throw new UnsupportedOperationException("Etapa pendiente: consulta derivada por título");
  }

  // TODO2: consulta derivada combinada de editorial y título, sin implementar SQL manual.
  default List<Libro> findByEditorialIdAndTituloContainingIgnoreCaseOrderByTituloAscIdAsc(
      Long editorialId, String titulo) {
    throw new UnsupportedOperationException("Etapa pendiente: consulta combinada");
  }

  boolean existsByEditorialId(Long editorialId);
}
