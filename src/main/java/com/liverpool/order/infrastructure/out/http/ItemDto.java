package com.liverpool.order.infrastructure.out.http;

/**
 * Registro de {@code /items} tal como lo entrega el servicio externo.
 *
 * @param itemId código del producto
 * @param skuId identificador del SKU
 * @param quantity cantidad
 * @param displayName nombre del producto
 * @param deliveryStatus estatus de entrega
 */
record ItemDto(
    String itemId,
    String skuId,
    Integer quantity,
    String displayName,
    String deliveryStatus) {
}
