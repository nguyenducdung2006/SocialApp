package com.example.AsmJava5.repository;

import com.example.AsmJava5.model.UserPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserPurchaseRepository extends JpaRepository<UserPurchase, Long> {

    List<UserPurchase> findByUserId(Long userId);

    Optional<UserPurchase> findByIdAndUserId(Long id, Long userId);

    List<UserPurchase> findByUserIdAndItemItemType(Long userId, String itemType);

    boolean existsByUserIdAndItemId(Long userId, Long itemId);
}
