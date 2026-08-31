package com.orderinventory.inventory.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String ORDER_PLACED_TOPIC = "order-placed";
    public static final String INVENTORY_UPDATED_TOPIC = "inventory-updated";
    public static final String ORDER_OUT_OF_STOCK_TOPIC = "order-out-of-stock";
    public static final String ORDER_CANCELLED_TOPIC = "order-cancelled";

    @Bean
    public NewTopic inventoryUpdatedTopic() {
        return TopicBuilder.name(INVENTORY_UPDATED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderOutOfStockTopic() {
        return TopicBuilder.name(ORDER_OUT_OF_STOCK_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
