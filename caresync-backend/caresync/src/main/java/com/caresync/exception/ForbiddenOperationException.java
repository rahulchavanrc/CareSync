package com.caresync.exception;

/** Thrown when an authenticated user attempts to act on a resource they don't own
 *  (e.g. a patient trying to view another patient's medical records). */
public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException(String message) {
        super(message);
    }
}
