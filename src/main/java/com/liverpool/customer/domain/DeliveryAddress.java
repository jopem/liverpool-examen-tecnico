package com.liverpool.customer.domain;

import java.util.regex.Pattern;

/**
 * Dirección de envío de un cliente.
 *
 * @param addressId identificador asignado por el sistema, inmutable
 * @param street calle
 * @param exteriorNumber número exterior, como texto (admite {@code 12-B} o {@code S/N})
 * @param interiorNumber número interior; {@code null} si no tiene
 * @param neighborhood colonia
 * @param zipCode código postal de 5 dígitos, como texto para conservar el cero inicial
 * @param city ciudad
 * @param state estado
 * @param references referencias para ubicar el domicilio; {@code null} si no hay
 */
public record DeliveryAddress(
    String addressId,
    String street,
    String exteriorNumber,
    String interiorNumber,
    String neighborhood,
    String zipCode,
    String city,
    String state,
    String references) {

  private static final Pattern ZIP_CODE = Pattern.compile("^\\d{5}$");

  /**
   * Valida los campos obligatorios, quita espacios sobrantes y convierte en nulos los campos
   * opcionales en blanco.
   */
  public DeliveryAddress {
    addressId = required(addressId, "El identificador de la dirección");
    street = required(street, "La calle");
    exteriorNumber = required(exteriorNumber, "El número exterior");
    neighborhood = required(neighborhood, "La colonia");
    zipCode = required(zipCode, "El código postal");
    city = required(city, "La ciudad");
    state = required(state, "El estado");
    if (!ZIP_CODE.matcher(zipCode).matches()) {
      throw new IllegalArgumentException("El código postal debe tener 5 dígitos");
    }
    interiorNumber = optional(interiorNumber);
    references = optional(references);
  }

  private static String required(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " es obligatorio");
    }
    return value.trim();
  }

  private static String optional(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }
}
