package com.liverpool.customer.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.liverpool.customer.application.command.AddressData;
import com.liverpool.customer.application.command.CreateCustomerCommand;
import com.liverpool.customer.application.command.UpdateCustomerCommand;
import com.liverpool.customer.application.exception.ConcurrentCustomerModificationException;
import com.liverpool.customer.application.exception.CustomerAlreadyExistsException;
import com.liverpool.customer.application.exception.CustomerNotFoundException;
import com.liverpool.customer.application.exception.EmailAlreadyExistsException;
import com.liverpool.customer.application.exception.OrdersUnavailableException;
import com.liverpool.customer.application.port.out.CustomerOrdersPort;
import com.liverpool.customer.application.view.AddressView;
import com.liverpool.customer.application.view.CustomerView;
import com.liverpool.customer.domain.AddressNotFoundException;
import com.liverpool.customer.domain.UserId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de los casos de uso de clientes con un almacén en memoria.
 */
class CustomerUseCasesTest {

  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-10-17T10:00:00Z"), ZoneOffset.UTC);

  private final InMemoryCustomerRepository repository = new InMemoryCustomerRepository();
  private boolean ordersDown;
  private final CustomerOrdersPort orders = userId -> {
    if (ordersDown) {
      throw new OrdersUnavailableException(new RuntimeException());
    }
    return "u1".equals(userId.value()) ? List.of("100", "200") : List.of();
  };

  private final CreateCustomerHandler create =
      new CreateCustomerHandler(repository, orders, CLOCK);
  private final GetCustomerHandler get = new GetCustomerHandler(repository, orders);
  private final UpdateCustomerHandler update =
      new UpdateCustomerHandler(repository, orders, CLOCK);
  private final ManageDeliveryAddressesHandler addresses =
      new ManageDeliveryAddressesHandler(repository, CLOCK);

  private static CreateCustomerCommand command(String userId, String email) {
    return new CreateCustomerCommand(userId, "Juan", "García", "López", email);
  }

  private static AddressData address(String street) {
    return new AddressData(street, "1", null, "Centro", "01210", "CDMX", "CDMX", null);
  }

  /** Con userId explícito se usa ese y se calculan sus pedidos. */
  @Test
  void createUsesGivenUserIdAndShowsOrders() {
    CustomerView view = create.execute(command("u1", "a@b.com"));
    assertThat(view.customer().userId().value()).isEqualTo("u1");
    assertThat(view.orders()).containsExactly("100", "200");
    assertThat(view.customer().createdAt()).isEqualTo(CLOCK.instant());
  }

  /** Sin userId se genera uno. */
  @Test
  void createGeneratesUserIdWhenMissing() {
    CustomerView view = create.execute(command(null, "a@b.com"));
    assertThat(view.customer().userId().value()).isNotBlank();
    assertThat(view.orders()).isEmpty();
  }

  /** Un userId o un correo repetidos son conflictos. */
  @Test
  void createRejectsDuplicates() {
    create.execute(command("u1", "a@b.com"));
    assertThatThrownBy(() -> create.execute(command("u1", "otro@b.com")))
        .isInstanceOf(CustomerAlreadyExistsException.class);
    assertThatThrownBy(() -> create.execute(command("u2", "A@B.com")))
        .isInstanceOf(EmailAlreadyExistsException.class);
  }

