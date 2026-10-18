package com.liverpool.order.application.port.out;

import com.liverpool.order.application.exception.OrderSourceUnavailableException;
import com.liverpool.order.domain.Order;
import java.util.List;

/**
 * Puerto de salida: origen de los pedidos, ya armados con sus artículos.
 */
public interface OrderSourcePort {

  /**
   * Obtiene todos los pedidos disponibles.
   *
   * @return los pedidos; vacío si no hay ninguno
   * @throws OrderSourceUnavailableException si el origen no responde y no hay copia utilizable
   */
  List<Order> findAll();
}
