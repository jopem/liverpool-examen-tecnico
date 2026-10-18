package com.liverpool.customer.infrastructure.out.mongo;

import com.liverpool.customer.domain.Customer;
import com.liverpool.customer.domain.DeliveryAddress;
import com.liverpool.customer.domain.Email;
import com.liverpool.customer.domain.UserId;
import java.util.List;

/**
 * Convierte entre el cliente del dominio y su documento de MongoDB.
 */
final class CustomerDocumentMapper {

  private CustomerDocumentMapper() {
  }

  static CustomerDocument toDocument(Customer customer) {
    CustomerDocument document = new CustomerDocument();
    document.id = customer.userId().value();
    document.version = customer.version();
    document.firstName = customer.firstName();
    document.paternalLastName = customer.paternalLastName();
    document.maternalLastName = customer.maternalLastName();
    document.email = customer.email().value();
    document.defaultAddressId = customer.defaultAddressId();
    document.deliveryAddresses = customer.deliveryAddresses().stream()
        .map(CustomerDocumentMapper::addressToDocument).toList();
    document.createdAt = customer.createdAt();
    document.updatedAt = customer.updatedAt();
    return document;
  }

  static Customer toDomain(CustomerDocument document) {
    List<CustomerDocument.AddressDocument> addresses = document.deliveryAddresses == null
        ? List.of() : document.deliveryAddresses;
    return new Customer(new UserId(document.id), document.firstName,
        document.paternalLastName, document.maternalLastName, new Email(document.email),
        document.defaultAddressId,
        addresses.stream().map(CustomerDocumentMapper::addressToDomain).toList(),
        document.createdAt, document.updatedAt, document.version);
  }

  private static CustomerDocument.AddressDocument addressToDocument(DeliveryAddress address) {
    CustomerDocument.AddressDocument document = new CustomerDocument.AddressDocument();
    document.addressId = address.addressId();
    document.street = address.street();
    document.exteriorNumber = address.exteriorNumber();
    document.interiorNumber = address.interiorNumber();
    document.neighborhood = address.neighborhood();
    document.zipCode = address.zipCode();
    document.city = address.city();
    document.state = address.state();
    document.references = address.references();
    return document;
  }

  private static DeliveryAddress addressToDomain(CustomerDocument.AddressDocument document) {
    return new DeliveryAddress(document.addressId, document.street, document.exteriorNumber,
        document.interiorNumber, document.neighborhood, document.zipCode, document.city,
        document.state, document.references);
  }
}
