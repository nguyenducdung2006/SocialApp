package com.example.AsmJava5.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String username;
    private String email;
    private String password;

    @Column(name = "full_name")
    private String fullName;
    // Thêm field này vào User.java
    @Lob
    @Column(name = "avatar_data", columnDefinition = "VARBINARY(MAX)")
    private byte[] avatarData;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String bio;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "wallet_balance")
    private BigDecimal walletBalance = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "is_banned")
    private Boolean isBanned = false;

    @Column(name = "ban_until")
    private LocalDateTime banUntil;

    private String role = "USER";

    @Column(name = "equipped_frame", columnDefinition = "NVARCHAR(MAX)")
    private String equippedFrame;

    @Column(name = "equipped_bg", columnDefinition = "NVARCHAR(MAX)")
    private String equippedBg;

    @Column(name = "equipped_name_fx", columnDefinition = "NVARCHAR(MAX)")
    private String equippedNameFx;

    @Column(name = "equipped_chat_fx", columnDefinition = "NVARCHAR(MAX)")
    private String equippedChatFx;

    @Column(name = "equipped_home_bg", columnDefinition = "NVARCHAR(MAX)")
    private String equippedHomeBg;


    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    // Thêm vào User.java
    @Column(name = "equipped_item_name")
    private String equippedItemName;


}
