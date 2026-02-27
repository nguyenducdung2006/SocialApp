package com.example.AsmJava5.repository;

import com.example.AsmJava5.model.ShopItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShopItemRepository extends JpaRepository<ShopItem, Long> {

    // Chỉ lấy FRAME, BADGE, STICKER — loại bỏ NEWSFEED_THEME và PROFILE_THEME
    @Query("SELECT s FROM ShopItem s WHERE s.isAvailable = true " +
            "AND s.itemType NOT IN ('NEWSFEED_THEME', 'PROFILE_THEME', 'PROFILE_BG')")
    List<ShopItem> findAvailableItems();

    List<ShopItem> findByItemType(String itemType);
}
