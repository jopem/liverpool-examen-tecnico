package com.liverpool.customer.application.port.out;

import com.liverpool.customer.application.exception.ConcurrentCustomerModificationException;
import com.liverpool.customer.application.exception.CustomerAlreadyExistsException;
import com.liverpool.customer.application.exception.EmailAlreadyExistsException;
import com.liverpool.customer.domain.Customer;
import com.liverpool.customer.domain.Email;
import com.liverpool.customer.domain.UserId;
import java.util.Optional;

/**
 * Puerto de salida: almacén de clientes.
 */
public interface CustomerRepositoryPort {

  /**
   * Busca un cliente por su identificador.
   *
   * @param userId identificador
   * @return el cliente, si existe
   */
  Optional<Customer> findById(UserId userId);

  /**
   * Busca un cliente por su correo.
   *
   * @param email correo ya normalizado
   * @return el cliente, si existe
   */
  Optional<Customer> findByEmail(Email email);

  /**
   * Guarda un cliente nuevo (sin versión) o actualiza uno existente.
   *
   * @param customer el cliente
   * @return el cliente guardado, con su versión actualizada
   * @throws CustomerAlreadyExistsException si ya hay un cliente con ese userId
   * @throws EmailAlreadyExistsException si otro cliente ya usa ese correo
   * @throws ConcurrentCustomerModificationException si el cliente cambió desde que se leyó
   */
  Customer save(Customer customer);
}
