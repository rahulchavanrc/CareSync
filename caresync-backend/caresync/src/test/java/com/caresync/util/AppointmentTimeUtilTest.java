package com.caresync.util;

import com.caresync.entity.Appointment;
import com.caresync.entity.AppointmentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppointmentTimeUtilTest {

    private final LocalDate day = LocalDate.of(2026, 8, 10);

    private Appointment existingAppointment(LocalTime start, int durationMinutes) {
        return Appointment.builder()
                .appointmentDate(day)
                .timeSlot(start)
                .durationMinutes(durationMinutes)
                .status(AppointmentStatus.CONFIRMED)
                .build();
    }

    @Test
    @DisplayName("No existing appointments -> doctor is free, no overlap")
    void noExistingAppointments_noOverlap() {
        LocalDateTime start = LocalDateTime.of(day, LocalTime.of(10, 0));
        LocalDateTime end = start.plusMinutes(30);

        boolean overlap = AppointmentTimeUtil.hasOverlap(start, end, List.of());

        assertFalse(overlap);
    }

    @Test
    @DisplayName("New slot exactly matches an existing slot -> overlap")
    void identicalSlot_overlaps() {
        Appointment existing = existingAppointment(LocalTime.of(10, 0), 30);
        LocalDateTime newStart = LocalDateTime.of(day, LocalTime.of(10, 0));
        LocalDateTime newEnd = newStart.plusMinutes(30);

        boolean overlap = AppointmentTimeUtil.hasOverlap(newStart, newEnd, List.of(existing));

        assertTrue(overlap);
    }

    @Test
    @DisplayName("New slot partially overlaps the tail of an existing appointment")
    void partialOverlap_tail() {
        // existing: 10:00-10:30, new: 10:15-10:45 -> overlaps
        Appointment existing = existingAppointment(LocalTime.of(10, 0), 30);
        LocalDateTime newStart = LocalDateTime.of(day, LocalTime.of(10, 15));
        LocalDateTime newEnd = newStart.plusMinutes(30);

        boolean overlap = AppointmentTimeUtil.hasOverlap(newStart, newEnd, List.of(existing));

        assertTrue(overlap);
    }

    @Test
    @DisplayName("New slot starts exactly when existing appointment ends -> back-to-back, no overlap")
    void backToBack_noOverlap() {
        // existing: 10:00-10:30, new: 10:30-11:00 -> should NOT overlap ([s1,e1) vs [s2,e2))
        Appointment existing = existingAppointment(LocalTime.of(10, 0), 30);
        LocalDateTime newStart = LocalDateTime.of(day, LocalTime.of(10, 30));
        LocalDateTime newEnd = newStart.plusMinutes(30);

        boolean overlap = AppointmentTimeUtil.hasOverlap(newStart, newEnd, List.of(existing));

        assertFalse(overlap);
    }

    @Test
    @DisplayName("New slot is fully outside any existing appointment -> no overlap")
    void disjointSlot_noOverlap() {
        Appointment existing = existingAppointment(LocalTime.of(9, 0), 30);
        LocalDateTime newStart = LocalDateTime.of(day, LocalTime.of(14, 0));
        LocalDateTime newEnd = newStart.plusMinutes(30);

        boolean overlap = AppointmentTimeUtil.hasOverlap(newStart, newEnd, List.of(existing));

        assertFalse(overlap);
    }

    @Test
    @DisplayName("New slot fully contains a shorter existing appointment -> overlap")
    void newSlotEnvelopsExisting_overlaps() {
        // existing: 10:15-10:30 (15 min), new: 10:00-11:00
        Appointment existing = existingAppointment(LocalTime.of(10, 15), 15);
        LocalDateTime newStart = LocalDateTime.of(day, LocalTime.of(10, 0));
        LocalDateTime newEnd = newStart.plusMinutes(60);

        boolean overlap = AppointmentTimeUtil.hasOverlap(newStart, newEnd, List.of(existing));

        assertTrue(overlap);
    }

    @Test
    @DisplayName("Doctor with a full day of back-to-back slots is free only in the remaining gap")
    void fullScheduleWithGap_findsFreeSlot() {
        List<Appointment> fullMorning = List.of(
                existingAppointment(LocalTime.of(9, 0), 30),
                existingAppointment(LocalTime.of(9, 30), 30),
                existingAppointment(LocalTime.of(10, 0), 30)
        );

        // Requested slot falls inside the booked block -> unavailable
        LocalDateTime busyStart = LocalDateTime.of(day, LocalTime.of(9, 30));
        assertTrue(AppointmentTimeUtil.hasOverlap(busyStart, busyStart.plusMinutes(30), fullMorning));

        // Requested slot right after the booked block -> available
        LocalDateTime freeStart = LocalDateTime.of(day, LocalTime.of(10, 30));
        assertFalse(AppointmentTimeUtil.hasOverlap(freeStart, freeStart.plusMinutes(30), fullMorning));
    }
}
