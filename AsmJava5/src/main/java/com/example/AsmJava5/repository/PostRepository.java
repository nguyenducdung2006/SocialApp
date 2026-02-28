package com.example.AsmJava5.repository;

import com.example.AsmJava5.model.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = {"user"})
    Optional<Post> findById(Long id);

    // Có Pageable — dùng trong HomeController
    @EntityGraph(attributePaths = {"user"})
    List<Post> findByIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    // Không Pageable — dùng trong PostService
    @EntityGraph(attributePaths = {"user"})
    List<Post> findByIsDeletedFalseOrderByCreatedAtDesc();

    // Có Pageable — ranking
    @EntityGraph(attributePaths = {"user"})
    org.springframework.data.domain.Page<Post> findByIsDeletedFalseOrderByLikesCountDesc(Pageable pageable);

    // Theo userId — dùng trong PostService và ProfileController
    @EntityGraph(attributePaths = {"user"})
    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Theo userId không xóa
    @EntityGraph(attributePaths = {"user"})
    List<Post> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId);

    // Đếm tổng số bài viết của User
    long countByUserIdAndIsDeletedFalse(Long userId);

    // Lấy toàn bộ tags (List)
    List<Post> findAllByIsDeletedFalse();

    // Lấy danh sách phân trang cho API Load More
    @EntityGraph(attributePaths = {"user"})
    org.springframework.data.domain.Page<Post> findAllByIsDeletedFalse(org.springframework.data.domain.Pageable pageable);

    // Dành cho chức năng Xu Hướng (Sắp xếp theo View)
    @EntityGraph(attributePaths = {"user"})
    org.springframework.data.domain.Page<Post> findAllByIsDeletedFalseOrderByViewsDesc(org.springframework.data.domain.Pageable pageable);

    // Dùng để clear sample
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query("DELETE FROM Post p WHERE p.imageData IS NULL")
    void deleteByImageDataIsNull();

    // Chức năng Tìm kiếm
    @EntityGraph(attributePaths = {"user"})
    @org.springframework.data.jpa.repository.Query("SELECT p FROM Post p WHERE p.isDeleted = false AND (" +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.tags) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.user.username) LIKE LOWER(CONCAT('%', :keyword, '%')) ) " +
            "ORDER BY p.createdAt DESC")
    List<Post> searchPosts(@org.springframework.data.repository.query.Param("keyword") String keyword);

    // Lấy bài viết của người đang Follow
    @EntityGraph(attributePaths = {"user"})
    @org.springframework.data.jpa.repository.Query("SELECT p FROM Post p WHERE p.isDeleted = false AND p.user.id IN " +
            "(SELECT f.following.id FROM Follow f WHERE f.follower.id = :followerId)")
    org.springframework.data.domain.Page<Post> findPostsFromFollowing(@org.springframework.data.repository.query.Param("followerId") Long followerId, org.springframework.data.domain.Pageable pageable);

    // Dành cho chức năng Phổ biến (Tổng hợp Views & Likes => Hot score)
    @EntityGraph(attributePaths = {"user"})
    @org.springframework.data.jpa.repository.Query("SELECT p FROM Post p WHERE p.isDeleted = false ORDER BY (p.likesCount * 2 + p.views) DESC")
    org.springframework.data.domain.Page<Post> findPopularPosts(org.springframework.data.domain.Pageable pageable);
}
