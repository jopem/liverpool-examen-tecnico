package com.liverpool.order.domain;

import java.time.LocalDate;
import java.util.List;

/**
 * Pedido de solo lectura, tal como lo entrega el servicio externo.
 *
 * @param orderRef número de pedido
 * @param userId identificador del usuario que hizo el pedido
 * @param salesChannel canal de venta
 * @param estimatedDeliveryDate fecha estimada de entrega (el campo {@code orderStatus} del
 *     servicio externo)
 * @param storeName nombre de la tienda, tal como llega (puede traer espacios dobles)
 * @param items artículos del pedido; nunca es nulo
 */
public record Order(
    String orderRef,
    String userId,
    SalesChannel salesChannel,
    LocalDate estimatedDeliveryDate,
    String storeName,
    List<OrderItem> items) {

  /**
   * Valida los datos obligatorios y hace inmutable la lista de artículos.
   */
  public Order {
    if (orderRef == null || orderRef.isBlank()) {
      throw new IllegalArgumentException("El número de pedido (orderRef) es obligatorio");
    }
    if (userId == null || userId.isBlank()) {
      throw new IllegalArgumentException("El userId es obligatorio");
    }
    if (salesChannel == null) {
      throw new IllegalArgumentException("El canal de venta es obligatorio");
    }
    items = items == null ? List.of() : List.copyOf(items);
  }
}
