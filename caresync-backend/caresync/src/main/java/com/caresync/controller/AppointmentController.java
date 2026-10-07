package com.caresync.controller;

import com.caresync.dto.request.BookAppointmentRequest;
import com.caresync.dto.request.UpdateAppointmentStatusRequest;
import com.caresync.dto.response.AppointmentResponse;
import com.caresync.entity.User;
import com.caresync.security.SecurityUtil;
import com.caresync.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final SecurityUtil securityUtil;

    @PostMapping("/patient/book")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponse> bookAppointment(@Valid @RequestBody BookAppointmentRequest request) {
        User currentUser = securityUtil.getCurrentUser();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.bookAppointment(currentUser, request));
    }

    @GetMapping("/patient/me")
    @PreAuthorize("hasRole('PATIENT')")
    public List<AppointmentResponse> getMyAppointments() {
        User currentUser = securityUtil.getCurrentUser();
        return appointmentService.getMyAppointmentsAsPatient(currentUser);
    }

    @GetMapping("/doctor/schedule")
    @PreAuthorize("hasRole('DOCTOR')")
    public List<AppointmentResponse> getMySchedule() {
        User currentUser = securityUtil.getCurrentUser();
        return appointmentService.getMyScheduleAsDoctor(currentUser);
    }

    @PatchMapping("/doctor/{appointmentId}/status")
    @PreAuthorize("hasRole('DOCTOR')")
    public AppointmentResponse updateStatus(@PathVariable Long appointmentId,
                                             @Valid @RequestBody UpdateAppointmentStatusRequest request) {
        User currentUser = securityUtil.getCurrentUser();
        return appointmentService.updateStatus(currentUser, appointmentId, request.getStatus());
    }
}
