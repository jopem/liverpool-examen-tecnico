package com.liverpool.order.infrastructure.in.rest;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.liverpool.order.application.exception.EmptySearchCriteriaException;
import com.liverpool.order.application.exception.OrderNotFoundException;
import com.liverpool.order.application.exception.OrderSourceUnavailableException;
import com.liverpool.order.application.port.in.GetOrderUseCase;
import com.liverpool.order.application.port.in.SearchOrdersUseCase;
import com.liverpool.order.domain.Order;
import com.liverpool.order.domain.OrderItem;
import com.liverpool.order.domain.SalesChannel;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Pruebas de {@link OrderRestAdapter} y {@link OrderRestExceptionHandler} con los casos de uso
 * simulados.
 */
class OrderRestAdapterTest {

  private static final Order ORDER = new Order("3010091676", "u1", SalesChannel.ONLINE,
      LocalDate.of(2025, 12, 6), "L  SANTA FE",
      List.of(OrderItem.resolved("3010091676-1", "1", 3, "Pantalón Levi´s", "Compra en línea"),
          OrderItem.unresolved("3010091676-2")));

  private SearchOrdersUseCase search;
  private GetOrderUseCase getOrder;
  private MockMvc mvc;

  @BeforeEach
  void setUp() {
    search = mock(SearchOrdersUseCase.class);
    getOrder = mock(GetOrderUseCase.class);
    mvc = MockMvcBuilders.standaloneSetup(new OrderRestAdapter(search, getOrder))
        .setControllerAdvice(new OrderRestExceptionHandler())
        .build();
  }

  /** La búsqueda devuelve los pedidos con la forma del contrato. */
  @Test
  void searchReturnsOrders() throws Exception {
    when(search.execute(any())).thenReturn(List.of(ORDER));

    mvc.perform(get("/api/v1/orders/search").param("q", "pantalon"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].orderRef").value("3010091676"))
        .andExpect(jsonPath("$[0].salesChannel").value("online"))
        .andExpect(jsonPath("$[0].estimatedDeliveryDate").value("2025-12-06"))
        .andExpect(jsonPath("$[0].items[0].itemId").value("3010091676-1"))
        .andExpect(jsonPath("$[0].items[0].quantity").value(3))
        .andExpect(jsonPath("$[0].items[0].detailsAvailable").value(true));
  }

  /** Un artículo sin detalle muestra solo su código. */
  @Test
  void unresolvedItemShowsOnlyItemId() throws Exception {
    when(getOrder.execute("3010091676")).thenReturn(ORDER);

    mvc.perform(get("/api/v1/orders/3010091676"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[1].itemId").value("3010091676-2"))
        .andExpect(jsonPath("$.items[1].detailsAvailable").value(false))
        .andExpect(jsonPath("$.items[1].displayName").doesNotExist())
        .andExpect(jsonPath("$.items[1].quantity").doesNotExist());
  }

  /** Un pedido inexistente responde 404 en formato Problem Details. */
  @Test
  void unknownOrderIs404() throws Exception {
    when(getOrder.execute("1")).thenThrow(new OrderNotFoundException("1"));

    mvc.perform(get("/api/v1/orders/1"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value("Pedido no encontrado"))
        .andExpect(jsonPath("$.detail").value("No existe el pedido 1"));
  }

  /** Una búsqueda sin criterios responde 400. */
  @Test
  void searchWithoutCriteriaIs400() throws Exception {
    when(search.execute(any())).thenThrow(new EmptySearchCriteriaException());

    mvc.perform(get("/api/v1/orders/search"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Búsqueda sin criterios"));
  }

  /** Si el servicio externo no está disponible responde 503. */
  @Test
  void unavailableSourceIs503() throws Exception {
    when(search.execute(any()))
        .thenThrow(new OrderSourceUnavailableException("x", new RuntimeException()));

    mvc.perform(get("/api/v1/orders/search").param("q", "a"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.title").value("Servicio de pedidos no disponible"));
  }
}
