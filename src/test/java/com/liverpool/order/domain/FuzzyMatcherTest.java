package com.liverpool.order.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Pruebas de {@link FuzzyMatcher}.
 */
class FuzzyMatcherTest {

  /** La tolerancia crece con la longitud y es cero con dígitos. */
  @Test
  void toleranceDependsOnLength() {
    assertThat(FuzzyMatcher.tolerance("sol")).isZero();
    assertThat(FuzzyMatcher.tolerance("sant")).isEqualTo(1);
    assertThat(FuzzyMatcher.tolerance("pantalon")).isEqualTo(2);
    assertThat(FuzzyMatcher.tolerance("30100916")).isZero();
  }

  /** Una palabra de 4 a 7 letras admite un error. */
  @Test
  void mediumWordAllowsOneTypo() {
    assertThat(FuzzyMatcher.matchesWord("santa", "santa")).isTrue();
    assertThat(FuzzyMatcher.matchesWord("sante", "santa")).isTrue();
    assertThat(FuzzyMatcher.matchesWord("sinte", "santa")).isFalse();
  }

  /** Una palabra de 8 letras o más admite dos errores. */
  @Test
  void longWordAllowsTwoTypos() {
    assertThat(FuzzyMatcher.matchesWord("pantalen", "pantalon")).isTrue();
    assertThat(FuzzyMatcher.matchesWord("pontalen", "pantalon")).isTrue();
    assertThat(FuzzyMatcher.matchesWord("pontelen", "pantalon")).isFalse();
  }

  /** Un intercambio de letras contiguas cuenta como un solo error. */
  @Test
  void transpositionCountsAsOneError() {
    assertThat(FuzzyMatcher.matchesWord("satna", "santa")).isTrue();
  }

  /** Una palabra corta debe ser exacta. */
  @Test
  void shortWordMustBeExact() {
    assertThat(FuzzyMatcher.matchesWord("fe", "fa")).isFalse();
    assertThat(FuzzyMatcher.matchesWord("fe", "fe")).isTrue();
  }

  /** Los números no admiten errores. */
  @Test
  void numbersAreNotFuzzy() {
    assertThat(FuzzyMatcher.matchesWord("3010091677", "3010091676")).isFalse();
    assertThat(FuzzyMatcher.matchesPrefix("3010091677", "3010091676")).isFalse();
  }

  /** El prefijo exacto coincide. */
  @Test
  void exactPrefixMatches() {
    assertThat(FuzzyMatcher.matchesPrefix("pant", "pantalon")).isTrue();
    assertThat(FuzzyMatcher.matchesPrefix("3010", "3010091676")).isTrue();
  }

  /** El prefijo tolera un error de ortografía. */
  @Test
  void prefixToleratesTypo() {
    assertThat(FuzzyMatcher.matchesPrefix("pontal", "pantalon")).isTrue();
    assertThat(FuzzyMatcher.matchesPrefix("zapatos", "pantalon")).isFalse();
  }

  /** Un prefijo más largo que la palabra no coincide. */
  @Test
  void longerQueryThanTargetDoesNotMatch() {
    assertThat(FuzzyMatcher.matchesPrefix("pantalones", "sol")).isFalse();
  }
}
