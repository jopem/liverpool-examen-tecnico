package com.liverpool.customer.domain;

/**
 * El cliente no tiene una dirección con el identificador indicado.
 */
public class AddressNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Crea la excepción para un identificador de dirección.
   *
   * @param addressId identificador que no se encontró
   */
  public AddressNotFoundException(String addressId) {
    super("No existe la dirección " + addressId);
  }
}
