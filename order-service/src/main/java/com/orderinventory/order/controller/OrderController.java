package com.orderinventory.order.controller;

import com.orderinventory.order.dto.CreateOrderRequest;
import com.orderinventory.order.dto.OrderDetailsResponse;
import com.orderinventory.order.dto.OrderResponse;
import com.orderinventory.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order Controller", description = "Endpoints for managing customer orders and asynchronous event publishing")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create a new order", description = "Persists an order in PENDING status and publishes an order-placed event to Kafka")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("REST request to create order: {}", request);
        OrderResponse response = orderService.createOrder(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Fetches the full details and current status of an order")
    public ResponseEntity<OrderDetailsResponse> getOrderById(@PathVariable Long id) {
        log.info("REST request to get order with ID: {}", id);
        OrderDetailsResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List all orders", description = "Retrieves paginated orders with sorting options")
    public ResponseEntity<Page<OrderDetailsResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort) {
        
        Sort.Direction direction = Sort.Direction.DESC;
        String property = "id";

        if (sort.length > 0) {
            String[] sortParts = sort[0].split(",");
            property = sortParts[0];
            if (sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc")) {
                direction = Sort.Direction.ASC;
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, property));
        Page<OrderDetailsResponse> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get orders by customer ID", description = "Retrieves all orders placed by a specific customer")
    public ResponseEntity<List<OrderDetailsResponse>> getOrdersByCustomer(@PathVariable Long customerId) {
        log.info("REST request to get orders for customerId: {}", customerId);
        List<OrderDetailsResponse> orders = orderService.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order", description = "Cancels a pending or confirmed order")
    public ResponseEntity<OrderDetailsResponse> cancelOrder(@PathVariable Long id) {
        log.info("REST request to cancel order with ID: {}", id);
        OrderDetailsResponse response = orderService.cancelOrder(id);
        return ResponseEntity.ok(response);
    }
}
