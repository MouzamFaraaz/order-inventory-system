package com.orderinventory.order.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .description("Microservice for managing orders, lifecycle status transitions, and Kafka events")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Order Inventory System Team")
                                .email("dev@orderinventory.com")));
    }
}