  /** Los datos inválidos no llegan a guardarse. */
  @Test
  void createRejectsInvalidData() {
    assertThatThrownBy(() -> create.execute(command("u1", "mal")))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> get.execute("u1")).isInstanceOf(CustomerNotFoundException.class);
  }

  /** Si no se pueden consultar los pedidos, el cliente se devuelve sin ellos. */
  @Test
  void customerIsReturnedWhenOrdersAreUnavailable() {
    create.execute(command("u1", "a@b.com"));
    ordersDown = true;
    CustomerView view = get.execute("u1");
    assertThat(view.customer().userId().value()).isEqualTo("u1");
    assertThat(view.orders()).isNull();
    assertThat(create.execute(command("u2", "c@d.com")).orders()).isNull();
  }

  /** La consulta de un cliente inexistente falla. */
  @Test
  void getFailsWhenMissing() {
    assertThatThrownBy(() -> get.execute("nadie")).isInstanceOf(CustomerNotFoundException.class);
  }

  /** La actualización cambia los datos y conserva las direcciones. */
  @Test
  void updateChangesPersonalDataKeepingAddresses() {
    create.execute(command("u1", "a@b.com"));
    addresses.add("u1", address("Reforma"));
    CustomerView view = update.execute("u1",
        new UpdateCustomerCommand("Pedro", "Pérez", null, "p@b.com"));
    assertThat(view.customer().firstName()).isEqualTo("Pedro");
    assertThat(view.customer().email().value()).isEqualTo("p@b.com");
    assertThat(view.customer().deliveryAddresses()).hasSize(1);
  }

  /** Se puede conservar el mismo correo, pero no tomar el de otro cliente. */
  @Test
  void updateChecksEmailOwnership() {
    create.execute(command("u1", "a@b.com"));
    create.execute(command("u2", "c@d.com"));
    update.execute("u1", new UpdateCustomerCommand("Juan", "García", null, "a@b.com"));
    assertThatThrownBy(() -> update.execute("u1",
        new UpdateCustomerCommand("Juan", "García", null, "c@d.com")))
        .isInstanceOf(EmailAlreadyExistsException.class);
    assertThatThrownBy(() -> update.execute("nadie",
        new UpdateCustomerCommand("J", "G", null, "x@y.com")))
        .isInstanceOf(CustomerNotFoundException.class);
  }

  /** Las direcciones se agregan, listan, consultan y reemplazan. */
  @Test
  void addressesCrud() {
    create.execute(command("u1", "a@b.com"));
    AddressView first = addresses.add("u1", address("Reforma"));
    AddressView second = addresses.add("u1", address("Insurgentes"));
    assertThat(first.defaultAddress()).isTrue();
    assertThat(second.defaultAddress()).isFalse();
    assertThat(addresses.list("u1")).hasSize(2);
    assertThat(addresses.get("u1", second.address().addressId()).address().street())
        .isEqualTo("Insurgentes");
    AddressView replaced = addresses.replace("u1", second.address().addressId(),
        address("Universidad"));
    assertThat(replaced.address().street()).isEqualTo("Universidad");
    assertThat(replaced.address().addressId()).isEqualTo(second.address().addressId());
  }

  /** Predeterminada y borrado siguen las reglas del dominio. */
  @Test
  void defaultAndRemoveRules() {
    create.execute(command("u1", "a@b.com"));
    String a1 = addresses.add("u1", address("A")).address().addressId();
    assertThat(addresses.get("u1", a1).defaultAddress()).isTrue();
    String a2 = addresses.add("u1", address("B")).address().addressId();
    addresses.setDefault("u1", a2);
    assertThat(addresses.get("u1", a2).defaultAddress()).isTrue();
    addresses.remove("u1", a2);
    assertThat(addresses.get("u1", a1).defaultAddress()).isTrue();
    assertThat(get.execute("u1").customer().deliveryAddresses()).hasSize(1);
  }

  /** Cliente o dirección inexistentes dan error. */
  @Test
  void addressOperationsFailForMissingTargets() {
    assertThatThrownBy(() -> addresses.list("nadie"))
        .isInstanceOf(CustomerNotFoundException.class);
    create.execute(command("u1", "a@b.com"));
    assertThatThrownBy(() -> addresses.get("u1", "zz"))
        .isInstanceOf(AddressNotFoundException.class);
    assertThatThrownBy(() -> addresses.remove("u1", "zz"))
        .isInstanceOf(AddressNotFoundException.class);
    assertThatThrownBy(() -> addresses.add("u1",
        new AddressData("c", "1", null, "col", "12", "x", "y", null)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  /** Una modificación sobre datos viejos se detecta por la versión. */
  @Test
  void staleUpdateIsDetected() {
    create.execute(command("u1", "a@b.com"));
    var stale = repository.findById(new UserId("u1")).orElseThrow();
    update.execute("u1", new UpdateCustomerCommand("Otro", "García", null, "a@b.com"));
    assertThatThrownBy(() -> repository.save(stale))
        .isInstanceOf(ConcurrentCustomerModificationException.class);
  }
}
