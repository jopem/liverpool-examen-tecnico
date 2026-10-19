package com.liverpool.customer.infrastructure.out.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.liverpool.customer.application.exception.OrdersUnavailableException;
import com.liverpool.customer.domain.UserId;
import com.liverpool.order.application.exception.OrderSourceUnavailableException;
import com.liverpool.order.application.port.in.FindOrderRefsByUserUseCase;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de {@link OrderModuleOrdersAdapter}: consulta de pedidos al módulo {@code order}.
 */
class OrderModuleOrdersAdapterTest {

  /** Los pedidos se piden al módulo order y su fallo se traduce. */
  @Test
  void delegatesAndTranslatesFailure() {
    FindOrderRefsByUserUseCase useCase = mock(FindOrderRefsByUserUseCase.class);
    var ordersAdapter = new OrderModuleOrdersAdapter(useCase);
    when(useCase.execute("u1")).thenReturn(List.of("100"));
    when(useCase.execute("u2"))
        .thenThrow(new OrderSourceUnavailableException("x", new RuntimeException()));

    assertThat(ordersAdapter.findOrderRefs(new UserId("u1"))).containsExactly("100");
    assertThatThrownBy(() -> ordersAdapter.findOrderRefs(new UserId("u2")))
        .isInstanceOf(OrdersUnavailableException.class);
  }
}
