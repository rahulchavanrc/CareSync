package com.caresync.service;

import com.caresync.dto.response.DoctorResponse;
import com.caresync.entity.Doctor;
import com.caresync.exception.ResourceNotFoundException;
import com.caresync.repository.DoctorRepository;
import com.caresync.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final ReviewRepository reviewRepository;

    public List<DoctorResponse> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(this::withRatingStats)
                .toList();
    }

    public List<DoctorResponse> searchBySpecialization(String specialization) {
        return doctorRepository.findBySpecializationIgnoreCaseContaining(specialization).stream()
                .map(this::withRatingStats)
                .toList();
    }

    public DoctorResponse getDoctorById(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));
        return withRatingStats(doctor);
    }

    private DoctorResponse withRatingStats(Doctor doctor) {
        Double avgRating = reviewRepository.findAverageRatingByDoctorId(doctor.getDoctorId()).orElse(null);
        long reviewCount = reviewRepository.countByDoctor_DoctorId(doctor.getDoctorId());
        return DoctorResponse.from(doctor, avgRating, reviewCount);
    }
}
