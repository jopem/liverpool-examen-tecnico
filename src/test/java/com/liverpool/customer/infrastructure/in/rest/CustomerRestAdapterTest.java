package com.liverpool.customer.infrastructure.in.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.liverpool.customer.application.exception.ConcurrentCustomerModificationException;
import com.liverpool.customer.application.exception.CustomerAlreadyExistsException;
import com.liverpool.customer.application.exception.CustomerNotFoundException;
import com.liverpool.customer.application.exception.EmailAlreadyExistsException;
import com.liverpool.customer.application.port.in.CreateCustomerUseCase;
import com.liverpool.customer.application.port.in.GetCustomerUseCase;
import com.liverpool.customer.application.port.in.ManageDeliveryAddressesUseCase;
import com.liverpool.customer.application.port.in.UpdateCustomerUseCase;
import com.liverpool.customer.application.view.AddressView;
import com.liverpool.customer.application.view.CustomerView;
import com.liverpool.customer.domain.AddressNotFoundException;
import com.liverpool.customer.domain.Customer;
import com.liverpool.customer.domain.DeliveryAddress;
import com.liverpool.customer.domain.Email;
import com.liverpool.customer.domain.UserId;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Pruebas de {@link CustomerRestAdapter} y {@link CustomerRestExceptionHandler} con los casos
 * de uso simulados.
 */
class CustomerRestAdapterTest {

  private static final String VALID_CUSTOMER = "{\"userId\":\"u1\",\"firstName\":\"Juan\","
      + "\"paternalLastName\":\"García\",\"email\":\"juan@email.com\"}";
  private static final String VALID_ADDRESS = "{\"street\":\"Reforma\",\"exteriorNumber\":\"1\","
      + "\"neighborhood\":\"Centro\",\"zipCode\":\"01210\",\"city\":\"CDMX\",\"state\":\"CDMX\"}";

  private static final DeliveryAddress ADDRESS = new DeliveryAddress("a1", "Reforma", "1", null,
      "Centro", "01210", "CDMX", "CDMX", null);
  private static final Customer CUSTOMER = new Customer(new UserId("u1"), "Juan", "García",
      null, new Email("juan@email.com"), "a1", List.of(ADDRESS),
      Instant.parse("2026-10-17T10:00:00Z"), Instant.parse("2026-10-17T10:00:00Z"), 0L);
  private static final CustomerView VIEW = new CustomerView(CUSTOMER, List.of("100"));

  private CreateCustomerUseCase create;
  private GetCustomerUseCase getCustomer;
  private UpdateCustomerUseCase update;
  private ManageDeliveryAddressesUseCase addresses;
  private MockMvc mvc;

  @BeforeEach
  void setUp() {
    create = mock(CreateCustomerUseCase.class);
    getCustomer = mock(GetCustomerUseCase.class);
    update = mock(UpdateCustomerUseCase.class);
    addresses = mock(ManageDeliveryAddressesUseCase.class);
    mvc = MockMvcBuilders
        .standaloneSetup(new CustomerRestAdapter(create, getCustomer, update, addresses))
        .setControllerAdvice(new CustomerRestExceptionHandler())
        .build();
  }

