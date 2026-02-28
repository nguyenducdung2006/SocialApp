package com.example.AsmJava5.controller;

import com.example.AsmJava5.model.User;
import com.example.AsmJava5.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @GetMapping
    public String settingsPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");
        if (email == null) return "redirect:/auth/login";

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return "redirect:/auth/login";

        model.addAttribute("currentUser", user);
        return "settings";
    }

    // ===== ĐỔI MẬT KHẨU =====
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes ra) {
        String email = (String) session.getAttribute("email");
        if (email == null) return "redirect:/auth/login";

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return "redirect:/auth/login";

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            ra.addFlashAttribute("errorPassword", "Mật khẩu hiện tại không đúng!");
            return "redirect:/settings";
        }

        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("errorPassword", "Mật khẩu mới không khớp!");
            return "redirect:/settings";
        }

        if (newPassword.length() < 6) {
            ra.addFlashAttribute("errorPassword", "Mật khẩu mới phải có ít nhất 6 ký tự!");
            return "redirect:/settings";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        ra.addFlashAttribute("successPassword", "Đổi mật khẩu thành công!");
        return "redirect:/settings";
    }

    // ===== ĐỔI EMAIL =====
    @PostMapping("/change-email")
    public String changeEmail(@RequestParam String currentPassword,
                              @RequestParam String newEmail,
                              HttpSession session,
                              RedirectAttributes ra) {
        String email = (String) session.getAttribute("email");
        if (email == null) return "redirect:/auth/login";

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return "redirect:/auth/login";

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            ra.addFlashAttribute("errorEmail", "Mật khẩu hiện tại không đúng!");
            return "redirect:/settings";
        }

        if (userRepository.findByEmail(newEmail).isPresent()) {
            ra.addFlashAttribute("errorEmail", "Email này đã được sử dụng bởi tài khoản khác!");
            return "redirect:/settings";
        }

        user.setEmail(newEmail);
        userRepository.save(user);
        session.setAttribute("email", newEmail);
        ra.addFlashAttribute("successEmail", "Đổi email thành công!");
        return "redirect:/settings";
    }

    // ===== XÓA TÀI KHOẢN =====
    @PostMapping("/delete-account")
    public String deleteAccount(@RequestParam String currentPassword,
                                HttpSession session,
                                RedirectAttributes ra) {
        String email = (String) session.getAttribute("email");
        if (email == null) return "redirect:/auth/login";

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return "redirect:/auth/login";

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            ra.addFlashAttribute("errorDelete", "Mật khẩu không đúng! Xóa tài khoản thất bại.");
            return "redirect:/settings";
        }

        // Soft-delete: đặt email thành null-like và ban user
        user.setIsBanned(true);
        user.setEmail("deleted_" + user.getId() + "@deleted.com");
        user.setUsername("[Tài khoản đã xóa]");
        userRepository.save(user);

        session.invalidate();
        return "redirect:/auth/login?deleted=1";
    }
}
