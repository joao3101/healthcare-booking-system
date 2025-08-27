package com.uphill.healthcare_booking_system.integration;

import java.time.LocalDateTime;

public interface RoomReservationClient {
    void reserveRoom(Long roomId, LocalDateTime start, LocalDateTime end);
}
