package com.example.AsmJava5.controller;

import com.example.AsmJava5.model.Message;
import com.example.AsmJava5.model.User;
import com.example.AsmJava5.repository.MessageRepository;
import com.example.AsmJava5.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    // client gửi: /app/chat.send
    // payload: { senderId, receiverId, content }
    @MessageMapping("/chat.send")
    public void handleChatMessage(@Payload Map<String, Object> payload) {
        try {
            Long senderId = Long.valueOf(payload.get("senderId").toString());
            Long receiverId = Long.valueOf(payload.get("receiverId").toString());
            String content = payload.get("content").toString().trim();

            if (content.isEmpty()) return;

            User sender = userRepository.findById(senderId).orElse(null);
            User receiver = userRepository.findById(receiverId).orElse(null);
            if (sender == null || receiver == null) return;

            // Lưu DB
            Message msg = Message.builder()
                    .sender(sender)
                    .receiver(receiver)
                    .content(content)
                    .isRead(false)
                    .sentAt(LocalDateTime.now())
                    .build();
            Message saved = messageRepository.save(msg);

            // Build payload gởi về client
            Map<String, Object> response = new HashMap<>();
            response.put("id", saved.getId());
            response.put("senderId", senderId);
            response.put("senderName", sender.getFullName() != null ? sender.getFullName() : sender.getUsername());
            response.put("receiverId", receiverId);
            response.put("content", content);
            response.put("sentAt", saved.getSentAt().format(DateTimeFormatter.ofPattern("HH:mm dd/MM")));

            // Gửi tới topic của receiver
            messagingTemplate.convertAndSend("/topic/chat-" + receiverId, (Object) response);
            // Chỉ gửi cho sender nếu sender khác receiver (tránh gửi 2 lần khi tự chat với mình)
            if (!senderId.equals(receiverId)) {
                messagingTemplate.convertAndSend("/topic/chat-" + senderId, (Object) response);
            }

        } catch (Exception e) {
            // log error silently
        }
    }

    // Typing indicator: client gửi /app/chat.typing
    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload Map<String, Object> payload) {
        try {
            Long senderId = Long.valueOf(payload.get("senderId").toString());
            Long receiverId = Long.valueOf(payload.get("receiverId").toString());
            Boolean isTyping = Boolean.valueOf(payload.get("typing").toString());

            Map<String, Object> typingPayload = new HashMap<>();
            typingPayload.put("senderId", senderId);
            typingPayload.put("typing", isTyping);

            messagingTemplate.convertAndSend("/topic/chat-" + receiverId, (Object) typingPayload);
        } catch (Exception ignored) {}
    }
}
