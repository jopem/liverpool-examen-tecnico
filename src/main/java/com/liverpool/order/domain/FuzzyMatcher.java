package com.liverpool.order.domain;

/**
 * Compara dos palabras ya normalizadas, con tolerancia a errores mínimos de ortografía.
 *
 * <p>La tolerancia depende de la longitud de la palabra buscada: hasta 3 letras, ninguna; de 4 a
 * 7, una letra; de 8 o más, dos letras. Las palabras con dígitos (números de pedido, fechas) no
 * admiten errores. Los umbrales son parte del comportamiento de la búsqueda y no se configuran.
 */
public final class FuzzyMatcher {

  private static final int SHORT_WORD_MAX = 3;
  private static final int MEDIUM_WORD_MAX = 7;

  private FuzzyMatcher() {
  }

  /**
   * Indica cuántos errores se toleran para una palabra buscada.
   *
   * @param word la palabra buscada, normalizada
   * @return 0 si contiene dígitos o tiene hasta 3 letras; 1 si tiene de 4 a 7; 2 si tiene más
   */
  public static int tolerance(String word) {
    if (containsDigit(word) || word.length() <= SHORT_WORD_MAX) {
      return 0;
    }
    return word.length() <= MEDIUM_WORD_MAX ? 1 : 2;
  }

  /**
   * Compara la palabra buscada con una palabra completa.
   *
   * @param query la palabra buscada, normalizada
   * @param target la palabra del pedido, normalizada
   * @return {@code true} si son iguales o difieren dentro de la tolerancia
   */
  public static boolean matchesWord(String query, String target) {
    return distance(query, target) <= tolerance(query);
  }

  /**
   * Compara la palabra buscada con el comienzo de una palabra, para la búsqueda mientras se
   * escribe.
   *
   * @param query la palabra buscada, normalizada
   * @param target la palabra del pedido, normalizada
   * @return {@code true} si {@code target} empieza con {@code query}, o si el comienzo de
   *     {@code target} difiere de {@code query} dentro de la tolerancia
   */
  public static boolean matchesPrefix(String query, String target) {
    if (target.startsWith(query)) {
      return true;
    }
    int tolerance = tolerance(query);
    int min = Math.max(1, query.length() - tolerance);
    int max = Math.min(target.length(), query.length() + tolerance);
    for (int length = min; length <= max; length++) {
      if (distance(query, target.substring(0, length)) <= tolerance) {
        return true;
      }
    }
    return false;
  }

  /**
   * Calcula la distancia de edición con transposición de letras contiguas (inserciones,
   * borrados, sustituciones e intercambios, cada uno con costo 1).
   */
  private static int distance(String a, String b) {
    int[][] d = new int[a.length() + 1][b.length() + 1];
    for (int i = 0; i <= a.length(); i++) {
      d[i][0] = i;
    }
    for (int j = 0; j <= b.length(); j++) {
      d[0][j] = j;
    }
    for (int i = 1; i <= a.length(); i++) {
      for (int j = 1; j <= b.length(); j++) {
        int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
        d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), d[i - 1][j - 1] + cost);
        if (i > 1 && j > 1 && a.charAt(i - 1) == b.charAt(j - 2)
            && a.charAt(i - 2) == b.charAt(j - 1)) {
          d[i][j] = Math.min(d[i][j], d[i - 2][j - 2] + 1);
        }
      }
    }
    return d[a.length()][b.length()];
  }

  private static boolean containsDigit(String word) {
    return word.chars().anyMatch(Character::isDigit);
  }
}
