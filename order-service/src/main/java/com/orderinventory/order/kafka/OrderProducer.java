package com.orderinventory.order.kafka;

import com.orderinventory.order.config.KafkaTopicConfig;
import com.orderinventory.order.dto.event.OrderCancelledEvent;
import com.orderinventory.order.dto.event.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderPlaced(OrderPlacedEvent event) {
        log.info("Publishing OrderPlacedEvent for orderId: {} to topic: {}", event.getOrderId(), KafkaTopicConfig.ORDER_PLACED_TOPIC);
        
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                KafkaTopicConfig.ORDER_PLACED_TOPIC,
                String.valueOf(event.getProductId()),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully published OrderPlacedEvent for orderId: {} at offset: {}",
                        event.getOrderId(), result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish OrderPlacedEvent for orderId: {}", event.getOrderId(), ex);
            }
        });
    }

    public void publishOrderCancelled(OrderCancelledEvent event) {
        log.info("Publishing OrderCancelledEvent for orderId: {} to topic: {}", event.getOrderId(), KafkaTopicConfig.ORDER_CANCELLED_TOPIC);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                KafkaTopicConfig.ORDER_CANCELLED_TOPIC,
                String.valueOf(event.getProductId()),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully published OrderCancelledEvent for orderId: {} at offset: {}",
                        event.getOrderId(), result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish OrderCancelledEvent for orderId: {}", event.getOrderId(), ex);
            }
        });
    }
}
