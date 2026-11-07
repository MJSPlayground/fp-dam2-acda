package es.mjsplayground.acda.spring.catalogo.model.service;

import es.mjsplayground.acda.spring.catalogo.model.entity.*;
import es.mjsplayground.acda.spring.catalogo.model.repository.PedidoRepository;
import java.time.Clock;
import java.time.temporal.ChronoUnit;
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
    if (clave == null || !clave.matches("[A-Za-z0-9_-]{1,60}"))
      throw new ReglaCatalogoException(
          "Clave de solicitud de 1 a 60 letras/dígitos/guion/guionbajo.");
    Presupuesto p = presupuestos.calcular(lineas);
    for (Presupuesto.Linea l :
        p.lineas().stream().sorted(Comparator.comparing(Presupuesto.Linea::libroId)).toList())
      existencias.descontar(l.libroId(), l.version(), l.cantidad());
    Pedido pedido =
        new Pedido(
            clave,
            reloj.instant().truncatedTo(ChronoUnit.MICROS),
            p.subtotal(),
            p.descuento(),
            p.total(),
            p.lineas().stream()
                .map(l -> new LineaPedido(l.isbn(), l.titulo(), l.cantidad(), l.precioUnitario()))
                .toList());
    pedidos.saveAndFlush(pedido);
    eventos.publishEvent(new CambioCatalogo(pedido.getId(), "CONFIRMAR_PEDIDO", reloj.instant()));
    return PedidoDatos.de(pedido);
  }

  @Transactional(readOnly = true)
  public List<PedidoDatos> listar() {
    return pedidos.findAllByOrderByFechaDescIdDesc().stream().map(PedidoDatos::de).toList();
  }
}
