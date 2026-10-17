package com.liverpool.customer.application.exception;

/**
 * El cliente no existe.
 */
public class CustomerNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Crea la excepción para un identificador de cliente.
   *
   * @param userId identificador que no se encontró
   */
  public CustomerNotFoundException(String userId) {
    super("No existe el cliente " + userId);
  }
}
