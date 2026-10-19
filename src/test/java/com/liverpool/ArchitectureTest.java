package com.liverpool;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Reglas de arquitectura: hacen cumplir la dirección de las dependencias de la arquitectura
 * hexagonal y el aislamiento entre los módulos {@code customer} y {@code order}.
 */
class ArchitectureTest {

  private static JavaClasses classes;

  @BeforeAll
  static void importClasses() {
    classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.liverpool");
  }

  /**
   * El dominio es Java puro: no usa Spring, MongoDB, Jackson ni Jakarta.
   */
  @Test
  void domainDoesNotDependOnFrameworks() {
    noClasses().that().resideInAPackage("..domain..")
        .should().dependOnClassesThat().resideInAnyPackage(
            "org.springframework..", "com.mongodb..", "org.bson..",
            "tools.jackson..", "com.fasterxml.jackson..", "jakarta..")
        .check(classes);
  }

  /**
   * El dominio no conoce las capas de aplicación ni de infraestructura.
   */
  @Test
  void domainDoesNotDependOnOuterLayers() {
    noClasses().that().resideInAPackage("..domain..")
        .should().dependOnClassesThat().resideInAnyPackage("..application..", "..infrastructure..")
        .check(classes);
  }

  /**
   * La capa de aplicación no conoce los adaptadores de infraestructura.
   */
  @Test
  void applicationDoesNotDependOnInfrastructure() {
    noClasses().that().resideInAPackage("..application..")
        .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
        .check(classes);
  }

  /**
   * Los adaptadores solo usan los puertos de entrada y salida; las implementaciones de los casos
   * de uso las ensambla únicamente la configuración.
   */
  @Test
  void adaptersDoNotUseHandlersDirectly() {
    noClasses().that().resideInAnyPackage("..infrastructure.in..", "..infrastructure.out..")
        .should().dependOnClassesThat().resideInAPackage("..application.usecase..")
        .check(classes);
  }

  /**
   * El dominio y la aplicación de {@code customer} no importan nada del módulo {@code order}.
   */
  @Test
  void customerCoreDoesNotKnowOrder() {
    noClasses().that().resideInAnyPackage("..customer.domain..", "..customer.application..")
        .should().dependOnClassesThat().resideInAPackage("..order..")
        .check(classes);
  }

  /**
   * Desde {@code customer} solo se puede usar la capa de aplicación de {@code order}.
   */
  @Test
  void customerOnlyUsesOrderApplication() {
    noClasses().that().resideInAPackage("..customer..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..order.domain..", "..order.infrastructure..")
        .check(classes);
  }

  /**
   * El módulo {@code order} no conoce el módulo {@code customer}.
   */
  @Test
  void orderDoesNotKnowCustomer() {
    noClasses().that().resideInAPackage("..order..")
        .should().dependOnClassesThat().resideInAPackage("..customer..")
        .check(classes);
  }

  /**
   * No hay dependencias cíclicas entre módulos.
   */
  @Test
  void modulesAreFreeOfCycles() {
    slices().matching("com.liverpool.(*)..")
        .should().beFreeOfCycles()
        .check(classes);
  }

  /**
   * No se usan paquetes propios de una arquitectura MVC en capas.
   */
  @Test
  void noMvcLayerPackages() {
    noClasses()
        .should().resideInAnyPackage("..controller..", "..service..")
        .check(classes);
  }
}
