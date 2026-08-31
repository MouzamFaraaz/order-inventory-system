package com.orderinventory.inventory.service;

import com.orderinventory.inventory.dto.ProductRequest;
import com.orderinventory.inventory.dto.ProductResponse;
import com.orderinventory.inventory.dto.StockUpdateRequest;
import com.orderinventory.inventory.dto.event.OrderCancelledEvent;
import com.orderinventory.inventory.dto.event.OrderPlacedEvent;

import java.util.List;

public interface InventoryService {
    List<ProductResponse> getAllProducts();
    ProductResponse getProductById(Long id);
    ProductResponse addProduct(ProductRequest request);
    ProductResponse updateStock(Long id, StockUpdateRequest request);
    void processOrderPlaced(OrderPlacedEvent event);
    void processOrderCancelled(OrderCancelledEvent event);
}
