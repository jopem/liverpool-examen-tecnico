package com.liverpool.order.infrastructure.config;

import com.liverpool.order.application.port.in.FindOrderRefsByUserUseCase;
import com.liverpool.order.application.port.in.GetOrderUseCase;
import com.liverpool.order.application.port.in.SearchOrdersUseCase;
import com.liverpool.order.application.port.out.OrderSourcePort;
import com.liverpool.order.application.usecase.FindOrderRefsByUserHandler;
import com.liverpool.order.application.usecase.GetOrderHandler;
import com.liverpool.order.application.usecase.SearchOrdersHandler;
import com.liverpool.order.infrastructure.out.cache.CachingOrderSourceAdapter;
import com.liverpool.order.infrastructure.out.http.MockApiOrderSourceAdapter;
import java.net.http.HttpClient;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Ensambla el módulo de pedidos: el adaptador HTTP, su caché y los casos de uso.
 */
@Configuration
@EnableConfigurationProperties(OrdersProperties.class)
public class OrderModuleConfiguration {

  /**
   * Crea el origen de pedidos: el adaptador HTTP envuelto en la caché.
   *
   * @param properties configuración del módulo
   * @return el puerto de salida listo para usar
   */
  @Bean
  OrderSourcePort orderSourcePort(OrdersProperties properties) {
    HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(properties.source().timeout())
        .build();
    JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
    factory.setReadTimeout(properties.source().timeout());
    RestClient client = RestClient.builder()
        .baseUrl(properties.source().baseUrl())
        .requestFactory(factory)
        .build();
    return new CachingOrderSourceAdapter(new MockApiOrderSourceAdapter(client),
        properties.cache().ttl(), properties.cache().maxStale(), Clock.systemUTC());
  }

  /**
   * Crea el caso de uso de búsqueda.
   *
   * @param source origen de pedidos
   * @return el caso de uso
   */
  @Bean
  SearchOrdersUseCase searchOrdersUseCase(OrderSourcePort source) {
    return new SearchOrdersHandler(source);
  }

  /**
   * Crea el caso de uso de detalle.
   *
   * @param source origen de pedidos
   * @return el caso de uso
   */
  @Bean
  GetOrderUseCase getOrderUseCase(OrderSourcePort source) {
    return new GetOrderHandler(source);
  }

  /**
   * Crea el caso de uso que consulta el módulo de clientes.
   *
   * @param source origen de pedidos
   * @return el caso de uso
   */
  @Bean
  FindOrderRefsByUserUseCase findOrderRefsByUserUseCase(OrderSourcePort source) {
    return new FindOrderRefsByUserHandler(source);
  }
}
