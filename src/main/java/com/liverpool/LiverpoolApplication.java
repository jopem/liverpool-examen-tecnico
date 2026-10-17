package com.liverpool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación de clientes y pedidos.
 */
@SpringBootApplication
public class LiverpoolApplication {

  /**
   * Arranca la aplicación.
   *
   * @param args argumentos de línea de comandos
   */
  public static void main(String[] args) {
    SpringApplication.run(LiverpoolApplication.class, args);
  }
}
