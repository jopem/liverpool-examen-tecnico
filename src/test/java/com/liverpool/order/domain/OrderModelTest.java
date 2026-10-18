package com.liverpool.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de {@link Order}, {@link OrderItem} y {@link SalesChannel}.
 */
class OrderModelTest {

  /** El canal se interpreta sin distinguir mayúsculas. */
  @Test
  void parsesSalesChannel() {
    assertThat(SalesChannel.fromValue("Online")).isEqualTo(SalesChannel.ONLINE);
    assertThat(SalesChannel.fromValue(" physical ")).isEqualTo(SalesChannel.PHYSICAL);
    assertThat(SalesChannel.PHYSICAL.value()).isEqualTo("physical");
  }

  /** Un canal desconocido o nulo se rechaza. */
  @Test
  void rejectsUnknownSalesChannel() {
    assertThatThrownBy(() -> SalesChannel.fromValue("telefono"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> SalesChannel.fromValue(null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  /** Un artículo sin detalle conserva solo el código. */
  @Test
  void unresolvedItemKeepsOnlyItemId() {
    OrderItem item = OrderItem.unresolved("632005897-1");
    assertThat(item.detailsAvailable()).isFalse();
    assertThat(item.displayName()).isNull();
    assertThat(item.quantity()).isNull();
  }

  /** Un artículo resuelto exige código y cantidad válida. */
  @Test
  void resolvedItemRequiresIdAndQuantity() {
    assertThat(OrderItem.resolved("a-1", "1", 2, "Zapato", "Compra en línea").detailsAvailable())
        .isTrue();
    assertThatThrownBy(() -> OrderItem.resolved(" ", "1", 2, "Zapato", "x"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> OrderItem.resolved("a-1", "1", -1, "Zapato", "x"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  /** El pedido exige número, usuario y canal. */
  @Test
  void orderRequiresMandatoryFields() {
    LocalDate date = LocalDate.of(2025, 12, 6);
    assertThatThrownBy(() -> new Order("", "u", SalesChannel.ONLINE, date, "t", List.of()))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Order("r", null, SalesChannel.ONLINE, date, "t", List.of()))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Order("r", "u", null, date, "t", List.of()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  /** La lista de artículos es una copia inmutable y nunca nula. */
  @Test
  void orderItemsAreImmutableCopy() {
    List<OrderItem> source = new ArrayList<>(List.of(OrderItem.unresolved("r-1")));
    Order order = new Order("r", "u", SalesChannel.ONLINE, null, null, source);
    source.clear();
    assertThat(order.items()).hasSize(1);
    assertThatThrownBy(() -> order.items().clear())
        .isInstanceOf(UnsupportedOperationException.class);
    assertThat(new Order("r", "u", SalesChannel.ONLINE, null, null, null).items()).isEmpty();
  }
}
