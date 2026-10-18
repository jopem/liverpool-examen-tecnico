package com.liverpool.order.application.port.in;

import com.liverpool.order.application.exception.OrderNotFoundException;
import com.liverpool.order.application.exception.OrderSourceUnavailableException;
import com.liverpool.order.domain.Order;

/**
 * Puerto de entrada. Caso de uso: consultar el detalle de un pedido por su número.
 */
public interface GetOrderUseCase {

  /**
   * Busca el pedido con el número exacto indicado.
   *
   * @param orderRef número de pedido
   * @return el pedido con sus artículos
   * @throws OrderNotFoundException si no existe
   * @throws OrderSourceUnavailableException si el origen no está disponible
   */
  Order execute(String orderRef);
}
