package com.liverpool.customer.application.port.in;

import com.liverpool.customer.application.command.CreateCustomerCommand;
import com.liverpool.customer.application.exception.CustomerAlreadyExistsException;
import com.liverpool.customer.application.exception.EmailAlreadyExistsException;
import com.liverpool.customer.application.view.CustomerView;

/**
 * Puerto de entrada. Caso de uso: dar de alta un cliente.
 */
public interface CreateCustomerUseCase {

  /**
   * Crea el cliente. Si no se indica {@code userId}, se genera un UUID.
   *
   * @param command datos del cliente
   * @return el cliente creado con sus pedidos
   * @throws IllegalArgumentException si algún dato es inválido
   * @throws CustomerAlreadyExistsException si el userId ya existe
   * @throws EmailAlreadyExistsException si el correo ya lo usa otro cliente
   */
  CustomerView execute(CreateCustomerCommand command);
}
