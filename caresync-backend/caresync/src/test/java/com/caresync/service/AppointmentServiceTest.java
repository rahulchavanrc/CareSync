package com.caresync.service;

import com.caresync.dto.request.BookAppointmentRequest;
import com.caresync.dto.response.AppointmentResponse;
import com.caresync.entity.*;
import com.caresync.exception.DoctorUnavailableException;
import com.caresync.exception.ForbiddenOperationException;
import com.caresync.exception.ResourceNotFoundException;
import com.caresync.repository.AppointmentRepository;
import com.caresync.repository.DoctorRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private User patientUser;
    private User doctorUser;
    private Doctor doctor;
    private Patient patient;
    private final LocalDate appointmentDate = LocalDate.now().plusDays(3);

    @BeforeEach
    void setUp() {
        patientUser = User.builder().userId(1L).email("patient@caresync.com").role(Role.ROLE_PATIENT).build();
        doctorUser = User.builder().userId(2L).email("doctor@caresync.com").role(Role.ROLE_DOCTOR).build();

        doctor = Doctor.builder()
                .doctorId(10L)
                .user(doctorUser)
                .fullName("Dr. Aisha Khan")
                .specialization("Cardiologist")
                .build();

        patient = Patient.builder()
                .patientId(20L)
                .user(patientUser)
                .fullName("Jordan Lee")
                .build();
    }

    @Test
    @DisplayName("Booking succeeds when the doctor has no conflicting appointments")
    void bookAppointment_success() {
        BookAppointmentRequest request = new BookAppointmentRequest();
        request.setDoctorId(10L);
        request.setAppointmentDate(appointmentDate);
        request.setTimeSlot(LocalTime.of(11, 0));

        when(patientRepository.findByUser_UserId(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(10L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findByDoctor_DoctorIdAndAppointmentDateAndStatusNot(
                10L, appointmentDate, AppointmentStatus.CANCELLED))
                .thenReturn(List.of()); // full schedule is empty -> free
        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> {
                    Appointment a = invocation.getArgument(0);
                    a.setAppId(100L);
                    return a;
                });

        AppointmentResponse response = appointmentService.bookAppointment(patientUser, request);

        assertEquals(100L, response.getAppId());
        assertEquals(AppointmentStatus.PENDING, response.getStatus());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Booking against a fully-booked doctor throws DoctorUnavailableException (custom exception handling)")
    void bookAppointment_fullSchedule_throwsDoctorUnavailable() {
        BookAppointmentRequest request = new BookAppointmentRequest();
        request.setDoctorId(10L);
        request.setAppointmentDate(appointmentDate);
        request.setTimeSlot(LocalTime.of(9, 30)); // collides with the mocked existing slot below

        Appointment existingBooking = Appointment.builder()
                .appId(99L)
                .doctor(doctor)
                .patient(patient)
                .appointmentDate(appointmentDate)
                .timeSlot(LocalTime.of(9, 0))
                .durationMinutes(60) // 9:00-10:00, fully occupying the doctor's morning
                .status(AppointmentStatus.CONFIRMED)
                .build();

        when(patientRepository.findByUser_UserId(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(10L)).thenReturn(Optional.of(doctor));
        // Mock the repository to simulate a "Full Schedule" scenario for this doctor/day.
        when(appointmentRepository.findByDoctor_DoctorIdAndAppointmentDateAndStatusNot(
                10L, appointmentDate, AppointmentStatus.CANCELLED))
                .thenReturn(List.of(existingBooking));

        DoctorUnavailableException ex = assertThrows(DoctorUnavailableException.class,
                () -> appointmentService.bookAppointment(patientUser, request));

        assertTrue(ex.getMessage().contains("Dr. Aisha Khan"));
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Booking with an unknown doctor id throws ResourceNotFoundException")
    void bookAppointment_unknownDoctor_throwsNotFound() {
        BookAppointmentRequest request = new BookAppointmentRequest();
        request.setDoctorId(999L);
        request.setAppointmentDate(appointmentDate);
        request.setTimeSlot(LocalTime.of(9, 0));

        when(patientRepository.findByUser_UserId(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> appointmentService.bookAppointment(patientUser, request));

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("A doctor cannot update the status of another doctor's appointment")
    void updateStatus_wrongDoctor_throwsForbidden() {
        User otherDoctorUser = User.builder().userId(3L).email("other@caresync.com").role(Role.ROLE_DOCTOR).build();
        Doctor otherDoctor = Doctor.builder().doctorId(11L).user(otherDoctorUser).fullName("Dr. Other").build();

        Appointment appointment = Appointment.builder()
                .appId(100L)
                .doctor(doctor) // owned by doctor 10, not 11
                .patient(patient)
                .appointmentDate(appointmentDate)
                .timeSlot(LocalTime.of(9, 0))
                .status(AppointmentStatus.PENDING)
                .build();

        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(doctorRepository.findByUser_UserId(3L)).thenReturn(Optional.of(otherDoctor));

        assertThrows(ForbiddenOperationException.class,
                () -> appointmentService.updateStatus(otherDoctorUser, 100L, AppointmentStatus.CONFIRMED));

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Doctor can confirm their own PENDING appointment")
    void updateStatus_ownAppointment_succeeds() {
        Appointment appointment = Appointment.builder()
                .appId(100L)
                .doctor(doctor)
                .patient(patient)
                .appointmentDate(appointmentDate)
                .timeSlot(LocalTime.of(9, 0))
                .status(AppointmentStatus.PENDING)
                .build();

        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(doctorRepository.findByUser_UserId(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponse response = appointmentService.updateStatus(doctorUser, 100L, AppointmentStatus.CONFIRMED);

        assertEquals(AppointmentStatus.CONFIRMED, response.getStatus());
    }

    @Test
    @DisplayName("Cannot transition a COMPLETED appointment to any other state")
    void updateStatus_terminalState_throwsIllegalState() {
        Appointment appointment = Appointment.builder()
                .appId(100L)
                .doctor(doctor)
                .patient(patient)
                .appointmentDate(appointmentDate)
                .timeSlot(LocalTime.of(9, 0))
                .status(AppointmentStatus.COMPLETED)
                .build();

        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(doctorRepository.findByUser_UserId(2L)).thenReturn(Optional.of(doctor));

        assertThrows(IllegalStateException.class,
                () -> appointmentService.updateStatus(doctorUser, 100L, AppointmentStatus.CONFIRMED));

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }
}
