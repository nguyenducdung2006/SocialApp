package com.example.AsmJava5.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    // Các path KHÔNG cần đăng nhập
    private static final String[] PUBLIC_PATHS = {
            "/auth/login", "/auth/register", "/auth/logout",
            "/css/", "/js/", "/images/", "/fonts/",
            "/upload/image/", "/profile/avatar/",
            "/home", "/",
            "/shop"
    };

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String path = request.getRequestURI();

        // Kiểm tra public path
        for (String pub : PUBLIC_PATHS) {
            if (path.equals(pub) || path.startsWith(pub)) {
                return true;
            }
        }

        // Kiểm tra session
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("email") != null) {
            return true;
        }

        // Chưa đăng nhập → redirect
        response.sendRedirect("/auth/login");
        return false;
    }
}
