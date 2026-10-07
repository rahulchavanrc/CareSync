package com.caresync.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DoctorCreateRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank
    private String fullName;

    @NotBlank
    private String specialization;

    private Integer experienceYears;

    private BigDecimal consultationFee;

    private String bio;

    /** Optional. If omitted, the frontend falls back to a generated avatar. */
    private String photoUrl;
}
