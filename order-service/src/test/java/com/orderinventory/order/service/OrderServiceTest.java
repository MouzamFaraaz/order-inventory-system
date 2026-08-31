package com.orderinventory.order.service;

import com.orderinventory.order.dto.CreateOrderRequest;
import com.orderinventory.order.dto.OrderDetailsResponse;
import com.orderinventory.order.dto.OrderResponse;
import com.orderinventory.order.entity.Order;
import com.orderinventory.order.entity.OrderStatus;
import com.orderinventory.order.exception.OrderNotFoundException;
import com.orderinventory.order.kafka.OrderProducer;
import com.orderinventory.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderProducer orderProducer;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        sampleOrder = Order.builder()
                .id(1001L)
                .customerId(101L)
                .productId(55L)
                .quantity(2)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateOrder_Success() {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(101L)
                .productId(55L)
                .quantity(2)
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);
        doNothing().when(orderProducer).publishOrderPlaced(any());

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals(1001L, response.getOrderId());
        assertEquals(OrderStatus.PENDING, response.getStatus());
        assertEquals("Order received, awaiting inventory confirmation", response.getMessage());

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderProducer, times(1)).publishOrderPlaced(any());
    }

    @Test
    void testGetOrderById_Success() {
        when(orderRepository.findById(1001L)).thenReturn(Optional.of(sampleOrder));

        OrderDetailsResponse response = orderService.getOrderById(1001L);

        assertNotNull(response);
        assertEquals(1001L, response.getId());
        assertEquals(101L, response.getCustomerId());
        assertEquals(55L, response.getProductId());
        assertEquals(2, response.getQuantity());
        assertEquals(OrderStatus.PENDING, response.getStatus());
    }

    @Test
    void testGetOrderById_NotFound() {
        when(orderRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(9999L));
    }

    @Test
    void testCancelOrder_Success() {
        when(orderRepository.findById(1001L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderDetailsResponse response = orderService.cancelOrder(1001L);

        assertNotNull(response);
        assertEquals(OrderStatus.CANCELLED, response.getStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testUpdateOrderStatus_Confirmed() {
        when(orderRepository.findById(1001L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        orderService.updateOrderStatus(1001L, OrderStatus.CONFIRMED);

        assertEquals(OrderStatus.CONFIRMED, sampleOrder.getStatus());
        verify(orderRepository, times(1)).save(sampleOrder);
    }
}
