package com.orderinventory.notification.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderinventory.notification.dto.event.InventoryUpdatedEvent;
import com.orderinventory.notification.dto.event.OrderOutOfStockEvent;
import com.orderinventory.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "inventory-updated", groupId = "${spring.kafka.consumer.group-id:notification-service-group}")
    public void consumeInventoryUpdated(String message) throws Exception {
        log.info("Notification Service received [inventory-updated]: {}", message);
        InventoryUpdatedEvent event = objectMapper.readValue(message, InventoryUpdatedEvent.class);
        notificationService.handleInventoryUpdated(event);
    }

    @KafkaListener(topics = "order-out-of-stock", groupId = "${spring.kafka.consumer.group-id:notification-service-group}")
    public void consumeOrderOutOfStock(String message) throws Exception {
        log.info("Notification Service received [order-out-of-stock]: {}", message);
        OrderOutOfStockEvent event = objectMapper.readValue(message, OrderOutOfStockEvent.class);
        notificationService.handleOrderOutOfStock(event);
    }
}
