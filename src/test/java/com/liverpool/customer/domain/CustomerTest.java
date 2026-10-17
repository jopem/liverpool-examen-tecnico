package com.liverpool.customer.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de {@link Customer}, {@link DeliveryAddress}, {@link Email} y {@link UserId}.
 */
class CustomerTest {

  private static final Instant T0 = Instant.parse("2026-10-17T10:00:00Z");
  private static final Instant T1 = Instant.parse("2026-10-17T11:00:00Z");

  private static Customer customer() {
    return Customer.register(new UserId("u1"), " Juan ", "García", " ",
        new Email("Juan@Email.com "), T0);
  }

  private static DeliveryAddress address(String id) {
    return new DeliveryAddress(id, "Av. Santa Fe", "123", null, "Santa Fe", "01210", "CDMX",
        "CDMX", null);
  }

  /** El alta limpia los datos y no trae direcciones. */
  @Test
  void registerNormalizesData() {
    Customer customer = customer();
    assertThat(customer.firstName()).isEqualTo("Juan");
    assertThat(customer.maternalLastName()).isNull();
    assertThat(customer.email().value()).isEqualTo("juan@email.com");
    assertThat(customer.deliveryAddresses()).isEmpty();
    assertThat(customer.defaultAddressId()).isNull();
    assertThat(customer.createdAt()).isEqualTo(T0);
  }

  /** Nombre, primer apellido y correo son obligatorios. */
  @Test
  void rejectsMissingMandatoryData() {
    assertThatThrownBy(() -> Customer.register(new UserId("u"), "", "G", null,
        new Email("a@b.com"), T0)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> Customer.register(new UserId("u"), "J", null, null,
        new Email("a@b.com"), T0)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Email("no-es-correo"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new UserId("  ")).isInstanceOf(IllegalArgumentException.class);
  }

  /** El userId no exige formato UUID. */
  @Test
  void userIdAcceptsNonUuid() {
    assertThat(new UserId("12665a1b2c3d4e5f6a7b8c9d0e").value())
        .isEqualTo("12665a1b2c3d4e5f6a7b8c9d0e");
  }

  /** La primera dirección queda predeterminada y las siguientes no la cambian. */
  @Test
  void firstAddressBecomesDefault() {
    Customer withTwo = customer().addAddress(address("a1"), T1).addAddress(address("a2"), T1);
    assertThat(withTwo.defaultAddressId()).isEqualTo("a1");
    assertThat(withTwo.deliveryAddresses()).hasSize(2);
    assertThat(withTwo.updatedAt()).isEqualTo(T1);
  }

  /** Fijar una predeterminada inexistente falla. */
  @Test
  void setDefaultRequiresExistingAddress() {
    Customer withOne = customer().addAddress(address("a1"), T1);
    assertThat(withOne.addAddress(address("a2"), T1).setDefaultAddress("a2", T1)
        .defaultAddressId()).isEqualTo("a2");
    assertThatThrownBy(() -> withOne.setDefaultAddress("zz", T1))
        .isInstanceOf(AddressNotFoundException.class);
  }

  /** Al borrar la predeterminada se promueve la más antigua restante. */
  @Test
  void removingDefaultPromotesOldest() {
    Customer customer = customer().addAddress(address("a1"), T1)
        .addAddress(address("a2"), T1).addAddress(address("a3"), T1)
        .setDefaultAddress("a3", T1);
    Customer after = customer.removeAddress("a3", T1);
    assertThat(after.defaultAddressId()).isEqualTo("a1");
    assertThat(after.deliveryAddresses()).extracting(DeliveryAddress::addressId)
        .containsExactly("a1", "a2");
  }

  /** Borrar una dirección que no es la predeterminada no la cambia. */
  @Test
  void removingNonDefaultKeepsDefault() {
    Customer customer = customer().addAddress(address("a1"), T1).addAddress(address("a2"), T1);
    assertThat(customer.removeAddress("a2", T1).defaultAddressId()).isEqualTo("a1");
  }

  /** Al borrar la única dirección no queda predeterminada. */
  @Test
  void removingOnlyAddressClearsDefault() {
    Customer after = customer().addAddress(address("a1"), T1).removeAddress("a1", T1);
    assertThat(after.defaultAddressId()).isNull();
    assertThat(after.deliveryAddresses()).isEmpty();
  }

  /** Reemplazar una dirección conserva su posición y su predeterminada. */
  @Test
  void replaceKeepsPositionAndDefault() {
    Customer customer = customer().addAddress(address("a1"), T1).addAddress(address("a2"), T1);
    DeliveryAddress changed = new DeliveryAddress("a1", "Reforma", "9", "2B", "Centro", "64000",
        "Monterrey", "NL", "Portón negro");
    Customer after = customer.replaceAddress(changed, T1);
    assertThat(after.deliveryAddresses().get(0)).isEqualTo(changed);
    assertThat(after.defaultAddressId()).isEqualTo("a1");
    assertThatThrownBy(() -> customer.replaceAddress(address("zz"), T1))
        .isInstanceOf(AddressNotFoundException.class);
  }

  /** Actualizar datos personales conserva las direcciones, la fecha de alta y la versión. */
  @Test
  void updateKeepsAddressesAndCreation() {
    Customer saved = new Customer(new UserId("u1"), "J", "G", null, new Email("a@b.com"), "a1",
        List.of(address("a1")), T0, T0, 4L);
    Customer after = saved.updatePersonalData("Pedro", "Pérez", "Ruiz", new Email("p@b.com"), T1);
    assertThat(after.firstName()).isEqualTo("Pedro");
    assertThat(after.deliveryAddresses()).hasSize(1);
    assertThat(after.createdAt()).isEqualTo(T0);
    assertThat(after.updatedAt()).isEqualTo(T1);
    assertThat(after.version()).isEqualTo(4L);
  }

  /** La dirección valida sus campos y el código postal conserva el cero inicial. */
  @Test
  void addressValidatesFields() {
    assertThat(address("a").zipCode()).isEqualTo("01210");
    assertThatThrownBy(() -> new DeliveryAddress("a", "c", "1", null, "col", "1210", "x", "y",
        null)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new DeliveryAddress("a", " ", "1", null, "col", "01210", "x", "y",
        null)).isInstanceOf(IllegalArgumentException.class);
  }

  /** Una predeterminada que no existe entre las direcciones es inválida. */
  @Test
  void rejectsDanglingDefault() {
    assertThatThrownBy(() -> new Customer(new UserId("u"), "J", "G", null,
        new Email("a@b.com"), "zz", List.of(), T0, T0, null))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
