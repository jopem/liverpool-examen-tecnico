package com.liverpool.customer.infrastructure.out.mongo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.liverpool.customer.application.exception.ConcurrentCustomerModificationException;
import com.liverpool.customer.application.exception.CustomerAlreadyExistsException;
import com.liverpool.customer.application.exception.EmailAlreadyExistsException;
import com.liverpool.customer.domain.Customer;
import com.liverpool.customer.domain.DeliveryAddress;
import com.liverpool.customer.domain.Email;
import com.liverpool.customer.domain.UserId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;

/**
 * Pruebas del adaptador de salida a MongoDB: mapeo del documento y traducción de los errores de
 * la base de datos.
 */
class MongoCustomerRepositoryAdapterTest {

  private static final Customer CUSTOMER = new Customer(new UserId("u1"), "Juan", "García",
      "López", new Email("juan@email.com"), "a1",
      List.of(new DeliveryAddress("a1", "Reforma", "1", "2B", "Centro", "01210", "CDMX", "CDMX",
          "Portón negro")),
      Instant.parse("2026-10-17T10:00:00Z"), Instant.parse("2026-10-17T10:00:00Z"), 3L);

  private final SpringDataCustomerRepository springData =
      mock(SpringDataCustomerRepository.class);
  private final MongoCustomerRepositoryAdapter adapter =
      new MongoCustomerRepositoryAdapter(springData);

  /** El cliente sobrevive al viaje a documento y de regreso, versión incluida. */
  @Test
  void mapperRoundTrip() {
    CustomerDocument document = CustomerDocumentMapper.toDocument(CUSTOMER);
    assertThat(document.id).isEqualTo("u1");
    assertThat(document.deliveryAddresses).hasSize(1);
    assertThat(CustomerDocumentMapper.toDomain(document)).isEqualTo(CUSTOMER);
  }

  /** Un documento sin direcciones se lee como un cliente sin direcciones. */
  @Test
  void documentWithoutAddressesIsReadAsEmptyList() {
    CustomerDocument document = CustomerDocumentMapper.toDocument(CUSTOMER);
    document.deliveryAddresses = null;
    document.defaultAddressId = null;
    assertThat(CustomerDocumentMapper.toDomain(document).deliveryAddresses()).isEmpty();
  }

  /** Guardar devuelve el cliente con la versión que asignó la base de datos. */
  @Test
  void saveReturnsStoredCustomer() {
    when(springData.save(any())).thenAnswer(call -> {
      CustomerDocument document = call.getArgument(0);
      document.version = 4L;
      return document;
    });
    assertThat(adapter.save(CUSTOMER).version()).isEqualTo(4L);
  }

  /** Un índice duplicado de correo se traduce a conflicto de correo. */
  @Test
  void duplicateEmailIsTranslated() {
    when(springData.save(any())).thenThrow(new DuplicateKeyException(
        "E11000 duplicate key error index: email dup key"));
    assertThatThrownBy(() -> adapter.save(CUSTOMER))
        .isInstanceOf(EmailAlreadyExistsException.class);
  }

  /** Un _id duplicado se traduce a conflicto de userId. */
  @Test
  void duplicateIdIsTranslated() {
    when(springData.save(any())).thenThrow(new DuplicateKeyException(
        "E11000 duplicate key error index: _id_ dup key"));
    assertThatThrownBy(() -> adapter.save(CUSTOMER))
        .isInstanceOf(CustomerAlreadyExistsException.class);
  }

  /** Un fallo de versión se traduce a modificación simultánea. */
  @Test
  void optimisticLockIsTranslated() {
    when(springData.save(any())).thenThrow(new OptimisticLockingFailureException("versión"));
    assertThatThrownBy(() -> adapter.save(CUSTOMER))
        .isInstanceOf(ConcurrentCustomerModificationException.class);
  }

  /** Las búsquedas convierten el documento a dominio. */
  @Test
  void findsConvertDocuments() {
    CustomerDocument document = CustomerDocumentMapper.toDocument(CUSTOMER);
    when(springData.findById("u1")).thenReturn(Optional.of(document));
    when(springData.findByEmail("juan@email.com")).thenReturn(Optional.of(document));
    assertThat(adapter.findById(new UserId("u1"))).contains(CUSTOMER);
    assertThat(adapter.findByEmail(new Email("juan@email.com"))).contains(CUSTOMER);
    assertThat(adapter.findById(new UserId("otro"))).isEmpty();
  }
}
