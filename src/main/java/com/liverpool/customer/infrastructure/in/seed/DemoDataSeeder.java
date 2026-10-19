package com.liverpool.customer.infrastructure.in.seed;

import com.liverpool.customer.application.command.AddressData;
import com.liverpool.customer.application.command.CreateCustomerCommand;
import com.liverpool.customer.application.exception.CustomerAlreadyExistsException;
import com.liverpool.customer.application.exception.EmailAlreadyExistsException;
import com.liverpool.customer.application.port.in.CreateCustomerUseCase;
import com.liverpool.customer.application.port.in.ManageDeliveryAddressesUseCase;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Carga clientes ficticios al arrancar con el perfil {@code demo}. Los seis primeros usan los
 * {@code userId} que existen en el servicio de pedidos; el séptimo no tiene pedidos. Es
 * idempotente: omite los que ya existen.
 */
@Component
@Profile("demo")
public class DemoDataSeeder implements ApplicationRunner {

  private static final Logger LOG = LoggerFactory.getLogger(DemoDataSeeder.class);

  private static final AddressData SANTA_FE = new AddressData("Av. Santa Fe", "123", "4B",
      "Santa Fe", "01210", "Ciudad de México", "CDMX", "Frente al parque, portón negro");
  private static final AddressData REFORMA = new AddressData("Paseo de la Reforma", "250", null,
      "Juárez", "06600", "Ciudad de México", "CDMX", null);
  private static final AddressData MONTERREY = new AddressData("Calle Hidalgo", "45-B", null,
      "Centro", "64000", "Monterrey", "Nuevo León", null);
  private static final AddressData TOLUCA = new AddressData("Av. Paseo Tollocan", "S/N", null,
      "Universidad", "50130", "Toluca", "Estado de México", "Junto a la plaza comercial");

  private final CreateCustomerUseCase createCustomer;
  private final ManageDeliveryAddressesUseCase addresses;

  /**
   * Crea el seeder.
   *
   * @param createCustomer puerto de entrada de alta de clientes
   * @param addresses puerto de entrada de direcciones
   */
  public DemoDataSeeder(CreateCustomerUseCase createCustomer,
      ManageDeliveryAddressesUseCase addresses) {
    this.createCustomer = createCustomer;
    this.addresses = addresses;
  }

  @Override
  public void run(ApplicationArguments args) {
    seed("75c97531-abf5-4524-8107-90aa48d08efc", "Juan", "García", "López",
        "juan.garcia@example.com", List.of(SANTA_FE, REFORMA));
    seed("aa2ae8bf-6b32-45dc-bb69-14e899bd8fed", "María", "Hernández", "Soto",
        "maria.hernandez@example.com", List.of(TOLUCA));
    seed("b446eb39-84fc-46e2-923b-91771d806c38", "Luis", "Martínez", null,
        "luis.martinez@example.com", List.of(MONTERREY));
    seed("93a5c099-3bbe-4718-8a0a-2e1bae0e08c8", "Ana", "Torres", "Ruiz",
        "ana.torres@example.com", List.of(REFORMA));
    seed("f6ec8262-7017-4532-a382-70c86e7844fc", "Carlos", "Ramírez", "Vega",
        "carlos.ramirez@example.com", List.of());
    seed("12665a1b2c3d4e5f6a7b8c9d0e", "Sofía", "Morales", "Díaz",
        "sofia.morales@example.com", List.of(SANTA_FE));
    seed("0f0f0f0f-0000-4000-8000-000000000007", "Pedro", "Sánchez", "Lara",
        "pedro.sanchez@example.com", List.of());
  }

  private void seed(String userId, String firstName, String paternalLastName,
      String maternalLastName, String email, List<AddressData> customerAddresses) {
    try {
      createCustomer.execute(new CreateCustomerCommand(userId, firstName, paternalLastName,
          maternalLastName, email));
      customerAddresses.forEach(address -> addresses.add(userId, address));
      LOG.info("Cliente de prueba creado: {}", userId);
    } catch (CustomerAlreadyExistsException | EmailAlreadyExistsException e) {
      LOG.info("Cliente de prueba omitido, ya existe: {}", userId);
    }
  }
}
