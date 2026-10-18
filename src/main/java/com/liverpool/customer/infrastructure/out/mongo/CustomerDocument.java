package com.liverpool.customer.infrastructure.out.mongo;

import java.time.Instant;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Documento de la colección {@code customers}. El {@code _id} es el {@code userId} y las
 * direcciones van embebidas.
 */
@Document(collection = "customers")
class CustomerDocument {

  @Id
  String id;

  @Version
  Long version;

  String firstName;
  String paternalLastName;
  String maternalLastName;

  @Indexed(unique = true)
  String email;

  String defaultAddressId;
  List<AddressDocument> deliveryAddresses;
  Instant createdAt;
  Instant updatedAt;

  /**
   * Dirección embebida en el documento del cliente.
   */
  static class AddressDocument {
    String addressId;
    String street;
    String exteriorNumber;
    String interiorNumber;
    String neighborhood;
    String zipCode;
    String city;
    String state;
    String references;
  }
}
