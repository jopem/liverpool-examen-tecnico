package com.liverpool.customer.application.port.in;

import com.liverpool.customer.application.command.UpdateCustomerCommand;
import com.liverpool.customer.application.exception.ConcurrentCustomerModificationException;
import com.liverpool.customer.application.exception.CustomerNotFoundException;
import com.liverpool.customer.application.exception.EmailAlreadyExistsException;
import com.liverpool.customer.application.view.CustomerView;

/**
 * Puerto de entrada. Caso de uso: actualizar los datos personales de un cliente.
 */
public interface UpdateCustomerUseCase {

  /**
   * Reemplaza nombre, apellidos y correo; no toca las direcciones.
   *
   * @param userId identificador del cliente
   * @param command datos nuevos
   * @return el cliente actualizado con sus pedidos
   * @throws CustomerNotFoundException si no existe
   * @throws IllegalArgumentException si algún dato es inválido
   * @throws EmailAlreadyExistsException si otro cliente ya usa el correo
   * @throws ConcurrentCustomerModificationException si el cliente cambió mientras se actualizaba
   */
  CustomerView execute(String userId, UpdateCustomerCommand command);
}
