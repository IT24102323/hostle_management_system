package com.hostel.management.entity;

import com.hostel.management.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * User entity for authentication - stores email, hashed password, and role.
 * Links to Resident when role is RESIDENT.
 * Email verification required before login (verified=true for seeded users).
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String name;

    /** Links to Resident when role is RESIDENT; null for ADMIN */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id")
    private Resident resident;

    /** Only verified users can log in. Seeded users are set verified=true manually. */
    @Column(nullable = false)
    private Boolean verified = false;

    @Column(name = "verification_token", length = 64)
    private String verificationToken;

    @Column(name = "verification_token_expires_at")
    private LocalDateTime verificationTokenExpiresAt;
}
