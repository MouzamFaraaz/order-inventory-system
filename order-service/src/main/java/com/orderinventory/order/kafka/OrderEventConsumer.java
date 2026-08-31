package com.orderinventory.order.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderinventory.order.config.KafkaTopicConfig;
import com.orderinventory.order.dto.event.InventoryUpdatedEvent;
import com.orderinventory.order.dto.event.OrderOutOfStockEvent;
import com.orderinventory.order.entity.OrderStatus;
import com.orderinventory.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopicConfig.INVENTORY_UPDATED_TOPIC, groupId = "${spring.kafka.consumer.group-id:order-service-group}")
    public void consumeInventoryUpdated(String message) throws Exception {
        log.info("Received message on topic [{}]: {}", KafkaTopicConfig.INVENTORY_UPDATED_TOPIC, message);
        InventoryUpdatedEvent event = objectMapper.readValue(message, InventoryUpdatedEvent.class);
        log.info("Processing InventoryUpdatedEvent for orderId: {}, status: {}", event.getOrderId(), event.getStatus());
        orderService.updateOrderStatus(event.getOrderId(), OrderStatus.CONFIRMED);
    }

    @KafkaListener(topics = KafkaTopicConfig.ORDER_OUT_OF_STOCK_TOPIC, groupId = "${spring.kafka.consumer.group-id:order-service-group}")
    public void consumeOrderOutOfStock(String message) throws Exception {
        log.info("Received message on topic [{}]: {}", KafkaTopicConfig.ORDER_OUT_OF_STOCK_TOPIC, message);
        OrderOutOfStockEvent event = objectMapper.readValue(message, OrderOutOfStockEvent.class);
        log.info("Processing OrderOutOfStockEvent for orderId: {}, reason: {}", event.getOrderId(), event.getReason());
        orderService.updateOrderStatus(event.getOrderId(), OrderStatus.OUT_OF_STOCK);
    }
}
