package com.liverpool;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Verifica que el contexto de Spring arranca correctamente.
 */
@SpringBootTest
class LiverpoolApplicationTests {

  /**
   * El contexto de la aplicación se carga sin errores.
   */
  @Test
  void contextLoads() {
    // Si el contexto no arranca, la prueba falla antes de llegar aquí.
  }
}
