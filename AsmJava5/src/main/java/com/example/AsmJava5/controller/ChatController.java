package com.example.AsmJava5.controller;

import com.example.AsmJava5.model.Message;
import com.example.AsmJava5.model.User;
import com.example.AsmJava5.repository.MessageRepository;
import com.example.AsmJava5.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    private User getUser(HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null) return null;
        return userRepository.findByEmail(email).orElse(null);
    }

    // Trang chat chính (danh sách conversations)
    @GetMapping
    public String chatHome(Model model, HttpSession session) {
        User me = getUser(session);
        if (me == null) return "redirect:/auth/login";

        List<Map<String, Object>> conversations = buildConversationList(me);
        model.addAttribute("conversations", conversations);
        model.addAttribute("me", me);
        model.addAttribute("activeChatUser", null);
        model.addAttribute("messages", List.of());
        return "chat";
    }

    // Chat với một user cụ thể
    @GetMapping("/{userId}")
    public String chatWith(@PathVariable Long userId, Model model, HttpSession session) {
        User me = getUser(session);
        if (me == null) return "redirect:/auth/login";

        User other = userRepository.findById(userId).orElse(null);
        if (other == null) return "redirect:/chat";

        // Đánh dấu đã đọc
        messageRepository.markAsRead(userId, me.getId());

        List<Message> messages = messageRepository.findConversation(me.getId(), userId);
        List<Map<String, Object>> conversations = buildConversationList(me);

        model.addAttribute("conversations", conversations);
        model.addAttribute("me", me);
        model.addAttribute("activeChatUser", other);
        model.addAttribute("messages", messages);
        return "chat";
    }

    // Đánh dấu đã đọc (AJAX)
    @PostMapping("/{userId}/read")
    @ResponseBody
    public ResponseEntity<?> markRead(@PathVariable Long userId, HttpSession session) {
        User me = getUser(session);
        if (me == null) return ResponseEntity.status(401).build();
        messageRepository.markAsRead(userId, me.getId());
        return ResponseEntity.ok().build();
    }

    // Số unread (AJAX cho navbar badge)
    @GetMapping("/unread-count")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> unreadCount(HttpSession session) {
        User me = getUser(session);
        if (me == null) return ResponseEntity.status(401).body(Map.of("count", 0L));
        long count = messageRepository.countUnread(me.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    // Helper: build danh sách conversations với preview
    private List<Map<String, Object>> buildConversationList(User me) {
        List<Object[]> rows = messageRepository.findRecentConversationUserIds(me.getId());
        List<Map<String, Object>> conversations = new ArrayList<>();
        for (Object[] row : rows) {
            Long otherId = ((Number) row[0]).longValue();
            userRepository.findById(otherId).ifPresent(other -> {
                Map<String, Object> conv = new LinkedHashMap<>();
                conv.put("user", other);

                List<Message> lastMsgs = messageRepository.findLastMessage(me.getId(), otherId);
                if (!lastMsgs.isEmpty()) {
                    Message last = lastMsgs.get(0);
                    String preview = last.getContent();
                    if (preview.length() > 35) preview = preview.substring(0, 35) + "...";
                    conv.put("lastMessage", preview);
                    conv.put("lastTime", last.getSentAt());
                    conv.put("isMine", last.getSender().getId().equals(me.getId()));
                } else {
                    conv.put("lastMessage", "");
                    conv.put("lastTime", null);
                    conv.put("isMine", false);
                }

                long unread = messageRepository.countUnreadFrom(otherId, me.getId());
                conv.put("unread", unread);
                conversations.add(conv);
            });
        }
        return conversations;
    }
}
