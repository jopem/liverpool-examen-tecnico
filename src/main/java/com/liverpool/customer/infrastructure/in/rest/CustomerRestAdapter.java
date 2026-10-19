package com.liverpool.customer.infrastructure.in.rest;

import com.liverpool.customer.application.port.in.CreateCustomerUseCase;
import com.liverpool.customer.application.port.in.GetCustomerUseCase;
import com.liverpool.customer.application.port.in.ManageDeliveryAddressesUseCase;
import com.liverpool.customer.application.port.in.UpdateCustomerUseCase;
import com.liverpool.customer.application.view.CustomerView;
import com.liverpool.customer.infrastructure.in.rest.CustomerRequests.Address;
import com.liverpool.customer.infrastructure.in.rest.CustomerRequests.CreateCustomer;
import com.liverpool.customer.infrastructure.in.rest.CustomerRequests.UpdateCustomer;
import com.liverpool.customer.infrastructure.in.rest.CustomerResponses.AddressResponse;
import com.liverpool.customer.infrastructure.in.rest.CustomerResponses.CustomerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST para clientes y sus direcciones de entrega.
 */
@RestController
@Tag(name = "Clientes", description = "Clientes y sus direcciones de entrega")
@RequestMapping("/api/v1/customers")
public class CustomerRestAdapter {

  private final CreateCustomerUseCase createCustomer;
  private final GetCustomerUseCase getCustomer;
  private final UpdateCustomerUseCase updateCustomer;
  private final ManageDeliveryAddressesUseCase addresses;

  /**
   * Crea el adaptador.
   *
   * @param createCustomer puerto de entrada de alta
   * @param getCustomer puerto de entrada de consulta
   * @param updateCustomer puerto de entrada de actualización
   * @param addresses puerto de entrada de direcciones
   */
  public CustomerRestAdapter(CreateCustomerUseCase createCustomer,
      GetCustomerUseCase getCustomer, UpdateCustomerUseCase updateCustomer,
      ManageDeliveryAddressesUseCase addresses) {
    this.createCustomer = createCustomer;
    this.getCustomer = getCustomer;
    this.updateCustomer = updateCustomer;
    this.addresses = addresses;
  }

  /**
   * Crea un cliente.
   *
   * @param request datos del cliente; el {@code userId} es opcional
   * @return 201 con el cliente creado y su ubicación
   */
  @Operation(summary = "Crea un cliente (el userId es opcional)")
  @PostMapping
  public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CreateCustomer request) {
    CustomerView view = createCustomer.execute(request.toCommand());
    String userId = view.customer().userId().value();
    return ResponseEntity.created(URI.create("/api/v1/customers/" + userId))
        .body(CustomerResponse.from(view));
  }

  /**
   * Consulta un cliente con sus pedidos.
   *
   * @param userId identificador del cliente
   * @return el cliente
   */
  @Operation(summary = "Consulta un cliente con sus pedidos")
  @GetMapping("/{userId}")
  public CustomerResponse get(@PathVariable String userId) {
    return CustomerResponse.from(getCustomer.execute(userId));
  }

  /**
   * Actualiza los datos personales de un cliente; no toca sus direcciones.
   *
   * @param userId identificador del cliente
   * @param request datos nuevos
   * @return el cliente actualizado
   */
  @Operation(summary = "Actualiza los datos personales de un cliente")
  @PutMapping("/{userId}")
  public CustomerResponse update(@PathVariable String userId,
      @Valid @RequestBody UpdateCustomer request) {
    return CustomerResponse.from(updateCustomer.execute(userId, request.toCommand()));
  }

  /**
   * Agrega una dirección de entrega.
   *
   * @param userId identificador del cliente
   * @param request datos de la dirección
   * @return 201 con la dirección creada y su ubicación
   */
  @Operation(summary = "Agrega una dirección de entrega")
  @PostMapping("/{userId}/delivery-addresses")
  public ResponseEntity<AddressResponse> addAddress(@PathVariable String userId,
      @Valid @RequestBody Address request) {
    AddressResponse created = AddressResponse.from(addresses.add(userId, request.toData()));
    return ResponseEntity.created(URI.create(
        "/api/v1/customers/" + userId + "/delivery-addresses/" + created.addressId()))
        .body(created);
  }

  /**
   * Lista las direcciones de entrega de un cliente.
   *
   * @param userId identificador del cliente
   * @return las direcciones, de la más antigua a la más reciente
   */
  @Operation(summary = "Lista las direcciones de entrega")
  @GetMapping("/{userId}/delivery-addresses")
  public List<AddressResponse> listAddresses(@PathVariable String userId) {
    return addresses.list(userId).stream().map(AddressResponse::from).toList();
  }

  /**
   * Consulta una dirección de entrega.
   *
   * @param userId identificador del cliente
   * @param addressId identificador de la dirección
   * @return la dirección
   */
  @Operation(summary = "Consulta una dirección de entrega")
  @GetMapping("/{userId}/delivery-addresses/{addressId}")
  public AddressResponse getAddress(@PathVariable String userId,
      @PathVariable String addressId) {
    return AddressResponse.from(addresses.get(userId, addressId));
  }

  /**
   * Reemplaza los datos de una dirección de entrega.
   *
   * @param userId identificador del cliente
   * @param addressId identificador de la dirección
   * @param request datos nuevos
   * @return la dirección actualizada
   */
  @Operation(summary = "Reemplaza una dirección de entrega")
  @PutMapping("/{userId}/delivery-addresses/{addressId}")
  public AddressResponse replaceAddress(@PathVariable String userId,
      @PathVariable String addressId, @Valid @RequestBody Address request) {
    return AddressResponse.from(addresses.replace(userId, addressId, request.toData()));
  }

  /**
   * Elimina una dirección de entrega.
   *
   * @param userId identificador del cliente
   * @param addressId identificador de la dirección
   * @return 204 sin contenido
   */
  @Operation(summary = "Elimina una dirección de entrega")
  @DeleteMapping("/{userId}/delivery-addresses/{addressId}")
  public ResponseEntity<Void> removeAddress(@PathVariable String userId,
      @PathVariable String addressId) {
    addresses.remove(userId, addressId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Fija la dirección de entrega predeterminada.
   *
   * @param userId identificador del cliente
   * @param addressId identificador de la dirección
   * @return 204 sin contenido
   */
  @Operation(summary = "Fija la dirección predeterminada")
  @PutMapping("/{userId}/delivery-addresses/{addressId}/default")
  public ResponseEntity<Void> setDefaultAddress(@PathVariable String userId,
      @PathVariable String addressId) {
    addresses.setDefault(userId, addressId);
    return ResponseEntity.noContent().build();
  }
}
