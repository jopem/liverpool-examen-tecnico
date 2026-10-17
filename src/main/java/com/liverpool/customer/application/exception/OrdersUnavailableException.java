package com.liverpool.customer.application.exception;

/**
 * No se pudieron consultar los pedidos del cliente.
 */
public class OrdersUnavailableException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Crea la excepción con la causa original.
   *
   * @param cause error del módulo de pedidos
   */
  public OrdersUnavailableException(Throwable cause) {
    super("No se pudieron consultar los pedidos del cliente", cause);
  }
}
