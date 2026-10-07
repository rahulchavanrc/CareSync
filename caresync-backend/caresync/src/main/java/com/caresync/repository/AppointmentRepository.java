package com.caresync.repository;

import com.caresync.entity.Appointment;
import com.caresync.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByDoctor_DoctorIdOrderByAppointmentDateAscTimeSlotAsc(Long doctorId);

    List<Appointment> findByPatient_PatientIdOrderByAppointmentDateDescTimeSlotDesc(Long patientId);

    /**
     * Fetches all non-cancelled appointments for a doctor on a given date. The actual
     * time-overlap comparison is done in Java (see util.AppointmentTimeUtil) so the logic
     * is dialect-independent and unit-testable in isolation with Mockito/JUnit.
     */
    List<Appointment> findByDoctor_DoctorIdAndAppointmentDateAndStatusNot(
            Long doctorId, LocalDate date, AppointmentStatus excludedStatus);

    long countByStatus(AppointmentStatus status);
}
