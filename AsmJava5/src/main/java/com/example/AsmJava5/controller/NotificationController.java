package com.example.AsmJava5.controller;

import com.example.AsmJava5.model.Notification;
import com.example.AsmJava5.model.User;
import com.example.AsmJava5.repository.NotificationRepository;
import com.example.AsmJava5.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /** Trang thông báo đầy đủ */
    @GetMapping("/notifications")
    public String notificationsPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");
        if (email == null) return "redirect:/auth/login";
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return "redirect:/auth/login";

        List<Notification> notifications = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(user.getId());

        // Đánh dấu tất cả đã đọc khi vào trang
        notificationRepository.markAllAsRead(user.getId());

        model.addAttribute("notifications", notifications);
        model.addAttribute("sessionUser", user);
        model.addAttribute("currentUser", user);
        return "notifications";
    }

    /** API trả số thông báo chưa đọc */
    @GetMapping("/api/notifications/count")
    @ResponseBody
    public ResponseEntity<?> unreadCount(HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null) return ResponseEntity.ok(Map.of("count", 0));
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.ok(Map.of("count", 0));
        long count = notificationRepository.countByRecipientIdAndIsReadFalse(user.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    /** Mark all as read (AJAX) */
    @PostMapping("/api/notifications/mark-read")
    @ResponseBody
    public ResponseEntity<?> markAllRead(HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();
        notificationRepository.markAllAsRead(user.getId());
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
