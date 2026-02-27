package com.example.AsmJava5.controller;

import com.example.AsmJava5.model.*;
import com.example.AsmJava5.repository.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/wallet")
public class WalletController {

    private final UserRepository userRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    private User getUser(HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null) return null;
        return userRepository.findByEmail(email).orElse(null);
    }

    @GetMapping("/recharge")
    public String rechargePage(Model model, HttpSession session) {
        User user = getUser(session);
        if (user == null) return "redirect:/auth/login";
        List<WalletTransaction> history =
                walletTransactionRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("history", history);
        return "wallet";
    }

    @PostMapping("/recharge")
    public String doRecharge(@RequestParam BigDecimal amount,
                             HttpSession session, RedirectAttributes ra) {
        try {
            User user = getUser(session);
            if (user == null) return "redirect:/auth/login";
            if (amount.compareTo(BigDecimal.valueOf(1000)) < 0)
                throw new RuntimeException("Số tiền nạp tối thiểu 1,000đ!");
            if (amount.compareTo(BigDecimal.valueOf(10_000_000)) > 0)
                throw new RuntimeException("Số tiền nạp tối đa 10,000,000đ!");

            user.setWalletBalance(user.getWalletBalance().add(amount));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            WalletTransaction tx = new WalletTransaction();
            tx.setUser(user);
            tx.setAmount(amount);
            tx.setTransactionType("DEPOSIT");
            tx.setDescription("Nạp tiền vào ví");
            tx.setCreatedAt(LocalDateTime.now());
            walletTransactionRepository.save(tx);

            ra.addFlashAttribute("success",
                    "Nạp thành công " +
                            String.format("%,.0f", amount.doubleValue()) + "đ! 🎉");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/wallet/recharge";
    }
}
