package es.mjsplayground.acda.spring.catalogo.model.repository;

import es.mjsplayground.acda.spring.catalogo.model.entity.Editorial;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EditorialRepository extends JpaRepository<Editorial, Long> {
  List<Editorial> findAllByOrderByNombreAscIdAsc();
}
