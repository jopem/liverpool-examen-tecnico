package com.liverpool.order.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de {@link OrderSearchPolicy} con datos parecidos a los del servicio externo.
 */
class OrderSearchPolicyTest {

  private static final Order SANTA_FE = new Order(
      "3010091676", "75c97531", SalesChannel.ONLINE, LocalDate.of(2025, 12, 6),
      "L  SANTA FE",
      List.of(
          OrderItem.resolved("3010091676-1", "1", 3, "Pantalón Levi´s", "Compra en línea"),
          OrderItem.unresolved("3010091676-2")));

  private static final Order NO_ITEMS = new Order(
      "632005897", "75c97531", SalesChannel.PHYSICAL, LocalDate.of(2026, 1, 15),
      "Polanco", List.of(OrderItem.unresolved("632005897-1")));

  private static OrderSearchCriteria text(String text) {
    return new OrderSearchCriteria(text, null, null, null);
  }

  /** Sin criterios, cualquier pedido cumple. */
  @Test
  void emptyCriteriaMatchesEverything() {
    OrderSearchCriteria empty = new OrderSearchCriteria(null, " ", "", null);
    assertThat(empty.isEmpty()).isTrue();
    assertThat(OrderSearchPolicy.matches(SANTA_FE, empty)).isTrue();
  }

  /** Busca sin acentos, sin mayúsculas y por prefijo. */
  @Test
  void freeTextIgnoresAccentsCaseAndMatchesPrefix() {
    assertThat(OrderSearchPolicy.matches(SANTA_FE, text("PANTALON"))).isTrue();
    assertThat(OrderSearchPolicy.matches(SANTA_FE, text("pant"))).isTrue();
    assertThat(OrderSearchPolicy.matches(SANTA_FE, text("3010"))).isTrue();
  }

  /** Tolera comas y espacios de más. */
  @Test
  void freeTextIgnoresCommasAndExtraSpaces() {
    assertThat(OrderSearchPolicy.matches(SANTA_FE, text("santa,  fe"))).isTrue();
  }

  /** Tolera errores mínimos de ortografía. */
  @Test
  void freeTextToleratesTypos() {
    assertThat(OrderSearchPolicy.matches(SANTA_FE, text("pontalon"))).isTrue();
    assertThat(OrderSearchPolicy.matches(SANTA_FE, text("sante"))).isTrue();
  }

  /** Todas las palabras deben coincidir. */
  @Test
  void everyWordMustMatch() {
    assertThat(OrderSearchPolicy.matches(SANTA_FE, text("santa pantalon"))).isTrue();
    assertThat(OrderSearchPolicy.matches(SANTA_FE, text("santa zapatos"))).isFalse();
  }

  /** Solo la última palabra se busca por prefijo. */
  @Test
  void onlyLastWordIsPrefix() {
    assertThat(OrderSearchPolicy.matches(SANTA_FE, text("san fe"))).isFalse();
    assertThat(OrderSearchPolicy.matches(SANTA_FE, text("santa f"))).isTrue();
  }

  /** La fecha estimada se busca como texto ISO por prefijo. */
  @Test
  void matchesEstimatedDateByPrefix() {
    assertThat(OrderSearchPolicy.matches(SANTA_FE, text("2025-12"))).isTrue();
    assertThat(OrderSearchPolicy.matches(SANTA_FE, text("2026"))).isFalse();
  }

  /** Un número de pedido equivocado no coincide. */
  @Test
  void wrongOrderRefDoesNotMatch() {
    assertThat(OrderSearchPolicy.matches(SANTA_FE, text("3010091677"))).isFalse();
  }

  /** Un pedido sin detalle de items se encuentra por los demás campos, no por producto. */
  @Test
  void orderWithoutItemDetailsMatchesOtherFieldsOnly() {
    assertThat(OrderSearchPolicy.matches(NO_ITEMS, text("polanco"))).isTrue();
    assertThat(OrderSearchPolicy.matches(NO_ITEMS, text("pantalon"))).isFalse();
  }

  /** Los filtros por campo se combinan y solo miran su propio campo. */
  @Test
  void fieldFiltersAreCombinedAndFieldSpecific() {
    assertThat(OrderSearchPolicy.matches(SANTA_FE,
        new OrderSearchCriteria(null, "3010", "2025-12", "santa"))).isTrue();
    assertThat(OrderSearchPolicy.matches(SANTA_FE,
        new OrderSearchCriteria(null, "3010", null, "polanco"))).isFalse();
    assertThat(OrderSearchPolicy.matches(SANTA_FE,
        new OrderSearchCriteria(null, null, null, "pantalon"))).isFalse();
  }

  /** El texto libre y los filtros deben cumplirse a la vez. */
  @Test
  void textAndFiltersMustAllHold() {
    assertThat(OrderSearchPolicy.matches(SANTA_FE,
        new OrderSearchCriteria("pantalon", null, null, "santa"))).isTrue();
    assertThat(OrderSearchPolicy.matches(SANTA_FE,
        new OrderSearchCriteria("zapatos", null, null, "santa"))).isFalse();
  }
}
