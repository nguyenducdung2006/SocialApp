package com.example.AsmJava5.controller;

import com.example.AsmJava5.dto.LoginRequest;
import com.example.AsmJava5.dto.RegisterRequest;
import com.example.AsmJava5.model.User;
import com.example.AsmJava5.service.JwtService;
import com.example.AsmJava5.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    @GetMapping("/login")
    public String showLogin(HttpSession session) {
        if (session.getAttribute("email") != null) return "redirect:/home";
        return "login";
    }

    @GetMapping("/register")
    public String showRegister(HttpSession session) {
        if (session.getAttribute("email") != null) return "redirect:/home";
        return "register";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String email,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {
        try {
            LoginRequest req = new LoginRequest();
            req.setEmail(email);
            req.setPassword(password);

            User user = userService.login(req);

            // DEBUG
            System.out.println("=== LOGIN DEBUG ===");
            System.out.println("User found: " + user.getEmail());
            System.out.println("Session ID trước: " + session.getId());

            session.setAttribute("email", user.getEmail());

            System.out.println("Session ID sau: " + session.getId());
            System.out.println("Email trong session: " + session.getAttribute("email"));
            System.out.println("==================");

            if ("ADMIN".equals(user.getRole())) return "redirect:/admin/dashboard";
            return "redirect:/home";

        } catch (Exception e) {
            System.out.println("LOGIN FAILED: " + e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }


    @PostMapping("/register")
    public String processRegister(@RequestParam String username,
                                  @RequestParam String email,
                                  @RequestParam String password,
                                  @RequestParam String confirmPassword,
                                  Model model) {
        try {
            if (!password.equals(confirmPassword)) {
                model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
                return "register";
            }
            RegisterRequest req = new RegisterRequest();
            req.setUsername(username);
            req.setEmail(email);
            req.setPassword(password);
            userService.register(req);
            return "redirect:/auth/login?success";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }

    // ✅ DEBUG endpoint - xóa sau khi fix xong
    @GetMapping("/check-session")
    @ResponseBody
    public String checkSession(HttpSession session) {
        String email = (String) session.getAttribute("email");
        return "Session email: " + (email != null ? email : "NULL - chưa đăng nhập!");
    }
}
