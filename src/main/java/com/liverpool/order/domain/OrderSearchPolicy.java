package com.liverpool.order.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Define en qué campos se busca un pedido y cómo se combinan los criterios.
 *
 * <p>Cada palabra del criterio debe coincidir con alguna palabra del campo; la última se busca
 * por prefijo, porque se supone que la persona todavía está escribiendo. Todos los criterios
 * indicados deben cumplirse a la vez.
 */
public final class OrderSearchPolicy {

  private OrderSearchPolicy() {
  }

  /**
   * Indica si un pedido cumple todos los criterios indicados.
   *
   * @param order el pedido a evaluar
   * @param criteria los criterios; si están vacíos, el pedido siempre cumple
   * @return {@code true} si el pedido cumple todos los criterios
   */
  public static boolean matches(Order order, OrderSearchCriteria criteria) {
    return matchesWords(criteria.text(), freeTextWords(order))
        && matchesWords(criteria.orderRef(), TextNormalizer.words(order.orderRef()))
        && matchesWords(criteria.orderStatus(), dateWords(order))
        && matchesWords(criteria.storeName(), TextNormalizer.words(order.storeName()));
  }

  private static boolean matchesWords(String query, List<String> targets) {
    List<String> queryWords = TextNormalizer.words(query);
    for (int i = 0; i < queryWords.size(); i++) {
      String queryWord = queryWords.get(i);
      boolean isLast = i == queryWords.size() - 1;
      boolean found = targets.stream().anyMatch(target -> isLast
          ? FuzzyMatcher.matchesPrefix(queryWord, target)
          : FuzzyMatcher.matchesWord(queryWord, target));
      if (!found) {
        return false;
      }
    }
    return true;
  }

  private static List<String> freeTextWords(Order order) {
    List<String> words = new ArrayList<>();
    words.addAll(TextNormalizer.words(order.orderRef()));
    words.addAll(dateWords(order));
    words.addAll(TextNormalizer.words(order.storeName()));
    for (OrderItem item : order.items()) {
      words.addAll(TextNormalizer.words(item.displayName()));
    }
    return words;
  }

  private static List<String> dateWords(Order order) {
    return order.estimatedDeliveryDate() == null
        ? List.of()
        : TextNormalizer.words(order.estimatedDeliveryDate().toString());
  }
}
