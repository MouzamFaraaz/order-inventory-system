package com.orderinventory.order.service;

import com.orderinventory.order.dto.CreateOrderRequest;
import com.orderinventory.order.dto.OrderDetailsResponse;
import com.orderinventory.order.dto.OrderResponse;
import com.orderinventory.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);
    OrderDetailsResponse getOrderById(Long id);
    Page<OrderDetailsResponse> getAllOrders(Pageable pageable);
    List<OrderDetailsResponse> getOrdersByCustomerId(Long customerId);
    OrderDetailsResponse cancelOrder(Long id);
    void updateOrderStatus(Long orderId, OrderStatus status);
}
