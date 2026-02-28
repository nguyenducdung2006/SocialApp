package com.example.AsmJava5.repository;

import com.example.AsmJava5.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u " +
            "WHERE u.id != :userId AND u.id NOT IN " +
            "(SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId) " +
            "ORDER BY (SELECT COUNT(f2) FROM Follow f2 WHERE f2.following.id = u.id) DESC")
    java.util.List<User> findSuggestedUsers(@org.springframework.data.repository.query.Param("userId") Long userId, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u " +
            "ORDER BY (SELECT COUNT(f) FROM Follow f WHERE f.following.id = u.id) DESC")
    java.util.List<User> findSuggestedUsersGuest(org.springframework.data.domain.Pageable pageable);
}
