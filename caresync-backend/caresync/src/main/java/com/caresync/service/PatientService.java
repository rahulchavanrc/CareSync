package com.caresync.service;

import com.caresync.dto.response.PatientResponse;
import com.caresync.entity.Patient;
import com.caresync.entity.User;
import com.caresync.exception.ResourceNotFoundException;
import com.caresync.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientResponse getMyProfile(User currentUser) {
        Patient patient = patientRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for current user"));
        return PatientResponse.from(patient);
    }

    public Patient getPatientEntityByUserId(Long userId) {
        return patientRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for current user"));
    }

    public List<PatientResponse> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(PatientResponse::from)
                .toList();
    }
}

