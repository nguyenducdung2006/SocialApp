package com.example.AsmJava5.repository;

import com.example.AsmJava5.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Lấy tất cả thông báo của 1 user (mới nhất trước)
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    // Đếm thông báo chưa đọc
    long countByRecipientIdAndIsReadFalse(Long recipientId);

    // Đánh dấu tất cả đã đọc
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient.id = :recipientId")
    void markAllAsRead(Long recipientId);
}
