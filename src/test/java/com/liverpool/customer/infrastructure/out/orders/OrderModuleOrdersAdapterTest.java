package com.liverpool.customer.infrastructure.out.orders;

import static org.assertj.core.api.Assertions.assertThat;

import com.liverpool.customer.domain.UserId;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de la versión provisional de {@link OrderModuleOrdersAdapter}.
 */
class OrderModuleOrdersAdapterTest {

  /** Mientras no exista el módulo de pedidos, el cliente no tiene pedidos. */
  @Test
  void returnsNoOrders() {
    assertThat(new OrderModuleOrdersAdapter().findOrderRefs(new UserId("u1"))).isEmpty();
  }
}
