package com.liverpool.order.domain;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Normaliza texto para compararlo sin distinguir acentos, mayúsculas ni signos.
 */
public final class TextNormalizer {

  private TextNormalizer() {
  }

  /**
   * Quita acentos, pasa a minúsculas, reemplaza todo carácter que no sea letra o número por un
   * espacio y colapsa los espacios.
   *
   * @param text el texto original; puede ser nulo
   * @return el texto normalizado, o cadena vacía si el original es nulo
   */
  public static String normalize(String text) {
    if (text == null) {
      return "";
    }
    String withoutAccents = Normalizer.normalize(text, Normalizer.Form.NFD)
        .replaceAll("\\p{M}+", "");
    return withoutAccents.toLowerCase(Locale.ROOT)
        .replaceAll("[^\\p{L}\\p{N}]+", " ")
        .trim();
  }

  /**
   * Normaliza el texto y lo divide en palabras.
   *
   * @param text el texto original; puede ser nulo
   * @return las palabras normalizadas, o una lista vacía si no hay ninguna
   */
  public static List<String> words(String text) {
    String normalized = normalize(text);
    return normalized.isEmpty() ? List.of() : Arrays.asList(normalized.split(" "));
  }
}
