package com.statikk.notification.repository;

import com.statikk.notification.entity.NotificationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationRecord, Long> {
    List<NotificationRecord> findByUserIdOrderByCreatedAtDesc(Long userId);
    long countByUserIdAndReadStatusFalse(Long userId);
}
