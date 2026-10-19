package com.liverpool;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Datos generales de la documentación OpenAPI (Swagger UI en {@code /swagger-ui.html}).
 */
@Configuration
public class OpenApiConfiguration {

  /**
   * Describe la API.
   *
   * @return la descripción general de la API
   */
  @Bean
  OpenAPI openApi() {
    return new OpenAPI().info(new Info()
        .title("Liverpool - Clientes y pedidos")
        .version("v1")
        .description("Gestión de clientes y direcciones de entrega (MongoDB) y búsqueda de "
            + "pedidos e items tomados de un servicio externo de solo lectura. La búsqueda "
            + "ignora acentos, mayúsculas y signos, y tolera errores mínimos de ortografía. "
            + "El campo `orders` del cliente se calcula al consultar y no se guarda."));
  }
}
