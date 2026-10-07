package com.caresync.util;

import com.caresync.entity.Appointment;

import java.time.LocalDateTime;
import java.util.List;

public final class AppointmentTimeUtil {

    private AppointmentTimeUtil() {
    }

    /**
     * Returns true if a new appointment spanning [newStart, newEnd) would overlap with
     * any of the doctor's existing (non-cancelled) appointments on that day.
     * Two intervals [s1,e1) and [s2,e2) overlap iff s1 < e2 AND s2 < e1.
     */
    public static boolean hasOverlap(LocalDateTime newStart, LocalDateTime newEnd,
                                      List<Appointment> existingAppointments) {
        if (existingAppointments == null || existingAppointments.isEmpty()) {
            return false;
        }
        for (Appointment existing : existingAppointments) {
            LocalDateTime existingStart = existing.getStartDateTime();
            LocalDateTime existingEnd = existing.getEndDateTime();
            boolean overlaps = newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd);
            if (overlaps) {
                return true;
            }
        }
        return false;
    }
}
