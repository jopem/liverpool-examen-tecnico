package com.liverpool.order.domain;

/**
 * Artículo de un pedido.
 *
 * <p>Un pedido puede referenciar artículos que no existen en el servicio de items; en ese caso
 * solo se conoce su {@code itemId} y {@code detailsAvailable} es {@code false}.
 *
 * @param itemId código del producto, con la forma {@code orderRef-skuId}
 * @param skuId identificador del SKU, o {@code null} si el detalle no está disponible
 * @param quantity cantidad pedida, o {@code null} si el detalle no está disponible
 * @param displayName nombre del producto, o {@code null} si el detalle no está disponible
 * @param deliveryStatus estatus de entrega, o {@code null} si el detalle no está disponible
 * @param detailsAvailable {@code true} si el artículo se resolvió en el servicio de items
 */
public record OrderItem(
    String itemId,
    String skuId,
    Integer quantity,
    String displayName,
    String deliveryStatus,
    boolean detailsAvailable) {

  /**
   * Valida que el artículo tenga código y, si el detalle está disponible, una cantidad válida.
   */
  public OrderItem {
    if (itemId == null || itemId.isBlank()) {
      throw new IllegalArgumentException("El código del producto (itemId) es obligatorio");
    }
    if (detailsAvailable && (quantity == null || quantity < 0)) {
      throw new IllegalArgumentException("La cantidad no puede ser nula ni negativa");
    }
  }

  /**
   * Crea un artículo cuyo detalle se resolvió en el servicio de items.
   *
   * @param itemId código del producto
   * @param skuId identificador del SKU
   * @param quantity cantidad pedida
   * @param displayName nombre del producto
   * @param deliveryStatus estatus de entrega
   * @return el artículo con {@code detailsAvailable} en {@code true}
   */
  public static OrderItem resolved(String itemId, String skuId, int quantity,
      String displayName, String deliveryStatus) {
    return new OrderItem(itemId, skuId, quantity, displayName, deliveryStatus, true);
  }

  /**
   * Crea un artículo del que solo se conoce el código.
   *
   * @param itemId código del producto
   * @return el artículo con {@code detailsAvailable} en {@code false}
   */
  public static OrderItem unresolved(String itemId) {
    return new OrderItem(itemId, null, null, null, null, false);
  }
}
