package com.example.AsmJava5.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "posts")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String title;

    @Column(name = "image_url")
    private String imageUrl;

    @Lob
    @Column(name = "image_data", columnDefinition = "VARBINARY(MAX)")
    private byte[] imageData;

    @Column(name = "image_name")
    private String imageName;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    private String tags;

    @Builder.Default
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    private int views = 0;

    @Column(name = "likes_count")
    private int likesCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
