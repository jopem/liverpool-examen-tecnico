package com.liverpool.order.infrastructure.in.rest;

import com.liverpool.order.application.port.in.GetOrderUseCase;
import com.liverpool.order.application.port.in.SearchOrdersUseCase;
import com.liverpool.order.domain.OrderSearchCriteria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST para consultar y buscar pedidos (solo lectura).
 */
@RestController
@Tag(name = "Pedidos", description = "Consulta y búsqueda de pedidos (solo lectura)")
@RequestMapping("/api/v1/orders")
public class OrderRestAdapter {

  private final SearchOrdersUseCase searchOrders;
  private final GetOrderUseCase getOrder;

  /**
   * Crea el adaptador.
   *
   * @param searchOrders puerto de entrada de búsqueda
   * @param getOrder puerto de entrada de detalle
   */
  public OrderRestAdapter(SearchOrdersUseCase searchOrders, GetOrderUseCase getOrder) {
    this.searchOrders = searchOrders;
    this.getOrder = getOrder;
  }

  /**
   * Busca pedidos sin distinguir acentos, mayúsculas ni signos, y tolerando errores mínimos de
   * ortografía. Todos los parámetros son opcionales, pero debe enviarse al menos uno.
   *
   * @param q texto libre, buscado en número de pedido, fecha, tienda y nombre de los artículos
   * @param orderRef filtro por número de pedido
   * @param orderStatus filtro por fecha estimada de entrega (por ejemplo {@code 2025-12})
   * @param storeName filtro por nombre de tienda
   * @return los pedidos que cumplen todos los criterios
   */
  @Operation(summary = "Busca pedidos ignorando acentos, mayúsculas y errores mínimos")
  @GetMapping("/search")
  public List<OrderResponse> search(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String orderRef,
      @RequestParam(required = false) String orderStatus,
      @RequestParam(required = false) String storeName) {
    return searchOrders.execute(new OrderSearchCriteria(q, orderRef, orderStatus, storeName))
        .stream().map(OrderResponse::from).toList();
  }

  /**
   * Consulta el detalle de un pedido.
   *
   * @param orderRef número exacto del pedido
   * @return el pedido con sus artículos
   */
  @Operation(summary = "Consulta el detalle de un pedido por su número exacto")
  @GetMapping("/{orderRef}")
  public OrderResponse get(@PathVariable String orderRef) {
    return OrderResponse.from(getOrder.execute(orderRef));
  }
}
