package com.liverpool.customer.application.usecase;

import com.liverpool.customer.application.exception.CustomerNotFoundException;
import com.liverpool.customer.application.port.in.GetCustomerUseCase;
import com.liverpool.customer.application.port.out.CustomerOrdersPort;
import com.liverpool.customer.application.port.out.CustomerRepositoryPort;
import com.liverpool.customer.application.view.CustomerView;
import com.liverpool.customer.domain.UserId;

/**
 * Implementación de {@link GetCustomerUseCase}.
 */
public class GetCustomerHandler implements GetCustomerUseCase {

  private final CustomerRepositoryPort repository;
  private final OrdersLookup ordersLookup;

  /**
   * Crea el caso de uso.
   *
   * @param repository almacén de clientes
   * @param ordersPort pedidos del cliente
   */
  public GetCustomerHandler(CustomerRepositoryPort repository, CustomerOrdersPort ordersPort) {
    this.repository = repository;
    this.ordersLookup = new OrdersLookup(ordersPort);
  }

  @Override
  public CustomerView execute(String userId) {
    UserId id = new UserId(userId);
    return repository.findById(id)
        .map(ordersLookup::viewOf)
        .orElseThrow(() -> new CustomerNotFoundException(id.value()));
  }
}
