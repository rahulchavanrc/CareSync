package com.caresync.controller;

import com.caresync.dto.request.CreateMedicalRecordRequest;
import com.caresync.dto.response.MedicalRecordResponse;
import com.caresync.entity.User;
import com.caresync.security.SecurityUtil;
import com.caresync.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;
    private final SecurityUtil securityUtil;

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicalRecordResponse> createRecord(@Valid @RequestBody CreateMedicalRecordRequest request) {
        User currentUser = securityUtil.getCurrentUser();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(medicalRecordService.createRecord(currentUser, request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public List<MedicalRecordResponse> getMyRecords() {
        User currentUser = securityUtil.getCurrentUser();
        return medicalRecordService.getMyRecords(currentUser);
    }

    @GetMapping("/{recordId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public MedicalRecordResponse getRecord(@PathVariable Long recordId) {
        User currentUser = securityUtil.getCurrentUser();
        return medicalRecordService.getRecordById(currentUser, recordId);
    }

    @GetMapping("/{recordId}/prescription/pdf")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<byte[]> downloadPrescriptionPdf(@PathVariable Long recordId) {
        User currentUser = securityUtil.getCurrentUser();
        byte[] pdf = medicalRecordService.downloadPrescriptionPdf(currentUser, recordId);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("prescription-" + recordId + ".pdf")
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(pdf);
    }
}
