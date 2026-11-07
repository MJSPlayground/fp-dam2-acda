package es.mjsplayground.acda.spring.catalogo.web;

import es.mjsplayground.acda.spring.catalogo.model.service.*;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CatalogoController {
  private final CatalogoService catalogo;
  private final EditorialService editoriales;

  public CatalogoController(CatalogoService catalogo, EditorialService editoriales) {
    this.catalogo = catalogo;
    this.editoriales = editoriales;
  }

  @GetMapping("/")
  public String inicio() {
    return "redirect:/libros";
  }

  @GetMapping("/libros")
  public String libros(
      @RequestParam(defaultValue = "") String texto,
      @RequestParam(required = false) Long editorial,
      Model m) {
    m.addAttribute("texto", texto);
    m.addAttribute("editorialSeleccionada", editorial);
    m.addAttribute("editoriales", editoriales.listar());
    try {
      m.addAttribute("libros", catalogo.listar(texto, editorial));
    } catch (UnsupportedOperationException e) {
      m.addAttribute("libros", List.of());
      m.addAttribute("mensaje", e.getMessage());
    }
    return "libros";
  }

  @GetMapping("/libros/nuevo")
  public String nuevo(Model m) {
    m.addAttribute("libroForm", new LibroForm());
    m.addAttribute("editoriales", editoriales.listar());
    return "libro-form";
  }

  @GetMapping("/libros/{id}/editar")
  public String editar(@PathVariable Long id, Model m) {
    m.addAttribute("libroForm", LibroForm.de(catalogo.obtener(id)));
    m.addAttribute("editoriales", editoriales.listar());
    return "libro-form";
  }

  @PostMapping("/libros/guardar")
  public String guardar(
      @Valid @ModelAttribute LibroForm libroForm,
      BindingResult errores,
      Model m,
      RedirectAttributes flash) {
    if (!errores.hasErrors())
      try {
        catalogo.guardar(libroForm.solicitud());
        flash.addFlashAttribute("mensaje", "Libro guardado.");
        return "redirect:/libros";
      } catch (RuntimeException e) {
        errores.reject("negocio", ErroresWeb.mensaje(e));
      }
    m.addAttribute("editoriales", editoriales.listar());
    return "libro-form";
  }

  @PostMapping("/libros/{id}/eliminar")
  public String eliminar(@PathVariable Long id, RedirectAttributes f) {
    catalogo.eliminar(id);
    f.addFlashAttribute("mensaje", "Libro eliminado.");
    return "redirect:/libros";
  }

  @GetMapping("/editoriales")
  public String editoriales(Model m) {
    m.addAttribute("editoriales", editoriales.listar());
    if (!m.containsAttribute("editorialForm")) m.addAttribute("editorialForm", new EditorialForm());
    return "editoriales";
  }

  @GetMapping("/editoriales/{id}/editar")
  public String editarEditorial(@PathVariable Long id, Model m) {
    EditorialService.Datos e =
        editoriales.listar().stream()
            .filter(x -> x.id().equals(id))
            .findFirst()
            .orElseThrow(() -> new NoEncontradoException("Editorial no encontrada."));
    EditorialForm f = new EditorialForm();
    f.setId(e.id());
    f.setNombre(e.nombre());
    m.addAttribute("editorialForm", f);
    return editoriales(m);
  }

  @PostMapping("/editoriales/guardar")
  public String guardarEditorial(
      @Valid @ModelAttribute EditorialForm editorialForm,
      BindingResult errores,
      Model m,
      RedirectAttributes f) {
    if (!errores.hasErrors())
      try {
        editoriales.guardar(editorialForm.getId(), editorialForm.getNombre());
        f.addFlashAttribute("mensaje", "Editorial guardada.");
        return "redirect:/editoriales";
      } catch (RuntimeException e) {
        errores.reject("negocio", ErroresWeb.mensaje(e));
      }
    m.addAttribute("editoriales", editoriales.listar());
    return "editoriales";
  }

  @PostMapping("/editoriales/{id}/eliminar")
  public String eliminarEditorial(@PathVariable Long id, RedirectAttributes f) {
    editoriales.eliminar(id);
    f.addFlashAttribute("mensaje", "Editorial eliminada.");
    return "redirect:/editoriales";
  }

  @PostMapping("/demo")
  public String demo(RedirectAttributes f) {
    catalogo.demo();
    f.addFlashAttribute("mensaje", "Demostración creada expresamente en base vacía.");
    return "redirect:/libros";
  }
}
