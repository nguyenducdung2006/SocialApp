package com.example.AsmJava5.repository;

import com.example.AsmJava5.model.SavedPost;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedPostRepository extends JpaRepository<SavedPost, Long> {

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    Optional<SavedPost> findByUserIdAndPostId(Long userId, Long postId);

    @EntityGraph(attributePaths = {"post", "post.user"})
    List<SavedPost> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    void deleteByUserIdAndPostId(Long userId, Long postId);
    
    // Xóa toàn bộ saved cho 1 post 
    void deleteByPostId(Long postId);
}
