package com.liverpool.customer.domain;

/**
 * Identificador del cliente, el mismo {@code userId} que usa el servicio de pedidos.
 *
 * <p>No se exige formato UUID: el servicio externo tiene identificadores que no lo son.
 *
 * @param value el identificador, sin espacios al inicio ni al final
 */
public record UserId(String value) {

  private static final int MAX_LENGTH = 100;

  /**
   * Valida que el identificador no esté vacío ni sea demasiado largo.
   */
  public UserId {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("El userId es obligatorio");
    }
    value = value.trim();
    if (value.length() > MAX_LENGTH) {
      throw new IllegalArgumentException(
          "El userId no puede superar " + MAX_LENGTH + " caracteres");
    }
  }
}
