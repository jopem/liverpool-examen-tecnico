package com.liverpool.order.domain;

import java.util.Locale;

/**
 * Canal de venta por el que se hizo un pedido.
 */
public enum SalesChannel {

  /** Pedido hecho desde la aplicación o el sitio web. */
  ONLINE("online"),

  /** Pedido hecho en una tienda física. */
  PHYSICAL("physical");

  private final String value;

  SalesChannel(String value) {
    this.value = value;
  }

  /**
   * Devuelve el valor tal como lo entrega el servicio externo.
   *
   * @return el valor en minúsculas, por ejemplo {@code online}
   */
  public String value() {
    return value;
  }

  /**
   * Obtiene el canal a partir del valor del servicio externo, sin distinguir mayúsculas.
   *
   * @param value el valor recibido, por ejemplo {@code online}
   * @return el canal correspondiente
   * @throws IllegalArgumentException si el valor es nulo o no corresponde a ningún canal
   */
  public static SalesChannel fromValue(String value) {
    if (value != null) {
      String normalized = value.trim().toLowerCase(Locale.ROOT);
      for (SalesChannel channel : values()) {
        if (channel.value.equals(normalized)) {
          return channel;
        }
      }
    }
    throw new IllegalArgumentException("Canal de venta desconocido: " + value);
  }
}
