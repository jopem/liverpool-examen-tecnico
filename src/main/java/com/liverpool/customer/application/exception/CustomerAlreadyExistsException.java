package com.liverpool.customer.application.exception;

/**
 * Ya existe un cliente con ese userId.
 */
public class CustomerAlreadyExistsException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Crea la excepción para un identificador de cliente.
   *
   * @param userId identificador que ya está en uso
   */
  public CustomerAlreadyExistsException(String userId) {
    super("Ya existe un cliente con el userId " + userId);
  }
}
