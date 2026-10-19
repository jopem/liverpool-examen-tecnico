package com.liverpool.order.infrastructure.in.rest;

import com.liverpool.order.application.exception.EmptySearchCriteriaException;
import com.liverpool.order.application.exception.OrderNotFoundException;
import com.liverpool.order.application.exception.OrderSourceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce los errores del módulo de pedidos a respuestas Problem Details (RFC 7807).
 */
@RestControllerAdvice(basePackageClasses = OrderRestAdapter.class)
public class OrderRestExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(OrderRestExceptionHandler.class);

  /**
   * El pedido no existe.
   *
   * @param e el error
   * @return respuesta 404
   */
  @ExceptionHandler(OrderNotFoundException.class)
  public ProblemDetail notFound(OrderNotFoundException e) {
    return problem(HttpStatus.NOT_FOUND, "Pedido no encontrado", e.getMessage());
  }

  /**
   * La búsqueda no trae criterios.
   *
   * @param e el error
   * @return respuesta 400
   */
  @ExceptionHandler(EmptySearchCriteriaException.class)
  public ProblemDetail badRequest(EmptySearchCriteriaException e) {
    return problem(HttpStatus.BAD_REQUEST, "Búsqueda sin criterios", e.getMessage());
  }

  /**
   * El servicio externo no responde y no hay copia utilizable.
   *
   * @param e el error
   * @return respuesta 503
   */
  @ExceptionHandler(OrderSourceUnavailableException.class)
  public ProblemDetail unavailable(OrderSourceUnavailableException e) {
    LOG.error("Servicio de pedidos no disponible", e);
    return problem(HttpStatus.SERVICE_UNAVAILABLE, "Servicio de pedidos no disponible",
        "No se pudieron consultar los pedidos. Intenta de nuevo en unos minutos.");
  }

  private static ProblemDetail problem(HttpStatus status, String title, String detail) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setTitle(title);
    return problem;
  }
}
