package com.liverpool.order.application.usecase;

import com.liverpool.order.application.port.in.FindOrderRefsByUserUseCase;
import com.liverpool.order.application.port.out.OrderSourcePort;
import com.liverpool.order.domain.Order;
import java.util.List;

/**
 * Implementación de {@link FindOrderRefsByUserUseCase}. El servicio externo filtra por
 * coincidencia parcial, por eso el filtro por usuario se repite aquí de forma exacta.
 */
public class FindOrderRefsByUserHandler implements FindOrderRefsByUserUseCase {

  private final OrderSourcePort source;

  /**
   * Crea el caso de uso.
   *
   * @param source origen de los pedidos
   */
  public FindOrderRefsByUserHandler(OrderSourcePort source) {
    this.source = source;
  }

  @Override
  public List<String> execute(String userId) {
    return source.findAll().stream()
        .filter(order -> order.userId().equals(userId))
        .map(Order::orderRef)
        .toList();
  }
}
