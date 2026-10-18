package com.liverpool.order.application.usecase;

import com.liverpool.order.application.exception.OrderSourceUnavailableException;
import com.liverpool.order.application.port.out.OrderSourcePort;
import com.liverpool.order.domain.Order;
import java.util.List;

/**
 * Origen de pedidos de prueba con una lista fija o con fallo.
 */
class FakeOrderSource implements OrderSourcePort {

  private final List<Order> orders;
  private final boolean failing;

  FakeOrderSource(List<Order> orders) {
    this.orders = orders;
    this.failing = false;
  }

  FakeOrderSource() {
    this.orders = List.of();
    this.failing = true;
  }

  @Override
  public List<Order> findAll() {
    if (failing) {
      throw new OrderSourceUnavailableException("caído", new RuntimeException());
    }
    return orders;
  }
}
