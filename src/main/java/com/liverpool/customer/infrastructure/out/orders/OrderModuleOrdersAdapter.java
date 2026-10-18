package com.liverpool.customer.infrastructure.out.orders;

import com.liverpool.customer.application.port.out.CustomerOrdersPort;
import com.liverpool.customer.domain.UserId;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida hacia los pedidos del cliente.
 *
 * <p>Versión provisional: el módulo {@code order} todavía no existe, así que siempre devuelve una
 * lista vacía. Se reemplaza cuando se conecte con ese módulo.
 */
@Component
public class OrderModuleOrdersAdapter implements CustomerOrdersPort {

  @Override
  public List<String> findOrderRefs(UserId userId) {
    return List.of();
  }
}
