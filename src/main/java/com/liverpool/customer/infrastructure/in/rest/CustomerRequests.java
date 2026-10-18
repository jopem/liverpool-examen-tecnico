package com.liverpool.customer.infrastructure.in.rest;

import com.liverpool.customer.application.command.AddressData;
import com.liverpool.customer.application.command.CreateCustomerCommand;
import com.liverpool.customer.application.command.UpdateCustomerCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Cuerpos de las peticiones REST de clientes y direcciones.
 */
public final class CustomerRequests {

  private CustomerRequests() {
  }

  /**
   * Alta de un cliente.
   *
   * @param userId identificador opcional; si se omite, el sistema genera un UUID
   * @param firstName nombre
   * @param paternalLastName apellido paterno
   * @param maternalLastName apellido materno, opcional
   * @param email correo electrónico
   */
  public record CreateCustomer(
      String userId,
      @NotBlank(message = "es obligatorio") String firstName,
      @NotBlank(message = "es obligatorio") String paternalLastName,
      String maternalLastName,
      @NotBlank(message = "es obligatorio")
      @Email(message = "no tiene un formato válido") String email) {

    CreateCustomerCommand toCommand() {
      return new CreateCustomerCommand(userId, firstName, paternalLastName, maternalLastName,
          email);
    }
  }

  /**
   * Actualización de los datos personales de un cliente.
   *
   * @param firstName nombre
   * @param paternalLastName apellido paterno
   * @param maternalLastName apellido materno, opcional (si se omite, se borra)
   * @param email correo electrónico
   */
  public record UpdateCustomer(
      @NotBlank(message = "es obligatorio") String firstName,
      @NotBlank(message = "es obligatorio") String paternalLastName,
      String maternalLastName,
      @NotBlank(message = "es obligatorio")
      @Email(message = "no tiene un formato válido") String email) {

    UpdateCustomerCommand toCommand() {
      return new UpdateCustomerCommand(firstName, paternalLastName, maternalLastName, email);
    }
  }

  /**
   * Datos de una dirección de entrega.
   *
   * @param street calle
   * @param exteriorNumber número exterior
   * @param interiorNumber número interior, opcional
   * @param neighborhood colonia
   * @param zipCode código postal de 5 dígitos
   * @param city ciudad
   * @param state estado
   * @param references referencias, opcional
   */
  public record Address(
      @NotBlank(message = "es obligatorio") String street,
      @NotBlank(message = "es obligatorio") String exteriorNumber,
      String interiorNumber,
      @NotBlank(message = "es obligatorio") String neighborhood,
      @NotBlank(message = "es obligatorio")
      @Pattern(regexp = "\\d{5}", message = "debe tener 5 dígitos") String zipCode,
      @NotBlank(message = "es obligatorio") String city,
      @NotBlank(message = "es obligatorio") String state,
      String references) {

    AddressData toData() {
      return new AddressData(street, exteriorNumber, interiorNumber, neighborhood, zipCode,
          city, state, references);
    }
  }
}
