package com.example.AsmJava5.controller;

import com.example.AsmJava5.model.Post;
import com.example.AsmJava5.model.User;
import com.example.AsmJava5.repository.PostRepository;
import com.example.AsmJava5.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

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
                .findByIsDeletedFalseOrderByLikesCountDesc(PageRequest.of(0, 6));

        model.addAttribute("posts", latestPosts);
        model.addAttribute("rankingPosts", rankingPosts);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("sessionUser", currentUser); // ← fix nền trang chủ
        return "home";
    }
}
