package com.liverpool.customer.application.view;

import com.liverpool.customer.domain.Customer;
import java.util.List;

/**
 * Un cliente junto con sus pedidos, que se calculan al consultar y no se guardan.
 *
 * @param customer el cliente
 * @param orders números de pedido del cliente; {@code null} si no se pudieron consultar
 */
public record CustomerView(Customer customer, List<String> orders) {
}
