package com.liverpool.order.domain;

/**
 * Criterios de búsqueda de pedidos. Los valores nulos o en blanco se ignoran.
 *
 * @param text texto libre, buscado en {@code orderRef}, fecha estimada, tienda y nombre de los
 *     artículos
 * @param orderRef filtro por número de pedido
 * @param orderStatus filtro por fecha estimada de entrega, como texto ISO (por ejemplo
 *     {@code 2025-12})
 * @param storeName filtro por nombre de tienda
 */
public record OrderSearchCriteria(
    String text,
    String orderRef,
    String orderStatus,
    String storeName) {

  /**
   * Indica si no se indicó ningún criterio.
   *
   * @return {@code true} si todos los criterios están vacíos
   */
  public boolean isEmpty() {
    return isBlank(text) && isBlank(orderRef) && isBlank(orderStatus) && isBlank(storeName);
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
