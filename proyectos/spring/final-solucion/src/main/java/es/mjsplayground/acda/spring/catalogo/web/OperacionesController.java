package es.mjsplayground.acda.spring.catalogo.web;

import es.mjsplayground.acda.spring.catalogo.model.service.*;
import java.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OperacionesController {
  private final CatalogoService catalogo;
  private final ExistenciasService existencias;
  private final PresupuestoService presupuestos;
  private final PedidoService pedidos;

  public OperacionesController(
      CatalogoService catalogo,
      ExistenciasService existencias,
      PresupuestoService presupuestos,
      PedidoService pedidos) {
    this.catalogo = catalogo;
    this.existencias = existencias;
    this.presupuestos = presupuestos;
    this.pedidos = pedidos;
  }

  private void referencias(Model m) {
    m.addAttribute("librosDisponibles", catalogo.listar("", null));
  }

  @GetMapping("/operaciones")
  public String operaciones(Model m) {
    SeleccionForm form = new SeleccionForm();
    form.setClave("pedido-" + UUID.randomUUID());
    List<SeleccionForm.Linea> lineas = new ArrayList<>();
    for (LibroDatos l : catalogo.listar("", null)) {
      SeleccionForm.Linea f = new SeleccionForm.Linea();
      f.setLibroId(l.id());
      f.setVersion(l.version());
      f.setTitulo(l.titulo());
      lineas.add(f);
    }
    form.setLineas(lineas);
    m.addAttribute("seleccionForm", form);
    referencias(m);
    return "operaciones";
  }

  @PostMapping("/operaciones/reponer")
  public String reponer(
      @RequestParam Long libroId,
      @RequestParam Long version,
      @RequestParam int unidades,
      RedirectAttributes f) {
    existencias.reponer(libroId, version, unidades);
    f.addFlashAttribute("mensaje", "Reposición guardada.");
    return "redirect:/operaciones";
  }

  @PostMapping("/operaciones/presupuesto")
  public String presupuesto(@ModelAttribute SeleccionForm seleccionForm, Model m) {
    try {
      m.addAttribute("presupuesto", presupuestos.calcular(seleccionForm.solicitud()));
    } catch (RuntimeException e) {
      m.addAttribute("errorOperacion", ErroresWeb.mensaje(e));
    }
    referencias(m);
    return "operaciones";
  }

  @PostMapping("/operaciones/confirmar")
  public String confirmar(
      @ModelAttribute SeleccionForm seleccionForm, Model m, RedirectAttributes f) {
    try {
      PedidoDatos p = pedidos.confirmar(seleccionForm.getClave(), seleccionForm.solicitud());
      f.addFlashAttribute("mensaje", "Pedido " + p.clave() + " confirmado: " + p.total() + " €.");
      return "redirect:/pedidos";
    } catch (RuntimeException e) {
      m.addAttribute("errorOperacion", ErroresWeb.mensaje(e));
      referencias(m);
      return "operaciones";
    }
  }

  @GetMapping("/pedidos")
  public String pedidos(Model m) {
    m.addAttribute("pedidos", pedidos.listar());
    return "pedidos";
  }
}
