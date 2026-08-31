package com.orderinventory.inventory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inventoryServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Inventory Service API")
                        .description("Microservice for managing product catalog, stock reserves, and Kafka inventory events")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Order Inventory System Team")
                                .email("dev@orderinventory.com")));
    }
}
