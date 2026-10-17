package com.liverpool.customer.application.usecase;

import com.liverpool.customer.application.exception.OrdersUnavailableException;
import com.liverpool.customer.application.port.out.CustomerOrdersPort;
import com.liverpool.customer.application.view.CustomerView;
import com.liverpool.customer.domain.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calcula los pedidos de un cliente sin que un fallo del servicio externo impida devolver al
 * cliente.
 */
final class OrdersLookup {

  private static final Logger LOG = LoggerFactory.getLogger(OrdersLookup.class);

  private final CustomerOrdersPort ordersPort;

  OrdersLookup(CustomerOrdersPort ordersPort) {
    this.ordersPort = ordersPort;
  }

  CustomerView viewOf(Customer customer) {
    try {
      return new CustomerView(customer, ordersPort.findOrderRefs(customer.userId()));
    } catch (OrdersUnavailableException e) {
      LOG.warn("No se pudieron consultar los pedidos del cliente {}; se devuelve sin ellos",
          customer.userId().value(), e);
      return new CustomerView(customer, null);
    }
  }
}
