package com.example.AsmJava5.controller;

import com.example.AsmJava5.model.User;
import com.example.AsmJava5.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/internal/admin-setup")
@RequiredArgsConstructor
public class AdminSetupController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Chỉ cho phép localhost truy cập (127.0.0.1 hoặc 0:0:0:0:0:0:0:1)
    private boolean isLocalhost(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        return "127.0.0.1".equals(remoteAddr) || "0:0:0:0:0:0:0:1".equals(remoteAddr);
    }

    @GetMapping
    public String showSetupForm(HttpServletRequest request, Model model) {
        if (!isLocalhost(request)) {
            return "redirect:/auth/login"; // Chặn nếu không phải localhost
        }
        return "admin-setup"; // Render form
    }

    @PostMapping
    public String createAdmin(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String fullName,
            HttpServletRequest request,
            RedirectAttributes ra) {
        
        if (!isLocalhost(request)) {
            return "redirect:/auth/login";
        }

        if (userRepository.existsByUsername(username)) {
            ra.addFlashAttribute("error", "Username đã tồn tại!");
            return "redirect:/internal/admin-setup";
        }

        if (userRepository.existsByEmail(email)) {
            ra.addFlashAttribute("error", "Email đã tồn tại!");
            return "redirect:/internal/admin-setup";
        }

        User admin = new User();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setFullName(fullName);
        admin.setRole("ADMIN");

        userRepository.save(admin);

        ra.addFlashAttribute("success", "Tài khoản Admin đã được tạo thành công! Bạn có thể lấy tài khoản này để đăng nhập.");
        return "redirect:/internal/admin-setup";
    }
}
