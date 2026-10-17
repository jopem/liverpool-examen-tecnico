package com.liverpool.customer.application.command;

/**
 * Datos personales que reemplazan a los actuales del cliente.
 *
 * @param firstName nombre
 * @param paternalLastName apellido paterno
 * @param maternalLastName apellido materno, opcional
 * @param email correo electrónico
 */
public record UpdateCustomerCommand(
    String firstName,
    String paternalLastName,
    String maternalLastName,
    String email) {
}
