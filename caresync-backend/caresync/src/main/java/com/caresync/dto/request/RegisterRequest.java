package com.caresync.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Registration payload. Only ROLE_PATIENT can self-register through /api/auth/register.
 * Doctors are onboarded by an Admin via /api/admin/doctors.
 */
@Getter
@Setter
public class RegisterRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank
    private String fullName;

    private String bloodGroup;

    @Past
    private LocalDate dob;

    private String gender;
}
