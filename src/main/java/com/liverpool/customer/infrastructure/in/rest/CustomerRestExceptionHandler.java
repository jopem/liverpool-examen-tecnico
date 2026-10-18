package com.liverpool.customer.infrastructure.in.rest;

import com.liverpool.customer.application.exception.ConcurrentCustomerModificationException;
import com.liverpool.customer.application.exception.CustomerAlreadyExistsException;
import com.liverpool.customer.application.exception.CustomerNotFoundException;
import com.liverpool.customer.application.exception.EmailAlreadyExistsException;
import com.liverpool.customer.domain.AddressNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce los errores del módulo de clientes a respuestas Problem Details (RFC 7807).
 */
@RestControllerAdvice(basePackageClasses = CustomerRestAdapter.class)
public class CustomerRestExceptionHandler {

  /**
   * El cliente o la dirección no existen.
   *
   * @param e el error
   * @return respuesta 404
   */
  @ExceptionHandler({CustomerNotFoundException.class, AddressNotFoundException.class})
  public ProblemDetail notFound(RuntimeException e) {
    return problem(HttpStatus.NOT_FOUND, "Recurso no encontrado", e.getMessage());
  }

  /**
   * El userId o el correo ya existen, o el cliente cambió mientras se actualizaba.
   *
   * @param e el error
   * @return respuesta 409
   */
  @ExceptionHandler({CustomerAlreadyExistsException.class, EmailAlreadyExistsException.class,
      ConcurrentCustomerModificationException.class})
  public ProblemDetail conflict(RuntimeException e) {
    return problem(HttpStatus.CONFLICT, "Conflicto", e.getMessage());
  }

  /**
   * Un dato no pasa la validación del dominio.
   *
   * @param e el error
   * @return respuesta 400
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail invalidData(IllegalArgumentException e) {
    return problem(HttpStatus.BAD_REQUEST, "Datos inválidos", e.getMessage());
  }

  /**
   * El cuerpo no pasa la validación de formato; indica qué campos fallaron.
   *
   * @param e el error
   * @return respuesta 400 con el detalle por campo
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail invalidBody(MethodArgumentNotValidException e) {
    Map<String, String> errors = new LinkedHashMap<>();
    e.getBindingResult().getFieldErrors()
        .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
    ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Datos inválidos",
        "Hay campos con errores");
    problem.setProperty("errors", errors);
    return problem;
  }

  /**
   * El cuerpo no es un JSON legible.
   *
   * @param e el error
   * @return respuesta 400
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ProblemDetail unreadable(HttpMessageNotReadableException e) {
    return problem(HttpStatus.BAD_REQUEST, "Cuerpo inválido",
        "El cuerpo de la petición no es un JSON válido");
  }

  private static ProblemDetail problem(HttpStatus status, String title, String detail) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setTitle(title);
    return problem;
  }
}
