package com.caresync.exception;

/** Thrown e.g. when registering with an email that already exists. */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
