package com.liverpool.order.application.exception;

/**
 * El origen de los pedidos no está disponible y no hay una copia utilizable.
 */
public class OrderSourceUnavailableException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Crea la excepción con la causa original.
   *
   * @param message descripción del problema
   * @param cause error que lo originó
   */
  public OrderSourceUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
