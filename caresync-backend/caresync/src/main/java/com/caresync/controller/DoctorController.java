package com.caresync.controller;

import com.caresync.dto.response.DoctorResponse;
import com.caresync.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public List<DoctorResponse> getAllDoctors(@RequestParam(required = false) String specialization) {
        if (specialization != null && !specialization.isBlank()) {
            return doctorService.searchBySpecialization(specialization);
        }
        return doctorService.getAllDoctors();
    }

    @GetMapping("/{doctorId}")
    public DoctorResponse getDoctor(@PathVariable Long doctorId) {
        return doctorService.getDoctorById(doctorId);
    }
}
