package com.caresync.service;

import com.caresync.dto.request.CreateReviewRequest;
import com.caresync.entity.*;
import com.caresync.exception.DuplicateResourceException;
import com.caresync.exception.ForbiddenOperationException;
import com.caresync.repository.AppointmentRepository;
import com.caresync.repository.PatientRepository;
import com.caresync.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User ownerPatientUser;
    private User strangerPatientUser;
    private Patient ownerPatient;
    private Doctor doctor;
    private Appointment completedAppointment;

    @BeforeEach
    void setUp() {
        ownerPatientUser = User.builder().userId(1L).email("owner@caresync.com").role(Role.ROLE_PATIENT).build();
        strangerPatientUser = User.builder().userId(2L).email("stranger@caresync.com").role(Role.ROLE_PATIENT).build();
        User doctorUser = User.builder().userId(3L).email("doc@caresync.com").role(Role.ROLE_DOCTOR).build();

        doctor = Doctor.builder().doctorId(10L).user(doctorUser).fullName("Dr. House").build();
        ownerPatient = Patient.builder().patientId(20L).user(ownerPatientUser).fullName("Jordan Lee").build();

        completedAppointment = Appointment.builder()
                .appId(50L)
                .doctor(doctor)
                .patient(ownerPatient)
                .appointmentDate(LocalDate.now().minusDays(1))
                .timeSlot(LocalTime.of(9, 0))
                .status(AppointmentStatus.COMPLETED)
                .build();
    }

    @Test
    @DisplayName("Patient can review their own completed appointment")
    void createReview_success() {
        CreateReviewRequest request = new CreateReviewRequest();
        request.setAppointmentId(50L);
        request.setRating(5);
        request.setComment("Great visit");

        when(patientRepository.findByUser_UserId(1L)).thenReturn(Optional.of(ownerPatient));
        when(appointmentRepository.findById(50L)).thenReturn(Optional.of(completedAppointment));
        when(reviewRepository.existsByAppointment_AppId(50L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> {
            Review r = inv.getArgument(0);
            r.setReviewId(100L);
            return r;
        });

        var response = reviewService.createReview(ownerPatientUser, request);

        assertEquals(5, response.getRating());
        assertEquals("Jordan L.", response.getReviewerDisplayName());
    }

    @Test
    @DisplayName("A patient cannot review someone else's appointment")
    void createReview_wrongPatient_throwsForbidden() {
        CreateReviewRequest request = new CreateReviewRequest();
        request.setAppointmentId(50L);
        request.setRating(4);

        Patient stranger = Patient.builder().patientId(21L).user(strangerPatientUser).fullName("Alex Rivera").build();

        when(patientRepository.findByUser_UserId(2L)).thenReturn(Optional.of(stranger));
        when(appointmentRepository.findById(50L)).thenReturn(Optional.of(completedAppointment));

        assertThrows(ForbiddenOperationException.class,
                () -> reviewService.createReview(strangerPatientUser, request));
    }

    @Test
    @DisplayName("Cannot review an appointment that isn't COMPLETED yet")
    void createReview_notCompleted_throwsIllegalState() {
        completedAppointment.setStatus(AppointmentStatus.CONFIRMED);

        CreateReviewRequest request = new CreateReviewRequest();
        request.setAppointmentId(50L);
        request.setRating(4);

        when(patientRepository.findByUser_UserId(1L)).thenReturn(Optional.of(ownerPatient));
        when(appointmentRepository.findById(50L)).thenReturn(Optional.of(completedAppointment));

        assertThrows(IllegalStateException.class,
                () -> reviewService.createReview(ownerPatientUser, request));
    }

    @Test
    @DisplayName("Cannot review the same appointment twice")
    void createReview_alreadyReviewed_throwsDuplicate() {
        CreateReviewRequest request = new CreateReviewRequest();
        request.setAppointmentId(50L);
        request.setRating(3);

        when(patientRepository.findByUser_UserId(1L)).thenReturn(Optional.of(ownerPatient));
        when(appointmentRepository.findById(50L)).thenReturn(Optional.of(completedAppointment));
        when(reviewRepository.existsByAppointment_AppId(50L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> reviewService.createReview(ownerPatientUser, request));
    }
}
