package com.orderinventory.inventory.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderinventory.inventory.config.KafkaTopicConfig;
import com.orderinventory.inventory.dto.event.OrderCancelledEvent;
import com.orderinventory.inventory.dto.event.OrderPlacedEvent;
import com.orderinventory.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventConsumer {

    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopicConfig.ORDER_PLACED_TOPIC, groupId = "${spring.kafka.consumer.group-id:inventory-service-group}")
    public void consumeOrderPlaced(String message) throws Exception {
        log.info("Received message on topic [{}]: {}", KafkaTopicConfig.ORDER_PLACED_TOPIC, message);
        OrderPlacedEvent event = objectMapper.readValue(message, OrderPlacedEvent.class);
        log.info("Processing OrderPlacedEvent: orderId={}, productId={}, quantity={}",
                event.getOrderId(), event.getProductId(), event.getQuantity());
        inventoryService.processOrderPlaced(event);
    }

    @KafkaListener(topics = KafkaTopicConfig.ORDER_CANCELLED_TOPIC, groupId = "${spring.kafka.consumer.group-id:inventory-service-group}")
    public void consumeOrderCancelled(String message) throws Exception {
        log.info("Received message on topic [{}]: {}", KafkaTopicConfig.ORDER_CANCELLED_TOPIC, message);
        OrderCancelledEvent event = objectMapper.readValue(message, OrderCancelledEvent.class);
        log.info("Processing OrderCancelledEvent: orderId={}, productId={}, quantity={}",
                event.getOrderId(), event.getProductId(), event.getQuantity());
        inventoryService.processOrderCancelled(event);
    }
}
