package com.orderinventory.inventory.controller;

import com.orderinventory.inventory.dto.ProductRequest;
import com.orderinventory.inventory.dto.ProductResponse;
import com.orderinventory.inventory.dto.StockUpdateRequest;
import com.orderinventory.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product Inventory Controller", description = "Endpoints for viewing and updating product catalog and stock levels")
public class ProductController {

    private final InventoryService inventoryService;

    @GetMapping
    @Operation(summary = "List all products", description = "Retrieves all products in catalog with available and reserved quantities")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        log.info("REST request to list all products");
        List<ProductResponse> products = inventoryService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product details", description = "Retrieves details of a product by ID")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        log.info("REST request to get product with ID: {}", id);
        ProductResponse product = inventoryService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PostMapping
    @Operation(summary = "Add new product (Admin)", description = "Creates a new product in the catalog")
    public ResponseEntity<ProductResponse> addProduct(@Valid @RequestBody ProductRequest request) {
        log.info("REST request to add product: {}", request.getName());
        ProductResponse response = inventoryService.addProduct(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/stock")
    @Operation(summary = "Update stock manually (Admin)", description = "Updates available and reserved stock for a specific product")
    public ResponseEntity<ProductResponse> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {
        log.info("REST request to update stock for product ID {}: {}", id, request);
        ProductResponse response = inventoryService.updateStock(id, request);
        return ResponseEntity.ok(response);
    }
}
