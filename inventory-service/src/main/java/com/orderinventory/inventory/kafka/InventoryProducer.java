package com.orderinventory.inventory.kafka;

import com.orderinventory.inventory.config.KafkaTopicConfig;
import com.orderinventory.inventory.dto.event.InventoryUpdatedEvent;
import com.orderinventory.inventory.dto.event.OrderOutOfStockEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishInventoryUpdated(InventoryUpdatedEvent event) {
        log.info("Publishing InventoryUpdatedEvent for orderId: {}, remainingStock: {} to topic: {}",
                event.getOrderId(), event.getRemainingStock(), KafkaTopicConfig.INVENTORY_UPDATED_TOPIC);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                KafkaTopicConfig.INVENTORY_UPDATED_TOPIC,
                String.valueOf(event.getOrderId()),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully published InventoryUpdatedEvent for orderId: {} at offset: {}",
                        event.getOrderId(), result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish InventoryUpdatedEvent for orderId: {}", event.getOrderId(), ex);
            }
        });
    }

    public void publishOrderOutOfStock(OrderOutOfStockEvent event) {
        log.info("Publishing OrderOutOfStockEvent for orderId: {}, reason: {} to topic: {}",
                event.getOrderId(), event.getReason(), KafkaTopicConfig.ORDER_OUT_OF_STOCK_TOPIC);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                KafkaTopicConfig.ORDER_OUT_OF_STOCK_TOPIC,
                String.valueOf(event.getOrderId()),
                event
        );

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully published OrderOutOfStockEvent for orderId: {} at offset: {}",
                        event.getOrderId(), result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish OrderOutOfStockEvent for orderId: {}", event.getOrderId(), ex);
            }
        });
    }
}
