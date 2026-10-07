package com.caresync.controller;

import com.caresync.dto.request.DoctorCreateRequest;
import com.caresync.dto.response.AuthResponse;
import com.caresync.dto.response.DashboardStatsResponse;
import com.caresync.dto.response.DoctorResponse;
import com.caresync.dto.response.PatientResponse;
import com.caresync.service.AdminService;
import com.caresync.service.AuthService;
import com.caresync.service.DoctorService;
import com.caresync.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AuthService authService;
    private final AdminService adminService;
    private final DoctorService doctorService;
    private final PatientService patientService;

    @PostMapping("/doctors")
    public ResponseEntity<AuthResponse> createDoctor(@Valid @RequestBody DoctorCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createDoctor(request));
    }

    @GetMapping("/dashboard")
    public DashboardStatsResponse getDashboardStats() {
        return adminService.getDashboardStats();
    }

    @GetMapping("/doctors")
    public List<DoctorResponse> getAllDoctors() {
        return doctorService.getAllDoctors();
    }

    @GetMapping("/patients")
    public List<PatientResponse> getAllPatients() {
        return patientService.getAllPatients();
    }
}
