package com.liverpool.order.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.liverpool.order.application.exception.EmptySearchCriteriaException;
import com.liverpool.order.application.exception.OrderNotFoundException;
import com.liverpool.order.application.exception.OrderSourceUnavailableException;
import com.liverpool.order.domain.Order;
import com.liverpool.order.domain.OrderItem;
import com.liverpool.order.domain.OrderSearchCriteria;
import com.liverpool.order.domain.SalesChannel;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de {@link SearchOrdersHandler} y {@link GetOrderHandler}.
 */
class OrderUseCasesTest {

  private static final Order SANTA_FE = new Order("3010091676", "u1", SalesChannel.ONLINE,
      LocalDate.of(2025, 12, 6), "L  SANTA FE",
      List.of(OrderItem.resolved("3010091676-1", "1", 3, "Pantalón Levi´s", "Compra en línea")));
  private static final Order TOLUCA = new Order("20251216366900020031", "u2",
      SalesChannel.PHYSICAL, LocalDate.of(2025, 12, 8), "Liverpool Galerías Toluca",
      List.of(OrderItem.resolved("2025-1", "1", 2, "Control Xbox one", "Pedido entregado")));

  private final FakeOrderSource source = new FakeOrderSource(List.of(SANTA_FE, TOLUCA));

  /** La búsqueda devuelve solo los pedidos que cumplen el criterio. */
  @Test
  void searchReturnsMatchingOrders() {
    var useCase = new SearchOrdersHandler(source);
    assertThat(useCase.execute(new OrderSearchCriteria("xbox", null, null, null)))
        .containsExactly(TOLUCA);
    assertThat(useCase.execute(new OrderSearchCriteria(null, null, "2025-12", null)))
        .containsExactly(SANTA_FE, TOLUCA);
    assertThat(useCase.execute(new OrderSearchCriteria("zapatos", null, null, null))).isEmpty();
  }

  /** Sin criterios la búsqueda se rechaza. */
  @Test
  void searchRejectsEmptyCriteria() {
    var useCase = new SearchOrdersHandler(source);
    assertThatThrownBy(() -> useCase.execute(new OrderSearchCriteria(" ", null, "", null)))
        .isInstanceOf(EmptySearchCriteriaException.class);
  }

  /** Si el origen falla, el error se propaga. */
  @Test
  void searchPropagatesSourceFailure() {
    var useCase = new SearchOrdersHandler(new FakeOrderSource());
    assertThatThrownBy(() -> useCase.execute(new OrderSearchCriteria("a", null, null, null)))
        .isInstanceOf(OrderSourceUnavailableException.class);
  }

  /** El detalle busca el número exacto, ignorando espacios alrededor. */
  @Test
  void getReturnsOrderByExactRef() {
    var useCase = new GetOrderHandler(source);
    assertThat(useCase.execute(" 3010091676 ")).isEqualTo(SANTA_FE);
  }

  /** Un número parcial o inexistente no es un pedido. */
  @Test
  void getFailsWhenRefIsNotExact() {
    var useCase = new GetOrderHandler(source);
    assertThatThrownBy(() -> useCase.execute("3010")).isInstanceOf(OrderNotFoundException.class);
    assertThatThrownBy(() -> useCase.execute(null)).isInstanceOf(OrderNotFoundException.class);
  }
}
