package com.liverpool.customer.application.usecase;

import com.liverpool.customer.application.command.AddressData;
import com.liverpool.customer.application.exception.CustomerNotFoundException;
import com.liverpool.customer.application.port.in.ManageDeliveryAddressesUseCase;
import com.liverpool.customer.application.port.out.CustomerRepositoryPort;
import com.liverpool.customer.application.view.AddressView;
import com.liverpool.customer.domain.Customer;
import com.liverpool.customer.domain.DeliveryAddress;
import com.liverpool.customer.domain.UserId;
import java.time.Clock;
import java.util.List;
import java.util.UUID;

/**
 * Implementación de {@link ManageDeliveryAddressesUseCase}.
 */
public class ManageDeliveryAddressesHandler implements ManageDeliveryAddressesUseCase {

  private final CustomerRepositoryPort repository;
  private final Clock clock;

  /**
   * Crea el caso de uso.
   *
   * @param repository almacén de clientes
   * @param clock reloj
   */
  public ManageDeliveryAddressesHandler(CustomerRepositoryPort repository, Clock clock) {
    this.repository = repository;
    this.clock = clock;
  }

  @Override
  public AddressView add(String userId, AddressData data) {
    Customer customer = load(userId);
    DeliveryAddress address = data.toAddress(UUID.randomUUID().toString());
    Customer saved = repository.save(customer.addAddress(address, clock.instant()));
    return view(saved, address.addressId());
  }

  @Override
  public List<AddressView> list(String userId) {
    Customer customer = load(userId);
    return customer.deliveryAddresses().stream()
        .map(address -> view(customer, address.addressId()))
        .toList();
  }

  @Override
  public AddressView get(String userId, String addressId) {
    return view(load(userId), addressId);
  }

  @Override
  public AddressView replace(String userId, String addressId, AddressData data) {
    Customer customer = load(userId);
    Customer saved = repository.save(
        customer.replaceAddress(data.toAddress(addressId), clock.instant()));
    return view(saved, addressId);
  }

  @Override
  public void remove(String userId, String addressId) {
    Customer customer = load(userId);
    repository.save(customer.removeAddress(addressId, clock.instant()));
  }

  @Override
  public void setDefault(String userId, String addressId) {
    Customer customer = load(userId);
    repository.save(customer.setDefaultAddress(addressId, clock.instant()));
  }

  private Customer load(String userId) {
    UserId id = new UserId(userId);
    return repository.findById(id)
        .orElseThrow(() -> new CustomerNotFoundException(id.value()));
  }

  private static AddressView view(Customer customer, String addressId) {
    return new AddressView(customer.findAddress(addressId),
        addressId.equals(customer.defaultAddressId()));
  }
}
