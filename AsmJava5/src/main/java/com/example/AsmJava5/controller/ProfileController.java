package com.example.AsmJava5.controller;

import com.example.AsmJava5.model.*;
import com.example.AsmJava5.repository.*;
import com.example.AsmJava5.service.PostService;
import com.example.AsmJava5.service.ShopService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final PostService postService;
    private final ShopService shopService;
    private final UserPurchaseRepository userPurchaseRepository;

    private User getUser(HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null) return null;
        return userRepository.findByEmail(email).orElse(null);
    }

    private String saveFile(MultipartFile file, String subDir) throws Exception {
        String uploadDir = System.getProperty("user.dir") + "/uploads/" + subDir + "/";
        Path dirPath = Paths.get(uploadDir);
        if (!Files.exists(dirPath)) Files.createDirectories(dirPath);
        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf(".")) : ".jpg";
        String fileName = subDir + "_" + System.currentTimeMillis() + ext;
        Files.write(dirPath.resolve(fileName), file.getBytes());
        return "/uploads/" + subDir + "/" + fileName;
    }

    private void deleteOldFile(String url) {
        if (url != null && url.startsWith("/uploads/")) {
            try {
                Files.deleteIfExists(
                        Paths.get(System.getProperty("user.dir") + url));
            } catch (Exception ignored) {}
        }
    }

    // ===== PROFILE PAGE =====
    @GetMapping
    public String profile(Model model, HttpSession session) {
        User user = getUser(session);
        if (user == null) return "redirect:/auth/login";

        List<Post> posts = postService.getPostsByUser(user.getId());
        List<UserPurchase> purchases = shopService.getUserPurchases(user.getId());
        Map<String, List<UserPurchase>> itemsByType = purchases.stream()
                .collect(Collectors.groupingBy(p -> p.getItem().getItemType()));

        model.addAttribute("user", user);
        model.addAttribute("posts", posts);
        model.addAttribute("purchases", purchases);
        model.addAttribute("itemsByType", itemsByType);
        model.addAttribute("totalLikes",
                posts.stream().mapToInt(Post::getLikesCount).sum());
        model.addAttribute("totalViews",
                posts.stream().mapToInt(Post::getViews).sum());
        return "profile";
    }

    // ===== LẤY AVATAR =====
    @GetMapping("/avatar/{userId}")
    public ResponseEntity<byte[]> getAvatar(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getAvatarData() == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .cacheControl(CacheControl.maxAge(1, java.util.concurrent.TimeUnit.DAYS))
                .body(user.getAvatarData());
    }

    // ===== CẬP NHẬT PROFILE (tên + bio) =====
    @PostMapping("/update")
    public String updateProfile(@RequestParam(required = false) String fullName,
                                @RequestParam(required = false) String bio,
                                HttpSession session, RedirectAttributes ra) {
        try {
            User user = getUser(session);
            if (user == null) return "redirect:/auth/login";
            if (fullName != null) user.setFullName(fullName);
            if (bio != null) user.setBio(bio);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            ra.addFlashAttribute("success", "Cập nhật profile thành công! ✅");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }

    // ===== UPLOAD AVATAR TỪ THIẾT BỊ =====
    @PostMapping("/update-avatar")
    public String updateAvatar(@RequestParam MultipartFile avatar,
                               HttpSession session, RedirectAttributes ra) {
        try {
            User user = getUser(session);
            if (user == null) return "redirect:/auth/login";
            if (avatar == null || avatar.isEmpty())
                throw new RuntimeException("Chưa chọn ảnh!");
            if (avatar.getSize() > 5 * 1024 * 1024)
                throw new RuntimeException("Ảnh tối đa 5MB!");

            // Lưu vào DB dạng byte[]
            user.setAvatarData(avatar.getBytes());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            ra.addFlashAttribute("success", "Đã cập nhật avatar! 🎨");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }

    // ===== UPLOAD ẢNH BÌA =====
    @PostMapping("/update-cover")
    public String updateCover(@RequestParam MultipartFile cover,
                              HttpSession session, RedirectAttributes ra) {
        try {
            User user = getUser(session);
            if (user == null) return "redirect:/auth/login";
            if (cover == null || cover.isEmpty())
                throw new RuntimeException("Chưa chọn ảnh!");
            if (cover.getSize() > 5 * 1024 * 1024)
                throw new RuntimeException("Ảnh tối đa 5MB!");

            deleteOldFile(user.getEquippedBg());
            String url = saveFile(cover, "covers");
            user.setEquippedBg(url);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            ra.addFlashAttribute("success", "Đã cập nhật ảnh bìa! 🎨");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }

    // ===== XÓA ẢNH BÌA =====
    @PostMapping("/remove-cover")
    public String removeCover(HttpSession session, RedirectAttributes ra) {
        try {
            User user = getUser(session);
            if (user == null) return "redirect:/auth/login";
            deleteOldFile(user.getEquippedBg());
            user.setEquippedBg(null);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            ra.addFlashAttribute("success", "Đã xóa ảnh bìa!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }

    // ===== TRANG BỊ VẬT PHẨM =====
    @PostMapping("/equip/{purchaseId}")
    public String equipItem(@PathVariable Long purchaseId,
                            HttpSession session, RedirectAttributes ra) {
        try {
            User user = getUser(session);
            if (user == null) return "redirect:/auth/login";
            shopService.equipItem(user, purchaseId);
            ra.addFlashAttribute("success", "Đã trang bị vật phẩm! ✨");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }

    // ===== GỠ TRANG BỊ =====
    @PostMapping("/unequip/{type}")
    public String unequipItem(@PathVariable String type,
                              HttpSession session, RedirectAttributes ra) {
        try {
            User user = getUser(session);
            if (user == null) return "redirect:/auth/login";
            shopService.unequipItem(user, type);
            ra.addFlashAttribute("success", "Đã gỡ vật phẩm!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }

    // ===== XÓA BÀI ĐĂNG =====
    @PostMapping("/delete-post/{postId}")
    public String deletePost(@PathVariable Long postId,
                             HttpSession session, RedirectAttributes ra) {
        try {
            User user = getUser(session);
            if (user == null) return "redirect:/auth/login";
            postService.softDelete(postId, user.getId());
            ra.addFlashAttribute("success", "Đã xóa bài đăng!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }
}
