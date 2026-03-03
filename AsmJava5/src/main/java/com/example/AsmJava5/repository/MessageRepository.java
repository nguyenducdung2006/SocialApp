package com.example.AsmJava5.repository;

import com.example.AsmJava5.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Lấy toàn bộ tin nhắn giữa 2 user (sắp xếp theo thời gian)
    @Query("""
        SELECT m FROM Message m
        WHERE (m.sender.id = :userId1 AND m.receiver.id = :userId2)
           OR (m.sender.id = :userId2 AND m.receiver.id = :userId1)
        ORDER BY m.sentAt ASC
    """)
    List<Message> findConversation(@Param("userId1") Long userId1,
                                   @Param("userId2") Long userId2);

    // Đánh dấu đã đọc
    @Modifying
    @Transactional
    @Query("""
        UPDATE Message m SET m.isRead = true
        WHERE m.sender.id = :senderId AND m.receiver.id = :receiverId AND m.isRead = false
    """)
    void markAsRead(@Param("senderId") Long senderId,
                    @Param("receiverId") Long receiverId);

    // Đếm tin nhắn chưa đọc của user (tổng)
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :userId AND m.isRead = false")
    long countUnread(@Param("userId") Long userId);

    // Lấy danh sách users đã nhắn tin với user (conversations gần đây)
    @Query(value = """
        SELECT other_user_id, MAX(sent_at) AS last_time
        FROM (
            SELECT receiver_id AS other_user_id, sent_at
            FROM messages
            WHERE sender_id = :userId AND receiver_id != :userId
            UNION ALL
            SELECT sender_id AS other_user_id, sent_at
            FROM messages
            WHERE receiver_id = :userId AND sender_id != :userId
            UNION ALL
            SELECT sender_id AS other_user_id, sent_at
            FROM messages
            WHERE sender_id = :userId AND receiver_id = :userId
        ) as UserChats
        GROUP BY other_user_id
        ORDER BY last_time DESC
    """, nativeQuery = true)
    List<Object[]> findRecentConversationUserIds(@Param("userId") Long userId);

    // Tin nhắn mới nhất giữa 2 user
    @Query("""
        SELECT m FROM Message m
        WHERE (m.sender.id = :userId1 AND m.receiver.id = :userId2)
           OR (m.sender.id = :userId2 AND m.receiver.id = :userId1)
        ORDER BY m.sentAt DESC
    """)
    List<Message> findLastMessage(@Param("userId1") Long userId1,
                                  @Param("userId2") Long userId2);

    // Đếm unread từ một user cụ thể
    @Query("""
        SELECT COUNT(m) FROM Message m
        WHERE m.sender.id = :senderId AND m.receiver.id = :receiverId AND m.isRead = false
    """)
    long countUnreadFrom(@Param("senderId") Long senderId,
                         @Param("receiverId") Long receiverId);
}
