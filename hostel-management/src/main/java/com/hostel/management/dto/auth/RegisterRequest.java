package com.hostel.management.dto.auth;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String nic;

    @Pattern(regexp = "^[0-9]{3}-[0-9]{7}$|^[0-9]{10}$|^$",
             message = "Contact must be a valid phone number")
    private String contact;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$", message = "Only Gmail addresses (…@gmail.com) are allowed")
    private String email;

    private String course;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
