package es.mjsplayground.acda.mongodb.liga.api;

import es.mjsplayground.acda.mongodb.liga.model.service.LigaException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.slf4j.*;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiErrors {
  private static final Logger log = LoggerFactory.getLogger(ApiErrors.class);

  public record ErrorApi(int estado, String mensaje, String ruta, Instant instante) {}

  private ResponseEntity<ErrorApi> respuesta(
      int estado, String mensaje, HttpServletRequest request) {
    return ResponseEntity.status(estado)
        .body(new ErrorApi(estado, mensaje, request.getRequestURI(), Instant.now()));
  }

  @ExceptionHandler(LigaException.class)
  ResponseEntity<ErrorApi> negocio(LigaException e, HttpServletRequest r) {
    return respuesta(e.estado(), e.getMessage(), r);
  }

  @ExceptionHandler({
    MethodArgumentNotValidException.class,
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class,
    org.springframework.web.bind.MissingServletRequestParameterException.class
  })
  ResponseEntity<ErrorApi> entrada(Exception e, HttpServletRequest r) {
    return respuesta(400, "Revisa campos obligatorios, tipos, longitudes y formato JSON.", r);
  }

  @ExceptionHandler(UnsupportedOperationException.class)
  ResponseEntity<ErrorApi> pendiente(Exception e, HttpServletRequest r) {
    return respuesta(501, e.getMessage(), r);
  }

  @ExceptionHandler({
    org.springframework.web.HttpRequestMethodNotSupportedException.class,
    org.springframework.web.HttpMediaTypeNotSupportedException.class,
    org.springframework.web.servlet.resource.NoResourceFoundException.class
  })
  ResponseEntity<ErrorApi> protocolo(Exception e, HttpServletRequest r) {
    org.springframework.web.ErrorResponse error = (org.springframework.web.ErrorResponse) e;
    return ResponseEntity.status(error.getStatusCode())
        .headers(error.getHeaders())
        .body(
            new ErrorApi(
                error.getStatusCode().value(),
                error.getBody().getDetail(),
                r.getRequestURI(),
                Instant.now()));
  }

  @ExceptionHandler({
    org.springframework.dao.DuplicateKeyException.class,
    org.springframework.dao.OptimisticLockingFailureException.class
  })
  ResponseEntity<ErrorApi> conflicto(Exception e, HttpServletRequest r) {
    return respuesta(
        409,
        "Nombre duplicado o equipo modificado por otra operación; actualiza antes de repetir.",
        r);
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ErrorApi> fallo(Exception e, HttpServletRequest r) {
    log.error("Fallo atendiendo {}", r.getRequestURI(), e);
    return respuesta(
        500, "No se pudo completar la operación. Consulta el registro del servidor.", r);
  }
}
