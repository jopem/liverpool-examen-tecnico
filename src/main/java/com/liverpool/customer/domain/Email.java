package com.liverpool.customer.domain;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Correo electrónico válido, guardado en minúsculas.
 *
 * @param value la dirección, sin espacios y en minúsculas
 */
public record Email(String value) {

  private static final Pattern FORMAT = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

  /**
   * Normaliza la dirección y valida su formato.
   */
  public Email {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("El correo electrónico es obligatorio");
    }
    value = value.trim().toLowerCase(Locale.ROOT);
    if (!FORMAT.matcher(value).matches()) {
      throw new IllegalArgumentException("El correo electrónico no tiene un formato válido");
    }
  }
}
