package com.uphill.healthcare_booking_system.domain.exceptions;

import java.time.LocalDateTime;

public class NoAvailableDoctorException extends RuntimeException {
    public NoAvailableDoctorException(String specialty, LocalDateTime start, LocalDateTime end) {
        super("No available doctor for specialty " + specialty + " in window " + start + "–" + end);
    }
}
