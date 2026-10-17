package com.liverpool.customer.application.usecase;

import com.liverpool.customer.application.command.CreateCustomerCommand;
import com.liverpool.customer.application.exception.CustomerAlreadyExistsException;
import com.liverpool.customer.application.exception.EmailAlreadyExistsException;
import com.liverpool.customer.application.port.in.CreateCustomerUseCase;
import com.liverpool.customer.application.port.out.CustomerOrdersPort;
import com.liverpool.customer.application.port.out.CustomerRepositoryPort;
import com.liverpool.customer.application.view.CustomerView;
import com.liverpool.customer.domain.Customer;
import com.liverpool.customer.domain.Email;
import com.liverpool.customer.domain.UserId;
import java.time.Clock;
import java.util.UUID;

/**
 * Implementación de {@link CreateCustomerUseCase}.
 */
public class CreateCustomerHandler implements CreateCustomerUseCase {

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
  public CreateCustomerHandler(CustomerRepositoryPort repository,
      CustomerOrdersPort ordersPort, Clock clock) {
    this.repository = repository;
    this.ordersLookup = new OrdersLookup(ordersPort);
    this.clock = clock;
  }

  @Override
  public CustomerView execute(CreateCustomerCommand command) {
    String rawId = command.userId() == null || command.userId().isBlank()
        ? UUID.randomUUID().toString() : command.userId();
    UserId userId = new UserId(rawId);
    Email email = new Email(command.email());
    Customer customer = Customer.register(userId, command.firstName(),
        command.paternalLastName(), command.maternalLastName(), email, clock.instant());
    if (repository.findById(userId).isPresent()) {
      throw new CustomerAlreadyExistsException(userId.value());
    }
    if (repository.findByEmail(email).isPresent()) {
      throw new EmailAlreadyExistsException(email.value());
    }
    return ordersLookup.viewOf(repository.save(customer));
  }
}
