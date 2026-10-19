package com.liverpool.order.infrastructure.out.http;

import java.util.List;

/**
 * Registro de {@code /pedidos} tal como lo entrega el servicio externo.
 *
 * @param orderRef número de pedido
 * @param userId identificador del usuario
 * @param canal canal de venta
 * @param orderStatus fecha estimada de entrega, en formato ISO
 * @param storeName nombre de la tienda
 * @param items códigos de los artículos
 */
record PedidoDto(
    String orderRef,
    String userId,
    String canal,
    String orderStatus,
    String storeName,
    List<String> items) {
}
