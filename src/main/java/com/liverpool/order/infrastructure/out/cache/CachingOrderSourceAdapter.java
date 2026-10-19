package com.liverpool.order.infrastructure.out.cache;

import com.liverpool.order.application.exception.OrderSourceUnavailableException;
import com.liverpool.order.application.port.out.OrderSourcePort;
import com.liverpool.order.domain.Order;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorador que guarda en memoria la última copia de los pedidos.
 *
 * <p>Mientras la copia tenga menos de {@code ttl} de antigüedad no se consulta el origen, porque
 * la búsqueda mientras se escribe pide los datos en cada tecla. Si el origen falla se sirve la
 * última copia buena, hasta {@code maxStale} de antigüedad; pasado ese límite, o si nunca hubo
 * copia, se propaga el error. El refresco es sincronizado: una sola consulta al origen a la vez.
 */
public class CachingOrderSourceAdapter implements OrderSourcePort {

  private static final Logger LOG = LoggerFactory.getLogger(CachingOrderSourceAdapter.class);

  private final OrderSourcePort delegate;
  private final Duration ttl;
  private final Duration maxStale;
  private final Clock clock;

  private List<Order> snapshot;
  private Instant loadedAt;

  /**
   * Crea el decorador.
   *
   * @param delegate origen real de los pedidos
   * @param ttl vigencia de la copia; cero la desactiva
   * @param maxStale antigüedad máxima de la copia que se sirve si el origen falla
   * @param clock reloj, inyectable para las pruebas
   */
  public CachingOrderSourceAdapter(OrderSourcePort delegate, Duration ttl, Duration maxStale,
      Clock clock) {
    this.delegate = delegate;
    this.ttl = ttl;
    this.maxStale = maxStale;
    this.clock = clock;
  }

  @Override
  public synchronized List<Order> findAll() {
    Instant now = clock.instant();
    if (snapshot != null && Duration.between(loadedAt, now).compareTo(ttl) < 0) {
      return snapshot;
    }
    try {
      snapshot = delegate.findAll();
      loadedAt = now;
      return snapshot;
    } catch (OrderSourceUnavailableException e) {
      if (snapshot != null && Duration.between(loadedAt, now).compareTo(maxStale) <= 0) {
        LOG.warn("El servicio de pedidos falló; se sirve la copia de hace {} s",
            Duration.between(loadedAt, now).toSeconds(), e);
        return snapshot;
      }
      throw e;
    }
  }
}
