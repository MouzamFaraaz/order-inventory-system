package com.orderinventory.inventory.repository;

import com.orderinventory.inventory.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByName(String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Product p SET p.availableQuantity = p.availableQuantity + :qty, " +
           "p.reservedQuantity = p.reservedQuantity - :qty " +
           "WHERE p.id = :id AND p.reservedQuantity >= :qty")
    int releaseStock(@Param("id") Long id, @Param("qty") Integer qty);
}
