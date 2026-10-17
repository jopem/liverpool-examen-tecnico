package com.liverpool.customer.application.port.in;

import com.liverpool.customer.application.command.AddressData;
import com.liverpool.customer.application.exception.CustomerNotFoundException;
import com.liverpool.customer.application.view.AddressView;
import com.liverpool.customer.domain.AddressNotFoundException;
import java.util.List;

/**
 * Puerto de entrada. Casos de uso de las direcciones de entrega de un cliente: agregar, listar,
 * consultar, reemplazar, eliminar y fijar la predeterminada.
 */
public interface ManageDeliveryAddressesUseCase {

  /**
   * Agrega una dirección con un identificador generado. La primera queda como predeterminada.
   *
   * @param userId identificador del cliente
   * @param data datos de la dirección
   * @return la dirección creada
   * @throws CustomerNotFoundException si el cliente no existe
   * @throws IllegalArgumentException si algún dato es inválido
   */
  AddressView add(String userId, AddressData data);

  /**
   * Lista las direcciones del cliente.
   *
   * @param userId identificador del cliente
   * @return las direcciones, de la más antigua a la más reciente
   * @throws CustomerNotFoundException si el cliente no existe
   */
  List<AddressView> list(String userId);

  /**
   * Consulta una dirección.
   *
   * @param userId identificador del cliente
   * @param addressId identificador de la dirección
   * @return la dirección
   * @throws CustomerNotFoundException si el cliente no existe
   * @throws AddressNotFoundException si la dirección no existe
   */
  AddressView get(String userId, String addressId);

  /**
   * Reemplaza los datos de una dirección.
   *
   * @param userId identificador del cliente
   * @param addressId identificador de la dirección
   * @param data datos nuevos
   * @return la dirección actualizada
   * @throws CustomerNotFoundException si el cliente no existe
   * @throws AddressNotFoundException si la dirección no existe
   * @throws IllegalArgumentException si algún dato es inválido
   */
  AddressView replace(String userId, String addressId, AddressData data);

  /**
   * Elimina una dirección. Si era la predeterminada, la más antigua restante pasa a serlo.
   *
   * @param userId identificador del cliente
   * @param addressId identificador de la dirección
   * @throws CustomerNotFoundException si el cliente no existe
   * @throws AddressNotFoundException si la dirección no existe
   */
  void remove(String userId, String addressId);

  /**
   * Fija la dirección predeterminada.
   *
   * @param userId identificador del cliente
   * @param addressId identificador de la dirección
   * @throws CustomerNotFoundException si el cliente no existe
   * @throws AddressNotFoundException si la dirección no existe
   */
  void setDefault(String userId, String addressId);
}
