package com.liverpool.customer.infrastructure.out.orders;

import com.liverpool.customer.application.exception.OrdersUnavailableException;
import com.liverpool.customer.application.port.out.CustomerOrdersPort;
import com.liverpool.customer.domain.UserId;
import com.liverpool.order.application.exception.OrderSourceUnavailableException;
import com.liverpool.order.application.port.in.FindOrderRefsByUserUseCase;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida que obtiene los pedidos de un cliente del módulo {@code order}, a través
 * de su puerto de entrada. Es el único punto de contacto entre los dos módulos.
 */
@Component
public class OrderModuleOrdersAdapter implements CustomerOrdersPort {

  private final FindOrderRefsByUserUseCase findOrderRefs;

  OrderModuleOrdersAdapter(FindOrderRefsByUserUseCase findOrderRefs) {
    this.findOrderRefs = findOrderRefs;
  }

  @Override
  public List<String> findOrderRefs(UserId userId) {
    try {
      return findOrderRefs.execute(userId.value());
    } catch (OrderSourceUnavailableException e) {
      throw new OrdersUnavailableException(e);
    }
  }
}
