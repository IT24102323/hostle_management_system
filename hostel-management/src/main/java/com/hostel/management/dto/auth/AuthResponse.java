package com.hostel.management.dto.auth;

import com.hostel.management.enums.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String email;
    private String name;
    private Role role;
    private Long residentId;
}
