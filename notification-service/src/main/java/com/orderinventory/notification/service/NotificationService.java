package com.orderinventory.notification.service;

import com.orderinventory.notification.dto.NotificationResponse;
import com.orderinventory.notification.dto.event.InventoryUpdatedEvent;
import com.orderinventory.notification.dto.event.OrderOutOfStockEvent;

import java.util.List;

public interface NotificationService {
    void handleInventoryUpdated(InventoryUpdatedEvent event);
    void handleOrderOutOfStock(OrderOutOfStockEvent event);
    List<NotificationResponse> getRecentNotifications();
    List<NotificationResponse> getNotificationsByOrderId(Long orderId);
}
