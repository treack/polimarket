package com.polimarket.polimarket_backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI polimarketOpenApi() {
        return new OpenAPI().info(new Info()
                .title("PoliMarket API")
                .version("1.0.0")
                .description("Servicios REST de Ventas, Bodega, Recursos Humanos, Proveedores y Entregas. "
                        + "Los controladores utilizan las fachadas de los componentes de negocio. "
                        + "La consulta de autorizacion RF-10 no crea una sesion autenticada. "
                        + "Registrar una salida modifica el inventario real de la base conectada."));
    }
}
