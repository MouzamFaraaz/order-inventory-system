package com.orderinventory.notification.repository;

import com.orderinventory.notification.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findByOrderIdOrderBySentAtDesc(Long orderId);
    List<NotificationLog> findTop50ByOrderBySentAtDesc();
}
