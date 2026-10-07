package com.caresync.dto.response;

import com.caresync.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private Role role;
    private Long profileId; // doctorId or patientId depending on role
}
