package com.caresync.service;

import com.caresync.dto.request.BookAppointmentRequest;
import com.caresync.dto.response.AppointmentResponse;
import com.caresync.entity.Appointment;
import com.caresync.entity.AppointmentStatus;
import com.caresync.entity.Doctor;
import com.caresync.entity.Patient;
import com.caresync.entity.User;
import com.caresync.exception.DoctorUnavailableException;
import com.caresync.exception.ForbiddenOperationException;
import com.caresync.exception.ResourceNotFoundException;
import com.caresync.repository.AppointmentRepository;
import com.caresync.repository.DoctorRepository;
import com.caresync.repository.PatientRepository;
import com.caresync.repository.ReviewRepository;
import com.caresync.util.AppointmentTimeUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public AppointmentResponse bookAppointment(User currentUser, BookAppointmentRequest request) {
        Patient patient = patientRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for current user"));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + request.getDoctorId()));

        int duration = request.getDurationMinutes() != null ? request.getDurationMinutes() : 30;
        LocalDateTime newStart = LocalDateTime.of(request.getAppointmentDate(), request.getTimeSlot());
        LocalDateTime newEnd = newStart.plusMinutes(duration);

        List<Appointment> sameDayAppointments = appointmentRepository
                .findByDoctor_DoctorIdAndAppointmentDateAndStatusNot(
                        doctor.getDoctorId(), request.getAppointmentDate(), AppointmentStatus.CANCELLED);

        if (AppointmentTimeUtil.hasOverlap(newStart, newEnd, sameDayAppointments)) {
            log.warn("Booking rejected: doctorId={} already booked around {} (requested {}-{})",
                    doctor.getDoctorId(), request.getTimeSlot(), newStart, newEnd);
            throw new DoctorUnavailableException(
                    "Dr. " + doctor.getFullName() + " is not available at the requested time slot");
        }

        Appointment appointment = Appointment.builder()
                .doctor(doctor)
                .patient(patient)
                .appointmentDate(request.getAppointmentDate())
                .timeSlot(request.getTimeSlot())
                .durationMinutes(duration)
                .status(AppointmentStatus.PENDING)
                .reasonForVisit(request.getReasonForVisit())
                .build();

        appointment = appointmentRepository.save(appointment);
        log.info("Appointment {} booked: patientId={} with doctorId={} on {} at {} [PENDING]",
                appointment.getAppId(), patient.getPatientId(), doctor.getDoctorId(),
                request.getAppointmentDate(), request.getTimeSlot());

        return AppointmentResponse.from(appointment);
    }

    @Transactional
    public AppointmentResponse updateStatus(User currentUser, Long appointmentId, AppointmentStatus newStatus) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        Doctor doctor = doctorRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for current user"));

        if (!appointment.getDoctor().getDoctorId().equals(doctor.getDoctorId())) {
            log.warn("AccessDenied: doctorId={} attempted to modify appointment {} owned by doctorId={}",
                    doctor.getDoctorId(), appointmentId, appointment.getDoctor().getDoctorId());
            throw new ForbiddenOperationException("You can only manage your own appointments");
        }

        validateTransition(appointment.getStatus(), newStatus);

        AppointmentStatus previous = appointment.getStatus();
        appointment.setStatus(newStatus);
        appointment = appointmentRepository.save(appointment);

        log.info("Appointment {} status changed: {} -> {} (by doctorId={})",
                appointmentId, previous, newStatus, doctor.getDoctorId());

        return AppointmentResponse.from(appointment);
    }

    private void validateTransition(AppointmentStatus current, AppointmentStatus next) {
        boolean valid = switch (current) {
            case PENDING -> next == AppointmentStatus.CONFIRMED || next == AppointmentStatus.CANCELLED;
            case CONFIRMED -> next == AppointmentStatus.COMPLETED || next == AppointmentStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false; // terminal states
        };
        if (!valid) {
            throw new IllegalStateException("Cannot transition appointment from " + current + " to " + next);
        }
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointmentsAsPatient(User currentUser) {
        Patient patient = patientRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for current user"));

        java.util.Set<Long> reviewedAppIds = new java.util.HashSet<>(
                reviewRepository.findReviewedAppointmentIdsByPatientId(patient.getPatientId()));

        return appointmentRepository
                .findByPatient_PatientIdOrderByAppointmentDateDescTimeSlotDesc(patient.getPatientId())
                .stream()
                .map(a -> AppointmentResponse.from(a, reviewedAppIds.contains(a.getAppId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyScheduleAsDoctor(User currentUser) {
        Doctor doctor = doctorRepository.findByUser_UserId(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for current user"));
        return appointmentRepository
                .findByDoctor_DoctorIdOrderByAppointmentDateAscTimeSlotAsc(doctor.getDoctorId())
                .stream().map(AppointmentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Appointment getAppointmentEntity(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));
    }
}
