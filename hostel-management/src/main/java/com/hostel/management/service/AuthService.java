package com.hostel.management.service;

import com.hostel.management.dto.auth.AuthResponse;
import com.hostel.management.dto.auth.LoginRequest;
import com.hostel.management.dto.auth.RegisterRequest;
import com.hostel.management.dto.auth.RegisterResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    RegisterResponse register(RegisterRequest request);

    void verifyEmail(String token);
}
