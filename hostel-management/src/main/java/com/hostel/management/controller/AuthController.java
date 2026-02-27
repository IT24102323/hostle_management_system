package com.hostel.management.controller;

import com.hostel.management.dto.auth.AuthResponse;
import com.hostel.management.dto.auth.LoginRequest;
import com.hostel.management.dto.auth.RegisterRequest;
import com.hostel.management.dto.auth.RegisterResponse;
import com.hostel.management.response.ApiResponse;
import com.hostel.management.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful.", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(201).body(ApiResponse.success(response.getMessage(), response));
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully. You can now log in.", "verified"));
    }
}
