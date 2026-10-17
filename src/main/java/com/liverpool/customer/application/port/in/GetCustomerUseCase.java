package com.liverpool.customer.application.port.in;

import com.liverpool.customer.application.exception.CustomerNotFoundException;
import com.liverpool.customer.application.view.CustomerView;

/**
 * Puerto de entrada. Caso de uso: consultar un cliente con sus pedidos.
 */
public interface GetCustomerUseCase {

  /**
   * Consulta el cliente.
   *
   * @param userId identificador
   * @return el cliente con sus pedidos
   * @throws CustomerNotFoundException si no existe
   */
  CustomerView execute(String userId);
}
