package com.liverpool.customer.application.usecase;

import com.liverpool.customer.application.exception.ConcurrentCustomerModificationException;
import com.liverpool.customer.application.exception.CustomerAlreadyExistsException;
import com.liverpool.customer.application.port.out.CustomerRepositoryPort;
import com.liverpool.customer.domain.Customer;
import com.liverpool.customer.domain.Email;
import com.liverpool.customer.domain.UserId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Almacén en memoria para las pruebas; imita la unicidad y el control de versión de Mongo.
 */
class InMemoryCustomerRepository implements CustomerRepositoryPort {

  private final Map<String, Customer> store = new HashMap<>();

  @Override
  public Optional<Customer> findById(UserId userId) {
    return Optional.ofNullable(store.get(userId.value()));
  }

  @Override
  public Optional<Customer> findByEmail(Email email) {
    return store.values().stream().filter(c -> c.email().equals(email)).findFirst();
  }

  @Override
  public Customer save(Customer customer) {
    Customer current = store.get(customer.userId().value());
    if (customer.version() == null && current != null) {
      throw new CustomerAlreadyExistsException(customer.userId().value());
    }
    if (customer.version() != null && !customer.version().equals(current.version())) {
      throw new ConcurrentCustomerModificationException(new RuntimeException());
    }
    long next = customer.version() == null ? 0 : customer.version() + 1;
    Customer saved = new Customer(customer.userId(), customer.firstName(),
        customer.paternalLastName(), customer.maternalLastName(), customer.email(),
        customer.defaultAddressId(), customer.deliveryAddresses(), customer.createdAt(),
        customer.updatedAt(), next);
    store.put(saved.userId().value(), saved);
    return saved;
  }
}
