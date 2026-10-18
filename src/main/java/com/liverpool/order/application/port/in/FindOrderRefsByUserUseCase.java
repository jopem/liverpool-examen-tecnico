package com.liverpool.order.application.port.in;

import com.liverpool.order.application.exception.OrderSourceUnavailableException;
import java.util.List;

/**
 * Puerto de entrada. Caso de uso: obtener los números de pedido de un usuario. Es la única puerta
 * del módulo {@code order} hacia el módulo {@code customer}.
 */
public interface FindOrderRefsByUserUseCase {

  /**
   * Devuelve los números de pedido cuyo {@code userId} es exactamente el indicado.
   *
   * @param userId identificador del usuario
   * @return los números de pedido, en el orden del origen; vacío si no tiene pedidos
   * @throws OrderSourceUnavailableException si el origen no está disponible
   */
  List<String> execute(String userId);
}
