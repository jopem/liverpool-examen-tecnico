package com.liverpool.customer.infrastructure.config;

import com.liverpool.customer.application.port.in.CreateCustomerUseCase;
import com.liverpool.customer.application.port.in.GetCustomerUseCase;
import com.liverpool.customer.application.port.in.ManageDeliveryAddressesUseCase;
import com.liverpool.customer.application.port.in.UpdateCustomerUseCase;
import com.liverpool.customer.application.port.out.CustomerOrdersPort;
import com.liverpool.customer.application.port.out.CustomerRepositoryPort;
import com.liverpool.customer.application.usecase.CreateCustomerHandler;
import com.liverpool.customer.application.usecase.GetCustomerHandler;
import com.liverpool.customer.application.usecase.ManageDeliveryAddressesHandler;
import com.liverpool.customer.application.usecase.UpdateCustomerHandler;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ensambla los casos de uso del módulo de clientes.
 */
@Configuration
public class CustomerModuleConfiguration {

  /**
   * Reloj del sistema, en UTC.
   *
   * @return el reloj
   */
  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }

  /**
   * Crea el caso de uso de alta.
   *
   * @param repository almacén de clientes
   * @param orders pedidos del cliente
   * @param clock reloj
   * @return el caso de uso
   */
  @Bean
  CreateCustomerUseCase createCustomerUseCase(CustomerRepositoryPort repository,
      CustomerOrdersPort orders, Clock clock) {
    return new CreateCustomerHandler(repository, orders, clock);
  }

  /**
   * Crea el caso de uso de consulta.
   *
   * @param repository almacén de clientes
   * @param orders pedidos del cliente
   * @return el caso de uso
   */
  @Bean
  GetCustomerUseCase getCustomerUseCase(CustomerRepositoryPort repository,
      CustomerOrdersPort orders) {
    return new GetCustomerHandler(repository, orders);
  }

  /**
   * Crea el caso de uso de actualización.
   *
   * @param repository almacén de clientes
   * @param orders pedidos del cliente
   * @param clock reloj
   * @return el caso de uso
   */
  @Bean
  UpdateCustomerUseCase updateCustomerUseCase(CustomerRepositoryPort repository,
      CustomerOrdersPort orders, Clock clock) {
    return new UpdateCustomerHandler(repository, orders, clock);
  }

  /**
   * Crea el caso de uso de direcciones.
   *
   * @param repository almacén de clientes
   * @param clock reloj
   * @return el caso de uso
   */
  @Bean
  ManageDeliveryAddressesUseCase manageDeliveryAddressesUseCase(
      CustomerRepositoryPort repository, Clock clock) {
    return new ManageDeliveryAddressesHandler(repository, clock);
  }
}
