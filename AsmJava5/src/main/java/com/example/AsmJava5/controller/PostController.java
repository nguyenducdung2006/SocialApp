package com.example.AsmJava5.controller;

import com.example.AsmJava5.model.*;
import com.example.AsmJava5.repository.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PostController {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ReactionRepository reactionRepository;
    private final ReportRepository reportRepository;
    private final FollowRepository followRepository;
    private final SavedPostRepository savedPostRepository;

    // ===== XEM CHI TIẾT BÀI ĐĂNG =====
    @GetMapping("/post/{id}")
    public String viewPost(@PathVariable Long id, Model model, HttpSession session) {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null || post.getIsDeleted()) return "redirect:/home";

        // Tăng views
        post.setViews(post.getViews() + 1);
        postRepository.save(post);

        String email = (String) session.getAttribute("email");
        User currentUser = null;
        boolean isReacted = false;
        String myReactionType = null;

        if (email != null) {
            currentUser = userRepository.findByEmail(email).orElse(null);
            if (currentUser != null) {
                Reaction existing = reactionRepository
                        .findByPostIdAndUserId(id, currentUser.getId());
                if (existing != null) {
                    isReacted = true;
                    myReactionType = existing.getReactionType();
                }
            }
        }

        List<Comment> comments = commentRepository
                .findByPostIdAndIsDeletedFalseOrderByCreatedAtDesc(id);

        boolean isOwner = currentUser != null &&
                currentUser.getId().equals(post.getUser().getId());

        boolean isFollowing = false;
        if (currentUser != null && !isOwner) {
            isFollowing = followRepository.existsByFollowerIdAndFollowingId(
                    currentUser.getId(), post.getUser().getId()
            );
        }

        // Kiểm tra xem đã lưu chưa
        boolean isSaved = false;
        if (currentUser != null) {
            isSaved = savedPostRepository.existsByUserIdAndPostId(currentUser.getId(), id);
        }

        model.addAttribute("post", post);
        model.addAttribute("isFollowing", isFollowing);
        model.addAttribute("isSaved", isSaved);
        model.addAttribute("comments", comments);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isReacted", isReacted);
        model.addAttribute("myReactionType", myReactionType);
        model.addAttribute("isOwner", isOwner);
        return "post-detail";
    }

    // ===== XÓA BÀI ĐĂNG =====
    @PostMapping("/post/{id}/delete")
    public String deletePost(@PathVariable Long id,
                             HttpSession session,
                             RedirectAttributes ra) {
        String email = (String) session.getAttribute("email");
        if (email == null) return "redirect:/auth/login";

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return "redirect:/auth/login";

        Post post = postRepository.findById(id).orElse(null);
        if (post == null) {
            ra.addFlashAttribute("error", "Bài không tồn tại!");
            return "redirect:/home";
        }

        if (!post.getUser().getId().equals(user.getId())) {
            ra.addFlashAttribute("error", "Bạn không có quyền xóa!");
            return "redirect:/post/" + id;
        }

        post.setIsDeleted(true);
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);

        ra.addFlashAttribute("success", "Đã xóa bài đăng!");
        return "redirect:/profile";
    }

    // ===== SỬA BÀI ĐĂNG — Hiện form =====
    @GetMapping("/post/{id}/edit")
    public String editPostForm(@PathVariable Long id,
                               Model model,
                               HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null) return "redirect:/auth/login";

        User user = userRepository.findByEmail(email).orElse(null);
        Post post = postRepository.findById(id).orElse(null);

        if (post == null || post.getIsDeleted()) return "redirect:/home";
        if (!post.getUser().getId().equals(user.getId())) return "redirect:/post/" + id;

        model.addAttribute("post", post);
        model.addAttribute("currentUser", user);
        return "post-edit";
    }

    // ===== SỬA BÀI ĐĂNG — Submit =====
    @PostMapping("/post/{id}/edit")
    public String editPost(@PathVariable Long id,
                           @RequestParam String title,
                           @RequestParam(required = false) String description,
                           @RequestParam(required = false) String tags,
                           @RequestParam(required = false) org.springframework.web.multipart.MultipartFile image,
                           HttpSession session,
                           RedirectAttributes ra) throws Exception {
        String email = (String) session.getAttribute("email");
        if (email == null) return "redirect:/auth/login";

        User user = userRepository.findByEmail(email).orElse(null);
        Post post = postRepository.findById(id).orElse(null);

        if (post == null || post.getIsDeleted()) return "redirect:/home";
        if (!post.getUser().getId().equals(user.getId())) return "redirect:/post/" + id;

        post.setTitle(title);
        post.setDescription(description);
        post.setTags(tags);
        post.setUpdatedAt(LocalDateTime.now());

        // Cập nhật ảnh nếu có upload mới
        if (image != null && !image.isEmpty()) {
            post.setImageData(image.getBytes());
            post.setImageName(image.getOriginalFilename());
        }

        postRepository.save(post);
        ra.addFlashAttribute("success", "Đã cập nhật bài đăng!");
        return "redirect:/post/" + id;
    }

    // ===== REACT (LIKE/LOVE/...) =====
    @PostMapping("/post/{id}/react")
    @ResponseBody
    public ResponseEntity<?> reactPost(@PathVariable Long id,
                                       @RequestParam(defaultValue = "LIKE") String type,
                                       HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null) return ResponseEntity.status(401).body("Chưa đăng nhập");

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(401).body("Không tìm thấy user");

        Post post = postRepository.findById(id).orElse(null);
        if (post == null) return ResponseEntity.notFound().build();

        Reaction existing = reactionRepository.findByPostIdAndUserId(id, user.getId());

        boolean nowReacted;
        String currentType;

        if (existing != null) {
            if (existing.getReactionType().equals(type)) {
                // Bấm lại cùng loại → bỏ react
                reactionRepository.delete(existing);
                post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
                nowReacted = false;
                currentType = null;
            } else {
                // Đổi loại react
                existing.setReactionType(type);
                reactionRepository.save(existing);
                nowReacted = true;
                currentType = type;
            }
        } else {
            // Thêm react mới
            Reaction reaction = Reaction.builder()
                    .post(post)
                    .user(user)
                    .reactionType(type)
                    .createdAt(LocalDateTime.now())
                    .build();
            reactionRepository.save(reaction);
            post.setLikesCount(post.getLikesCount() + 1);
            nowReacted = true;
            currentType = type;
        }

        postRepository.save(post);

        return ResponseEntity.ok(Map.of(
                "reacted", nowReacted,
                "reactionType", currentType != null ? currentType : "",
                "count", post.getLikesCount()
        ));
    }

    // ===== LƯU BÀI ĐĂNG (BOOKMARK) =====
    @PostMapping("/post/{id}/save")
    @ResponseBody
    public ResponseEntity<?> savePost(@PathVariable Long id, HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null) return ResponseEntity.status(401).body("Chưa đăng nhập");

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(401).body("Không tìm thấy user");

        Post post = postRepository.findById(id).orElse(null);
        if (post == null) return ResponseEntity.notFound().build();

        boolean existing = savedPostRepository.existsByUserIdAndPostId(user.getId(), id);
        if (existing) {
            savedPostRepository.deleteByUserIdAndPostId(user.getId(), id);
            return ResponseEntity.ok(Map.of("saved", false));
        } else {
            SavedPost savedPost = SavedPost.builder()
                .user(user)
                .post(post)
                .createdAt(LocalDateTime.now())
                .build();
            savedPostRepository.save(savedPost);
            return ResponseEntity.ok(Map.of("saved", true));
        }
    }

    // ===== BÌNH LUẬN =====
    @PostMapping("/post/{id}/comment")
    public String addComment(@PathVariable Long id,
                             @RequestParam String content,
                             HttpSession session,
                             RedirectAttributes ra) {
        String email = (String) session.getAttribute("email");
        if (email == null) return "redirect:/auth/login";

        if (content == null || content.trim().isEmpty()) {
            ra.addFlashAttribute("error", "Bình luận không được để trống!");
            return "redirect:/post/" + id;
        }

        User user = userRepository.findByEmail(email).orElse(null);
        Post post = postRepository.findById(id).orElse(null);
        if (post == null || post.getIsDeleted()) return "redirect:/home";

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .content(content.trim())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        commentRepository.save(comment);
        return "redirect:/post/" + id;
    }

    // ===== XÓA BÌNH LUẬN =====
    @PostMapping("/comment/{commentId}/delete")
    public String deleteComment(@PathVariable Long commentId,
                                HttpSession session,
                                RedirectAttributes ra) {
        String email = (String) session.getAttribute("email");
        if (email == null) return "redirect:/auth/login";

        User user = userRepository.findByEmail(email).orElse(null);
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) return "redirect:/home";

        boolean isCommentOwner = comment.getUser().getId().equals(user.getId());
        boolean isPostOwner = comment.getPost().getUser().getId().equals(user.getId());

        if (isCommentOwner || isPostOwner) {
            comment.setIsDeleted(true);
            comment.setUpdatedAt(LocalDateTime.now());
            commentRepository.save(comment);
        } else {
            ra.addFlashAttribute("error", "Bạn không có quyền xóa bình luận này!");
        }

        return "redirect:/post/" + comment.getPost().getId();
    }

    // ===== BÁO CÁO BÀI ĐĂNG =====
    @PostMapping("/post/{id}/report")
    @ResponseBody
    public ResponseEntity<?> reportPost(@PathVariable Long id,
                                        @RequestParam String reason,
                                        HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null) return ResponseEntity.status(401).body("Chưa đăng nhập");

        if (reason == null || reason.trim().isEmpty())
            return ResponseEntity.badRequest().body("Lý do không được để trống!");

        User reporter = userRepository.findByEmail(email).orElse(null);
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) return ResponseEntity.notFound().build();

        // Không thể tự báo cáo bài mình
        if (post.getUser().getId().equals(reporter.getId()))
            return ResponseEntity.badRequest().body("Không thể báo cáo bài của chính mình!");

        Report report = Report.builder()
                .post(post)
                .reporter(reporter)
                .reason(reason.trim())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        reportRepository.save(report);
        return ResponseEntity.ok("Đã gửi báo cáo! Chúng tôi sẽ xem xét sớm.");
    }
}