  /** El alta responde 201 con la ubicación y los pedidos. */
  @Test
  void createReturns201() throws Exception {
    when(create.execute(any())).thenReturn(VIEW);

    mvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
            .content(VALID_CUSTOMER))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/v1/customers/u1"))
        .andExpect(jsonPath("$.userId").value("u1"))
        .andExpect(jsonPath("$.orders[0]").value("100"))
        .andExpect(jsonPath("$.deliveryAddresses[0].defaultAddress").value(true));
  }

  /** Un cuerpo inválido responde 400 con el detalle por campo en español. */
  @Test
  void invalidBodyIs400WithFieldErrors() throws Exception {
    mvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
            .content("{\"firstName\":\"\",\"email\":\"mal\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.firstName").value("es obligatorio"))
        .andExpect(jsonPath("$.errors.paternalLastName").value("es obligatorio"))
        .andExpect(jsonPath("$.errors.email").value("no tiene un formato válido"));
  }

  /** Un JSON roto responde 400. */
  @Test
  void brokenJsonIs400() throws Exception {
    mvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
            .content("{no es json"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Cuerpo inválido"));
  }

  /** Un dato que el dominio rechaza responde 400. */
  @Test
  void domainValidationIs400() throws Exception {
    when(create.execute(any())).thenThrow(new IllegalArgumentException("dato malo"));

    mvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
            .content(VALID_CUSTOMER))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("dato malo"));
  }

  /** Userid o correo repetidos, o modificación simultánea, responden 409. */
  @Test
  void conflictsAre409() throws Exception {
    when(create.execute(any()))
        .thenThrow(new CustomerAlreadyExistsException("u1"))
        .thenThrow(new EmailAlreadyExistsException("a@b.com"));
    when(update.execute(eq("u1"), any()))
        .thenThrow(new ConcurrentCustomerModificationException(new RuntimeException()));

    for (int i = 0; i < 2; i++) {
      mvc.perform(post("/api/v1/customers").contentType(MediaType.APPLICATION_JSON)
              .content(VALID_CUSTOMER))
          .andExpect(status().isConflict());
    }
    mvc.perform(put("/api/v1/customers/u1").contentType(MediaType.APPLICATION_JSON)
            .content(VALID_CUSTOMER))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.title").value("Conflicto"));
  }

  /** Consultar un cliente inexistente responde 404. */
  @Test
  void unknownCustomerIs404() throws Exception {
    when(getCustomer.execute("nadie")).thenThrow(new CustomerNotFoundException("nadie"));

    mvc.perform(get("/api/v1/customers/nadie"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("No existe el cliente nadie"));
  }

  /** Si los pedidos no están disponibles, el campo orders se omite. */
  @Test
  void ordersFieldIsOmittedWhenUnavailable() throws Exception {
    when(getCustomer.execute("u1")).thenReturn(new CustomerView(CUSTOMER, null));

    mvc.perform(get("/api/v1/customers/u1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value("u1"))
        .andExpect(jsonPath("$.orders").doesNotExist());
  }

  /** La actualización responde 200 con el cliente. */
  @Test
  void updateReturnsCustomer() throws Exception {
    when(update.execute(eq("u1"), any())).thenReturn(VIEW);

    mvc.perform(put("/api/v1/customers/u1").contentType(MediaType.APPLICATION_JSON)
            .content(VALID_CUSTOMER))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("juan@email.com"));
  }

  /** Agregar una dirección responde 201 con su ubicación. */
  @Test
  void addAddressReturns201() throws Exception {
    when(addresses.add(eq("u1"), any())).thenReturn(new AddressView(ADDRESS, true));

    mvc.perform(post("/api/v1/customers/u1/delivery-addresses")
            .contentType(MediaType.APPLICATION_JSON).content(VALID_ADDRESS))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/v1/customers/u1/delivery-addresses/a1"))
        .andExpect(jsonPath("$.addressId").value("a1"))
        .andExpect(jsonPath("$.zipCode").value("01210"));
  }

  /** Un código postal que no tiene 5 dígitos responde 400. */
  @Test
  void badZipCodeIs400() throws Exception {
    mvc.perform(post("/api/v1/customers/u1/delivery-addresses")
            .contentType(MediaType.APPLICATION_JSON)
            .content(VALID_ADDRESS.replace("01210", "1210")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.zipCode").value("debe tener 5 dígitos"));
  }

  /** Listar y consultar direcciones. */
  @Test
  void listAndGetAddresses() throws Exception {
    when(addresses.list("u1")).thenReturn(List.of(new AddressView(ADDRESS, true)));
    when(addresses.get("u1", "a1")).thenReturn(new AddressView(ADDRESS, true));
    when(addresses.get("u1", "zz")).thenThrow(new AddressNotFoundException("zz"));

    mvc.perform(get("/api/v1/customers/u1/delivery-addresses"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].addressId").value("a1"));
    mvc.perform(get("/api/v1/customers/u1/delivery-addresses/a1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.defaultAddress").value(true));
    mvc.perform(get("/api/v1/customers/u1/delivery-addresses/zz"))
        .andExpect(status().isNotFound());
  }

  /** Reemplazar, eliminar y fijar predeterminada. */
  @Test
  void replaceRemoveAndSetDefault() throws Exception {
    when(addresses.replace(eq("u1"), eq("a1"), any()))
        .thenReturn(new AddressView(ADDRESS, true));

    mvc.perform(put("/api/v1/customers/u1/delivery-addresses/a1")
            .contentType(MediaType.APPLICATION_JSON).content(VALID_ADDRESS))
        .andExpect(status().isOk());
    mvc.perform(delete("/api/v1/customers/u1/delivery-addresses/a1"))
        .andExpect(status().isNoContent());
    mvc.perform(put("/api/v1/customers/u1/delivery-addresses/a1/default"))
        .andExpect(status().isNoContent());
    verify(addresses).remove("u1", "a1");
    verify(addresses).setDefault("u1", "a1");
  }
}
