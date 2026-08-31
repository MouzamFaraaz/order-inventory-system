package com.orderinventory.order.config;

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
    public NewTopic orderPlacedTopic() {
        return TopicBuilder.name(ORDER_PLACED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderCancelledTopic() {
        return TopicBuilder.name(ORDER_CANCELLED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
