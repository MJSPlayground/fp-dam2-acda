package es.mjsplayground.acda.spring.catalogo.model.service;

import es.mjsplayground.acda.spring.catalogo.model.entity.*;
import es.mjsplayground.acda.spring.catalogo.model.repository.PedidoRepository;
import java.time.Clock;
import java.util.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {
  private final PresupuestoService presupuestos;
  private final ExistenciasService existencias;
  private final PedidoRepository pedidos;
  private final ApplicationEventPublisher eventos;
  private final Clock reloj;

  public PedidoService(
      PresupuestoService presupuestos,
      ExistenciasService existencias,
      PedidoRepository pedidos,
      ApplicationEventPublisher eventos,
      Clock reloj) {
    this.presupuestos = presupuestos;
    this.existencias = existencias;
    this.pedidos = pedidos;
    this.eventos = eventos;
    this.reloj = reloj;
  }

  @Transactional
  public PedidoDatos confirmar(String clave, List<LineaSolicitud> lineas) {
    // TODO reglas finales: confirmación coordinada con atomicidad y conflictos
    throw new UnsupportedOperationException(
        "Etapa pendiente: reglas finales: confirmación coordinada con atomicidad y conflictos");
  }

  @Transactional(readOnly = true)
  public List<PedidoDatos> listar() {
    return pedidos.findAllByOrderByFechaDescIdDesc().stream().map(PedidoDatos::de).toList();
  }
}
