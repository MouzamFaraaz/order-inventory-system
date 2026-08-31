package com.orderinventory.notification.service;

import com.orderinventory.notification.dto.NotificationResponse;
import com.orderinventory.notification.dto.event.InventoryUpdatedEvent;
import com.orderinventory.notification.dto.event.OrderOutOfStockEvent;
import com.orderinventory.notification.entity.NotificationLog;
import com.orderinventory.notification.repository.NotificationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private NotificationLog sampleLog;

    @BeforeEach
    void setUp() {
        sampleLog = NotificationLog.builder()
                .id(1L)
                .orderId(1001L)
                .productId(55L)
                .notificationType("ORDER_CONFIRMATION")
                .status("CONFIRMED")
                .message("Order #1001 CONFIRMED!")
                .sentAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testHandleInventoryUpdated() {
        InventoryUpdatedEvent event = InventoryUpdatedEvent.builder()
                .orderId(1001L)
                .productId(55L)
                .status("CONFIRMED")
                .remainingStock(48)
                .build();

        when(notificationLogRepository.save(any(NotificationLog.class))).thenReturn(sampleLog);

        notificationService.handleInventoryUpdated(event);

        ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository, times(1)).save(captor.capture());
        assertEquals(1001L, captor.getValue().getOrderId());
        assertEquals("ORDER_CONFIRMATION", captor.getValue().getNotificationType());
        assertEquals("CONFIRMED", captor.getValue().getStatus());
    }

    @Test
    void testHandleOrderOutOfStock() {
        OrderOutOfStockEvent event = OrderOutOfStockEvent.builder()
                .orderId(1002L)
                .productId(55L)
                .status("OUT_OF_STOCK")
                .reason("Insufficient stock")
                .build();

        when(notificationLogRepository.save(any(NotificationLog.class))).thenReturn(sampleLog);

        notificationService.handleOrderOutOfStock(event);

        ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository, times(1)).save(captor.capture());
        assertEquals(1002L, captor.getValue().getOrderId());
        assertEquals("ORDER_OUT_OF_STOCK", captor.getValue().getNotificationType());
        assertEquals("OUT_OF_STOCK", captor.getValue().getStatus());
    }

    @Test
    void testGetNotificationsByOrderId() {
        when(notificationLogRepository.findByOrderIdOrderBySentAtDesc(1001L)).thenReturn(List.of(sampleLog));

        List<NotificationResponse> result = notificationService.getNotificationsByOrderId(1001L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1001L, result.get(0).getOrderId());
    }
}
