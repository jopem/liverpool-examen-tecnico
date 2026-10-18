package com.liverpool.order.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Pruebas de {@link TextNormalizer}.
 */
class TextNormalizerTest {

  /** Quita acentos y pasa a minúsculas. */
  @Test
  void removesAccentsAndLowercases() {
    assertThat(TextNormalizer.normalize("Pantalón CAFÉ")).isEqualTo("pantalon cafe");
  }

  /** Colapsa los espacios dobles de los datos reales ("L  SANTA FE"). */
  @Test
  void collapsesSpaces() {
    assertThat(TextNormalizer.normalize("L  SANTA FE")).isEqualTo("l santa fe");
  }

  /** Trata el acento suelto y las comas como separadores. */
  @Test
  void replacesPunctuationWithSpace() {
    assertThat(TextNormalizer.normalize("Levi´s, azul")).isEqualTo("levi s azul");
  }

  /** Conserva los números y divide las fechas en partes. */
  @Test
  void keepsDigitsAndSplitsDates() {
    assertThat(TextNormalizer.words("2025-12-06")).containsExactly("2025", "12", "06");
  }

  /** Un texto nulo o sin letras ni números no produce palabras. */
  @Test
  void nullOrSymbolsOnlyHaveNoWords() {
    assertThat(TextNormalizer.normalize(null)).isEmpty();
    assertThat(TextNormalizer.words(null)).isEmpty();
    assertThat(TextNormalizer.words(" ,, ")).isEmpty();
  }
}
