package com.caresync.service;

import com.caresync.dto.response.DashboardStatsResponse;
import com.caresync.entity.AppointmentStatus;
import com.caresync.repository.AppointmentRepository;
import com.caresync.repository.DoctorRepository;
import com.caresync.repository.PatientRepository;
import com.caresync.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final ReviewRepository reviewRepository;

    public DashboardStatsResponse getDashboardStats() {
        return DashboardStatsResponse.builder()
                .totalDoctors(doctorRepository.count())
                .totalPatients(patientRepository.count())
                .totalAppointments(appointmentRepository.count())
                .pendingCount(appointmentRepository.countByStatus(AppointmentStatus.PENDING))
                .confirmedCount(appointmentRepository.countByStatus(AppointmentStatus.CONFIRMED))
                .completedCount(appointmentRepository.countByStatus(AppointmentStatus.COMPLETED))
                .cancelledCount(appointmentRepository.countByStatus(AppointmentStatus.CANCELLED))
                .totalReviews(reviewRepository.count())
                .platformAverageRating(reviewRepository.findOverallAverageRating().orElse(null))
                .build();
    }
}
