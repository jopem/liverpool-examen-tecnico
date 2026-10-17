package com.liverpool.customer.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Cliente, raíz del agregado: protege sus datos y gestiona sus direcciones de entrega.
 *
 * <p>Es inmutable: cada operación devuelve un cliente nuevo. Las direcciones conservan el orden
 * en que se agregaron, así que la primera es la más antigua.
 *
 * @param userId identificador, igual al de los pedidos
 * @param firstName nombre
 * @param paternalLastName apellido paterno
 * @param maternalLastName apellido materno; {@code null} si no tiene
 * @param email correo electrónico
 * @param defaultAddressId dirección predeterminada; {@code null} si no hay direcciones
 * @param deliveryAddresses direcciones de entrega, de 0 a N
 * @param createdAt fecha de alta
 * @param updatedAt fecha de la última modificación
 * @param version versión para el control de concurrencia; {@code null} si aún no se guarda. Es
 *     un dato opaco que solo la persistencia interpreta.
 */
public record Customer(
    UserId userId,
    String firstName,
    String paternalLastName,
    String maternalLastName,
    Email email,
    String defaultAddressId,
    List<DeliveryAddress> deliveryAddresses,
    Instant createdAt,
    Instant updatedAt,
    Long version) {

  /**
   * Valida los datos obligatorios y que la dirección predeterminada exista.
   */
  public Customer {
    if (userId == null || email == null) {
      throw new IllegalArgumentException("El userId y el correo son obligatorios");
    }
    firstName = required(firstName, "El nombre");
    paternalLastName = required(paternalLastName, "El apellido paterno");
    maternalLastName = maternalLastName == null || maternalLastName.isBlank()
        ? null : maternalLastName.trim();
    deliveryAddresses = deliveryAddresses == null ? List.of() : List.copyOf(deliveryAddresses);
    if (defaultAddressId != null && deliveryAddresses.stream()
        .noneMatch(address -> address.addressId().equals(defaultAddressId))) {
      throw new IllegalArgumentException("La dirección predeterminada no existe");
    }
  }

  /**
   * Da de alta un cliente sin direcciones.
   *
   * @param userId identificador
   * @param firstName nombre
   * @param paternalLastName apellido paterno
   * @param maternalLastName apellido materno, opcional
   * @param email correo electrónico
   * @param now fecha de alta
   * @return el cliente nuevo, sin versión
   */
  public static Customer register(UserId userId, String firstName, String paternalLastName,
      String maternalLastName, Email email, Instant now) {
    return new Customer(userId, firstName, paternalLastName, maternalLastName, email, null,
        List.of(), now, now, null);
  }

  /**
   * Reemplaza los datos personales; no toca las direcciones.
   *
   * @param newFirstName nombre
   * @param newPaternalLastName apellido paterno
   * @param newMaternalLastName apellido materno, opcional
   * @param newEmail correo electrónico
   * @param now fecha de la modificación
   * @return el cliente actualizado
   */
  public Customer updatePersonalData(String newFirstName, String newPaternalLastName,
      String newMaternalLastName, Email newEmail, Instant now) {
    return new Customer(userId, newFirstName, newPaternalLastName, newMaternalLastName,
        newEmail, defaultAddressId, deliveryAddresses, createdAt, now, version);
  }

  /**
   * Agrega una dirección. Si es la primera, queda como predeterminada.
   *
   * @param address la dirección nueva
   * @param now fecha de la modificación
   * @return el cliente con la dirección agregada
   */
  public Customer addAddress(DeliveryAddress address, Instant now) {
    List<DeliveryAddress> updated = new ArrayList<>(deliveryAddresses);
    updated.add(address);
    String newDefault = defaultAddressId == null ? address.addressId() : defaultAddressId;
    return withAddresses(updated, newDefault, now);
  }

  /**
   * Reemplaza los datos de una dirección, conservando su identificador y su posición.
   *
   * @param newData los datos nuevos; su {@code addressId} indica la dirección a reemplazar
   * @param now fecha de la modificación
   * @return el cliente con la dirección reemplazada
   * @throws AddressNotFoundException si el cliente no tiene esa dirección
   */
  public Customer replaceAddress(DeliveryAddress newData, Instant now) {
    findAddress(newData.addressId());
    List<DeliveryAddress> updated = deliveryAddresses.stream()
        .map(address -> address.addressId().equals(newData.addressId()) ? newData : address)
        .toList();
    return withAddresses(updated, defaultAddressId, now);
  }

  /**
   * Elimina una dirección. Si era la predeterminada y quedan otras, la más antigua pasa a serlo;
   * si era la única, el cliente queda sin predeterminada.
   *
   * @param addressId identificador de la dirección
   * @param now fecha de la modificación
   * @return el cliente sin esa dirección
   * @throws AddressNotFoundException si el cliente no tiene esa dirección
   */
  public Customer removeAddress(String addressId, Instant now) {
    findAddress(addressId);
    List<DeliveryAddress> updated = deliveryAddresses.stream()
        .filter(address -> !address.addressId().equals(addressId))
        .toList();
    String newDefault = defaultAddressId;
    if (addressId.equals(defaultAddressId)) {
      newDefault = updated.isEmpty() ? null : updated.get(0).addressId();
    }
    return withAddresses(updated, newDefault, now);
  }

  /**
   * Fija la dirección predeterminada.
   *
   * @param addressId identificador de la dirección
   * @param now fecha de la modificación
   * @return el cliente con la nueva predeterminada
   * @throws AddressNotFoundException si el cliente no tiene esa dirección
   */
  public Customer setDefaultAddress(String addressId, Instant now) {
    findAddress(addressId);
    return withAddresses(deliveryAddresses, addressId, now);
  }

  /**
   * Busca una dirección del cliente.
   *
   * @param addressId identificador de la dirección
   * @return la dirección
   * @throws AddressNotFoundException si el cliente no tiene esa dirección
   */
  public DeliveryAddress findAddress(String addressId) {
    return deliveryAddresses.stream()
        .filter(address -> address.addressId().equals(addressId))
        .findFirst()
        .orElseThrow(() -> new AddressNotFoundException(addressId));
  }

  private Customer withAddresses(List<DeliveryAddress> addresses, String newDefault,
      Instant now) {
    return new Customer(userId, firstName, paternalLastName, maternalLastName, email,
        newDefault, addresses, createdAt, now, version);
  }

  private static String required(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " es obligatorio");
    }
    return value.trim();
  }
}
