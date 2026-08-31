package com.orderinventory.order.service;

import com.orderinventory.order.dto.CreateOrderRequest;
import com.orderinventory.order.dto.OrderDetailsResponse;
import com.orderinventory.order.dto.OrderResponse;
import com.orderinventory.order.dto.event.OrderCancelledEvent;
import com.orderinventory.order.dto.event.OrderPlacedEvent;
import com.orderinventory.order.entity.Order;
import com.orderinventory.order.entity.OrderStatus;
import com.orderinventory.order.exception.InvalidOrderStateException;
import com.orderinventory.order.exception.OrderNotFoundException;
import com.orderinventory.order.kafka.OrderProducer;
import com.orderinventory.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderProducer orderProducer;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating new order for customerId: {}, productId: {}, quantity: {}",
                request.getCustomerId(), request.getProductId(), request.getQuantity());

        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Saved order with ID: {} and status: {}", savedOrder.getId(), savedOrder.getStatus());

        // Publish OrderPlacedEvent to Kafka topic
        OrderPlacedEvent event = OrderPlacedEvent.builder()
                .orderId(savedOrder.getId())
                .productId(savedOrder.getProductId())
                .quantity(savedOrder.getQuantity())
                .customerId(savedOrder.getCustomerId())
                .timestamp(LocalDateTime.now())
                .build();

        orderProducer.publishOrderPlaced(event);

        return OrderResponse.builder()
                .orderId(savedOrder.getId())
                .status(savedOrder.getStatus())
                .message("Order received, awaiting inventory confirmation")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailsResponse getOrderById(Long id) {
        log.info("Fetching order by id: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
        return OrderDetailsResponse.fromEntity(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDetailsResponse> getAllOrders(Pageable pageable) {
        log.info("Fetching paginated orders, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return orderRepository.findAll(pageable).map(OrderDetailsResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDetailsResponse> getOrdersByCustomerId(Long customerId) {
        log.info("Fetching orders for customerId: {}", customerId);
        return orderRepository.findByCustomerId(customerId)
                .stream()
                .map(OrderDetailsResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderDetailsResponse cancelOrder(Long id) {
        log.info("Attempting to cancel order with id: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException("Order with id " + id + " is already cancelled");
        }

        if (order.getStatus() == OrderStatus.OUT_OF_STOCK) {
            throw new InvalidOrderStateException("Order with id " + id + " cannot be cancelled as it is already OUT_OF_STOCK");
        }

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);
        log.info("Order with id: {} cancelled successfully (previous status: {})", id, previousStatus);

        // Publish compensation event to release reserved inventory
        if (previousStatus == OrderStatus.CONFIRMED) {
            OrderCancelledEvent cancelledEvent = OrderCancelledEvent.builder()
                    .orderId(updatedOrder.getId())
                    .productId(updatedOrder.getProductId())
                    .quantity(updatedOrder.getQuantity())
                    .build();
            orderProducer.publishOrderCancelled(cancelledEvent);
            log.info("Published OrderCancelledEvent for orderId: {} to release reserved inventory", id);
        }

        return OrderDetailsResponse.fromEntity(updatedOrder);
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        log.info("Updating order status for orderId: {} to {}", orderId, status);
        orderRepository.findById(orderId).ifPresentOrElse(order -> {
            // Prevent overriding terminal states like CANCELLED
            if (order.getStatus() != OrderStatus.CANCELLED) {
                order.setStatus(status);
                orderRepository.save(order);
                log.info("Order status updated successfully for orderId: {} -> {}", orderId, status);
            } else {
                log.warn("Skipping status update for orderId: {} because it is already CANCELLED", orderId);
            }
        }, () -> log.error("Order not found for status update, orderId: {}", orderId));
    }
}
