package com.caresync.service;

import com.caresync.dto.request.CreateReviewRequest;
import com.caresync.dto.response.ReviewResponse;
import com.caresync.entity.Appointment;
import com.caresync.entity.AppointmentStatus;
import com.caresync.entity.Patient;
import com.caresync.entity.Review;
import com.caresync.entity.User;
import com.caresync.exception.DuplicateResourceException;
import com.caresync.exception.ForbiddenOperationException;
import com.caresync.exception.ResourceNotFoundException;
import com.caresync.repository.AppointmentRepository;
import com.caresync.repository.PatientRepository;
import com.caresync.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;

    @Transactional
    public ReviewResponse createReview(User currentUser, CreateReviewRequest request) {
        Patient patient = patientRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for current user"));

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with id: " + request.getAppointmentId()));

        if (!appointment.getPatient().getPatientId().equals(patient.getPatientId())) {
            log.warn("AccessDenied: patientId={} attempted to review appointment {} owned by patientId={}",
                    patient.getPatientId(), appointment.getAppId(), appointment.getPatient().getPatientId());
            throw new ForbiddenOperationException("You can only review your own appointments");
        }

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("You can only review an appointment after it's been completed");
        }

        if (reviewRepository.existsByAppointment_AppId(appointment.getAppId())) {
            throw new DuplicateResourceException("This appointment has already been reviewed");
        }

        Review review = Review.builder()
                .appointment(appointment)
                .doctor(appointment.getDoctor())
                .patient(patient)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        review = reviewRepository.save(review);
        log.info("Review {} created for appointment {} (doctorId={}, rating={})",
                review.getReviewId(), appointment.getAppId(), appointment.getDoctor().getDoctorId(),
                request.getRating());

        return ReviewResponse.from(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsForDoctor(Long doctorId) {
        return reviewRepository.findByDoctor_DoctorIdOrderByCreatedAtDesc(doctorId).stream()
                .map(ReviewResponse::from)
                .toList();
    }
}
