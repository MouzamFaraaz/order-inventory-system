package com.orderinventory.notification.dto;

import com.orderinventory.notification.entity.NotificationLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private Long orderId;
    private Long productId;
    private String notificationType;
    private String status;
    private String message;
    private LocalDateTime sentAt;

    public static NotificationResponse fromEntity(NotificationLog entity) {
        return NotificationResponse.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .productId(entity.getProductId())
                .notificationType(entity.getNotificationType())
                .status(entity.getStatus())
                .message(entity.getMessage())
                .sentAt(entity.getSentAt())
                .build();
    }
}
