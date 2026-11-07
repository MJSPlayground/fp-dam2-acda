package es.mjsplayground.acda.spring.catalogo.model.repository;

import es.mjsplayground.acda.spring.catalogo.model.entity.Pedido;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
  List<Pedido> findAllByOrderByFechaDescIdDesc();
}
