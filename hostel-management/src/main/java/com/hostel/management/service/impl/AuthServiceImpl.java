package com.hostel.management.service.impl;

import com.hostel.management.dto.auth.AuthResponse;
import com.hostel.management.dto.auth.LoginRequest;
import com.hostel.management.dto.auth.RegisterRequest;
import com.hostel.management.dto.auth.RegisterResponse;
import com.hostel.management.dto.resident.ResidentRequestDto;
import com.hostel.management.dto.resident.ResidentResponseDto;
import com.hostel.management.entity.Resident;
import com.hostel.management.entity.User;
import com.hostel.management.enums.ResidentStatus;
import com.hostel.management.enums.Role;
import com.hostel.management.exception.BadRequestException;
import com.hostel.management.repository.ResidentRepository;
import com.hostel.management.repository.UserRepository;
import com.hostel.management.service.AuthService;
import com.hostel.management.service.EmailService;
import com.hostel.management.service.ResidentService;
import com.hostel.management.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class AuthServiceImpl implements AuthService {

    private static final int VERIFICATION_TOKEN_BYTES = 32;
    private static final int VERIFICATION_TOKEN_HOURS = 24;

    private final UserRepository userRepository;
    private final ResidentRepository residentRepository;
    private final ResidentService residentService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public AuthServiceImpl(UserRepository userRepository,
                          ResidentRepository residentRepository,
                          ResidentService residentService,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil,
                          EmailService emailService) {
        this.userRepository = userRepository;
        this.residentRepository = residentRepository;
        this.residentService = residentService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String input = request.getEmailOrUsername().trim().toLowerCase();
        String password = request.getPassword();

        // Allow "admin" as alias for admin@hostelhub.com
        String lookupEmail = "admin".equals(input) ? "admin@hostelhub.com" : input;

        User user = userRepository.findByEmailIgnoreCase(lookupEmail)
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (user.getVerified() == null || !user.getVerified()) {
            throw new BadRequestException("Please verify your email before logging in. Check your inbox for the verification link.");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadRequestException("Invalid email or password");
        }

        Long residentId = user.getResident() != null ? user.getResident().getId() : null;
        String name = user.getName() != null ? user.getName() : user.getEmail();
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), residentId, name);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(name)
                .role(user.getRole())
                .residentId(residentId)
                .build();
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BadRequestException("An account with this email already exists.");
        }

        ResidentRequestDto dto = ResidentRequestDto.builder()
                .name(request.getName())
                .nic(request.getNic())
                .contact(request.getContact())
                .email(request.getEmail())
                .course(request.getCourse())
                .status(ResidentStatus.PENDING)
                .joinDate(LocalDate.now())
                .build();

        ResidentResponseDto residentDto = residentService.createResident(dto);
        Resident resident = residentRepository.findById(residentDto.getId()).orElseThrow();

        String verificationToken = generateVerificationToken();
        LocalDateTime tokenExpiry = LocalDateTime.now().plusHours(VERIFICATION_TOKEN_HOURS);

        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.RESIDENT)
                .name(request.getName())
                .resident(resident)
                .verified(false)
                .verificationToken(verificationToken)
                .verificationTokenExpiresAt(tokenExpiry)
                .build();
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), user.getName(), verificationToken);

        return RegisterResponse.builder()
                .message("Registration successful! Please check your email to verify your account before logging in.")
                .email(user.getEmail())
                .build();
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        if (token == null || token.isBlank()) {
            throw new BadRequestException("Invalid verification link.");
        }

        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification link."));

        if (user.getVerificationTokenExpiresAt() != null
                && user.getVerificationTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification link has expired. Please register again.");
        }

        user.setVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiresAt(null);
        userRepository.save(user);
    }

    private String generateVerificationToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[VERIFICATION_TOKEN_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
