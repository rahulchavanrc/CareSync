package com.caresync.dto.response;

import com.caresync.entity.Appointment;
import com.caresync.entity.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
public class AppointmentResponse {
    private Long appId;
    private Long doctorId;
    private String doctorName;
    private String specialization;
    private Long patientId;
    private String patientName;
    private LocalDate appointmentDate;
    private LocalTime timeSlot;
    private Integer durationMinutes;
    private AppointmentStatus status;
    private String reasonForVisit;
    @Builder.Default
    private boolean reviewed = false;

    public static AppointmentResponse from(Appointment a) {
        return from(a, false);
    }

    public static AppointmentResponse from(Appointment a, boolean reviewed) {
        return AppointmentResponse.builder()
                .appId(a.getAppId())
                .doctorId(a.getDoctor().getDoctorId())
                .doctorName(a.getDoctor().getFullName())
                .specialization(a.getDoctor().getSpecialization())
                .patientId(a.getPatient().getPatientId())
                .patientName(a.getPatient().getFullName())
                .appointmentDate(a.getAppointmentDate())
                .timeSlot(a.getTimeSlot())
                .durationMinutes(a.getDurationMinutes())
                .status(a.getStatus())
                .reasonForVisit(a.getReasonForVisit())
                .reviewed(reviewed)
                .build();
    }
}
