package com.uphill.healthcare_booking_system.domain.exceptions;

import java.time.LocalDateTime;

public class NoAvailableRoomException extends RuntimeException {
    public NoAvailableRoomException(LocalDateTime start, LocalDateTime end) {
        super("No available room in window " + start + "–" + end);
    }
}
