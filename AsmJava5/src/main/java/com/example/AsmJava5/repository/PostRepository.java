package com.example.AsmJava5.repository;

import com.example.AsmJava5.model.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // Có Pageable — dùng trong HomeController
    List<Post> findByIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    // Không Pageable — dùng trong PostService
    List<Post> findByIsDeletedFalseOrderByCreatedAtDesc();

    // Có Pageable — ranking
    List<Post> findByIsDeletedFalseOrderByLikesCountDesc(Pageable pageable);

    // Theo userId — dùng trong PostService và ProfileController
    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Theo userId không xóa
    List<Post> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId);
}
