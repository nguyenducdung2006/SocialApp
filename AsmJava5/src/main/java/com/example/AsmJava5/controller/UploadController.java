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
}
