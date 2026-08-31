package com.orderinventory.notification.service;

import com.orderinventory.notification.dto.NotificationResponse;
import com.orderinventory.notification.dto.event.InventoryUpdatedEvent;
import com.orderinventory.notification.dto.event.OrderOutOfStockEvent;
import com.orderinventory.notification.entity.NotificationLog;
import com.orderinventory.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationLogRepository notificationLogRepository;

    @Override
    @Transactional
    public void handleInventoryUpdated(InventoryUpdatedEvent event) {
        String message = String.format("🎉 Order #%d CONFIRMED! Stock reserved successfully for product #%d. Remaining inventory stock: %d. Notification email and SMS dispatched to customer.",
                event.getOrderId(), event.getProductId(), event.getRemainingStock());

        log.info("\n======================================================\n" +
                 "[NOTIFICATION DISPATCHED - ORDER CONFIRMED]\n" +
                 "Order ID   : {}\n" +
                 "Product ID : {}\n" +
                 "Status     : {}\n" +
                 "Message    : {}\n" +
                 "======================================================",
                event.getOrderId(), event.getProductId(), event.getStatus(), message);

        NotificationLog logEntry = NotificationLog.builder()
                .orderId(event.getOrderId())
                .productId(event.getProductId())
                .notificationType("ORDER_CONFIRMATION")
                .status("CONFIRMED")
                .message(message)
                .build();

        notificationLogRepository.save(logEntry);
    }

    @Override
    @Transactional
    public void handleOrderOutOfStock(OrderOutOfStockEvent event) {
        String message = String.format("⚠️ Order #%d CANNOT BE FULFILLED! Product #%d is OUT_OF_STOCK (Reason: %s). Order status marked as OUT_OF_STOCK. Apology notification email dispatched to customer.",
                event.getOrderId(), event.getProductId(), event.getReason());

        log.warn("\n======================================================\n" +
                 "[NOTIFICATION DISPATCHED - ORDER OUT OF STOCK]\n" +
                 "Order ID   : {}\n" +
                 "Product ID : {}\n" +
                 "Status     : {}\n" +
                 "Reason     : {}\n" +
                 "Message    : {}\n" +
                 "======================================================",
                event.getOrderId(), event.getProductId(), event.getStatus(), event.getReason(), message);

        NotificationLog logEntry = NotificationLog.builder()
                .orderId(event.getOrderId())
                .productId(event.getProductId())
                .notificationType("ORDER_OUT_OF_STOCK")
                .status("OUT_OF_STOCK")
                .message(message)
                .build();

        notificationLogRepository.save(logEntry);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getRecentNotifications() {
        return notificationLogRepository.findTop50ByOrderBySentAtDesc()
                .stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByOrderId(Long orderId) {
        return notificationLogRepository.findByOrderIdOrderBySentAtDesc(orderId)
                .stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
