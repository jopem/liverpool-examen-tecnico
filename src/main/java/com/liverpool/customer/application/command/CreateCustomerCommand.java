package com.liverpool.customer.application.command;

/**
 * Datos para dar de alta un cliente.
 *
 * @param userId identificador opcional; si es nulo se genera uno
 * @param firstName nombre
 * @param paternalLastName apellido paterno
 * @param maternalLastName apellido materno, opcional
 * @param email correo electrónico
 */
public record CreateCustomerCommand(
    String userId,
    String firstName,
    String paternalLastName,
    String maternalLastName,
    String email) {
}
