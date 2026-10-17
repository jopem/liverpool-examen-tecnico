package com.liverpool.customer.application.port.out;

import com.liverpool.customer.application.exception.OrdersUnavailableException;
import com.liverpool.customer.domain.UserId;
import java.util.List;

/**
 * Puerto de salida: pedidos de un cliente. Es lo único que {@code customer} sabe del módulo de
 * pedidos.
 */
public interface CustomerOrdersPort {

  /**
   * Obtiene los números de pedido del cliente.
   *
   * @param userId identificador del cliente
   * @return los números de pedido; vacío si no tiene
   * @throws OrdersUnavailableException si no se pueden consultar los pedidos
   */
  List<String> findOrderRefs(UserId userId);
}
