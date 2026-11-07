package es.mjsplayground.acda.spring.catalogo.web;

import es.mjsplayground.acda.spring.catalogo.config.CatalogoProperties;
import es.mjsplayground.acda.spring.catalogo.model.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.*;
import org.springframework.core.env.*;
import org.springframework.dao.*;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class ErroresWeb {
  private static final Logger log = LoggerFactory.getLogger(ErroresWeb.class);
  private final Environment env;
  private final CatalogoProperties propiedades;

  public ErroresWeb(Environment env, CatalogoProperties propiedades) {
    this.env = env;
    this.propiedades = propiedades;
  }

  @ModelAttribute("seguro")
  boolean seguro() {
    return env.acceptsProfiles(Profiles.of("seguro"));
  }

  @ModelAttribute("puedeEditar")
  boolean editar(HttpServletRequest r) {
    return !seguro() || r.isUserInRole("GESTOR");
  }

  @ModelAttribute("maxStock")
  int maxStock() {
    return propiedades.maxStock();
  }

  @ModelAttribute("finalDisponible")
  boolean finalDisponible() {
    return false;
  }

  static String mensaje(RuntimeException e) {
    if (e instanceof ReglaCatalogoException
        || e instanceof NoEncontradoException
        || e instanceof UnsupportedOperationException) return e.getMessage();
    log.error("Fallo al procesar formulario", e);
    if (e instanceof DataIntegrityViolationException)
      return "ISBN/nombre o solicitud ya existente; no se han aplicado cambios.";
    if (e instanceof OptimisticLockingFailureException)
      return "El registro cambió: vuelve a abrir el formulario.";
    return "No se pudo completar la operación. Consulta el registro técnico.";
  }

  @ExceptionHandler(ReglaCatalogoException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  String regla(ReglaCatalogoException e, Model m) {
    m.addAttribute("errorMensaje", e.getMessage());
    return "fallo";
  }

  @ExceptionHandler(NoEncontradoException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  String ausente(NoEncontradoException e, Model m) {
    m.addAttribute("errorMensaje", e.getMessage());
    return "fallo";
  }

  @ExceptionHandler(UnsupportedOperationException.class)
  @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
  String pendiente(UnsupportedOperationException e, Model m) {
    m.addAttribute("errorMensaje", e.getMessage());
    return "fallo";
  }

  @ExceptionHandler({
    DataIntegrityViolationException.class,
    OptimisticLockingFailureException.class
  })
  @ResponseStatus(HttpStatus.CONFLICT)
  String conflicto(RuntimeException e, Model m) {
    m.addAttribute("errorMensaje", mensaje(e));
    return "fallo";
  }

  @ExceptionHandler(Exception.class)
  String tecnico(Exception e, Model m, jakarta.servlet.http.HttpServletResponse respuesta) {
    int status = 500;
    String texto = "No se pudo completar la operación. Consulta el registro técnico.";
    if (e instanceof org.springframework.web.ErrorResponse error) {
      status = error.getStatusCode().value();
      texto = "La petición no se pudo atender con ese método, ruta o contenido.";
      error
          .getHeaders()
          .forEach((nombre, valores) -> valores.forEach(v -> respuesta.addHeader(nombre, v)));
    } else if (e instanceof org.springframework.validation.BindException
        || e
            instanceof
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException) {
      status = 400;
      texto = "Revisa los tipos y valores de la petición.";
    } else log.error("Fallo web", e);
    respuesta.setStatus(status);
    m.addAttribute("errorMensaje", texto);
    return "fallo";
  }
}
