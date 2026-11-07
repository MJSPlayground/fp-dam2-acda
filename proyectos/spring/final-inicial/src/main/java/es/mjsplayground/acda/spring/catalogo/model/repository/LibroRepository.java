package es.mjsplayground.acda.spring.catalogo.model.repository;

import es.mjsplayground.acda.spring.catalogo.model.entity.Libro;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface LibroRepository extends JpaRepository<Libro, Long> {
  List<Libro> findByTituloContainingIgnoreCaseOrderByTituloAscIdAsc(String titulo);

  List<Libro> findByEditorialIdAndTituloContainingIgnoreCaseOrderByTituloAscIdAsc(
      Long editorialId, String titulo);

  @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @Query("select l from Libro l where l.id=:id")
  Optional<Libro> bloquear(@org.springframework.data.repository.query.Param("id") Long id);

  boolean existsByEditorialId(Long editorialId);
}
