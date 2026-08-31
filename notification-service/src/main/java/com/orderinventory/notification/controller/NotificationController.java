package com.orderinventory.notification.controller;

import com.orderinventory.notification.dto.NotificationResponse;
import com.orderinventory.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Controller", description = "Endpoints for viewing simulated real-time event notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "List recent notifications", description = "Retrieves the 50 most recent notification log records")
    public ResponseEntity<List<NotificationResponse>> getRecentNotifications() {
        log.info("REST request to get recent notifications");
        List<NotificationResponse> notifications = notificationService.getRecentNotifications();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get notifications by order ID", description = "Retrieves all notification log records for a specific order")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByOrderId(@PathVariable Long orderId) {
        log.info("REST request to get notifications for order ID: {}", orderId);
        List<NotificationResponse> notifications = notificationService.getNotificationsByOrderId(orderId);
        return ResponseEntity.ok(notifications);
    }
}
