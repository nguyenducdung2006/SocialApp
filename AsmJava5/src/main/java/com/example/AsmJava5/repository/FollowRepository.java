package com.example.AsmJava5.repository;

import com.example.AsmJava5.model.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Follow f WHERE f.follower.id = :followerId AND f.following.id = :followingId")
    void deleteByFollowerIdAndFollowingId(@Param("followerId") Long followerId,
                                          @Param("followingId") Long followingId);

    long countByFollowingId(Long followingId);
    long countByFollowerId(Long followerId);
}
