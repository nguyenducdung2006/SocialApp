package com.example.AsmJava5.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "user_purchases")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UserPurchase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id")
    private ShopItem item;

    @Column(name = "purchase_price")
    private BigDecimal purchasePrice;

    @Builder.Default
    @Column(name = "is_equipped")
    private Boolean isEquipped = false;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "purchased_at")
    private LocalDateTime purchasedAt = LocalDateTime.now();
}
