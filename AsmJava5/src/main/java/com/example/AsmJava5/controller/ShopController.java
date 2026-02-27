package com.example.AsmJava5.controller;

import com.example.AsmJava5.model.*;
import com.example.AsmJava5.repository.UserRepository;
import com.example.AsmJava5.service.ShopService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/shop")
public class ShopController {

    private final ShopService shopService;
    private final UserRepository userRepository;

    private User getUser(HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null) return null;
        return userRepository.findByEmail(email).orElse(null);
    }

    @GetMapping
    public String shop(Model model, HttpSession session) {
        User user = getUser(session);
        if (user == null) return "redirect:/auth/login";

        List<ShopItem> items = shopService.getAvailableItems();
        List<UserPurchase> purchases = shopService.getUserPurchases(user.getId());
        List<Long> ownedItemIds = purchases.stream()
                .map(p -> p.getItem().getId())
                .toList();

        model.addAttribute("items", items);
        model.addAttribute("purchases", purchases);
        model.addAttribute("ownedItemIds", ownedItemIds);
        model.addAttribute("user", user);
        return "shop";
    }

    @PostMapping("/buy/{itemId}")
    public String buyItem(@PathVariable Long itemId,
                          HttpSession session, RedirectAttributes ra) {
        try {
            User user = getUser(session);
            if (user == null) return "redirect:/auth/login";
            shopService.buyItem(user, itemId);
            ra.addFlashAttribute("success", "Mua thành công! ✅");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/shop";
    }
}
