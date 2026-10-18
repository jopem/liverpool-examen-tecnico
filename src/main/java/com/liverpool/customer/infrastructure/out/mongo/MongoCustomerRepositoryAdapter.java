package com.liverpool.customer.infrastructure.out.mongo;

import com.liverpool.customer.application.exception.ConcurrentCustomerModificationException;
import com.liverpool.customer.application.exception.CustomerAlreadyExistsException;
import com.liverpool.customer.application.exception.EmailAlreadyExistsException;
import com.liverpool.customer.application.port.out.CustomerRepositoryPort;
import com.liverpool.customer.domain.Customer;
import com.liverpool.customer.domain.Email;
import com.liverpool.customer.domain.UserId;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida que guarda los clientes en MongoDB.
 *
 * <p>La unicidad del {@code userId} (el {@code _id}) y del correo (índice único) y el control de
 * versiones los garantiza la base de datos; aquí solo se traducen sus errores.
 */
@Component
public class MongoCustomerRepositoryAdapter implements CustomerRepositoryPort {

  private final SpringDataCustomerRepository repository;

  MongoCustomerRepositoryAdapter(SpringDataCustomerRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<Customer> findById(UserId userId) {
    return repository.findById(userId.value()).map(CustomerDocumentMapper::toDomain);
  }

  @Override
  public Optional<Customer> findByEmail(Email email) {
    return repository.findByEmail(email.value()).map(CustomerDocumentMapper::toDomain);
  }

  @Override
  public Customer save(Customer customer) {
    try {
      return CustomerDocumentMapper.toDomain(
          repository.save(CustomerDocumentMapper.toDocument(customer)));
    } catch (DuplicateKeyException e) {
      if (String.valueOf(e.getMessage()).contains("email")) {
        throw new EmailAlreadyExistsException(customer.email().value());
      }
      throw new CustomerAlreadyExistsException(customer.userId().value());
    } catch (OptimisticLockingFailureException e) {
      throw new ConcurrentCustomerModificationException(e);
    }
  }
}
