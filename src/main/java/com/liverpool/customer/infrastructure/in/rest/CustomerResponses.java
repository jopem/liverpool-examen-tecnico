package com.liverpool.customer.infrastructure.in.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.liverpool.customer.application.view.AddressView;
import com.liverpool.customer.application.view.CustomerView;
import com.liverpool.customer.domain.Customer;
import java.time.Instant;
import java.util.List;

/**
 * Respuestas REST de clientes y direcciones.
 */
public final class CustomerResponses {

  private CustomerResponses() {
  }

  /**
   * Un cliente con sus direcciones y sus pedidos.
   *
   * @param userId identificador
   * @param firstName nombre
   * @param paternalLastName apellido paterno
   * @param maternalLastName apellido materno
   * @param email correo electrónico
   * @param defaultAddressId dirección predeterminada
   * @param deliveryAddresses direcciones de entrega
   * @param orders números de pedido, calculados al consultar; se omite si no se pudieron
   *     consultar
   * @param createdAt fecha de alta
   * @param updatedAt fecha de la última modificación
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record CustomerResponse(
      String userId,
      String firstName,
      String paternalLastName,
      String maternalLastName,
      String email,
      String defaultAddressId,
      List<AddressResponse> deliveryAddresses,
      List<String> orders,
      Instant createdAt,
      Instant updatedAt) {

    static CustomerResponse from(CustomerView view) {
      Customer customer = view.customer();
      List<AddressResponse> addresses = customer.deliveryAddresses().stream()
          .map(address -> AddressResponse.from(new AddressView(address,
              address.addressId().equals(customer.defaultAddressId()))))
          .toList();
      return new CustomerResponse(customer.userId().value(), customer.firstName(),
          customer.paternalLastName(), customer.maternalLastName(), customer.email().value(),
          customer.defaultAddressId(), addresses, view.orders(), customer.createdAt(),
          customer.updatedAt());
    }
  }

  /**
   * Una dirección de entrega.
   *
   * @param addressId identificador
   * @param street calle
   * @param exteriorNumber número exterior
   * @param interiorNumber número interior
   * @param neighborhood colonia
   * @param zipCode código postal
   * @param city ciudad
   * @param state estado
   * @param references referencias
   * @param defaultAddress si es la predeterminada del cliente
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record AddressResponse(
      String addressId,
      String street,
      String exteriorNumber,
      String interiorNumber,
      String neighborhood,
      String zipCode,
      String city,
      String state,
      String references,
      boolean defaultAddress) {

    static AddressResponse from(AddressView view) {
      var a = view.address();
      return new AddressResponse(a.addressId(), a.street(), a.exteriorNumber(),
          a.interiorNumber(), a.neighborhood(), a.zipCode(), a.city(), a.state(),
          a.references(), view.defaultAddress());
    }
  }
}
