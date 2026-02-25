package com.hostel.management.security;

import com.hostel.management.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Validates JWT on protected API routes. Public routes: /api/auth/login, /api/auth/register.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/verify"
    );

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Always skip JWT check for CORS pre-flight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        if (PUBLIC_PATHS.stream().anyMatch(path::equalsIgnoreCase)) return true;
        // Allow public visit requests (e.g. from index.html booking form)
        if ("/api/visits".equalsIgnoreCase(path) && "POST".equalsIgnoreCase(request.getMethod())) return true;
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"Missing or invalid authorization header.\"}");
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isValid(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"Invalid or expired token.\"}");
            return;
        }

        request.setAttribute("jwtEmail", jwtUtil.getEmail(token));
        request.setAttribute("jwtRole", jwtUtil.getRole(token));
        request.setAttribute("jwtResidentId", jwtUtil.getResidentId(token));
        filterChain.doFilter(request, response);
    }
}
