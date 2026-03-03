package com.example.AsmJava5.controller;

import com.example.AsmJava5.model.Post;
import com.example.AsmJava5.model.Report;
import com.example.AsmJava5.model.User;
import com.example.AsmJava5.repository.PostRepository;
import com.example.AsmJava5.repository.ReportRepository;
import com.example.AsmJava5.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminController {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private User getAdminUser(HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null) return null;
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null && "ADMIN".equals(user.getRole())) {
            return user;
        }
        return null;
    }

    @GetMapping
    public String viewReports(Model model, HttpSession session, RedirectAttributes ra) {
        User admin = getAdminUser(session);
        if (admin == null) {
            ra.addFlashAttribute("error", "Bạn không có quyền truy cập trang này.");
            return "redirect:/home";
        }
        
        List<Report> pendingReports = reportRepository.findByStatusOrderByCreatedAtDesc("PENDING");
        
        // Remove reports for posts that are already deleted
        pendingReports.removeIf(r -> r.getPost() == null || r.getPost().getIsDeleted());
        
        model.addAttribute("reports", pendingReports);
        model.addAttribute("me", admin);
        return "admin-dashboard";
    }

    @PostMapping("/{reportId}/keep")
    public String keepPost(@PathVariable Long reportId, HttpSession session, RedirectAttributes ra) {
        if (getAdminUser(session) == null) return "redirect:/home";
        
        Report report = reportRepository.findById(reportId).orElse(null);
        if (report != null) {
            report.setStatus("DISMISSED");
            report.setReviewedAt(LocalDateTime.now());
            report.setAdminNote("Đã kiểm duyệt, bài viết không vi phạm.");
            reportRepository.save(report);
            ra.addFlashAttribute("success", "Đã giữ lại bài viết và bỏ qua báo cáo!");
        }
        return "redirect:/admin/reports";
    }

    @PostMapping("/{reportId}/delete-warn")
    public String deleteAndWarn(@PathVariable Long reportId, HttpSession session, RedirectAttributes ra) {
        if (getAdminUser(session) == null) return "redirect:/home";

        Report report = reportRepository.findById(reportId).orElse(null);
        if (report != null) {
            Post post = report.getPost();
            if (post != null) {
                // Xóa mềm bài viết
                post.setIsDeleted(true);
                post.setUpdatedAt(LocalDateTime.now());
                postRepository.save(post);
                
                // Cảnh cáo người đăng
                User author = post.getUser();
                if (author != null) {
                    author.setWarningCount(author.getWarningCount() + 1);
                    userRepository.save(author);
                }
            }
            
            report.setStatus("RESOLVED");
            report.setReviewedAt(LocalDateTime.now());
            report.setAdminNote("Bài viết đã bị xóa và người dùng đã bị cảnh cáo.");
            reportRepository.save(report);
            ra.addFlashAttribute("success", "Đã xóa bài viết và ghi nhận cảnh cáo!");
        }
        return "redirect:/admin/reports";
    }

    @PostMapping("/{reportId}/lock")
    public String lockAccount(@PathVariable Long reportId, HttpSession session, RedirectAttributes ra) {
        if (getAdminUser(session) == null) return "redirect:/home";

        Report report = reportRepository.findById(reportId).orElse(null);
        if (report != null) {
             Post post = report.getPost();
             if (post != null) {
                // Xóa mềm bài viết
                post.setIsDeleted(true);
                post.setUpdatedAt(LocalDateTime.now());
                postRepository.save(post);
                
                // Khóa tài khoản
                User author = post.getUser();
                if (author != null) {
                    author.setIsBanned(true);
                    userRepository.save(author);
                }
             }
             
             report.setStatus("RESOLVED");
             report.setReviewedAt(LocalDateTime.now());
             report.setAdminNote("Tài khoản đã bị khóa do vi phạm nghiệp trọng.");
             reportRepository.save(report);
             ra.addFlashAttribute("success", "Đã khóa vĩnh viễn tài khoản người dùng!");
        }
        return "redirect:/admin/reports";
    }
}
