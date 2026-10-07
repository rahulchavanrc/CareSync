package com.caresync.service;

import com.caresync.dto.request.CreateMedicalRecordRequest;
import com.caresync.entity.*;
import com.caresync.exception.ForbiddenOperationException;
import com.caresync.repository.AppointmentRepository;
import com.caresync.repository.DoctorRepository;
import com.caresync.repository.MedicalRecordRepository;
import com.caresync.repository.PatientRepository;
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
class MedicalRecordServiceTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private PrescriptionPdfService prescriptionPdfService;

    @InjectMocks
    private MedicalRecordService medicalRecordService;

    private User ownerPatientUser;
    private User strangerPatientUser;
    private Doctor doctor;
    private Patient ownerPatient;
    private MedicalRecord record;

    @BeforeEach
    void setUp() {
        ownerPatientUser = User.builder().userId(1L).email("owner@caresync.com").role(Role.ROLE_PATIENT).build();
        strangerPatientUser = User.builder().userId(2L).email("stranger@caresync.com").role(Role.ROLE_PATIENT).build();
        User doctorUser = User.builder().userId(3L).email("doc@caresync.com").role(Role.ROLE_DOCTOR).build();

        doctor = Doctor.builder().doctorId(10L).user(doctorUser).fullName("Dr. House").build();
        ownerPatient = Patient.builder().patientId(20L).user(ownerPatientUser).fullName("Jordan Lee").build();

        Appointment appointment = Appointment.builder()
                .appId(50L)
                .doctor(doctor)
                .patient(ownerPatient)
                .appointmentDate(LocalDate.now().minusDays(1))
                .timeSlot(LocalTime.of(9, 0))
                .status(AppointmentStatus.COMPLETED)
                .build();

        record = MedicalRecord.builder()
                .recordId(500L)
                .appointment(appointment)
                .patient(ownerPatient)
                .doctor(doctor)
                .diagnosis("Seasonal allergy")
                .build();
    }

    @Test
    @DisplayName("A patient can view their own medical record")
    void getRecordById_owner_succeeds() {
        when(medicalRecordRepository.findById(500L)).thenReturn(Optional.of(record));

        var response = medicalRecordService.getRecordById(ownerPatientUser, 500L);

        assertEquals(500L, response.getRecordId());
    }

    @Test
    @DisplayName("A patient cannot view another patient's medical record (privacy rule)")
    void getRecordById_stranger_throwsForbidden() {
        when(medicalRecordRepository.findById(500L)).thenReturn(Optional.of(record));

        assertThrows(ForbiddenOperationException.class,
                () -> medicalRecordService.getRecordById(strangerPatientUser, 500L));
    }

    @Test
    @DisplayName("A patient cannot download another patient's prescription PDF (privacy rule)")
    void downloadPrescriptionPdf_stranger_throwsForbidden() {
        when(medicalRecordRepository.findById(500L)).thenReturn(Optional.of(record));

        assertThrows(ForbiddenOperationException.class,
                () -> medicalRecordService.downloadPrescriptionPdf(strangerPatientUser, 500L));
    }

    @Test
    @DisplayName("Doctor cannot add a medical record for a non-COMPLETED appointment")
    void createRecord_appointmentNotCompleted_throwsIllegalState() {
        User doctorUser = doctor.getUser();
        Appointment pendingAppointment = Appointment.builder()
                .appId(60L)
                .doctor(doctor)
                .patient(ownerPatient)
                .appointmentDate(LocalDate.now().plusDays(1))
                .timeSlot(LocalTime.of(9, 0))
                .status(AppointmentStatus.PENDING)
                .build();

        CreateMedicalRecordRequest request = new CreateMedicalRecordRequest();
        request.setAppointmentId(60L);
        request.setDiagnosis("TBD");

        when(doctorRepository.findByUser_UserId(3L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findById(60L)).thenReturn(Optional.of(pendingAppointment));

        assertThrows(IllegalStateException.class,
                () -> medicalRecordService.createRecord(doctorUser, request));
    }
}
