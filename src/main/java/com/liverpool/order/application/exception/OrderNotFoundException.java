package com.liverpool.order.application.exception;

/**
 * No existe un pedido con el número indicado.
 */
public class OrderNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Crea la excepción para un número de pedido.
   *
   * @param orderRef número de pedido que no se encontró
   */
  public OrderNotFoundException(String orderRef) {
    super("No existe el pedido " + orderRef);
  }
}
