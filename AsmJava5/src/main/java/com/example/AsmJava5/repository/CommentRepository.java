package com.example.AsmJava5.repository;

import com.example.AsmJava5.model.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    @EntityGraph(attributePaths = {"user"})
    List<Comment> findByPostIdAndIsDeletedFalseOrderByCreatedAtDesc(Long postId);
}
