package com.orderinventory.order.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderOutOfStockEvent {
    private Long orderId;
    private Long productId;
    private String status;
    private String reason;
}
