package com.liverpool.customer.application.exception;

/**
 * Otro cliente ya usa ese correo electrónico.
 */
public class EmailAlreadyExistsException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Crea la excepción para un correo.
   *
   * @param email correo que ya está en uso
   */
  public EmailAlreadyExistsException(String email) {
    super("Ya existe un cliente con el correo " + email);
  }
}
