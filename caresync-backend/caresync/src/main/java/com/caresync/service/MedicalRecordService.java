package com.caresync.service;

import com.caresync.dto.request.CreateMedicalRecordRequest;
import com.caresync.dto.response.MedicalRecordResponse;
import com.caresync.entity.Appointment;
import com.caresync.entity.AppointmentStatus;
import com.caresync.entity.Doctor;
import com.caresync.entity.MedicalRecord;
import com.caresync.entity.Patient;
import com.caresync.entity.User;
import com.caresync.exception.ForbiddenOperationException;
import com.caresync.exception.ResourceNotFoundException;
import com.caresync.repository.AppointmentRepository;
import com.caresync.repository.DoctorRepository;
import com.caresync.repository.MedicalRecordRepository;
import com.caresync.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private static final Logger log = LoggerFactory.getLogger(MedicalRecordService.class);

    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PrescriptionPdfService prescriptionPdfService;

    @Transactional
    public MedicalRecordResponse createRecord(User currentUser, CreateMedicalRecordRequest request) {
        Doctor doctor = doctorRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for current user"));

        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with id: " + request.getAppointmentId()));

        if (!appointment.getDoctor().getDoctorId().equals(doctor.getDoctorId())) {
            log.warn("AccessDenied: doctorId={} attempted to create a record for appointment {} owned by doctorId={}",
                    doctor.getDoctorId(), appointment.getAppId(), appointment.getDoctor().getDoctorId());
            throw new ForbiddenOperationException("You can only create records for your own appointments");
        }

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Medical records can only be added after an appointment is marked COMPLETED");
        }

        MedicalRecord record = MedicalRecord.builder()
                .appointment(appointment)
                .patient(appointment.getPatient())
                .doctor(doctor)
                .symptoms(request.getSymptoms())
                .diagnosis(request.getDiagnosis())
                .prescription(request.getPrescription())
                .build();

        record = medicalRecordRepository.save(record);
        log.info("MedicalRecord {} created for appointment {} by doctorId={}",
                record.getRecordId(), appointment.getAppId(), doctor.getDoctorId());

        return MedicalRecordResponse.from(record);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> getMyRecords(User currentUser) {
        Patient patient = patientRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for current user"));

        return medicalRecordRepository.findByPatient_PatientIdOrderByCreatedAtDesc(patient.getPatientId())
                .stream().map(MedicalRecordResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public MedicalRecordResponse getRecordById(User currentUser, Long recordId) {
        MedicalRecord record = getAuthorizedRecord(currentUser, recordId);
        return MedicalRecordResponse.from(record);
    }

    @Transactional(readOnly = true)
    public byte[] downloadPrescriptionPdf(User currentUser, Long recordId) {
        MedicalRecord record = getAuthorizedRecord(currentUser, recordId);
        return prescriptionPdfService.generate(record);
    }

    /** Shared privacy gate: only the owning patient or the treating doctor may access a record. */
    private MedicalRecord getAuthorizedRecord(User currentUser, Long recordId) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with id: " + recordId));

        boolean isOwningPatient = record.getPatient().getUser().getUserId().equals(currentUser.getUserId());
        boolean isTreatingDoctor = record.getDoctor().getUser().getUserId().equals(currentUser.getUserId());

        if (!isOwningPatient && !isTreatingDoctor) {
            log.warn("AccessDenied: userId={} attempted to access medical record {} they do not own",
                    currentUser.getUserId(), recordId);
            throw new ForbiddenOperationException("You do not have access to this medical record");
        }

        return record;
    }
}
