package com.example.AsmJava5.repository;

import com.example.AsmJava5.model.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    // Trả về object Reaction để kiểm tra loại react
    @Query("SELECT r FROM Reaction r WHERE r.post.id = :postId AND r.user.id = :userId")
    Reaction findByPostIdAndUserId(Long postId, Long userId);

    @Transactional
    void deleteByPostIdAndUserId(Long postId, Long userId);

    long countByPostId(Long postId);

    // Lấy danh sách Reaction (Yêu thích) của một người dùng
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"post", "post.user"})
    java.util.List<Reaction> findByUserIdOrderByCreatedAtDesc(Long userId);
}
