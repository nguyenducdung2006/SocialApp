package com.example.AsmJava5.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "reactions")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Reaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reaction_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    @Column(name = "reaction_type", nullable = false)
    private String reactionType = "LIKE";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
