package com.caresync.exception;

/** Thrown when a requested appointment slot overlaps an existing booking for the doctor. */
public class DoctorUnavailableException extends RuntimeException {
    public DoctorUnavailableException(String message) {
        super(message);
    }
}
