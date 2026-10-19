package com.liverpool.order.infrastructure.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuración del módulo de pedidos (prefijo {@code orders}).
 *
 * @param source datos de conexión al servicio externo
 * @param cache vigencia de la copia en memoria
 */
@ConfigurationProperties(prefix = "orders")
public record OrdersProperties(Source source, Cache cache) {

  /**
   * Conexión al servicio externo de pedidos e items.
   *
   * @param baseUrl URL base, sin barra final (se le añade {@code /pedidos} y {@code /items})
   * @param timeout tiempo máximo de conexión y de lectura
   */
  public record Source(
      @DefaultValue("https://6994a4eab081bc23e9c0f61e.mockapi.io/api/v1") String baseUrl,
      @DefaultValue("5s") Duration timeout) {
  }

  /**
   * Vigencia de la copia en memoria.
   *
   * @param ttl tiempo antes de volver a consultar el servicio externo; 0 desactiva la copia
   * @param maxStale antigüedad máxima de la copia que se sirve si el servicio falla
   */
  public record Cache(
      @DefaultValue("60s") Duration ttl,
      @DefaultValue("10m") Duration maxStale) {
  }
}
