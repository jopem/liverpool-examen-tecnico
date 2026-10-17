package com.liverpool.customer.application.exception;

/**
 * El cliente fue modificado por otra petición mientras se actualizaba.
 */
public class ConcurrentCustomerModificationException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Crea la excepción con la causa original.
   *
   * @param cause error de la persistencia
   */
  public ConcurrentCustomerModificationException(Throwable cause) {
    super("El cliente fue modificado por otra petición. Consúltalo de nuevo e inténtalo otra "
        + "vez.", cause);
  }
}
