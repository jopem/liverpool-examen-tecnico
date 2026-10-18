package com.liverpool.order.application.usecase;

import com.liverpool.order.application.exception.EmptySearchCriteriaException;
import com.liverpool.order.application.port.in.SearchOrdersUseCase;
import com.liverpool.order.application.port.out.OrderSourcePort;
import com.liverpool.order.domain.Order;
import com.liverpool.order.domain.OrderSearchCriteria;
import com.liverpool.order.domain.OrderSearchPolicy;
import java.util.List;

/**
 * Implementación de {@link SearchOrdersUseCase}.
 */
public class SearchOrdersHandler implements SearchOrdersUseCase {

  private final OrderSourcePort source;

  /**
   * Crea el caso de uso.
   *
   * @param source origen de los pedidos
   */
  public SearchOrdersHandler(OrderSourcePort source) {
    this.source = source;
  }

  @Override
  public List<Order> execute(OrderSearchCriteria criteria) {
    if (criteria.isEmpty()) {
      throw new EmptySearchCriteriaException();
    }
    return source.findAll().stream()
        .filter(order -> OrderSearchPolicy.matches(order, criteria))
        .toList();
  }
}
