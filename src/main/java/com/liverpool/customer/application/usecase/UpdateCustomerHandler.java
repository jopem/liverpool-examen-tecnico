package com.liverpool.customer.application.usecase;

import com.liverpool.customer.application.command.UpdateCustomerCommand;
import com.liverpool.customer.application.exception.CustomerNotFoundException;
import com.liverpool.customer.application.exception.EmailAlreadyExistsException;
import com.liverpool.customer.application.port.in.UpdateCustomerUseCase;
import com.liverpool.customer.application.port.out.CustomerOrdersPort;
import com.liverpool.customer.application.port.out.CustomerRepositoryPort;
import com.liverpool.customer.application.view.CustomerView;
import com.liverpool.customer.domain.Customer;
import com.liverpool.customer.domain.Email;
import com.liverpool.customer.domain.UserId;
import java.time.Clock;

/**
 * Implementación de {@link UpdateCustomerUseCase}.
 */
public class UpdateCustomerHandler implements UpdateCustomerUseCase {

  private final CustomerRepositoryPort repository;
  private final OrdersLookup ordersLookup;
  private final Clock clock;

  /**
   * Crea el caso de uso.
   *
   * @param repository almacén de clientes
   * @param ordersPort pedidos del cliente
   * @param clock reloj
   */
  public UpdateCustomerHandler(CustomerRepositoryPort repository,
      CustomerOrdersPort ordersPort, Clock clock) {
    this.repository = repository;
    this.ordersLookup = new OrdersLookup(ordersPort);
    this.clock = clock;
  }

  @Override
  public CustomerView execute(String userId, UpdateCustomerCommand command) {
    UserId id = new UserId(userId);
    Customer current = repository.findById(id)
        .orElseThrow(() -> new CustomerNotFoundException(id.value()));
    Email email = new Email(command.email());
    Customer updated = current.updatePersonalData(command.firstName(),
        command.paternalLastName(), command.maternalLastName(), email, clock.instant());
    repository.findByEmail(email)
        .filter(other -> !other.userId().equals(id))
        .ifPresent(other -> {
          throw new EmailAlreadyExistsException(email.value());
        });
    return ordersLookup.viewOf(repository.save(updated));
  }
}
