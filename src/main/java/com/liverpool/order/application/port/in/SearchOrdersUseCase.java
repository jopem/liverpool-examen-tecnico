package com.liverpool.order.application.port.in;

import com.liverpool.order.application.exception.EmptySearchCriteriaException;
import com.liverpool.order.application.exception.OrderSourceUnavailableException;
import com.liverpool.order.domain.Order;
import com.liverpool.order.domain.OrderSearchCriteria;
import java.util.List;

/**
 * Puerto de entrada. Caso de uso: buscar pedidos por texto libre o por campo.
 */
public interface SearchOrdersUseCase {

  /**
   * Devuelve los pedidos que cumplen todos los criterios.
   *
   * @param criteria criterios de búsqueda; debe indicar al menos uno
   * @return los pedidos encontrados, en el orden del origen
   * @throws EmptySearchCriteriaException si no se indicó ningún criterio
   * @throws OrderSourceUnavailableException si el origen no está disponible
   */
  List<Order> execute(OrderSearchCriteria criteria);
}
