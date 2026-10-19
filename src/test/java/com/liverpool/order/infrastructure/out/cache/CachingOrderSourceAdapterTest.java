package com.liverpool.order.infrastructure.out.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.liverpool.order.application.exception.OrderSourceUnavailableException;
import com.liverpool.order.application.port.out.OrderSourcePort;
import com.liverpool.order.domain.Order;
import com.liverpool.order.domain.SalesChannel;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de {@link CachingOrderSourceAdapter} con un reloj controlado.
 */
class CachingOrderSourceAdapterTest {

  private static final Order ORDER = new Order("1", "u", SalesChannel.ONLINE, null, null, null);

  /** Origen que cuenta las llamadas y puede fallar. */
  private static final class CountingSource implements OrderSourcePort {
    private int calls;
    private boolean failing;

    @Override
    public List<Order> findAll() {
      calls++;
      if (failing) {
        throw new OrderSourceUnavailableException("caído", new RuntimeException());
      }
      return List.of(ORDER);
    }
  }

  /** Reloj que se mueve a mano. */
  private static final class MutableClock extends Clock {
    private Instant now = Instant.parse("2026-10-17T00:00:00Z");

    void advance(Duration duration) {
      now = now.plus(duration);
    }

    @Override
    public ZoneOffset getZone() {
      return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return this;
    }

    @Override
    public Instant instant() {
      return now;
    }
  }

  private final CountingSource source = new CountingSource();
  private final MutableClock clock = new MutableClock();
  private final CachingOrderSourceAdapter cache = new CachingOrderSourceAdapter(
      source, Duration.ofSeconds(60), Duration.ofMinutes(10), clock);

  /** Dentro de la vigencia no se vuelve a consultar el origen. */
  @Test
  void servesFromMemoryWithinTtl() {
    cache.findAll();
    clock.advance(Duration.ofSeconds(59));
    assertThat(cache.findAll()).containsExactly(ORDER);
    assertThat(source.calls).isEqualTo(1);
  }

  /** Vencida la vigencia se consulta de nuevo. */
  @Test
  void refreshesAfterTtl() {
    cache.findAll();
    clock.advance(Duration.ofSeconds(60));
    cache.findAll();
    assertThat(source.calls).isEqualTo(2);
  }

  /** Si el origen falla se sirve la última copia buena. */
  @Test
  void servesStaleCopyWhenSourceFails() {
    cache.findAll();
    source.failing = true;
    clock.advance(Duration.ofMinutes(5));
    assertThat(cache.findAll()).containsExactly(ORDER);
  }

  /** Pasado el límite de antigüedad el error se propaga. */
  @Test
  void failsWhenStaleCopyIsTooOld() {
    cache.findAll();
    source.failing = true;
    clock.advance(Duration.ofMinutes(10).plusSeconds(1));
    assertThatThrownBy(cache::findAll).isInstanceOf(OrderSourceUnavailableException.class);
  }

  /** Sin copia previa el error se propaga. */
  @Test
  void failsWhenThereIsNoCopy() {
    source.failing = true;
    assertThatThrownBy(cache::findAll).isInstanceOf(OrderSourceUnavailableException.class);
  }

  /** Con vigencia cero siempre se consulta el origen. */
  @Test
  void zeroTtlDisablesCache() {
    var noCache = new CachingOrderSourceAdapter(source, Duration.ZERO, Duration.ZERO, clock);
    noCache.findAll();
    noCache.findAll();
    assertThat(source.calls).isEqualTo(2);
  }
}
