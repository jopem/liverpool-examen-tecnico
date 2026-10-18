package com.liverpool.order.application.exception;

/**
 * La búsqueda no indicó ningún criterio.
 */
public class EmptySearchCriteriaException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Crea la excepción con un mensaje fijo.
   */
  public EmptySearchCriteriaException() {
    super("Indica al menos un criterio de búsqueda: q, orderRef, orderStatus o storeName");
  }
}
