package com.caresync.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class BookAppointmentRequest {

    @NotNull
    private Long doctorId;

    @NotNull
    @Future(message = "Appointment date must be in the future")
    private LocalDate appointmentDate;

    @NotNull
    private LocalTime timeSlot;

    private Integer durationMinutes = 30;

    private String reasonForVisit;
}
