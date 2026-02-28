package com.example.AsmJava5.controller;

import com.example.AsmJava5.model.Post;
import com.example.AsmJava5.model.User;
import com.example.AsmJava5.repository.FollowRepository;
import com.example.AsmJava5.repository.PostRepository;
import com.example.AsmJava5.repository.ReactionRepository;
import com.example.AsmJava5.repository.SavedPostRepository;
import com.example.AsmJava5.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final SavedPostRepository savedPostRepository;
    private final ReactionRepository reactionRepository;

    private List<String> getTopTags() {
        List<Post> allPosts = postRepository.findAllByIsDeletedFalse();
        java.util.Map<String, Long> tagCount = new java.util.HashMap<>();
        for (Post p : allPosts) {
            if (p.getTags() != null) {
                String[] splitTags = p.getTags().split(",");
                for (String t : splitTags) {
                    String cleanTag = t.trim().replace("#", "");
                    if (!cleanTag.isEmpty()) {
                        tagCount.put(cleanTag, tagCount.getOrDefault(cleanTag, 0L) + 1);
                    }
                }
            }
        }
        return tagCount.entrySet().stream()
                .sorted(java.util.Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(6)
                .map(e -> "#" + e.getKey())
                .collect(java.util.stream.Collectors.toList());
    }

    private List<User> getSuggestedUsers(User currentUser) {
        if (currentUser != null) {
            return userRepository.findSuggestedUsers(currentUser.getId(), PageRequest.of(0, 3));
        }
        return userRepository.findSuggestedUsersGuest(PageRequest.of(0, 3));
    }

    @GetMapping({"/", "/home"})
    public String home(Model model, HttpSession session) {
        String email = (String) session.getAttribute("email");
        User currentUser = null;
        if (email != null) {
            currentUser = userRepository.findByEmail(email).orElse(null);
        }

        List<Post> latestPosts = postRepository
                .findByIsDeletedFalseOrderByCreatedAtDesc(PageRequest.of(0, 12));
        List<Post> rankingPosts = postRepository
                .findByIsDeletedFalseOrderByLikesCountDesc(PageRequest.of(0, 6)).getContent();

        long totalPosts = 0;
        long totalFollowers = 0;
        long totalFollowing = 0;

        if (currentUser != null) {
            totalPosts = postRepository.countByUserIdAndIsDeletedFalse(currentUser.getId());
            totalFollowers = followRepository.countByFollowingId(currentUser.getId());
            totalFollowing = followRepository.countByFollowerId(currentUser.getId());
        }

        model.addAttribute("totalPosts", totalPosts);
        model.addAttribute("totalFollowers", totalFollowers);
        model.addAttribute("totalFollowing", totalFollowing);

        model.addAttribute("hotTags", getTopTags());
        model.addAttribute("suggestedUsers", getSuggestedUsers(currentUser));

        model.addAttribute("posts", latestPosts);
        model.addAttribute("rankingPosts", rankingPosts);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("sessionUser", currentUser); // ← fix nền trang chủ
        return "home";
    }

    @GetMapping("/search")
    public String search(@org.springframework.web.bind.annotation.RequestParam(value = "q", required = false) String keyword, Model model, HttpSession session) {
        String email = (String) session.getAttribute("email");
        User currentUser = null;
        if (email != null) {
            currentUser = userRepository.findByEmail(email).orElse(null);
        }

        List<Post> searchResults;
        if (keyword == null || keyword.trim().isEmpty()) {
            searchResults = List.of(); // Empty list if no query
        } else {
            // Strip leading '#' to support tag search like "#vuwa" -> "vuwa"
            String cleanKeyword = keyword.trim().replaceAll("^#+", "");
            searchResults = postRepository.searchPosts(cleanKeyword);
        }

        model.addAttribute("searchResults", searchResults);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("sessionUser", currentUser);
        return "search-results";
    }

    @GetMapping("/ranking")
    public String ranking(Model model, HttpSession session) {
        String email = (String) session.getAttribute("email");
        User currentUser = null;
        if (email != null) {
            currentUser = userRepository.findByEmail(email).orElse(null);
        }

        List<Post> rankingPosts = postRepository
                .findByIsDeletedFalseOrderByLikesCountDesc(PageRequest.of(0, 50)).getContent();

        model.addAttribute("rankingPosts", rankingPosts);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("sessionUser", currentUser);
        return "ranking";
    }

    @GetMapping("/admin/clear-samples")
    @ResponseBody
    public String clearSamples() {
        postRepository.deleteByImageDataIsNull();
        return "Đã xóa toàn bộ bài viết không có ảnh chuẩn (Bài mẫu). Vui lòng tải lại trang Home!";
    }

    @GetMapping("/api/posts")
    @ResponseBody
    public ResponseEntity<?> getApiPosts(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "new") String tab,
                                         HttpSession session) {
        Pageable pageable = PageRequest.of(page - 1, 6, Sort.by("createdAt").descending());
        org.springframework.data.domain.Page<Post> postPage;

        switch (tab) {
            case "following":
                String email = (String) session.getAttribute("email");
                if (email == null) return ResponseEntity.status(401).body("Yêu cầu đăng nhập");
                User currentUser = userRepository.findByEmail(email).orElse(null);
                if (currentUser == null) return ResponseEntity.status(401).body("Yêu cầu đăng nhập");
                postPage = postRepository.findPostsFromFollowing(currentUser.getId(), pageable);
                break;
            case "popular":
                Pageable popPageable = PageRequest.of(page - 1, 6);
                postPage = postRepository.findPopularPosts(popPageable);
                break;
            case "ranking":
                Pageable rankPageable = PageRequest.of(page - 1, 6);
                postPage = postRepository.findByIsDeletedFalseOrderByLikesCountDesc(rankPageable);
                break;
            case "new":
            default:
                postPage = postRepository.findAllByIsDeletedFalse(pageable);
                break;
        }

        return ResponseEntity.ok(postPage.getContent());
    }

    @GetMapping("/trending")
    public String trending(Model model, HttpSession session) {
        String email = (String) session.getAttribute("email");
        User currentUser = null;
        if (email != null) {
            currentUser = userRepository.findByEmail(email).orElse(null);
        }

        // Lấy top 50 bài có View cao nhất
        org.springframework.data.domain.Page<Post> trendingPage = postRepository
                .findAllByIsDeletedFalseOrderByViewsDesc(PageRequest.of(0, 50));

        model.addAttribute("trendingPosts", trendingPage.getContent());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("sessionUser", currentUser);
        return "trending";
    }

    @GetMapping("/saved")
    public String saved(Model model, HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null) return "redirect:/auth/login";

        User currentUser = userRepository.findByEmail(email).orElse(null);
        if (currentUser == null) return "redirect:/auth/login";

        Pageable pageable = PageRequest.of(0, 50);
        List<com.example.AsmJava5.model.SavedPost> savedPostEntities = savedPostRepository
                .findByUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable);

        // Map sang list Post để template dễ render
        List<Post> savedPosts = savedPostEntities.stream()
                .map(com.example.AsmJava5.model.SavedPost::getPost)
                .filter(p -> !p.getIsDeleted())
                .toList();

        model.addAttribute("savedPosts", savedPosts);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("sessionUser", currentUser);
        return "saved";
    }

    @GetMapping("/liked")
    public String liked(Model model, HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null) return "redirect:/auth/login";

        User currentUser = userRepository.findByEmail(email).orElse(null);
        if (currentUser == null) return "redirect:/auth/login";

        List<com.example.AsmJava5.model.Reaction> reactions =
                reactionRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());

        List<Post> likedPosts = reactions.stream()
                .map(com.example.AsmJava5.model.Reaction::getPost)
                .filter(p -> !p.getIsDeleted())
                .toList();

        model.addAttribute("likedPosts", likedPosts);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("sessionUser", currentUser);
        return "liked";
    }
}
