package com.uphill.healthcare_booking_system.domain.exceptions;

public class InvalidAppointmentWindowException extends RuntimeException {
    public InvalidAppointmentWindowException() {
        super("Invalid appointment window");
    }
}
