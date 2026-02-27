package com.example.AsmJava5.config;

import com.example.AsmJava5.model.User;
import com.example.AsmJava5.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserRepository userRepository;

    @ModelAttribute("sessionUser")
    public User sessionUser(HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null) return null;
        return userRepository.findByEmail(email).orElse(null);
    }
}
