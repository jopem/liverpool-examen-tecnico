package com.liverpool.customer.application.command;

import com.liverpool.customer.domain.DeliveryAddress;

/**
 * Datos de una dirección de entrega, sin identificador (lo asigna el sistema).
 *
 * @param street calle
 * @param exteriorNumber número exterior
 * @param interiorNumber número interior, opcional
 * @param neighborhood colonia
 * @param zipCode código postal de 5 dígitos
 * @param city ciudad
 * @param state estado
 * @param references referencias, opcional
 */
public record AddressData(
    String street,
    String exteriorNumber,
    String interiorNumber,
    String neighborhood,
    String zipCode,
    String city,
    String state,
    String references) {

  /**
   * Construye la dirección del dominio con el identificador indicado.
   *
   * @param addressId identificador de la dirección
   * @return la dirección validada
   * @throws IllegalArgumentException si algún dato es inválido
   */
  public DeliveryAddress toAddress(String addressId) {
    return new DeliveryAddress(addressId, street, exteriorNumber, interiorNumber, neighborhood,
        zipCode, city, state, references);
  }
}
