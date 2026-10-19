package com.liverpool.customer.infrastructure.in.seed;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.liverpool.customer.application.exception.CustomerAlreadyExistsException;
import com.liverpool.customer.application.port.in.CreateCustomerUseCase;
import com.liverpool.customer.application.port.in.ManageDeliveryAddressesUseCase;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de {@link DemoDataSeeder}.
 */
class DemoDataSeederTest {

  private final CreateCustomerUseCase create = mock(CreateCustomerUseCase.class);
  private final ManageDeliveryAddressesUseCase addresses =
      mock(ManageDeliveryAddressesUseCase.class);
  private final DemoDataSeeder seeder = new DemoDataSeeder(create, addresses);

  /** Crea los 7 clientes de prueba y sus 6 direcciones. */
  @Test
  void createsSevenCustomersWithTheirAddresses() {
    seeder.run(null);

    verify(create, times(7)).execute(any());
    verify(addresses, times(6)).add(anyString(), any());
  }

  /** Si un cliente ya existe, se omite sin fallar y sin duplicar sus direcciones. */
  @Test
  void skipsExistingCustomers() {
    when(create.execute(any())).thenThrow(new CustomerAlreadyExistsException("x"));

    seeder.run(null);

    verify(create, times(7)).execute(any());
    verify(addresses, never()).add(anyString(), any());
  }
}
