package com.liverpool.customer.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.liverpool.customer.domain.DeliveryAddress;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de {@link AddressData}.
 */
class AddressDataTest {

  /** Convierte los datos en una dirección del dominio con el identificador indicado. */
  @Test
  void buildsDomainAddressWithGivenId() {
    AddressData data = new AddressData("Reforma", "1", " ", "Centro", "01210", "CDMX", "CDMX",
        null);

    DeliveryAddress address = data.toAddress("a1");

    assertThat(address.addressId()).isEqualTo("a1");
    assertThat(address.street()).isEqualTo("Reforma");
    assertThat(address.interiorNumber()).isNull();
  }

  /** Un dato inválido se rechaza al construir la dirección. */
  @Test
  void rejectsInvalidData() {
    AddressData data = new AddressData("Reforma", "1", null, "Centro", "12", "CDMX", "CDMX",
        null);

    assertThatThrownBy(() -> data.toAddress("a1")).isInstanceOf(IllegalArgumentException.class);
  }
}
