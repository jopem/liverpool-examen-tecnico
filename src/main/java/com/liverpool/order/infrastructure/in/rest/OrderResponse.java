package com.liverpool.order.infrastructure.in.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.liverpool.order.domain.Order;
import com.liverpool.order.domain.OrderItem;
import java.time.LocalDate;
import java.util.List;

/**
 * Respuesta REST de un pedido.
 *
 * @param orderRef número de pedido
 * @param userId identificador del usuario
 * @param salesChannel canal de venta ({@code online} o {@code physical})
 * @param estimatedDeliveryDate fecha estimada de entrega
 * @param storeName nombre de la tienda
 * @param items artículos del pedido
 */
public record OrderResponse(
    String orderRef,
    String userId,
    String salesChannel,
    LocalDate estimatedDeliveryDate,
    String storeName,
    List<ItemResponse> items) {

  /**
   * Convierte un pedido del dominio en su respuesta.
   *
   * @param order el pedido
   * @return la respuesta REST
   */
  public static OrderResponse from(Order order) {
    return new OrderResponse(order.orderRef(), order.userId(), order.salesChannel().value(),
        order.estimatedDeliveryDate(), order.storeName(),
        order.items().stream().map(ItemResponse::from).toList());
  }

  /**
   * Respuesta REST de un artículo. Los datos de detalle se omiten si no están disponibles.
   *
   * @param itemId código del producto
   * @param quantity cantidad
   * @param skuId identificador del SKU
   * @param displayName nombre del producto
   * @param deliveryStatus estatus de entrega
   * @param detailsAvailable si el artículo se resolvió en el servicio de items
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record ItemResponse(
      String itemId,
      Integer quantity,
      String skuId,
      String displayName,
      String deliveryStatus,
      boolean detailsAvailable) {

    static ItemResponse from(OrderItem item) {
      return new ItemResponse(item.itemId(), item.quantity(), item.skuId(), item.displayName(),
          item.deliveryStatus(), item.detailsAvailable());
    }
  }
}
