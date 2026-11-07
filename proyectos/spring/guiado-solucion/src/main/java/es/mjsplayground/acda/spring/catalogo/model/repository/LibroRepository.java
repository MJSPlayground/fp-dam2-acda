package es.mjsplayground.acda.spring.catalogo.model.repository;

import es.mjsplayground.acda.spring.catalogo.model.entity.Libro;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface LibroRepository extends JpaRepository<Libro, Long> {
  List<Libro> findByTituloContainingIgnoreCaseOrderByTituloAscIdAsc(String titulo);

  List<Libro> findByEditorialIdAndTituloContainingIgnoreCaseOrderByTituloAscIdAsc(
      Long editorialId, String titulo);

  boolean existsByEditorialId(Long editorialId);
}
