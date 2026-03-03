package com.example.AsmJava5.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người nhận thông báo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private User recipient;

    // Người trigger thông báo (có thể null với system notif)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    // Loại thông báo
    // FOLLOW, NEW_POST, COMMENT, POST_DELETED, POST_REPORTED
    @Column(nullable = false, length = 50)
    private String type;

    // Nội dung thông báo
    @Column(columnDefinition = "NVARCHAR(500)")
    private String message;

    // Link dẫn đến (post, profile, ...)
    @Column(length = 200)
    private String link;

    @Builder.Default
    @Column(name = "is_read")
    private Boolean isRead = false;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
