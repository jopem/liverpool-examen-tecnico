package com.liverpool.order.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.liverpool.order.domain.Order;
import com.liverpool.order.domain.SalesChannel;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de {@link FindOrderRefsByUserHandler}.
 */
class FindOrderRefsByUserHandlerTest {

  private static Order order(String ref, String userId) {
    return new Order(ref, userId, SalesChannel.ONLINE, null, null, null);
  }

  /** El filtro por usuario es exacto, aunque el servicio externo filtre en forma parcial. */
  @Test
  void filtersByExactUserId() {
    var useCase = new FindOrderRefsByUserHandler(new FakeOrderSource(List.of(
        order("1", "75c97531-abf5"), order("2", "75c97531"), order("3", "75c97531-abf5"))));
    assertThat(useCase.execute("75c97531-abf5")).containsExactly("1", "3");
    assertThat(useCase.execute("75c9")).isEmpty();
  }
}
