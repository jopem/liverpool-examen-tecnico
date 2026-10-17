package com.liverpool.customer.application.view;

import com.liverpool.customer.domain.DeliveryAddress;

/**
 * Una dirección de entrega y si es la predeterminada del cliente.
 *
 * @param address la dirección
 * @param defaultAddress {@code true} si es la predeterminada
 */
public record AddressView(DeliveryAddress address, boolean defaultAddress) {
}
