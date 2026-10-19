package com.liverpool.order.infrastructure.out.http;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.liverpool.order.application.exception.OrderSourceUnavailableException;
import com.liverpool.order.domain.Order;
import com.liverpool.order.domain.SalesChannel;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.client.RestClient;

/**
 * Pruebas de {@link MockApiOrderSourceAdapter} contra un servidor HTTP simulado.
 */
class MockApiOrderSourceAdapterTest {

  private static final String PEDIDOS = "["
      + "{\"orderRef\":\"3010091676\",\"userId\":\"75c97531\",\"canal\":\"online\","
      + "\"orderStatus\":\"2025-12-06\",\"storeName\":\"L  SANTA FE\","
      + "\"items\":[\"3010091676-1\",\"3010091676-2\"]},"
      + "{\"orderRef\":\"632005897\",\"userId\":\"75c97531\",\"canal\":\"physical\","
      + "\"orderStatus\":\"2026-01-15\",\"storeName\":\"Polanco\","
      + "\"items\":[\"632005897-1\"]},"
      + "{\"orderRef\":\"999\",\"userId\":\"x\",\"canal\":\"telefono\","
      + "\"orderStatus\":\"2026-01-15\",\"storeName\":\"Mala\",\"items\":[]},"
      + "{\"orderRef\":\"998\",\"userId\":\"x\",\"canal\":\"online\","
      + "\"orderStatus\":\"no-es-fecha\",\"storeName\":\"Mala\",\"items\":[]}]";

  private static final String ITEMS = "["
      + "{\"itemId\":\"3010091676-1\",\"skuId\":\"1\",\"quantity\":3,"
      + "\"displayName\":\"Pantalón Levi´s\",\"deliveryStatus\":\"Compra en línea\"},"
      + "{\"itemId\":\"3010091676-2\",\"skuId\":\"2\",\"quantity\":4,"
      + "\"displayName\":\"Vasos cristal 250ml\",\"deliveryStatus\":\"Compra en tienda\"}]";

  @RegisterExtension
  static WireMockExtension wireMock = WireMockExtension.newInstance().build();

  private MockApiOrderSourceAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new MockApiOrderSourceAdapter(
        RestClient.builder().baseUrl(wireMock.baseUrl()).build());
  }

  private static void stub(String path, int status, String body) {
    wireMock.stubFor(get(urlEqualTo(path)).willReturn(aResponse().withStatus(status)
        .withHeader("Content-Type", "application/json").withBody(body)));
  }

  /** Une pedidos e items y marca como no resueltos los items que no existen. */
  @Test
  void assemblesOrdersWithItems() {
    stub("/pedidos", 200, PEDIDOS);
    stub("/items", 200, ITEMS);

    List<Order> orders = adapter.findAll();

    assertThat(orders).extracting(Order::orderRef).containsExactly("3010091676", "632005897");
    Order first = orders.get(0);
    assertThat(first.salesChannel()).isEqualTo(SalesChannel.ONLINE);
    assertThat(first.estimatedDeliveryDate()).isEqualTo(LocalDate.of(2025, 12, 6));
    assertThat(first.items()).hasSize(2);
    assertThat(first.items().get(0).displayName()).isEqualTo("Pantalón Levi´s");
    assertThat(first.items().get(0).quantity()).isEqualTo(3);
    Order withoutDetail = orders.get(1);
    assertThat(withoutDetail.items()).hasSize(1);
    assertThat(withoutDetail.items().get(0).itemId()).isEqualTo("632005897-1");
    assertThat(withoutDetail.items().get(0).detailsAvailable()).isFalse();
  }

  /** Los registros con canal o fecha inválidos se omiten sin afectar al resto. */
  @Test
  void skipsInvalidRecords() {
    stub("/pedidos", 200, PEDIDOS);
    stub("/items", 200, ITEMS);
    assertThat(adapter.findAll()).extracting(Order::orderRef)
        .doesNotContain("999", "998");
  }

  /** El 404 "Not found" del servicio significa que no hay registros. */
  @Test
  void notFoundMeansEmpty() {
    stub("/pedidos", 404, "Not found");
    stub("/items", 404, "Not found");
    assertThat(adapter.findAll()).isEmpty();
  }

  /** Un error del servidor se informa como origen no disponible. */
  @Test
  void serverErrorMeansUnavailable() {
    stub("/pedidos", 500, "boom");
    stub("/items", 200, ITEMS);
    assertThatThrownBy(adapter::findAll).isInstanceOf(OrderSourceUnavailableException.class);
  }

  /** Solo se hacen lecturas: ninguna escritura al servicio externo. */
  @Test
  void onlyReads() {
    stub("/pedidos", 200, PEDIDOS);
    stub("/items", 200, ITEMS);
    adapter.findAll();
    wireMock.verify(1, getRequestedFor(urlEqualTo("/pedidos")));
    assertThat(wireMock.getAllServeEvents())
        .allSatisfy(event -> assertThat(event.getRequest().getMethod().getName())
            .isEqualTo("GET"));
  }
}
