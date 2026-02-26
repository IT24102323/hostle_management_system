package com.hostel.management.exception;

/**
 * Thrown when a business rule is violated or request data is invalid.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
