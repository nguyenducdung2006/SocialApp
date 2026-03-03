package com.example.AsmJava5.controller;

import com.example.AsmJava5.model.User;
import com.example.AsmJava5.repository.UserRepository;
import com.example.AsmJava5.service.PostService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Controller @RequiredArgsConstructor
@RequestMapping("/upload")
public class UploadController {

    private final PostService postService;
    private final UserRepository userRepository;

    @GetMapping
    public String showPage() { return "upload"; }

    @PostMapping
    public String doUpload(@RequestParam String title,
                           @RequestParam String description,
                           @RequestParam String tags,
                           @RequestParam MultipartFile image,
                           HttpSession session,
                           RedirectAttributes ra) {
        try {
            String email = (String) session.getAttribute("email");
            if (email == null) return "redirect:/auth/login";

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user!"));

            if ("ADMIN".equals(user.getRole())) throw new RuntimeException("Admin không thể đăng bài!");

            if (image.isEmpty()) throw new RuntimeException("Vui lòng chọn ảnh!");
            if (image.getSize() > 10 * 1024 * 1024) throw new RuntimeException("Ảnh tối đa 10MB!");
            String ct = image.getContentType();
            if (ct == null || !ct.startsWith("image/"))
                throw new RuntimeException("Chỉ chấp nhận file ảnh!");

            postService.upload(user, title, description, tags, image);
            ra.addFlashAttribute("success", "Đăng tải thành công!");
            return "redirect:/profile";

        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/upload";
        }
    }

    @GetMapping("/image/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        byte[] data = postService.getImageData(id);
        if (data == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .cacheControl(CacheControl.maxAge(7, java.util.concurrent.TimeUnit.DAYS))
                .body(data);
    }

    @PostMapping("/chat-img")
    @ResponseBody
    public ResponseEntity<?> uploadChatImage(@RequestParam("file") MultipartFile file, HttpSession session) {
        try {
            if (session.getAttribute("email") == null) {
                return ResponseEntity.status(401).body("Chưa đăng nhập");
            }
            if (file.isEmpty() || file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("Ảnh không hợp lệ hoặc > 5MB");
            }
            String ct = file.getContentType();
            if (ct == null || !ct.startsWith("image/")) {
                return ResponseEntity.badRequest().body("Chỉ hỗ trợ file ảnh");
            }

            String dirPathStr = System.getProperty("user.dir") + "/uploads/chat/";
            Path dirPath = Paths.get(dirPathStr);
            if (!Files.exists(dirPath)) Files.createDirectories(dirPath);

            String original = file.getOriginalFilename();
            String ext = (original != null && original.contains(".")) ? original.substring(original.lastIndexOf(".")) : ".jpg";
            String fileName = "chat_" + System.currentTimeMillis() + ext;
            
            Files.write(dirPath.resolve(fileName), file.getBytes());

            String imageUrl = "/uploads/chat/" + fileName;
            return ResponseEntity.ok(Map.of("url", imageUrl));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
