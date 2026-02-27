package com.example.AsmJava5.service;

import com.example.AsmJava5.model.*;
import com.example.AsmJava5.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopItemRepository shopItemRepository;
    private final UserPurchaseRepository userPurchaseRepository;
    private final UserRepository userRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    public List<ShopItem> getAvailableItems() {
        return shopItemRepository.findAvailableItems();
    }

    public List<UserPurchase> getUserPurchases(Long userId) {
        return userPurchaseRepository.findByUserId(userId);
    }

    @Transactional
    public void buyItem(User user, Long itemId) {
        ShopItem item = shopItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Vật phẩm không tồn tại!"));

        if (!item.getIsAvailable())
            throw new RuntimeException("Vật phẩm không còn bán!");

        if (List.of("NEWSFEED_THEME","PROFILE_THEME","PROFILE_BG")
                .contains(item.getItemType()))
            throw new RuntimeException("Loại vật phẩm này không hỗ trợ!");

        if (userPurchaseRepository.existsByUserIdAndItemId(user.getId(), itemId))
            throw new RuntimeException("Bạn đã sở hữu vật phẩm này!");

        if (user.getWalletBalance().compareTo(item.getPrice()) < 0)
            throw new RuntimeException("Số dư không đủ! Hãy nạp thêm tiền.");

        user.setWalletBalance(user.getWalletBalance().subtract(item.getPrice()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        UserPurchase purchase = new UserPurchase();
        purchase.setUser(user);
        purchase.setItem(item);
        purchase.setPurchasePrice(item.getPrice());
        purchase.setIsEquipped(false);
        purchase.setPurchasedAt(LocalDateTime.now());
        userPurchaseRepository.save(purchase);

        WalletTransaction tx = new WalletTransaction();
        tx.setUser(user);
        tx.setAmount(item.getPrice().negate());
        tx.setTransactionType("PURCHASE");
        tx.setDescription("Mua vật phẩm: " + item.getItemName());
        tx.setCreatedAt(LocalDateTime.now());
        walletTransactionRepository.save(tx);
    }

    @Transactional
    public void equipItem(User user, Long purchaseId) {
        UserPurchase purchase = userPurchaseRepository
                .findByIdAndUserId(purchaseId, user.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vật phẩm!"));

        String type = purchase.getItem().getItemType();

        List<UserPurchase> same = userPurchaseRepository
                .findByUserIdAndItemItemType(user.getId(), type);
        same.forEach(p -> p.setIsEquipped(false));
        userPurchaseRepository.saveAll(same);

        purchase.setIsEquipped(true);
        userPurchaseRepository.save(purchase);

        switch (type) {
            case "FRAME" -> user.setEquippedFrame(purchase.getItem().getImageUrl());
            case "BADGE" -> user.setEquippedNameFx(purchase.getItem().getImageUrl());
            case "CHAT_FX" -> user.setEquippedChatFx(purchase.getItem().getImageUrl());
            default -> throw new RuntimeException("Loại vật phẩm không hợp lệ: " + type);
        }
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void unequipItem(User user, String type) {
        switch (type.toUpperCase()) {
            case "FRAME"  -> user.setEquippedFrame(null);
            case "BADGE"  -> user.setEquippedNameFx(null);
            case "CHAT_FX"-> user.setEquippedChatFx(null);
            default -> throw new RuntimeException("Loại không hợp lệ: " + type);
        }
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        List<UserPurchase> purchases = userPurchaseRepository
                .findByUserIdAndItemItemType(user.getId(), type.toUpperCase());
        purchases.forEach(p -> p.setIsEquipped(false));
        userPurchaseRepository.saveAll(purchases);
    }
}
