package com.caresync.controller;

import com.caresync.dto.response.PatientResponse;
import com.caresync.entity.User;
import com.caresync.security.SecurityUtil;
import com.caresync.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final SecurityUtil securityUtil;

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public PatientResponse getMyProfile() {
        User currentUser = securityUtil.getCurrentUser();
        return patientService.getMyProfile(currentUser);
    }
}
