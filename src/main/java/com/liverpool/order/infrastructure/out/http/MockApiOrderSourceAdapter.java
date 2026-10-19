package com.liverpool.order.infrastructure.out.http;

import com.liverpool.order.application.exception.OrderSourceUnavailableException;
import com.liverpool.order.application.port.out.OrderSourcePort;
import com.liverpool.order.domain.Order;
import com.liverpool.order.domain.OrderItem;
import com.liverpool.order.domain.SalesChannel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Adaptador de salida que arma los pedidos con los servicios {@code /pedidos} e {@code /items}.
 *
 * <p>Solo lee: nunca escribe en el servicio externo. Un registro que no se puede interpretar se
 * omite con una advertencia, para que un dato sucio no deje sin servicio al resto.
 */
public class MockApiOrderSourceAdapter implements OrderSourcePort {

  private static final Logger LOG = LoggerFactory.getLogger(MockApiOrderSourceAdapter.class);

  private final RestClient client;

  /**
   * Crea el adaptador.
   *
   * @param client cliente HTTP con la URL base del servicio externo
   */
  public MockApiOrderSourceAdapter(RestClient client) {
    this.client = client;
  }

  @Override
  public List<Order> findAll() {
    try {
      List<PedidoDto> pedidos = fetch("/pedidos", new ParameterizedTypeReference<>() { });
      List<ItemDto> items = fetch("/items", new ParameterizedTypeReference<>() { });
      return assemble(pedidos, items);
    } catch (RestClientException e) {
      throw new OrderSourceUnavailableException(
          "El servicio de pedidos no está disponible", e);
    }
  }

  /** El servicio responde 404 con el texto "Not found" cuando no hay registros. */
  private <T> List<T> fetch(String path, ParameterizedTypeReference<List<T>> type) {
    try {
      List<T> body = client.get().uri(path).retrieve().body(type);
      return body == null ? List.of() : body;
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
        return List.of();
      }
      throw e;
    }
  }

  private List<Order> assemble(List<PedidoDto> pedidos, List<ItemDto> items) {
    Map<String, ItemDto> itemsById = new HashMap<>();
    for (ItemDto item : items) {
      itemsById.put(item.itemId(), item);
    }
    List<Order> orders = new ArrayList<>();
    for (PedidoDto pedido : pedidos) {
      try {
        orders.add(toOrder(pedido, itemsById));
      } catch (RuntimeException e) {
        LOG.warn("Se omite el pedido {} por datos inválidos: {}", pedido.orderRef(),
            e.getMessage());
      }
    }
    return orders;
  }

  private Order toOrder(PedidoDto pedido, Map<String, ItemDto> itemsById) {
    List<OrderItem> orderItems = new ArrayList<>();
    for (String itemId : pedido.items() == null ? List.<String>of() : pedido.items()) {
      ItemDto item = itemsById.get(itemId);
      if (item == null) {
        LOG.warn("El pedido {} referencia el item {}, que no existe en /items",
            pedido.orderRef(), itemId);
        orderItems.add(OrderItem.unresolved(itemId));
      } else {
        orderItems.add(OrderItem.resolved(item.itemId(), item.skuId(),
            item.quantity() == null ? 0 : item.quantity(), item.displayName(),
            item.deliveryStatus()));
      }
    }
    return new Order(pedido.orderRef(), pedido.userId(), SalesChannel.fromValue(pedido.canal()),
        LocalDate.parse(pedido.orderStatus()), pedido.storeName(), orderItems);
  }
}
