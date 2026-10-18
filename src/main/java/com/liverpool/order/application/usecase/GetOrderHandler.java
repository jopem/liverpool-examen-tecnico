package com.liverpool.order.application.usecase;

import com.liverpool.order.application.exception.OrderNotFoundException;
import com.liverpool.order.application.port.in.GetOrderUseCase;
import com.liverpool.order.application.port.out.OrderSourcePort;
import com.liverpool.order.domain.Order;

/**
 * Implementación de {@link GetOrderUseCase}.
 */
public class GetOrderHandler implements GetOrderUseCase {

  private final OrderSourcePort source;

  /**
   * Crea el caso de uso.
   *
   * @param source origen de los pedidos
   */
  public GetOrderHandler(OrderSourcePort source) {
    this.source = source;
  }

  @Override
  public Order execute(String orderRef) {
    String wanted = orderRef == null ? "" : orderRef.trim();
    return source.findAll().stream()
        .filter(order -> order.orderRef().equals(wanted))
        .findFirst()
        .orElseThrow(() -> new OrderNotFoundException(wanted));
  }
}
