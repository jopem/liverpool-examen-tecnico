package com.liverpool.customer.infrastructure.out.mongo;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repositorio de Spring Data sobre la colección {@code customers}.
 */
interface SpringDataCustomerRepository extends MongoRepository<CustomerDocument, String> {

  /**
   * Busca por correo.
   *
   * @param email correo normalizado
   * @return el documento, si existe
   */
  Optional<CustomerDocument> findByEmail(String email);
}
