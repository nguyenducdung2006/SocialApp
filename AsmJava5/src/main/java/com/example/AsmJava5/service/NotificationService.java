package com.example.AsmJava5.service;

import com.example.AsmJava5.model.Notification;
import com.example.AsmJava5.model.Post;
import com.example.AsmJava5.model.User;
import com.example.AsmJava5.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Tạo và gửi thông báo real-time qua WebSocket.
     * Dùng /topic/user-{id} thay vì user queue vì không có Spring Security Principal.
     */
    private void send(User recipient, User actor, String type, String message, String link) {
        if (recipient == null) return;
        // Không gửi thông báo cho chính mình
        if (actor != null && recipient.getId().equals(actor.getId())) return;

        Notification notif = Notification.builder()
                .recipient(recipient)
                .actor(actor)
                .type(type)
                .message(message)
                .link(link)
                .build();
        notificationRepository.save(notif);

        // Push real-time — dùng topic theo userId (không cần Principal)
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", notif.getId());
        payload.put("type", type);
        payload.put("message", message);
        payload.put("link", link);
        payload.put("actorName", actor != null ? actor.getUsername() : "System");
        payload.put("actorId", actor != null ? actor.getId() : null);

        try {
            // Broadcast đến topic riêng của user nhận thông báo
            messagingTemplate.convertAndSend(
                    "/topic/user-" + recipient.getId(),
                    (Object) payload
            );
        } catch (Exception ignored) {
            // User chưa kết nối WebSocket — thông báo vẫn lưu DB
        }
    }

    // ── THEO DÕI ──────────────────────────────────────────
    public void notifyFollow(User follower, User following) {
        send(following, follower,
                "FOLLOW",
                follower.getUsername() + " đã theo dõi bạn",
                "/profile/view/" + follower.getId());
    }

    // ── ĐĂNG BÀI MỚI ─────────────────────────────────────
    public void notifyNewPost(User author, Post post, Iterable<User> followers) {
        for (User follower : followers) {
            send(follower, author,
                    "NEW_POST",
                    author.getUsername() + " vừa đăng bài: " + post.getTitle(),
                    "/post/" + post.getId());
        }
    }

    // ── BÌNH LUẬN ────────────────────────────────────────
    public void notifyComment(User commenter, Post post) {
        if (post.getUser() == null) return;
        send(post.getUser(), commenter,
                "COMMENT",
                commenter.getUsername() + " đã bình luận vào bài: " + post.getTitle(),
                "/post/" + post.getId());
    }

    // ── BÀI BỊ XÓA BỞI ADMIN ────────────────────────────
    public void notifyPostDeleted(User postOwner, String postTitle) {
        if (postOwner == null) return;
        send(postOwner, null,
                "POST_DELETED",
                "Bài viết \"" + postTitle + "\" của bạn đã bị admin xóa do vi phạm nội dung.",
                "/profile");
    }

    // ── BÁO CÁO BÀI VIẾT ────────────────────────────────
    public void notifyPostReported(User postOwner, String postTitle) {
        if (postOwner == null) return;
        send(postOwner, null,
                "POST_REPORTED",
                "Bài viết \"" + postTitle + "\" của bạn đã bị báo cáo và đang chờ xem xét.",
                "/profile");
    }
}
